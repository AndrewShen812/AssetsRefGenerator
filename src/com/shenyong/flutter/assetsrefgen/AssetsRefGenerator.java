package com.shenyong.flutter.assetsrefgen;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class AssetsRefGenerator extends AnAction {

    private static ArrayList<String> projFiles;
    private static ArrayList<String> assetFiles;
    private static final String PUBSPEC = "pubspec.yaml";
    private static final String RES_FILE = "res.dart";

    static {
        projFiles = new ArrayList<>();
        projFiles.add("android");
        projFiles.add("ios");
        projFiles.add("lib");
        projFiles.add("pubspec.lock");
        projFiles.add(PUBSPEC);

        assetFiles = new ArrayList<>();
        assetFiles.add("asset");
        assetFiles.add("assets");
        assetFiles.add("images");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        String path = project.getBasePath();
        if (!checkFlutterProj(path)) {
            showErrMsg("当前似乎不在一个有效的Flutter工程目录");
            return;
        }
        if (!checkAssets(path)) {
            showErrMsg("当前工程似乎还没有定义资源目录（asset/assets/images）");
            return;
        }

        genAssetRef(path);
    }

    private void showErrMsg(String msg) {
        Messages.showMessageDialog(msg, "Flutter Assets Reference Generator", Messages.getErrorIcon());
    }

    private void showInfo(String msg) {
        Messages.showMessageDialog(msg, "Flutter Assets Reference Generator", Messages.getInformationIcon());
    }

    private boolean isAllFilesContained(String[] files, ArrayList<String> checkFiles) {
        int cnt = 0;
        for (String f : files) {
            if (checkFiles.contains(f)) {
                cnt++;
            }
        }
        return cnt >= checkFiles.size();
    }

    private boolean checkFlutterProj(String path) {
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            showErrMsg("当前似乎不在一个有效的Flutter工程目录");
        }
        return isAllFilesContained(dir.list(), projFiles);
    }

    private boolean checkAssets(String path) {
        File dir = new File(path);
        String[] files = dir.list();
        int cnt = 0;
        for (String f : files) {
            if (assetFiles.contains(f)) {
                cnt++;
            }
        }
        return cnt > 0;
    }

    private List<String> getAssets(String path) {
        System.out.println("扫描资源文件...");
        List<String> assets = new ArrayList<>();
        for (String name : assetFiles) {
            File dir = new File(path, name);
            getAssets(assets, dir, name);
        }
        return assets;
    }

    private void getAssets(List<String> assets, File dir, String prefix) {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                String name = f.getName();
                if ("2.0x".equals(name) || "3.0x".equals(name)) {
                    getAssets(assets, f, prefix);
                } else {
                    getAssets(assets, f, prefix + "/" + f.getName());
                }
            } else {
               String name = f.getName();
               String asset = "    - " + prefix + "/" + name;
               if (!assets.contains(asset)) {
                   assets.add(asset);
               }
            }
        }
    }

    private void genAssetRef(String path) {
        List<String> assets = getAssets(path);
        if (assets.isEmpty()) {
            return;
        }
        updatePubspec(path, assets);
        genResDart(path, assets);
    }

    /**
     * 更新pubspec.yaml文件中的资源声明
     * @param path 项目路径
     * @param assets 扫描生成的资源声明
     */
    private boolean updatePubspec(String path, List<String> assets) {
        System.out.println("重新生成 pubspec.yaml ...");
        File pubspec = new File(path, PUBSPEC);
        if (!pubspec.exists()) {
            return false;
        }
        List<String> outLines = new ArrayList<>();
        List<String> oldRemained = new ArrayList<>();
        List<String> delAssets = new ArrayList<>();
        boolean assetStart = false;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        boolean hasErr = false;
        try {
            reader = new BufferedReader(new FileReader(pubspec));
            String line = reader.readLine();
            while (line != null) {
                if (line.matches("^ {2}assets:")) {
                    // 检测到资源声明起始行
                    assetStart = true;
                    outLines.add(line);
                    line = reader.readLine();
                    continue;
                }
                if (assetStart) {
                    // 原pubspec.yaml文件中就有的资源声明，或资源声明之间的空行
                    if (line.matches("^ {2,}- .*") || line.matches("^\\S*$")) {
                        // 原有的其他声明，可能是已删除的，或引入的其他package的资源
                        if (line.matches("^ {2,}- .*") && !assets.contains(line)) {
                            oldRemained.add(line);
                        }
                        // TODO: 2020/1/8 处理资源删除
//                        if (!assets.contains(line)) {
//                            assets.add(line);
//                        }
                    } else {
                        // 资源声明结束
                        assetStart = false;
                        removeDeleted(assets, oldRemained);
                        // 默认按字母顺序排序
                        assets.sort(String::compareToIgnoreCase);
                        outLines.addAll(assets);
                        outLines.add(line);
                    }
                } else {
                    outLines.add(line);
                }
                line = reader.readLine();
            }
            // 将更新了资源声明的内容写回到pubspec.yaml文件
            writer = new BufferedWriter(new FileWriter(pubspec));
            for (String out : outLines) {
                writer.write(out);
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            hasErr = true;
//            showErrMsg("错误信息:" + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    reader = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                    writer = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            if (!hasErr) {
//                showInfo("已更新 pubspec.yaml 文件");
//            }
        }
        return !hasErr;
    }

    /**
     * 去掉已删除资源的旧声明，但保留引入的其他package的资源（以”  - packages/*"形式声明的）
     * @param newAssets 扫描生成的资源声明
     * @param oldRemained 遗留的其他声明
     */
    private void removeDeleted(List<String> newAssets, List<String> oldRemained) {
        for (String line: oldRemained) {
            if (line.matches("^ {2,}- packages/.*")) {
                newAssets.add(line);
            }
        }
    }

    private void genResDart(String path, List<String> assets) {
        System.out.println("更新 res.dart ...");
        File resFile = new File(path + "/" + "lib", RES_FILE);
        if (!resFile.exists()) {
            try {
                resFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(resFile));
            // TODO: 2020/1/8 其他语言地区格式处理
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            writer.write("/// Generated by AssetsRefGenerator on " + sdf.format(Calendar.getInstance().getTime()));
            writer.newLine();
            writer.write("class Res {");
            writer.newLine();
            for (String out : assets) {
                String assetPath = out.replaceAll(" {2,}- ", "").trim();
                String name = out.substring(out.lastIndexOf("/") + 1).split("\\.")[0];
                writer.write("  static const String " + name + " = \"" + assetPath + "\";");
                writer.newLine();
            }
            writer.write("}");
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                    writer = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("已更新资源声明");
        showInfo("Complete!\nAssets reference has been updated successfully.");
    }
}
