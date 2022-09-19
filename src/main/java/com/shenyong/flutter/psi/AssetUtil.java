package com.shenyong.flutter.psi;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.yaml.psi.impl.YAMLFileImpl;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.Collection;

public class AssetUtil {
    public static VirtualFile[] getAssetVirtualFile(PsiElement psiElement) {
        PsiFile[] psiFiles = getAssetPsiFiles(psiElement);
        if (psiFiles.length == 0) {
            return null;
        }
        VirtualFile[] virtualFiles = new VirtualFile[psiFiles.length];
        for (int i = 0; i < psiFiles.length; i++) {
            virtualFiles[i] = psiFiles[i].getVirtualFile();
        }
        return virtualFiles;
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
