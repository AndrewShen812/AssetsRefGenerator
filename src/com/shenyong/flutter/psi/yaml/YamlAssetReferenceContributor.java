package com.shenyong.flutter.psi.yaml;

import com.intellij.psi.PsiElement;
import com.shenyong.flutter.psi.FlutterAssetReference;
import com.shenyong.flutter.psi.FlutterAssetReferenceContributor;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

/**
 * @author shenyong
 * @date 2021/7/20
 */
public class YamlAssetReferenceContributor extends FlutterAssetReferenceContributor {

    @Override
    public Class<? extends PsiElement> provideAssetStringLiteralClass() {
        return YAMLPlainTextImpl.class;
    }

    @Override
    public FlutterAssetReference createAssetReference(PsiElement element) {
        return new FlutterAssetReference(element);
    }
}
