package com.actiontech.sqle.config;

import com.actiontech.sqle.from.SQLESettingUI;
import com.intellij.openapi.options.SearchableConfigurable;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.actiontech.sqle.constant.Constant.*;

@Slf4j
public class SQLEConfigurable implements SearchableConfigurable {

    private SQLESettings settings;

    private SQLESettingUI settingUI;

    public SQLEConfigurable() {
        settings = SQLESettings.getInstance();
        settingUI = new SQLESettingUI(settings);
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @NotNull
    @Override
    public String getId() {
        return SQLE_PLUGIN_ID;
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return SQLE_DISPLAY_NAME;
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return SQLE_HELP_TOPIC;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return settingUI.getRootPanel();
    }

    @Override
    public boolean isModified() {
        return settingUI.isModified();
    }

    @Override
    public void apply() {
        settingUI.apply(settings);
    }

    @Override
    public void reset() {
        settingUI.reset();
    }

    @Override
    public void disposeUIResources() {
        this.settingUI = null;
    }
}
