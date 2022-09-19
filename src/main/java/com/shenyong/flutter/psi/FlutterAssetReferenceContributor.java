package com.shenyong.flutter.psi;

import com.intellij.patterns.ElementPattern;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author shenyong
 * @date 2021/7/20
 */
public abstract class FlutterAssetReferenceContributor extends PsiReferenceContributor {

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
