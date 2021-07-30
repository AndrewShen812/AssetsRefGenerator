package com.shenyong.flutter.psi.dart;

import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.psi.impl.DartStringLiteralExpressionImpl;
import com.shenyong.flutter.psi.FlutterAssetReference;
import com.shenyong.flutter.psi.FlutterAssetReferenceContributor;

/**
 * @author shenyong
 * @date 2021/7/20
 */
public class DartAssetReferenceContributor extends FlutterAssetReferenceContributor {

    @Override
    public Class<? extends PsiElement> provideAssetStringLiteralClass() {
        return DartStringLiteralExpressionImpl.class;
    }

    @Override
    public FlutterAssetReference createAssetReference(PsiElement element) {
        return new FlutterAssetReference(element);
    }
}
