package com.shenyong.flutter.psi;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.patterns.StringPattern;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author shenyong
 * @date 2021/7/20
 */
public abstract class FlutterAssetReferenceContributor extends PsiReferenceContributor {

    // eg: assets/images/doge.jpeg
    public static final String ASSET_PATTERN = "^(\"|')?asset(s)?(/([-\\w]+|[1-9]\\.\\dx))*/[-\\w]+\\.(jp(e)?g|png|webp|bmp)(\"|')?$";
    private static final StringPattern ASSET_STRING = StandardPatterns.string().matches(ASSET_PATTERN);

    public abstract Class<? extends PsiElement> provideAssetStringLiteralClass();

    public abstract ElementPattern<? extends PsiElement> provideElementPatterns();

    public abstract FlutterAssetReference createAssetReference(PsiElement element);

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(provideElementPatterns(), new PsiReferenceProvider() {
            @Override
            public @NotNull PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                FlutterAssetReference reference = createAssetReference(element);
                if (reference == null) {
                    return PsiReference.EMPTY_ARRAY;
                }
                return new PsiReference[] { reference };
            }
        });
    }
}
