package com.shenyong.flutter.settings;

import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

/**
 * 参考自：https://github.com/YiiGuxing/TranslationPlugin/blob/master/src/main/kotlin/cn/yiiguxing/plugin/translate/ui/UI.kt
 * @author shenyong
 * @date 2022/12/28
 */
public class UI {
    public static MigLayout migLayout(String gapX, String gapY, String insets) {
        return new MigLayout(new LC().fill().gridGap(gapX, gapY).insets(insets));
    }

    public static MigLayout migLayout(String gapX) {
        return migLayout(gapX, "0!", "0");
    }

    public static MigLayout migLayout() {
        return migLayout("0!", "0!", "0");
    }

    public static CC fill() {
        return new CC().grow().push();
    }

    public static CC fillX() {
        return new CC().growX().pushX();
    }

    public static CC fillY() {
        return new CC().growY().pushY();
    }
}
