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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YamlRecursivePsiElementVisitor;
import org.jetbrains.yaml.psi.impl.YAMLFileImpl;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.Collection;

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

    public static YAMLPlainTextImpl findYamlReference(PsiElement dartPsiElement) {
        Project project = dartPsiElement.getProject();
        Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(project, "pubspec.yaml", ProjectScope.getAllScope(project));
        if (files.size() < 1) {
            return null;
        }
        YAMLFileImpl yamlFile = (YAMLFileImpl) PsiManager.getInstance(project).findFile(files.iterator().next());
        if (yamlFile == null) {
            return null;
        }
        String dartText = dartPsiElement.getText().replace("\"", "");
        Collection<YAMLPlainTextImpl> elements = PsiTreeUtil.findChildrenOfType(yamlFile, YAMLPlainTextImpl.class);
        for (YAMLPlainTextImpl e : elements) {
            if (dartText.equals(e.getText())) {
                return e;
            }
        }

        return null;
    }
}
