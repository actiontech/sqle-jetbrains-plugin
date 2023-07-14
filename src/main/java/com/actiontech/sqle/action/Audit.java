package com.actiontech.sqle.action;

import com.actiontech.sqle.config.SQLEAuditResult;
import com.actiontech.sqle.config.SQLESQLAnalysisResult;
import com.actiontech.sqle.config.SQLESettings;
import com.actiontech.sqle.from.SQLEAuditResultUI;
import com.actiontech.sqle.util.HttpClientUtil;
import com.actiontech.sqle.util.NotifyUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import java.util.List;

public class Audit {

    public static final String TOOL_WINDOW_NAME = "审核结果";

    public static void Audit(AnActionEvent e, String sql, HttpClientUtil.AuditType type) {
        Project project = e.getData(LangDataKeys.PROJECT);
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        toolWindowManager.unregisterToolWindow(TOOL_WINDOW_NAME);

        SQLESettings settings = SQLESettings.getInstance();

        ToolWindow toolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_NAME, true, ToolWindowAnchor.BOTTOM);

        HttpClientUtil client = new HttpClientUtil(settings);
        try {
            SQLEAuditResult result = client.AuditSQL(sql, type);

            String projectName = settings.getProjectName();
            String dataSourceName = settings.getDataSourceName();
            String schemaName = settings.getSchemaName();

            List<SQLESQLAnalysisResult> analysisResult = client.GetSQLAnalysis(sql, projectName, dataSourceName, schemaName);
            SQLEAuditResultUI ui = new SQLEAuditResultUI(result, analysisResult);
            createToolWindow(toolWindow, ui);
        } catch (Exception exception) {
            String errMessage = NotifyUtil.getExceptionMessage(exception);
            NotifyUtil.showErrorMessageDialog("Audit SQL Failed", errMessage);
        }
    }

    public static void createToolWindow(ToolWindow toolWindow, SQLEAuditResultUI ui) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(ui.getRootPanel(), "", false);
        toolWindow.getContentManager().addContent(content);

        toolWindow.setType(ToolWindowType.DOCKED, null);
        toolWindow.setAvailable(true);
        toolWindow.show(null);
    }
}
