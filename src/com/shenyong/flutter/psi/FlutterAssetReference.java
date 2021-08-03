package com.shenyong.flutter.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiBinaryFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public abstract class FlutterAssetReference extends PsiReferenceBase<PsiElement> {

    // eg: doge.jpeg
    private String fileName;

    public FlutterAssetReference(PsiElement psiElement) {
        super(psiElement);
        // eg: "assets/images/doge.jpeg"
        String text = psiElement.getText();
        try {
            this.fileName = text.substring(text.lastIndexOf('/') + 1).replaceAll("\"", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        return element instanceof PsiBinaryFile && ((PsiBinaryFile) element).getVirtualFile().getName().equals(fileName);
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        fileName = newElementName;
        // 不管是dart还是yaml中，psiElement指向的都是asset文件，这个接收到的 newElementName 就是重命名以后的文件名，
        // 如：icon_new.png。dart和yaml中的psiElement对应的文本包含 assets/... 路径前缀，所以只需要更新不包含路径前缀的文本部分。
        String oldText = myElement.getText();
        int startIndex = oldText.lastIndexOf('/') + 1;
        int endIndex = oldText.length();
        if (oldText.endsWith("\"")) {
            endIndex--;
        }
        TextRange rangeInElement = new TextRange(startIndex, endIndex);
        return ElementManipulators.getManipulator(myElement).handleContentChange(myElement, rangeInElement, newElementName);
    }
}
