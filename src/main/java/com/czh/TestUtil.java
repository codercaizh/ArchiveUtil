package com.czh;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TestUtil {
    public static void main(String[] args) throws IOException {
        List<File> allfiles = CommonUtil.getFileList(new File("/Volumes/backup/DisasterRecovery/相册归档"));
        File fileLog = new File("File.log");
        StringBuilder sb = new StringBuilder();
        for(File file : allfiles){
            sb.append(file.getName()).append("#codercai#").append(file.length()).append("\n");
        }
        Files.write(sb.toString().getBytes(StandardCharsets.UTF_8),fileLog);
    }
}
