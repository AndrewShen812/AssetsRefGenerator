package com.shenyong.flutter.psi.dart;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.patterns.StringPattern;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.psi.impl.DartStringLiteralExpressionImpl;
import com.shenyong.flutter.psi.FlutterAssetReference;
import com.shenyong.flutter.psi.FlutterAssetReferenceContributor;

/**
 * @author shenyong
 * @date 2021/7/20
 */
public class DartAssetReferenceContributor extends FlutterAssetReferenceContributor {

    // 在dart中，资源字符串规则：
    // 1、可能在"或'内；
    // 2、可能包含完整资源路径;
    // 3、可能是自定义的工具类来组装完整路径，参数字符串就只有文件名，且可能格式后缀都没有
    // 如：
    // Image.asset('assets/images/food01.jpeg'）
    // Utils.getImgPath('ic_launcher_news.png')
    // Utils.getImgPath('ic_launcher_news')
    public static final String ASSET_PATTERN = "^[\"']?(asset(s)?(/([-\\w]+|[1-9]\\.\\dx))*/)?[-\\w]+(.(jp(e)?g|(9.)?png|webp|bmp))?[\"']?$";
    private static final StringPattern DART_ASSET_STRING = StandardPatterns.string().matches(ASSET_PATTERN);


    @Override
    public Class<? extends PsiElement> provideAssetStringLiteralClass() {
        return DartStringLiteralExpressionImpl.class;
    }

    @Override
    public ElementPattern<? extends PsiElement> provideElementPatterns() {
        return PlatformPatterns
                .psiElement(provideAssetStringLiteralClass()).withText(DART_ASSET_STRING);
    }

    @Override
    public FlutterAssetReference createAssetReference(PsiElement element) {
        return new DartAssetReference(element);
    }
}
