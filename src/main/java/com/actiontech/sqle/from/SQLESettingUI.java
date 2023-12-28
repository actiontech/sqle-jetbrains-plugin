package com.actiontech.sqle.from;

import com.actiontech.sqle.config.InstStatusBarWidget;
import com.actiontech.sqle.config.SQLESettings;
import com.actiontech.sqle.config.SchemaStatusBarWidget;
import com.actiontech.sqle.util.HttpClientUtil;
import com.actiontech.sqle.util.NotifyUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.DocumentAdapter;
import org.apache.commons.lang.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;


public class SQLESettingUI extends Observable {
    private JPanel rootPanel;
    private JTextField sqleAddr;
    private JTextField sqleUserName;
    private JPasswordField sqlePassword;
    private JButton testConnBtn;
    private JRadioButton httpBtn;
    private JRadioButton httpsBtn;
    private JComboBox dbTypeBox;
    private JComboBox projectBox;
    private JComboBox dbDataSourceBox;
    private JComboBox SchemaBox;
    public static SQLESettings settings;

    public SQLESettingUI(SQLESettings settings) {
        this.settings = settings;
        loadSettings(settings);
        loadListener();
    }

    public JComponent getRootPanel() {
        return rootPanel;
    }

    private void loadSettings(SQLESettings settings) {
        String addr = settings.getSQLEAddr();
        sqleAddr.setText(addr);
        String username = settings.getUserName();
        sqleUserName.setText(username);
        String pwd = settings.getPassword();
        sqlePassword.setText(pwd);

        String projectName = settings.getProjectName();
        projectBox.removeAllItems();
        projectBox.addItem(projectName);

        String dbType = settings.getDBType();
        dbTypeBox.removeAllItems();
        dbTypeBox.addItem(dbType);

        String dataSourceName = settings.getDataSourceName();
        dbDataSourceBox.removeAllItems();
        dbDataSourceBox.addItem(dataSourceName);

        String schemaName = settings.getSchemaName();
        SchemaBox.removeAllItems();
        SchemaBox.addItem(schemaName);
    }

    public void reset() {
        loadSettings(this.settings);
    }

    public void apply(SQLESettings settings) {
        settings.setSQLEAddr(sqleAddr.getText());
        settings.setUserName(sqleUserName.getText());
        settings.setPassword(new String(sqlePassword.getPassword()));
        settings.setDBType((String) dbTypeBox.getSelectedItem());
        settings.setProjectName((String) projectBox.getSelectedItem());
        settings.setDataSourceName((String) dbDataSourceBox.getSelectedItem());
        settings.setSchemaName((String) SchemaBox.getSelectedItem());
    }

    public boolean isModified() {
        String dbType = (String) dbTypeBox.getSelectedItem();
        String projectName = (String) projectBox.getSelectedItem();
        String dataSourceName = (String) dbDataSourceBox.getSelectedItem();
        String schemaName = (String) SchemaBox.getSelectedItem();

        return !sqleAddr.getText().equals(ObjectUtils.defaultIfNull(settings.getSQLEAddr(), "")) || (httpsBtn.isSelected() != settings.isEnableHttps()) || !sqleUserName.getText().equals(ObjectUtils.defaultIfNull(settings.getUserName(), "")) || !(new String(sqlePassword.getPassword())).equals(ObjectUtils.defaultIfNull(settings.getPassword(), "")) || !ObjectUtils.defaultIfNull(dbType, "").equals(settings.getDBType()) || !ObjectUtils.defaultIfNull(projectName, "").equals(settings.getProjectName()) || !ObjectUtils.defaultIfNull(dataSourceName, "").equals(settings.getDataSourceName()) || !ObjectUtils.defaultIfNull(schemaName, "").equals(settings.getSchemaName());
    }

