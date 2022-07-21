package com.actiontech.sqle.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SQLEAuditResultItem {
    @SerializedName("number")
    private int Number;
    @SerializedName("exec_sql")
    private String ExecSQL;
    @SerializedName("audit_result")
    private String AuditResult;
    @SerializedName("audit_level")
    private String AuditLevel;
}
