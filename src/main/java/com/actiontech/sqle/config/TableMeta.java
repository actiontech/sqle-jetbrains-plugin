package com.actiontech.sqle.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TableMeta {
    @SerializedName("name")
    private String Name;

    @SerializedName("schema")
    private String Schema;

    @SerializedName("columns")
    private TableColumns Columns;

    @SerializedName("indexes")
    private TableIndexes Indexes;
}
