package com.shenyong.flutter.psi;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression;
import com.jetbrains.lang.dart.psi.impl.DartStringLiteralExpressionImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

public class FlutterAssetReference extends PsiReferenceBase<PsiElement> {

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
    public @Nullable PsiElement resolve() {
        if (myElement instanceof DartStringLiteralExpressionImpl) {
            // 这里本来应该直接返回图片文件的PsiFile，这样ctrl+click直接打开图片文件体验最好，
            // 但Dart插件内的DocumentationProvider不支持解析二进制类型文件：
            // FileOffsetsManager.loadLineOffsets(@NotNull VirtualFile file):
            //     assert !file.getFileType().isBinary();
            // 所以折中处理，Dart文件中的资源引用指向yaml中对应声明的PsiElement
            return AssetUtil.findYamlReference(myElement);
        }
        if (myElement instanceof YAMLPlainTextImpl) {
            Project project = myElement.getProject();
            PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, fileName, GlobalSearchScope.allScope(project));
            return psiFiles.length > 0 ? psiFiles[0] : null;
        }
        return null;
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
