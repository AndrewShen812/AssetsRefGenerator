package com.shenyong.flutter.psi.yaml;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.ProjectScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class YamlAssetLineMarkerProvider extends RelatedItemLineMarkerProvider {
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (!isAssetElement(element)) {
            return;
        }
        final List<PsiElement> properties = new ArrayList<>();
        Project project = element.getProject();
        String text = element.getText();
        String fileName = text.substring(text.lastIndexOf('/') + 1).replaceAll("\"", "");
        PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, fileName, ProjectScope.getProjectScope(project));
        if (psiFiles.length == 0) {
            return;
        }
        properties.add(psiFiles[0]);
        NavigationGutterIconBuilder<PsiElement> builder =
                // TODO: 2021/8/1  根据缩略图生成ICON
                NavigationGutterIconBuilder.create(AllIcons.General.LayoutPreviewOnly)
                        .setTargets(properties)
                        .setTooltipText("Navigate to " + text.replaceAll("\"", ""));
        result.add(builder.createLineMarkerInfo(element));
    }

    private boolean isAssetElement(PsiElement element) {
        String text = element.getText();
        return element instanceof YAMLPlainTextImpl
                && text.matches(YamlAssetReferenceContributor.ASSET_PATTERN)
                // yaml中有后缀的资源声明才关联图标
                && text.lastIndexOf('.') != -1;
    }
}
