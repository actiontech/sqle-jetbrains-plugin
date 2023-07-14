package com.actiontech.sqle.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SQLExplain {
    @SerializedName("sql")
    private String SQL;

    @SerializedName("classic_result")
    private ExplainClassicResult classicResult;
}