    private void loadListener() {
        Project project = ProjectManager.getInstance().getDefaultProject();
        InstStatusBarWidget instSchemaStatusBarWidget = new InstStatusBarWidget(project);
        settings.addObserver(instSchemaStatusBarWidget);

        SchemaStatusBarWidget schemaStatusBarWidget = new SchemaStatusBarWidget(project);
        settings.addObserver(schemaStatusBarWidget);

        // text field
        sqleAddr.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                settings.setSQLEAddr(sqleAddr.getText());
            }
        });
        sqleUserName.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                settings.setUserName(sqleUserName.getText());
            }
        });
        sqlePassword.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                String passwordContent = new String(sqlePassword.getPassword());
                settings.setPassword(passwordContent);
            }
        });

        // radio btn
        httpsBtn.addActionListener(event -> {
            settings.setEnableHttps(httpsBtn.isSelected());
        });
        httpBtn.addActionListener(event -> {
            settings.setEnableHttps(httpsBtn.isSelected());
        });

        // btn
        testConnBtn.addActionListener(event -> {
            try {
                HttpClientUtil client = new HttpClientUtil(settings);
                String token = client.Login();
                settings.setToken(token);
                NotifyUtil.showTipsDialog("Test Connection", "Test Connection Success");
            } catch (Exception e) {
                String errMessage = NotifyUtil.getExceptionMessage(e);
                NotifyUtil.showErrorMessageDialog("Test Connection Failed", errMessage);
            }
        });

        projectBox.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (sqleAddr.getText().equals("") || sqleUserName.getText().equals("") || new String(sqlePassword.getPassword()).equals("")) {
                    return;
                }

                Object selected = projectBox.getSelectedItem();
                String selectedItem;
                if (selected != null) {
                    selectedItem = selected.toString();
                } else {
                    selectedItem = "";
                }
                try {
                    HttpClientUtil client = new HttpClientUtil(settings);
                    HashMap<String, String> projects = client.GetProjectList();
                    projectBox.removeAllItems();

                    projects.forEach((k, v) -> {
                        projectBox.addItem(k);
                        if (k.equals(selectedItem)) {
                            projectBox.setSelectedItem(k);
                            settings.setProjectName(k);
                            settings.setProjectUID(v);
                        }
                    });
                    projectBox.updateUI();
                    projectBox.setPopupVisible(true);
                } catch (Exception exception) {
                    String errMessage = NotifyUtil.getExceptionMessage(exception);
                    NotifyUtil.showErrorMessageDialog("Get Project List Failed", errMessage);
                    projectBox.setFocusable(false);
                    projectBox.setFocusable(true);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });

        dbTypeBox.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                String projectSelected = (String) projectBox.getSelectedItem();
                if (projectSelected == null || projectSelected.equals("")) {
                    return;
                }

                Object selected = dbTypeBox.getSelectedItem();
                String selectedItem = "";
                if (selected != null) {
                    selectedItem = selected.toString();
                }
                try {
                    HttpClientUtil client = new HttpClientUtil(settings);
                    ArrayList<String> dbTypes = client.GetDBTypes();
                    dbTypeBox.removeAllItems();
                    for (int i = 0; i < dbTypes.size(); i++) {
                        dbTypeBox.addItem(dbTypes.get(i));
                        if (dbTypes.get(i).equals(selectedItem)) {
                            dbTypeBox.setSelectedItem(dbTypes.get(i));
                            settings.setDBType(dbTypes.get(i));
                        }
                    }
                    dbTypeBox.updateUI();
                    dbTypeBox.setPopupVisible(true);
                } catch (Exception exception) {
                    String errMessage = NotifyUtil.getExceptionMessage(exception);
                    NotifyUtil.showErrorMessageDialog("Get DBType List Failed", errMessage);
                    dbTypeBox.setFocusable(false);
                    dbTypeBox.setFocusable(true);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });

        dbDataSourceBox.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                String projectSelected = settings.getProjectUID();
                String dbTypeSelected = (String) dbTypeBox.getSelectedItem();
                if (projectSelected == null || projectSelected.equals("") || dbTypeSelected == null || dbTypeSelected.equals("")) {
                    return;
                }

                Object selected = dbDataSourceBox.getSelectedItem();
                String selectedItem = "";
                if (selected != null) {
                    selectedItem = selected.toString();
                }
                try {
                    HttpClientUtil client = new HttpClientUtil(settings);
                    ArrayList<String> dataSources = client.GetDataSourceNameList(projectSelected, dbTypeSelected);
                    dbDataSourceBox.removeAllItems();
                    for (int i = 0; i < dataSources.size(); i++) {
                        dbDataSourceBox.addItem(dataSources.get(i));
                        if (dataSources.get(i).equals(selectedItem)) {
                            dbDataSourceBox.setSelectedItem(dataSources.get(i));
                            settings.setDataSourceName(dataSources.get(i));
                        }
                    }
                    dbDataSourceBox.updateUI();
                    dbDataSourceBox.setPopupVisible(true);
                } catch (Exception exception) {
                    String errMessage = NotifyUtil.getExceptionMessage(exception);
                    NotifyUtil.showErrorMessageDialog("Get DataSource List Failed", errMessage);
                    dbDataSourceBox.setFocusable(false);
                    dbDataSourceBox.setFocusable(true);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });

        SchemaBox.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                String projectSelected = (String) projectBox.getSelectedItem();
                String dbTypeSelected = (String) dbTypeBox.getSelectedItem();
                String dbDataSourceSelected = (String) dbDataSourceBox.getSelectedItem();
                if (projectSelected == null || projectSelected.equals("") || dbTypeSelected == null || dbTypeSelected.equals("") || dbDataSourceSelected == null || dbDataSourceSelected.equals("")) {
                    return;
                }

                Object selected = SchemaBox.getSelectedItem();
                String selectedItem = "";
                if (selected != null) {
                    selectedItem = selected.toString();
                }
                try {
                    HttpClientUtil client = new HttpClientUtil(settings);
                    ArrayList<String> schemas = client.GetSchemaList(projectSelected, dbDataSourceSelected);
                    SchemaBox.removeAllItems();
                    for (int i = 0; i < schemas.size(); i++) {
                        SchemaBox.addItem(schemas.get(i));
                        if (schemas.get(i).equals(selectedItem)) {
                            SchemaBox.setSelectedItem(schemas.get(i));
                            settings.setSchemaName(schemas.get(i));
                        }
                    }
                    SchemaBox.updateUI();
                    SchemaBox.setPopupVisible(true);
                } catch (Exception exception) {
                    String errMessage = NotifyUtil.getExceptionMessage(exception);
                    NotifyUtil.showErrorMessageDialog("Get Schema List Failed", errMessage);
                    SchemaBox.setFocusable(false);
                    SchemaBox.setFocusable(true);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });
    }
}
