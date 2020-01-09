/*
 * MIT License
 *
 * Copyright (c) 2020 Andrew Shen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.shenyong.flutter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Flutter 资源声明和Dart引用生成工具
 * <p>
 * 功能： 扫描工程 asset/assets/images 目录下的资源文件，自动在 pubspec.yaml 文件中添加资源文件声明；并生成一个 res.dart 文件，
 * 包含所有资源文件的字符串声明。
 * <p>
 * 主要解决问题：无需手动编辑 pubspec.yaml 中的资源文件声明和代码中的资源引用字符串。即避免出错，也方便开发编码，像 Android 中
 * R.drawable.xxx 方式一样，更加愉快的引用资源。
 *
 * @date 2020年1月8日
 * @author sy
 */
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
        String path = Objects.requireNonNull(project).getBasePath();
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

    private void showSuccessInfo() {
        Messages.showMessageDialog("Complete!\nAssets reference has been updated successfully.",
                "Flutter Assets Reference Generator", Messages.getInformationIcon());
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
        if (path == null || path.isEmpty()) {
            return false;
        }
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            showErrMsg("当前似乎不在一个有效的Flutter工程目录");
        }
        return isAllFilesContained(Objects.requireNonNull(dir.list()), projFiles);
    }

    private boolean checkAssets(String path) {
        File dir = new File(path);
        String[] files = Objects.requireNonNull(dir.list());
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
        assetsNames.clear();
        List<String> assets = new ArrayList<>();
        for (String name : assetFiles) {
            File dir = new File(path, name);
            getAssets(assets, dir, name);
        }
        return assets;
    }

    private HashSet<String> assetsNames = new HashSet<>();
    private void getAssets(List<String> assets, File dir, String prefix) {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        List<File> fList = Arrays.asList(files);
        /* 处理资源变体，参考：
          https://flutterchina.club/assets-and-images/
          https://flutter.dev/docs/development/ui/assets-and-images
        */
        // 重新排序，文件排在目录前面。先处理文件，然后处理下级目录，方便处理资源变体
        fList.sort((o1, o2) -> {
            if (o1.isFile() && o2.isDirectory()) {
                return -1;
            } else if (o1.isDirectory() && o2.isFile()) {
                return 1;
            }
            return 0;
        });
        for (File f : fList) {
            String name = f.getName();
            if (f.isFile()) {
                // 如果添加过同名的，则认为当前资源为一个变体，不再添加
                if (!assetsNames.contains(name)) {
                    assetsNames.add(name);
                    String asset = "    - " + prefix + "/" + name;
                    assets.add(asset);
                }
            } else {
                // 2.0x 3.0x 等多分辨率目录处理
                if (name.matches("^[1-9](\\.\\d)x$")) {
                    getAssets(assets, f, prefix);
                } else {
                    getAssets(assets, f, prefix + "/" + name);
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
    private void updatePubspec(String path, List<String> assets) {
        System.out.println("更新 pubspec.yaml ...");
        File pubspec = new File(path, PUBSPEC);
        if (!pubspec.exists()) {
            return;
        }
        List<String> outLines = new ArrayList<>();
        List<String> oldRemained = new ArrayList<>();
        boolean assetStart = false;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(pubspec));
            String line = reader.readLine();
            while (line != null) {
                if (line.matches("^ {2}assets:")) {
                    // 检测到资源声明起始行"  assets:"
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
                if (line == null && assetStart) {
                    // 资源声明在yaml文件末尾的情况。判断asset声明未结束，但已读取到文件末尾了
                    assetStart = false;
                    removeDeleted(assets, oldRemained);
                    // 默认按字母顺序排序
                    assets.sort(String::compareToIgnoreCase);
                    outLines.addAll(assets);
                }
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
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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

    private static final Pattern PATTERN = Pattern.compile("packages/(?<pkgName>[a-z_]+)/.*");
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
            List<String> packages = new ArrayList<>();
            for (String out : assets) {
                String assetPath = out.replaceAll(" {2,}- ", "").trim();
                // 处理其他 package 的资源文件声明
                // 声明格式通常为：   - packages/package_name/...
                if (out.matches("^ {2,}- packages/[a-z_]+/.*")) {
                    // 获取包名称
                    Matcher matcher = PATTERN.matcher(assetPath);
                    if (matcher.find()) {
                        String pkgName = matcher.group("pkgName");
                        if (!packages.contains(pkgName)) {
                            packages.add(pkgName);
                        }
                    }
                    assetPath = assetPath.replaceFirst("packages/[a-z_]+/", "");
                }
                String name = out.substring(out.lastIndexOf("/") + 1).split("\\.")[0];
                writer.write("  static const String " + name + " = \"" + assetPath + "\";");
                writer.newLine();
            }
            writer.write("}");
            writer.newLine();
            if (!packages.isEmpty()) {
                writer.newLine();
                writer.write("class Packages {");
                writer.newLine();
                for (String pkg : packages) {
                    writer.write("  static const String " + pkg + " = \"" + pkg + "\";");
                    writer.newLine();
                }
                writer.write("}");
                writer.newLine();
            }

            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("已更新 Flutter 资源声明");
        showSuccessInfo();
    }
}
