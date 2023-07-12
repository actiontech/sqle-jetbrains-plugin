package com.actiontech.sqle.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TableMetaItemHeadResV1 {
    @SerializedName("field_name")
    private String fieldName;
}
