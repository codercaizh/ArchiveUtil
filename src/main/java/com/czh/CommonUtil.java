package com.czh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class CommonUtil {
    private static Map<String, File> dirsContainer = new ConcurrentHashMap<>();

    private static Pattern pattern = Pattern.compile("20[1-2][0-9][0-1][0-9][0-3][0-9]");

    private static Map<String,Long> currentTimeMap = new ConcurrentHashMap<>();

    private static Map<String,Boolean> typeMap = new ConcurrentHashMap<String,Boolean>();

    /**
     * 快速复制文件
     *
     * @param source
     * @param target
     * @return
     */
    public static File fastCopy(File source, File target) {
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(target);
            in = input.getChannel();
            out = output.getChannel();
            in.transferTo(0, in.size(), out);
            target.setLastModified(source.lastModified());
        } catch (Exception e) {
            e.printStackTrace();
            return source;
        } finally {
            try {
                input.close();
                in.close();
                output.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 生成目标文件夹(带缓存)
     *
     * @param descDirPath
     * @param year
     * @param month
     * @return
     */
    public static File createDescDir(String descDirPath, String year, String month) {
        String key = year + "/" + month;
        if (dirsContainer.containsKey(key)) {
            return dirsContainer.get(key);
        }
        File descDirFile = new File(descDirPath + "/" + year + "年/" + month + "月");
        if (!descDirFile.exists()) {
            descDirFile.mkdirs();
        }
        dirsContainer.put(key, descDirFile);
        return descDirFile;
    }

    /**
     * 创建存放其它文件的文件夹
     * @return
     */
    public static File createOtherDir(String descDirPath){
        String key = "other";
        if (dirsContainer.containsKey(key)) {
            return dirsContainer.get(key);
        }
        File descDirFile = new File(descDirPath + "/other");
        if(!descDirFile.exists()){
            descDirFile.mkdirs();
        }
        dirsContainer.put(key,descDirFile);
        return descDirFile;
    }

    /**
     * 判断字符串是否包含标准日期
     * @param str
     * @return
     */
    public static boolean isExistDateStr(String str) {
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    /**
     * 提取字符串的标准日期
     * @param str
     * @return
     */
    public static String extractDateStr(String str) {
        Matcher matcher = pattern.matcher(str);
        return matcher.find() ? matcher.group() : null;
    }

    /**
     * 批量异步执行并等待返回结果
     * @param executorService
     * @param callables
     * @return
     * @throws Exception
     */
    public static List asyncRun(ExecutorService executorService, List<Callable> callables) throws Exception {
        List<Future> futures = new ArrayList();
        List result = new ArrayList();
        for (Callable callable : callables) {
            futures.add(executorService.submit(callable));
        }
        for (Future future : futures) {
            Object file = future.get();
            if(file != null){
                result.add(file);
            }
        }
        return result;
    }

    /**
     * 开始记录时间
     * @param event
     */
    public static void startRecord(String event){
        if(currentTimeMap.containsKey(event)){
            throw new RuntimeException("该计时器已在计时");
        }
        currentTimeMap.put(event,System.currentTimeMillis());
    }

    /**
     *
     */
    public static long stopRecord(String event){
        Long startTime = currentTimeMap.get(event);
        if(startTime == null){
            throw new RuntimeException("该计时器不存在");
        }
        currentTimeMap.remove(event);
        return System.currentTimeMillis() - startTime;
    }

    /**
     *
     */
    public static String stopRecordWithFormat(String event){
        long useTime = stopRecord(event) / 1000;
        long h=useTime/3600;			//小时
        long m=(useTime%3600)/60;		//分钟
        long s=(useTime%3600)%60;		//秒
        if(h>0){
            return h+"小时"+m+"分钟"+s+"秒";
        }
        if(m>0){
            return m+"分钟"+s+"秒";
        }
        return s+"秒";
    }

    /**
     * 获取某个文件夹下所有子文件夹的目录
     * @param dir
     * @return
     */
    public static List<File> getFileList(File dir){
        List<File> list = new ArrayList<>();
        if(isFile(dir)){
            list.add(dir);
        }else{
            getAllFiles(list,dir);
        }
        return list;
    }

    /**
     * 遍历子目录获取所有文件
     * @param fileList
     * @param dir
     */
    private static void getAllFiles(List<File> fileList, File dir) {
        File[] allFiles = dir.listFiles();
        for (int i = 0; i < allFiles.length; i++) {
            File file = allFiles[i];

            if (isFile(file)) {
                fileList.add(file);
                int size = fileList.size();
                if(size % 100 == 0){
                    ArchiveUtil.logger.info("已加载{}个文件,请稍等",size);
                }
            } else {
                getAllFiles(fileList, file);
            }
        }
    }

    /**
     * 是否为文件
     * @param file
     * @return
     */
    private static boolean isFile(File file){
        String type = null;
        int index = file.getName().lastIndexOf(".");
        if(index > 0){
            type = file.getName().substring(index).trim().toLowerCase();
            type = type.length() > 0 ? type : null;
        }
        if(type == null){
            return file.isFile();
        }else if(typeMap.containsKey(type)){
            return true;
        }else {
            boolean isFile = file.isFile();
            if(isFile){
                typeMap.put(type,isFile);
            }
            return isFile;
        }
    }
}
