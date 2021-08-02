package com.shenyong.flutter.checker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author ShenYong
 * @date 2020/5/6
 */
public class AssetsChecker implements ICheck {

    private ArrayList<String> assetFiles;

    {
        assetFiles = new ArrayList<>();
        assetFiles.add("asset");
        assetFiles.add("assets");
        assetFiles.add("images");
    }

    @Override
    public CheckResult check(String path) {
        CheckResult result = new CheckResult();
        if (path == null || path.isEmpty()) {
            return result;
        }

        File dir = new File(path);
        String[] files = Objects.requireNonNull(dir.list());
        int cnt = 0;
        for (String f : files) {
            if (assetFiles.contains(f)) {
                cnt++;
            }
        }
        result.isOk = cnt > 0;
        return result;
    }

    public List<String> getAssetsDirs() {
        return assetFiles;
    }
}
