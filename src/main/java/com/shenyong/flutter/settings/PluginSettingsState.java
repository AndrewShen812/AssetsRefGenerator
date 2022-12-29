package com.shenyong.flutter.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.shenyong.flutter.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author shenyong
 * @date 2022/12/28
 */
@State(
        name = "com.shenyong.flutter.settings.PluginSettingsState",
        storages = @Storage("AssetsRefGeneratorSettings.xml")
)
public class PluginSettingsState implements PersistentStateComponent<PluginSettingsState> {

    public boolean isSnakeCase = Constants.IS_SNAKE_CASE;
    public String resFilePath = Constants.DEF_FILE_PATH;
    public String resClsName = Constants.DEF_CLS_NAME;

    public static PluginSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(PluginSettingsState.class);
    }

    @Override
    public @Nullable PluginSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PluginSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
