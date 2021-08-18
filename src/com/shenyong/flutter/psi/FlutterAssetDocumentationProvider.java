package com.shenyong.flutter.psi;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.file.PsiBinaryFileImpl;
import com.shenyong.flutter.image.FastImageInfo;
import com.shenyong.flutter.psi.dart.DartAssetReferenceContributor;
import net.coobird.thumbnailator.Thumbnails;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FlutterAssetDocumentationProvider extends AbstractDocumentationProvider {

    private static final int MAX_RAW_SIZE = 720;

    private static final String TMP_DIR = "FlutterAssetsRefGenerator";
    private static final String TMP_THUMBNAIL = "assetThumbnail";

    @Override
    public @Nullable String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        if (element instanceof PsiBinaryFileImpl) {
            return "Go to asset file: " + ((PsiBinaryFileImpl) element).getVirtualFile().getName();
        }
        return null;
    }

    @Override
    public @Nullable String generateDoc(@NotNull PsiElement element, @Nullable PsiElement originalElement) {
        if (originalElement == null) {
            return null;
        }
        String originalText = originalElement.getText();
        boolean isValidYamlEle = element instanceof YAMLPlainTextImpl;
        boolean isValidDartEle = originalText.matches(DartAssetReferenceContributor.ASSET_PATTERN);
        if (!isValidDartEle && !isValidYamlEle) {
            return null;
        }
        // 可能存在多个资源变体
        VirtualFile[] assetFiles = AssetUtil.getAssetVirtualFile(originalElement);
        if (assetFiles == null || assetFiles.length == 0) {
            return null;
        }
        // 根据尺寸从小到大显示
        List<VirtualFile> assetList = Arrays.asList(assetFiles);
        assetList.sort(new Comparator<VirtualFile>() {
            @Override
            public int compare(VirtualFile o1, VirtualFile o2) {
                try {
                    FastImageInfo imgInfo1 = new FastImageInfo(new File((o1.getPath())));
                    FastImageInfo imgInfo2 = new FastImageInfo(new File((o2.getPath())));
                    return imgInfo1.getWidth() - imgInfo2.getWidth();
                } catch (IOException e) {
                    return 0;
                }
            }
        });
        StringBuilder sb = new StringBuilder();
        for (VirtualFile assetFile: assetList) {
            File imgFile = new File(assetFile.getPath());
            String uri = imgFile.toURI().toString();
            FastImageInfo imageInfo;
            try {
                imageInfo = new FastImageInfo(imgFile);
            } catch (IOException e) {
                return null;
            }
            int rawW = imageInfo.getWidth();
            int rawH = imageInfo.getHeight();
            ShowSize size = getShowSize(rawW, rawH);
            sb.append("<div class='definition'><pre>");
            sb.append(getDefinitionStr(assetFile));
            sb.append("</pre></div");
            sb.append("<div class='content' width=\"").append(size.width).append("px\" height=\"").append(size.height).append("\">");
            File tmpThumbnail;
            if (Math.max(rawW, rawH) <= MAX_RAW_SIZE || Math.min(rawW, rawH) <= 0) { // 有可能读取尺寸异常
                sb.append("  <img style=\"width: auto;height: auto;max-width: 100%;max-height: 100%;\" src=\"").append(uri).append("\">");
            } else {
                try {
                    tmpThumbnail = getTmpThumbnail(assetFile);
                    if (!tmpThumbnail.exists()) {
                        // 加载缩小的图片，以节省内存
                        Thumbnails.of(imgFile).size(size.width, size.height).toFile(tmpThumbnail);
                    }
                    String thumbnailUri = tmpThumbnail.toURI().toString();
                    sb.append("  <img style=\"width: auto;height: auto;max-width: 100%;max-height: 100%;\" src=\"")
                            .append(thumbnailUri).append("\">");
                } catch (IOException e) {
                    e.printStackTrace();
                    sb.append("Failed to show preview.");
                }
            }
            sb.append("</div>");
            sb.append("<table class='sections'>");
            if (Math.min(rawW, rawH) > 0) {
                if (rawW > size.width) {
                    addKeyValueSection("real size: ", rawW + "x" + rawH + " px", sb);
                    addKeyValueSection("preview size: ", size.width + "x" + size.height + " px", sb);
                } else {
                    addKeyValueSection("size: ", rawW + "x" + rawH + " px", sb);
                }
            }
            sb.append("</table>");
        }
        return sb.toString();
    }

    private String getDefinitionStr(VirtualFile assetFile) {
        // 尝试从原始路径中截取asset(s)/开始的部分路径
        String path = assetFile.getPath();
        int assetIndex = path.indexOf("asset");
        return assetIndex >= 0 ? path.substring(assetIndex) : path;
    }

    public static File getTmpThumbnail(VirtualFile assetFile) throws IOException {
        File dir = new File(FileUtilRt.getTempDirectory(), TMP_DIR);
        String suffix = "_" + assetFile.getName();
        if (!dir.exists()) {
            FileUtilRt.createTempDirectory(TMP_DIR, "", true);
        }
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return FileUtilRt.createTempFile(dir, TMP_THUMBNAIL, suffix, false, true);
        }
        for (File f : files) {
            if (f.getName().equals(TMP_THUMBNAIL + suffix)) {
                return f;
            }
        }
        return FileUtilRt.createTempFile(dir, TMP_THUMBNAIL, suffix, false, true);
    }

    private ShowSize getShowSize(int rawW, int rawH) {
        ShowSize size = new ShowSize(rawW, rawH);
        if (Math.max(rawW, rawH) <= MAX_RAW_SIZE) {
            return size;
        }
        float scale = MAX_RAW_SIZE * 1f / rawW;
        size.width = (int) (rawW * scale);
        size.height = (int) (rawH * scale);
        return size;
    }

    private static class ShowSize {
        public int width;
        public int height;

        public ShowSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    private void addKeyValueSection(String key, String value, StringBuilder sb) {
        sb.append("<tr><td valign='top' class='section'><p>");
        sb.append(key);
        sb.append("</td><td valign='top'>");
        sb.append("<p>");
        sb.append(value);
        sb.append("</td>");
    }
}
