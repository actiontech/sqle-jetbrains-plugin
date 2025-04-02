package com.actiontech.sqle.constant;

import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.JBUI;

public class Constant {
    public static final String SQLE_PLUGIN_ID = "com.actiontech.sqle.sqle-jetbrains-plugin";
    public static final String SQLE_DISPLAY_NAME = "SQLE";
    public static final String SQLE_HELP_TOPIC = "https://actiontech.github.io/sqle-docs-cn/";
    public static final String NOT_SUPPORT_CHARACTER = "!@#$%^&*()_+=-~\\][{}\"\':/;<>?.,`";
    public static final JBDimension DIALOG_SIZE = JBUI.size(700, 480);
    public static final String COMMON_ERROR_MESSAGE = "Please check selected content doesn't have syntax error and SQLE configuration is correct";
    public static final String LOGIN_TYPE_PASSWORD = "password";
    public static final String LOGIN_TYPE_TOKEN = "token";
    public static final String BEARER_TOKEN_PREFIX = "Bearer ";
}
