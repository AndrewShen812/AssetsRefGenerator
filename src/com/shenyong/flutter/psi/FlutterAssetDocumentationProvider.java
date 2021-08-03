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
        boolean isValidYamlEle = originalText.matches("^asset(s)?(/([-\\w]+|[1-9]\\.\\dx))*/[-\\w]+\\.(jp(e)?g|png|webp|bmp)$");
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
        sb.append("</pre></div");
        sb.append("<div class='content' width=\"").append(imageInfo.getWidth()).append("\">");
        // TODO: 2021/7/30  资源变体处理
        sb.append("  <img width=\"").append(imageInfo.getWidth()).append("\" height=\"")
                .append(imageInfo.getHeight()).append("\" src=\"").append(uri).append("\">");
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
