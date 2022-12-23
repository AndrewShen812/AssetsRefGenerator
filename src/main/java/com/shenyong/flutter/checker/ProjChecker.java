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

    private final ArrayList<String> checkFiles;

    {
        checkFiles = new ArrayList<>();
        checkFiles.add("lib");
        checkFiles.add(".metadata");
        checkFiles.add("pubspec.lock");
        checkFiles.add("pubspec.yaml");
    }

    @Override
    public CheckResult check(String path) {
        CheckResult result = new CheckResult();
        if (path == null || path.isEmpty()) {
            return result;
        }
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            return result;
        }
        String[] files = Objects.requireNonNull(dir.list());
        int cnt = 0;
        ArrayList<String> missingFiles = new ArrayList<>(checkFiles);
        for (String f : files) {
            if (checkFiles.contains(f)) {
                cnt++;
                missingFiles.remove(f);
            }
        }
        result.isOk = cnt == checkFiles.size();
        result.missingFiles.addAll(missingFiles);
        return result;
    }
}
