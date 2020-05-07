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
import com.shenyong.flutter.checker.AssetsChecker;
import com.shenyong.flutter.checker.ProjChecker;

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
 * @author sy
 * @date 2020年1月8日
 */
public class AssetsRefGenerator extends AnAction {

    private static final String PUBSPEC = "pubspec.yaml";
    private static final String RES_FILE = "res.dart";
    private static final String MAC_OS_DS_STORE = ".DS_Store";

    private ProjChecker projChecker = new ProjChecker();
    private AssetsChecker assetsChecker = new AssetsChecker();

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        String path = Objects.requireNonNull(project).getBasePath();
        if (!projChecker.check(path)) {
            showErrMsg("Current directory does not seem to be a valid Flutter project directory.");
            return;
        }
        if (!assetsChecker.check(path)) {
            showErrMsg("No asset directory named asset, assets or images was found.");
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

    private List<String> getAssets(String path) {
        System.out.println("Scanning asset files under asset, assets and images...");
        assetsNames.clear();
        namedAssets.clear();
        List<String> assetsDirs = assetsChecker.getAssetsDirs();
        List<String> assets = new ArrayList<>();
        for (String name : assetsDirs) {
            File dir = new File(path, name);
            getAssets(assets, dir, name, false);
        }
        return assets;
    }

    private HashSet<String> assetsNames = new HashSet<>();
    private HashMap<String, String> namedAssets = new HashMap<>();

    /**
     * 遍历资源目录，生成资源声明
     * @param assets 资源声明集合
     * @param dir 目录
     * @param prefix 当前目录的资源路径前缀
     * @param inMultiRatioDir 当前是否在 2.0x 3.0x 等多像素比目录下，用于判断重名资源层级
     */
    private void getAssets(List<String> assets, File dir, String prefix, boolean inMultiRatioDir) {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                // 忽略 MacOS 中的 .DS_Store 文件
                return !MAC_OS_DS_STORE.equals(name);
            }
        });
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
                // 变体处理：在相邻子目录中查找具有相同名称的任何文件，如果添加过同名的，则认为当前资源为一个变体，不再添加。
                // 但非相邻子目录中的同名文件，不算变体，如：/imageStyle1/1.png 和 /imageStyle2/1.png
                String asset = "    - " + prefix + "/" + name;
                String nameKey = name.split("\\.")[0];
                if (!assetsNames.contains(name)) {
                    namedAssets.put(asset, nameKey);
                    assetsNames.add(name);
                    assets.add(asset);
                    System.out.println(asset);
                } else {
                    String existedAsset = "";
                    for (String s : assets) {
                        if (s.contains(name)) {
                            existedAsset = s;
                            break;
                        }
                    }
                    int existedDepth = existedAsset.split("/").length;
                    String[] newAsset = asset.split("/");
                    int newDepth = newAsset.length;
                    newDepth = inMultiRatioDir ? newDepth + 1 : newDepth;
                    if (newDepth > existedDepth) {
                        // 同名且有更深的路径层级，认为是变体
                        continue;
                    }
                    nameKey = nameKey.trim().replaceAll(" ", "_");
                    String namePrefix = prefix.replaceAll(" ", "_").replaceAll("/", "_");
                    namedAssets.put(asset, namePrefix + "_" + nameKey);
                    assets.add(asset);
                    System.out.println(asset);
                }
            } else {
                // 2.0x 3.0x 等多分辨率目录处理
                if (name.matches("^[1-9](\\.\\d)x$")) {
                    getAssets(assets, f, prefix, true);
                } else {
                    getAssets(assets, f, prefix + "/" + name, false);
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
     *
     * @param path   项目路径
     * @param assets 扫描生成的资源声明
     */
    private void updatePubspec(String path, List<String> assets) {
        System.out.println("Updating pubspec.yaml...");
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
     *
     * @param newAssets   扫描生成的资源声明
     * @param oldRemained 遗留的其他声明
     */
    private void removeDeleted(List<String> newAssets, List<String> oldRemained) {
        for (String line : oldRemained) {
            if (line.matches("^ {2,}- packages/.*")) {
                newAssets.add(line);
            }
        }
    }

    private static final Pattern PATTERN = Pattern.compile("packages/(?<pkgName>[a-z_]+)/.*");

    private void genResDart(String path, List<String> assets) {
        System.out.println("Updating res.dart...");
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
            List<String> assetDefines = new ArrayList<>();
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
                String name = namedAssets.get(out);
                if (name == null) {
                    name = out.substring(out.lastIndexOf("/") + 1).split("\\.")[0];
                }
                assetDefines.add("  static const String " + name + " = \"" + assetPath + "\";");
            }

            assetDefines.sort(String::compareToIgnoreCase);
            for (String s : assetDefines) {
                writer.write(s);
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
        System.out.println("Flutter assets reference has been updated.");
        showSuccessInfo();
    }
}
