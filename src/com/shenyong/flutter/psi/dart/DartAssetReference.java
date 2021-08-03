package com.shenyong.flutter.psi.dart;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.psi.impl.DartStringLiteralExpressionImpl;
import com.shenyong.flutter.psi.AssetUtil;
import com.shenyong.flutter.psi.FlutterAssetReference;
import org.jetbrains.annotations.Nullable;

public class DartAssetReference extends FlutterAssetReference {

    public DartAssetReference(PsiElement psiElement) {
        super(psiElement);
    }

    @Override
    public @Nullable PsiElement resolve() {
        if (myElement instanceof DartStringLiteralExpressionImpl) {
            // 这里本来应该直接返回图片文件的PsiFile，这样ctrl+click直接打开图片文件体验最好，
            // 但idea会尝试先用Dart插件中的DartDocumentationProvider显示文档，
            // 而DartDocumentationProvider又不支持PsiElement为二进制文件类型（如图片）的PsiFile时生成悬浮文档：
            // FileOffsetsManager.loadLineOffsets(@NotNull VirtualFile file):
            //     assert !file.getFileType().isBinary();
            // 所以折中处理，Dart文件中的资源引用指向yaml中对应声明的PsiElement
            return AssetUtil.findReferenceForDartElement(myElement);

//            PsiFile[] psiFiles = AssetUtil.getAssetPsiFiles(myElement);
//            return psiFiles.length > 0 ? psiFiles[0] : null;
        }
        return null;
    }
}
