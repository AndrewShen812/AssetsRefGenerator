package com.shenyong.flutter.psi;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

public class AssetUtil {
    public static VirtualFile getAssetVirtualFile(PsiElement psiElement) {

        String text = psiElement.getText();
        String fileName;
        try {
            fileName = text.substring(text.lastIndexOf('/') + 1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Project project = psiElement.getProject();
        PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, fileName, GlobalSearchScope.allScope(project));
        return psiFiles.length > 0 ? psiFiles[0].getVirtualFile() : null;
    }
}
