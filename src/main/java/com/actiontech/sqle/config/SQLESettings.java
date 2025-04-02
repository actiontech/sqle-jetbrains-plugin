package com.actiontech.sqle.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Observable;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@State(name = "SQLESettings", storages = {@Storage(file = "$APP_CONFIG$/SQLESettings.xml")})
public class SQLESettings extends Observable implements PersistentStateComponent<SQLESettings> {
    private String SQLEAddr;
    private boolean EnableHttps;
    private String UserName;
    private String Password;
    private String DBType;
    private String ProjectName;
    private String DataSourceName;
    private String SchemaName;
    private String Token;
    private String AccessToken;
    private String LoginType;

    public static SQLESettings getInstance() {
        return ServiceManager.getService(SQLESettings.class);
    }

    @Nullable
    @Override
    public SQLESettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull SQLESettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void setDataSourceName(String dataSourceName) {
        this.DataSourceName = dataSourceName;
        setChanged();
        notifyObservers();
    }

    public void setSchemaName(String schemaName) {
        this.SchemaName = schemaName;
        setChanged();
        notifyObservers();
    }
}
