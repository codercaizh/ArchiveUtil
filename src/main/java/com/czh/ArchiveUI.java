package com.czh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class ArchiveUI {
    public static final Logger logger = LoggerFactory.getLogger(ArchiveUI.class);

    public static void main(String[] args) throws Exception {
        logger.info("欢迎使用归档工具，请按要求输入配置");
        Scanner sc = new Scanner(System.in);
        while(true){
            logger.info("请输入需要归档备份的文件夹地址");
            String srcPath = getInput(sc);
            logger.info("请输入存放归档备份的文件夹地址");
            String desPath = getInput(sc);
            logger.info("正在检查文件夹地址的合法性");
            File src = new File(srcPath);
            if (!src.exists()){
                logger.info("需要归档备份的文件夹不存在");
                continue;
            }
            if (src.isFile()){
                logger.info("需要归档备份的文件夹路径应该为文件夹路径而非文件路径");
                continue;
            }
            File des = new File(desPath);
            if(!des.exists()){
                try{
                    des.mkdirs();
                }catch (Exception ex){
                    logger.info("存放归档备份的文件夹路径非法，或无读写权限:{}" + ex.getMessage());
                    continue;
                }
            }

            if(des.isFile()){
                logger.info("存放归档备份的文件夹路径应该为文件夹路径而非文件路径");
                continue;
            }

            File tmp = new File(des,System.currentTimeMillis() + "" + ThreadLocalRandom.current().nextInt(100000));
            try{
                if(!tmp.createNewFile()){
                    throw new RuntimeException("没有写入权限");
                }
            }catch (Exception ex){
                logger.info("存放归档备份的文件夹无法进行写入");
                continue;
            }
            tmp.delete();
            logger.info("文件夹校验通过");
            if(ArchiveUtil.threadCount == 0){
                int defaultThreadCount = 2;
                logger.info("请输入并发归档数(取值范围:1 - 64,默认值为{}),若不了解该参数,则直接回车下一步",defaultThreadCount);
                String c = getInput(sc);
                if(c != null && c.length() > 0){
                    try{
                        int count = Integer.parseInt(c);
                        if (count < 1 || count > 64){
                            throw new Exception();
                        }
                        ArchiveUtil.threadCount = count;
                    }catch (Exception ex){
                        logger.info("输入的参数非法,已设置成默认值");
                        ArchiveUtil.threadCount = defaultThreadCount;
                    }
                }else {
                    ArchiveUtil.threadCount = defaultThreadCount;
                }
            }
            ArchiveUtil.sourceDir = src;
            ArchiveUtil.targetDir = des;
            logger.info("开始进行归档");
            ArchiveUtil.run();
        }
    }

    private static String getInput(Scanner sc){
        return sc.nextLine();
    }
}
