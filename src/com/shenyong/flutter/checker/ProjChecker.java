package com.shenyong.flutter.checker;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

/**
 * 项目结构检查，看是否是一个有效的 Flutter 工程目录。
 * @author ShenYong
 * @date 2020/5/6
 */
public class ProjChecker implements ICheck {

    private ArrayList<String> checkFiles;

    {
        checkFiles = new ArrayList<>();
        checkFiles.add("lib");
        checkFiles.add(".metadata");
        checkFiles.add(".packages");
        checkFiles.add("pubspec.lock");
        checkFiles.add("pubspec.yaml");
    }

    @Override
    public boolean check(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            return false;
        }
        String[] files = Objects.requireNonNull(dir.list());
        int cnt = 0;
        for (String f : files) {
            if (checkFiles.contains(f)) {
                cnt++;
            }
        }
        return cnt == checkFiles.size();
    }
}
