package com.shenyong.flutter.psi;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.lang.dart.psi.impl.DartStringLiteralExpressionImpl;
import com.shenyong.flutter.Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FlutterAssetLineMarkerProvider extends RelatedItemLineMarkerProvider {
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (!isAssetElement(element)) {
            return;
        }
        final List<PsiElement> properties = new ArrayList<>();
        Project project = element.getProject();
        String text = element.getText();
        String fileName = text.substring(text.lastIndexOf('/') + 1).replaceAll("\"", "");
        PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, fileName, GlobalSearchScope.allScope(project));
        if (psiFiles.length == 0) {
            return;
        }
        properties.add(psiFiles[0]);
        NavigationGutterIconBuilder<PsiElement> builder =
                // TODO: 2021/8/1  根据缩略图生成ICON
                NavigationGutterIconBuilder.create(Icons.ICON)
                        .setTargets(properties)
                        .setTooltipText("Navigate to " + text.replaceAll("\"", ""));
        result.add(builder.createLineMarkerInfo(element));
    }

    private boolean isAssetElement(PsiElement element) {
        String text = element.getText();
        return (element instanceof DartStringLiteralExpressionImpl || element instanceof YAMLPlainTextImpl)
                && text.matches(FlutterAssetReferenceContributor.ASSET_PATTERN);
    }
}
