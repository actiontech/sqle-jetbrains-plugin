package com.actiontech.sqle.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;

@Data
@ToString
public class SQLEAuditResult {
    @SerializedName("audit_level")
    private String AuditLevel;
    @SerializedName("score")
    private int Score;
    @SerializedName("pass_rate")
    private float PassRate;
    @SerializedName("sql_results")
    private ArrayList<SQLEAuditResultItem> SQLResults;
}
