package com.shenyong.flutter.psi;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.shenyong.flutter.image.FastImageInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.io.File;
import java.io.IOException;

public class FlutterAssetDocumentationProvider extends AbstractDocumentationProvider {

    /**
     *
     * @param element
     * @param originalElement dart或yaml中的原始声明字符串
     * @return
     */
    @Override
    public @Nullable String generateDoc(@NotNull PsiElement element, @Nullable PsiElement originalElement) {
        if (originalElement == null) {
            return null;
        }
        String originalText = originalElement.getText();
        boolean isValidDartEle = element instanceof YAMLPlainTextImpl;
        boolean isValidYamlEle = originalText.matches("^asset(s)?(/([-\\w]+|[1-9]\\.\\dx))*/[-\\w]+\\.(jp(e)?g|(9\\.)?png|webp|bmp)$");
        if (!isValidDartEle && !isValidYamlEle) {
            return null;
        }
        VirtualFile assetFile = AssetUtil.getAssetVirtualFile(originalElement);
        if (assetFile == null) {
            return null;
        }
        File imgFile = new File(assetFile.getPath());
        String uri = imgFile.toURI().toString();
        FastImageInfo imageInfo = null;
        try {
            imageInfo = new FastImageInfo(imgFile);
        } catch (IOException e) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='definition'><pre>");
        if (isValidDartEle) {
            sb.append(element.getText());
        } else {
            sb.append(originalText);
        }
        int width = imageInfo.getWidth();
        int height = imageInfo.getHeight();
        if (Math.max(width, height) > 1000) {
            width /= 2;
            height /= 2;
        }
        sb.append("</pre></div");
//        sb.append("<div class='content' width=\"").append(width).append("px\">");
//        sb.append("<div class='content'>");
        sb.append("<div style=\"background-color:#ff0000;\" class='content' width=\"").append(width).append("px\" height=\"")
                .append(height).append("px\" src=\"").append(uri).append("\">");
        // TODO: 2021/7/30  资源变体处理
        // TODO: 2021/8/13  Mac本机屏幕显示正常，但在外接的分辨率更低的扩展显示器上，预览图片显示不全
//        sb.append("  <img width=\"").append(width).append("px\" height=\"")
//                .append(height).append("px\" src=\"").append(uri).append("\">");
        sb.append("  <img src=\"").append(uri).append("\">");
        sb.append("</div>");
        sb.append("<table class='sections'>");
        addKeyValueSection("size: ", imageInfo.getWidth() + "x" + imageInfo.getHeight() + " px", sb);
        sb.append("</table>");
        return sb.toString();
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
