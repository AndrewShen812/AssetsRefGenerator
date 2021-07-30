package com.shenyong.flutter.psi.dart;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.shenyong.flutter.psi.AssetUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class DartAssetDocumentationProvider extends AbstractDocumentationProvider {
    @Override
    public @Nullable String generateDoc(@NotNull PsiElement element, @Nullable PsiElement originalElement) {
        if (originalElement == null) {
            return "Unable to get doc for " + element.getText();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(DocumentationMarkup.DEFINITION_START);
        sb.append(originalElement.getText());
        sb.append(DocumentationMarkup.DEFINITION_END);
        sb.append(DocumentationMarkup.CONTENT_START);
        VirtualFile assetFile = AssetUtil.getAssetVirtualFile(originalElement);
        if (assetFile != null) {
            String uri = (new File(assetFile.getPath())).toURI().toString();
            // TODO: 2021/7/30  资源变体处理
            sb.append(assetFile.getPath() +
                    "<div>" +
                    "  <img src=\"" + uri + "\">" +
                    "</div>");
        }
        sb.append(DocumentationMarkup.CONTENT_END);
        sb.append(DocumentationMarkup.SECTIONS_START);
        if (assetFile != null) {
            addKeyValueSection("path:", assetFile.getPath(), sb);
            addKeyValueSection("uri:", (new File(assetFile.getPath())).toURI().toString(), sb);
        }
        sb.append(DocumentationMarkup.SECTIONS_END);
        return sb.toString();
    }

    private void addKeyValueSection(String key, String value, StringBuilder sb) {
        sb.append(DocumentationMarkup.SECTION_HEADER_START);
        sb.append(key);
        sb.append(DocumentationMarkup.SECTION_SEPARATOR);
        sb.append("<p>");
        sb.append(value);
        sb.append(DocumentationMarkup.SECTION_END);
    }
}
