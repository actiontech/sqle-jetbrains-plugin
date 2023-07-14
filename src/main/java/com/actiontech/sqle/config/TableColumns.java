package com.actiontech.sqle.config;


import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@ToString
public class TableColumns {
    @SerializedName("rows")
    private List<Map<String, String>> rows;

    @SerializedName("head")
    private List<TableMetaItemHeadResV1> head;
}
