package com.actiontech.sqle.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SQLEDataSourceNameListResult {
    @SerializedName("name")
    private String name;
}
