package com.czh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class ArchiveUtil {

    public static File sourceDir;

    public static File targetDir;

    private static int retryCount = 3;

    public static int threadCount = 0;

    public static final Logger logger = LoggerFactory.getLogger(ArchiveUtil.class);

    public static final Logger consoleLogger = LoggerFactory.getLogger("consoleLogger");

    private static ExecutorService executorService = null;

    public static void run() throws Exception {
        CommonUtil.startRecord("archive");
        Counter summaryCounter = new Counter();
        Counter detailCounter = new Counter();

        if(executorService == null){
            executorService  = threadCount == 1 ? Executors.newSingleThreadExecutor() : Executors.newFixedThreadPool(threadCount);
        }

        // 1、加载文件列表数据
        logger.info("正在加载文件列表,请稍等");
        CommonUtil.startRecord("loadFile");
        List<File> fileList = CommonUtil.getFileList(sourceDir);
        logger.info("文件列表加载完毕,文件总数:{},加载耗时:{}",fileList.size(), CommonUtil.stopRecordWithFormat("loadFile"));

        for(int i = 5 ;i > 0;i--){
            logger.info("归档即将在{}秒后开始执行...",i);
            Thread.sleep(1000);
        }
        summaryCounter.getCurrentJobCounter().set(fileList.size());

        // 2、对该文件列表进行同步，且若失败则重试一定次数
        while (summaryCounter.getCurrentRunTimeCounter().getAndIncrement() < retryCount) {
            detailCounter.reset();
            detailCounter.getCurrentJobCounter().set(fileList.size());
            logger.info("开始第{}次归档，本次归档数:{}", summaryCounter.getCurrentRunTimeCounter().get(), detailCounter.getCurrentJobCounter().get());
            fileList = CommonUtil.asyncRun(executorService,buildCallables(fileList,summaryCounter, detailCounter));
            if (fileList.isEmpty()) {
                break;
            }
        }

        // 3、输入执行结果
        if (!fileList.isEmpty()) {
            logger.info("---------------归档失败列表---------------");
            for (File file : fileList) {
                logger.info(file.getAbsolutePath());
            }
            logger.info("---------------归档失败列表---------------");
        }
        logger.info("归档完成,参与归档的文件总数:{},成功数:{},失败数:{},忽略数:{},总耗时:{}",
                summaryCounter.getCurrentJobCounter().get(),
                summaryCounter.getSuccessCounter().get(),
                summaryCounter.getFailCounter().get(),
                summaryCounter.getIgnoreCounter().get(),
                CommonUtil.stopRecordWithFormat("archive")
        );
    }

    private static List<Callable> buildCallables(List<File> fileList,Counter summaryCounter, Counter detailCounter) {
        List<Callable> callables = new ArrayList<>();
        for (File srcFile : fileList) {
            callables.add(new Callable<File>(){
                @Override
                public File call() {
                    return sync(srcFile, targetDir, new SyncFileCallbcak() {
                        @Override
                        public void call(File srcFile, File desFile,SyncStatus status) {
                            switch (status){
                                case SUCCESS:{
                                    detailCounter.getSuccessCounter().incrementAndGet();
                                    summaryCounter.getSuccessCounter().incrementAndGet();

                                    // 若是重试成功，则总失败数需要减1
                                    if(summaryCounter.getCurrentRunTimeCounter().get() > 1){
                                        summaryCounter.getFailCounter().decrementAndGet();
                                    }
                                }break;
                                case FAIL:{
                                    detailCounter.getFailCounter().incrementAndGet();
                                    if(summaryCounter.getCurrentRunTimeCounter().get() == 1){
                                        summaryCounter.getFailCounter().incrementAndGet();
                                    }
                                }break;
                                case IGNORE:{
                                    detailCounter.getIgnoreCounter().incrementAndGet();
                                    summaryCounter.getIgnoreCounter().incrementAndGet();
                                }break;
                            }
                            consoleLogger.info("{} => {} | 已归档,归档进度:{}/{},状态:{}",
                                    srcFile.getName(),
                                    desFile.getAbsolutePath(),
                                    (detailCounter.getSuccessCounter().get() +
                                            detailCounter.getFailCounter().get() +
                                            detailCounter.getIgnoreCounter().get()),
                                    detailCounter.getCurrentJobCounter(),
                                    status.getDesc());
                        }
                    });
                }
            });
        }
        return callables;
    }

    /**
     * 同步数据
     * @param srcFile
     * @param desDir
     * @return
     */
    private static File sync(File srcFile, File desDir,SyncFileCallbcak callbcak) {
        SyncStatus syncStatus = null;
        File desFile = null;
        String srcFileName = srcFile.getName();
        File descFileDir = null;
        if (CommonUtil.isExistDateStr(srcFileName)) {
            String date = CommonUtil.extractDateStr(srcFileName);
            String year = date.substring(0, 4);
            String month = Integer.parseInt(date.substring(4, 6)) + "";
            descFileDir = CommonUtil.createDescDir(desDir.getAbsolutePath(), year, month);
        }else{
            descFileDir = CommonUtil.createOtherDir(desDir.getAbsolutePath());
        }

        desFile = new File(descFileDir, srcFileName);
        if (desFile.exists()) {
            if (desFile.length() < srcFile.length()) {
                desFile.delete();
                desFile = new File(descFileDir, srcFileName);
            } else {
                syncStatus = SyncStatus.IGNORE;
            }
        }
        if (syncStatus == null) {
            File failFile = CommonUtil.fastCopy(srcFile, desFile);
            if (failFile == null) {
                syncStatus = SyncStatus.SUCCESS;
            } else {
                syncStatus = SyncStatus.FAIL;
            }
        }
        try{
            callbcak.call(srcFile,desFile,syncStatus);
        }catch (Exception ex){
            logger.warn("同步文件回调失败",ex);
        }
        return syncStatus == SyncStatus.FAIL ? srcFile : null;
    }
}
