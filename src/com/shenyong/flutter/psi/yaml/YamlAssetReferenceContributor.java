package com.shenyong.flutter.psi.yaml;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.patterns.StringPattern;
import com.intellij.psi.PsiElement;
import com.shenyong.flutter.psi.FlutterAssetReference;
import com.shenyong.flutter.psi.FlutterAssetReferenceContributor;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

/**
 * @author shenyong
 * @date 2021/7/20
 */
public class YamlAssetReferenceContributor extends FlutterAssetReferenceContributor {

    // 在pubspec.yaml中，资源声明可能存在精确到文件或只到目录的方式：
    // assets/images/doge.jpeg
    // assets/images/
    public static final String ASSET_PATTERN = "^asset(s)?(/([-\\w]+|[1-9]\\.\\dx))*/([-\\w]+\\.(jp(e)?g|png|webp|bmp)?)?$";
    private static final StringPattern YAML_ASSET_STRING = StandardPatterns.string().matches(ASSET_PATTERN);

    @Override
    public Class<? extends PsiElement> provideAssetStringLiteralClass() {
        return YAMLPlainTextImpl.class;
    }

    @Override
    public ElementPattern<? extends PsiElement> provideElementPatterns() {
        return PlatformPatterns
                .psiElement(provideAssetStringLiteralClass()).withText(YAML_ASSET_STRING);
    }

    @Override
    public FlutterAssetReference createAssetReference(PsiElement element) {
        return new YamlAssetReference(element);
    }
}
