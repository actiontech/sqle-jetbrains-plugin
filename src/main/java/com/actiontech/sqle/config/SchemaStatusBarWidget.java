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

public class SchemaStatusBarWidget extends EditorBasedStatusBarPopup implements Observer {
    public static String schemaNameStatusBarID = "SchemaName";
    public static String schemaName = "";

    public SchemaStatusBarWidget(@NotNull Project project) {
        super(project, false);
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        super.install(statusBar);
    }

    @Override
    protected @NotNull EditorBasedStatusBarPopup.WidgetState getWidgetState(@Nullable VirtualFile file) {
        String tips = "数据库";
        if (null != SQLESettingUI.settings && !Strings.isEmpty(SQLESettingUI.settings.getSchemaName())) {
            return new EditorBasedStatusBarPopup.WidgetState(tips, SQLESettingUI.settings.getSchemaName(), true);
        } else {
            return new EditorBasedStatusBarPopup.WidgetState(tips, "No schema", true);
        }
    }


    @Override
    protected @Nullable ListPopup createPopup(DataContext context) {
        HttpClientUtil client = new HttpClientUtil(SQLESettingUI.settings);
        ArrayList<String> schemaList;
        try {
            schemaList = client.GetSchemaList(SQLESettingUI.settings.getProjectName(), SQLESettingUI.settings.getDataSourceName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        return new ListPopupImpl(getProject(), new SchemaStatusBarWidget.schemaList(schemaList));
    }

    @Override
    protected @NotNull StatusBarWidget createInstance(@NotNull Project project) {
        return new SchemaStatusBarWidget(project);
    }

    @Override
    public @NonNls @NotNull String ID() {
        return schemaNameStatusBarID;
    }

    public void update(Observable o, Object arg) {
        if (o instanceof SQLESettings) {
            SQLESettings newValue = (SQLESettings) o;
            String schemaName = newValue.getSchemaName();
            if (schemaName == null) {
                return;
            }
            SchemaStatusBarWidget.schemaName = schemaName;

            for (Project project : ProjectManager.getInstance().getOpenProjects()) {
                StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);

                StatusBarWidget schemaWidget = statusBar.getWidget(schemaNameStatusBarID);
                if (schemaWidget instanceof SchemaStatusBarWidget) {
                    SchemaStatusBarWidget statusBarWidget = (SchemaStatusBarWidget) schemaWidget;
                    statusBarWidget.update();
                }
            }
        }
    }

    private class schemaList extends BaseListPopupStep<String> {
        @Override
        public @NotNull String getTextFor(String file) {
            return file;
        }

        @Override
        public @Nullable PopupStep<?> onChosen(String file, boolean finalChoice) {
            schemaName = file;
            SQLESettingUI.settings.setSchemaName(schemaName);
            updateComponent(getWidgetState(null));
            return super.onChosen(file, finalChoice);
        }

        public schemaList(List<String> files) {
            super("数据库", files);
        }
    }
}
