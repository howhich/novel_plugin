package com.demo.com.demo;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtil {
    public static void collectFiles(File dir, List<String> files) {
        File[] fileList = dir.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    collectFiles(file, files); // 递归调用
                } else {
                    files.add(file.getPath());
                }
            }
        }
        Collections.sort(files, new FileNameNumberComparator());
    }
    public static String extractNumber(String input) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
    static class FileNameNumberComparator implements Comparator<String> {
        @Override
        public int compare(String f1, String f2) {
            String numStr1 = extractNumber(f1);
            String numStr2 = extractNumber(f2);

            if (numStr1 == null && numStr2 == null) {
                return f1.compareTo(f2); // 如果都没有数字，按文件名排序
            } else if (numStr1 == null) {
                return 1; // f1没有数字，f2有数字，f1排在f2后面
            } else if (numStr2 == null) {
                return -1; // f2没有数字，f1有数字，f1排在f2前面
            } else {
                return Integer.compare(Integer.parseInt(numStr1), Integer.parseInt(numStr2)); // 比较数字大小
            }
        }
    }
}
