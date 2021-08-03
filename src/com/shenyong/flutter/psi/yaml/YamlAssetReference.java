package com.shenyong.flutter.psi.yaml;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.shenyong.flutter.psi.AssetUtil;
import com.shenyong.flutter.psi.FlutterAssetReference;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

public class YamlAssetReference extends FlutterAssetReference {

    // eg: doge.jpeg
    private String fileName = "";

    public YamlAssetReference(PsiElement psiElement) {
        super(psiElement);
        // eg: "assets/images/doge.jpeg"
        String text = psiElement.getText();
        try {
            int index = text.lastIndexOf('/');
            if (index != -1) {
                this.fileName = text.substring(text.lastIndexOf('/') + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public @Nullable PsiElement resolve() {
        if (!fileName.isEmpty() && myElement instanceof YAMLPlainTextImpl) {
            PsiFile[] psiFiles = AssetUtil.getAssetPsiFiles(myElement);
            return psiFiles.length > 0 ? psiFiles[0] : null;
        }
        return null;
    }
}
