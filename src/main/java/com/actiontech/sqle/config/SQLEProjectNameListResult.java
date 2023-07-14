package com.actiontech.sqle.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SQLEProjectNameListResult {
    @SerializedName("name")
    private String Name;
}
