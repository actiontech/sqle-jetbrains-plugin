package com.actiontech.sqle.config;

import com.actiontech.sqle.from.SQLESettingUI;
import com.actiontech.sqle.util.HttpClientUtil;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.text.Strings;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.status.EditorBasedStatusBarPopup;
import com.intellij.ui.popup.list.ListPopupImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class InstStatusBarWidget extends EditorBasedStatusBarPopup implements Observer {
    public static String InstNameStatusBarID = "InstName";
    public static String instName = "";

    public InstStatusBarWidget(@NotNull Project project) {
        super(project, false);
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        super.install(statusBar);
    }

    @Override
    protected @NotNull WidgetState getWidgetState(@Nullable VirtualFile file) {
        String tips = "配置SQLE数据源";
        if (null != SQLESettingUI.settings && !Strings.isEmpty(SQLESettingUI.settings.getDataSourceName())) {
            return new WidgetState(tips, SQLESettingUI.settings.getDataSourceName(), true);
        } else {
            return new WidgetState(tips, "No inst", true);
        }
    }

    @Override
    protected @Nullable ListPopup createPopup(DataContext context) {
        HttpClientUtil client = new HttpClientUtil(SQLESettingUI.settings);
        ArrayList<String> instList;
        try {
            instList = client.GetDataSourceNameList(SQLESettingUI.settings.getProjectUID(), SQLESettingUI.settings.getDBType());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        return new ListPopupImpl(getProject(), new instList(instList));
    }

    @Override
    protected @NotNull StatusBarWidget createInstance(@NotNull Project project) {
        return new InstStatusBarWidget(project);
    }

    @Override
    public @NonNls @NotNull String ID() {
        return InstNameStatusBarID;
    }

    public void update(Observable o, Object arg) {
        if (o instanceof SQLESettings) {
            SQLESettings newValue = (SQLESettings) o;
            String dataSourceName = newValue.getDataSourceName();
            if (null == dataSourceName) {
                return;
            }
            InstStatusBarWidget.instName = dataSourceName;

            for (Project project : ProjectManager.getInstance().getOpenProjects()) {
                StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);

                StatusBarWidget widget = statusBar.getWidget(InstNameStatusBarID);
                if (widget instanceof InstStatusBarWidget) {
                    InstStatusBarWidget instStatusBarWidget = (InstStatusBarWidget) widget;
                    instStatusBarWidget.update();
                }
            }
        }
    }

    private class instList extends BaseListPopupStep<String> {
        @Override
        public @NotNull String getTextFor(String file) {
            return file;
        }

        @Override
        public @Nullable PopupStep<?> onChosen(String file, boolean finalChoice) {
            instName = file;
            SQLESettingUI.settings.setDataSourceName(instName);
            updateComponent(getWidgetState(null));
            return super.onChosen(file, finalChoice);
        }

        public instList(List<String> files) {
            super("数据源", files);
        }
    }
}
