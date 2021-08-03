package com.shenyong.flutter.psi.dart;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.ProjectScope;
import com.jetbrains.lang.dart.psi.impl.DartStringLiteralExpressionImpl;
import com.shenyong.flutter.psi.AssetUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DartAssetLineMarkerProvider extends RelatedItemLineMarkerProvider {
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (!isAssetElement(element)) {
            return;
        }
        String dartText = element.getText().replaceAll("[\"']", "");
        int index = dartText.lastIndexOf('/');
        String fileName = dartText;
        if (index != -1) {
            fileName = dartText.substring(index + 1);
        }
        boolean hasSuffix = fileName.lastIndexOf('.') != -1;

        final List<PsiElement> properties = new ArrayList<>();
        Project project = element.getProject();
        PsiFile[] psiFiles;
        if (hasSuffix) {
            psiFiles = FilenameIndex.getFilesByName(project, fileName, ProjectScope.getProjectScope(project));
        } else {
            // 无后缀处理
            psiFiles = AssetUtil.getAssetFileWithoutSuffix(project, fileName);
        }
        if (psiFiles.length == 0) {
            return;
        }
        properties.add(psiFiles[0]);
        File imgFile = new File(psiFiles[0].getVirtualFile().getPath());
        String uri = imgFile.toURI().toString();
        NavigationGutterIconBuilder<PsiElement> builder =
                // TODO: 2021/8/1  根据缩略图生成ICON
                NavigationGutterIconBuilder.create(AllIcons.General.LayoutPreviewOnly)
                        .setTargets(properties)
                        .setTooltipText("Navigate to " + dartText);
        result.add(builder.createLineMarkerInfo(element));
    }

    private boolean isAssetElement(PsiElement element) {
        String text = element.getText();
        return element instanceof DartStringLiteralExpressionImpl
                && text.matches(DartAssetReferenceContributor.ASSET_PATTERN);
    }
}
