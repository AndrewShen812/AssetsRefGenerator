package com.shenyong.flutter.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author shenyong
 * @date 2022/12/28
 */
public class PluginSettingsConfigurable implements Configurable {

    private PluginSettingsComponent settingsComponent;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Flutter Assets Ref Generator demo";
    }

    @Override
    public @Nullable JComponent createComponent() {
        settingsComponent = new PluginSettingsComponent();
        return settingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        PluginSettingsState settings = PluginSettingsState.getInstance();
        boolean modified = !settings.resFilePath.equals(settingsComponent.getFilePath())
                || !settings.resClsName.equals(settingsComponent.getClassName())
                || settings.isSnakeCase != settingsComponent.isSnakeCase();
        return modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        PluginSettingsState settings = PluginSettingsState.getInstance();
        String inputPath = settingsComponent.getFilePath();
        /**
         * Dart 文件命名检查
         * 参考: https://dart.dev/guides/language/effective-dart/style#do-name-libraries-and-source-files-using-lowercase_with_underscores
         */
        if (!inputPath.matches("^\\S+\\.dart$")) {

            throw new ConfigurationException("File path MUST refer to a dart file.",
                    "Flutter Assets Ref Generator Settings Error");
        }
        if (!inputPath.matches("^\\S*[a-z_]+\\.dart$")) {
            throw new ConfigurationException("File path MUST end with a correct Dart file name(lowercase_with_underscores).",
                    "Flutter Assets Ref Generator Settings Error");
        }
        settings.resFilePath = inputPath;

        String inputClsName = settingsComponent.getClassName();
        /**
         * Dart 类名检查
         * 参考: https://dart.dev/guides/language/effective-dart/style#do-name-types-using-uppercamelcase
         */
        if (!inputClsName.matches("^[A-Z]\\w+$")) {
            throw new ConfigurationException("Class name MUST be a correct Dart class name(UpperCamelCase).",
                    "Flutter Assets Ref Generator Settings Error");
        }
        settings.resClsName = inputClsName;
        settings.isSnakeCase = settingsComponent.isSnakeCase();
    }

    @Override
    public void reset() {
        PluginSettingsState settings = PluginSettingsState.getInstance();
        settingsComponent.setFilePath(settings.resFilePath);
        settingsComponent.setClassName(settings.resClsName);
        settingsComponent.setSnakeCase(settings.isSnakeCase);
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}
