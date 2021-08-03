package com.shenyong.flutter.psi;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.shenyong.flutter.AssetsRefGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YamlRecursivePsiElementVisitor;
import org.jetbrains.yaml.psi.impl.YAMLFileImpl;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.Collection;

public class AssetUtil {
    public static VirtualFile getAssetVirtualFile(PsiElement psiElement) {
        PsiFile[] psiFiles = getAssetPsiFiles(psiElement);
        return psiFiles.length > 0 ? psiFiles[0].getVirtualFile() : null;
    }

    public static PsiFile[] getAssetPsiFiles(PsiElement psiElement) {
        String text = psiElement.getText().replaceAll("[\"']", "");
        String fileName = text;
        int slashIndex = text.lastIndexOf('/');
        if (slashIndex != -1) {
            fileName = text.substring(text.lastIndexOf('/') + 1);
        }
        boolean hasSuffix = fileName.lastIndexOf('.') != -1;
        Project project = psiElement.getProject();
        if (hasSuffix) {
            return FilenameIndex.getFilesByName(project, fileName, ProjectScope.getProjectScope(project));
        } else {
            return getAssetFileWithoutSuffix(project, fileName);
        }
    }

    public static YAMLPlainTextImpl findReferenceForDartElement(PsiElement dartPsiElement) {
        Project project = dartPsiElement.getProject();
        Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(project, "pubspec.yaml", ProjectScope.getProjectScope(project));
        if (files.size() < 1) {
            return null;
        }
        YAMLFileImpl yamlFile = (YAMLFileImpl) PsiManager.getInstance(project).findFile(files.iterator().next());
        if (yamlFile == null) {
            return null;
        }
        // dart 中资源引用可能的形式：
        // Image.asset('assets/images/food01.jpeg'）
        // Utils.getImgPath('ic_launcher_news.png')
        // Utils.getImgPath('ic_launcher_news')
        String dartText = dartPsiElement.getText().replaceAll("[\"']", "");
        int index = dartText.lastIndexOf('/');
        String name = dartText;
        if (index != -1) {
            name = dartText.substring(index + 1);
        }
        boolean hasSuffix = name.lastIndexOf('.') != -1;

        Collection<YAMLPlainTextImpl> elements = PsiTreeUtil.findChildrenOfType(yamlFile, YAMLPlainTextImpl.class);
        for (YAMLPlainTextImpl e : elements) {
            String yamlText = e.getText();
            if (!hasSuffix && yamlText.contains(name)) {
                int slashIndex = yamlText.lastIndexOf('/');
                String yamlName = yamlText;
                if (slashIndex != -1) {
                    yamlName = yamlText.substring(slashIndex + 1);
                }
                int dotIndex = yamlName.lastIndexOf('.');
                if (dotIndex != -1 && name.equals(yamlName.substring(0, dotIndex))) {
                    return e;
                }
            }
            if (dartText.equals(yamlText) || yamlText.endsWith(name)) {
                return e;
            }
        }

        return null;
    }

    public static PsiFile[] getAssetFileWithoutSuffix(Project project, String nameWithoutSuffix) {
        PsiFile[] files = FilenameIndex.getFilesByName(project, nameWithoutSuffix + ".png", ProjectScope.getProjectScope(project));
        if (files.length > 0) {
            return files;
        }
        files = FilenameIndex.getFilesByName(project, nameWithoutSuffix + ".jpg", ProjectScope.getProjectScope(project));
        if (files.length > 0) {
            return files;
        }
        files = FilenameIndex.getFilesByName(project, nameWithoutSuffix + ".jpeg", ProjectScope.getProjectScope(project));
        if (files.length > 0) {
            return files;
        }
        files = FilenameIndex.getFilesByName(project, nameWithoutSuffix + ".webp", ProjectScope.getProjectScope(project));
        if (files.length > 0) {
            return files;
        }
        files = FilenameIndex.getFilesByName(project, nameWithoutSuffix + ".bmp", ProjectScope.getProjectScope(project));
        if (files.length > 0) {
            return files;
        }
        return new PsiFile[0];
    }
}
