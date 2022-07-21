package com.actiontech.sqle.from;

import com.actiontech.sqle.config.SQLESettings;
import com.actiontech.sqle.util.HttpClientUtil;
import com.actiontech.sqle.util.NotifyUtil;
import com.intellij.ui.DocumentAdapter;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;

import static com.actiontech.sqle.constant.Constant.*;


public class SQLESettingUI {
    private JPanel rootPanel;
    private JTextField sqleAddr;
    private JTextField sqleUserName;
    private JPasswordField sqlePassword;
    private JButton testConnBtn;
    private JRadioButton httpBtn;
    private JRadioButton httpsBtn;
    private JComboBox dbTypeBox;
    private final SQLESettings settings;

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
        String dbType = settings.getDBType();
        dbTypeBox.removeAllItems();
        dbTypeBox.addItem(dbType);
    }

    public void reset() {
        loadSettings(this.settings);
    }

    public void apply(SQLESettings settings) {
        settings.setSQLEAddr(sqleAddr.getText());
        settings.setUserName(sqleUserName.getText());
        settings.setPassword(new String(sqlePassword.getPassword()));
        settings.setDBType((String) dbTypeBox.getSelectedItem());
    }

    public boolean isModified() {
        String dbType = (String) dbTypeBox.getSelectedItem();
        return !sqleAddr.getText().equals(ObjectUtils.defaultIfNull(settings.getSQLEAddr(), "")) ||
                (httpsBtn.isSelected() != settings.isEnableHttps()) ||
                !sqleUserName.getText().equals(ObjectUtils.defaultIfNull(settings.getUserName(), "")) ||
                !(new String(sqlePassword.getPassword())).equals(ObjectUtils.defaultIfNull(settings.getPassword(), "")) ||
                !ObjectUtils.defaultIfNull(dbType, "").equals(settings.getDBType());
    }

    private void loadListener() {
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
                if (StringUtils.containsAny(NOT_SUPPORT_CHARACTER, passwordContent)) {
                    NotifyUtil.showNotifyPopup(sqlePassword, "Your password have special symbol, please use file config, or explain will not be used in analysis");
                } else {
                    settings.setPassword(passwordContent);
                }
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

        dbTypeBox.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (sqleAddr.getText().equals("") ||
                        sqleUserName.getText().equals("") ||
                        new String(sqlePassword.getPassword()).equals("")) {
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
    }
}
