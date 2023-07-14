package com.actiontech.sqle.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class SQLESQLAnalysisResult {
    @SerializedName("sql_explain")
    private SQLExplain sqlExplain;

    @SerializedName("table_meta")
    private List<TableMeta> tableMeta;
}
