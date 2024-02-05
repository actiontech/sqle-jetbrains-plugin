package com.actiontech.sqle.util;

import com.actiontech.sqle.config.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.http.HttpStatus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpClientUtil {
    private static final Logger LOG = Logger.getInstance(HttpClientUtil.class);

    private String uriHead;
    private String token;
    private SQLESettings settings;

    private static final String loginPath = "/v1/dms/sessions";
    private static final String auditPath = "/sqle/v2/audit_files";
    private static final String driversPath = "/sqle/v1/configurations/drivers";

    private static final String ruleKnowledgePath = "/sqle/v1/rule_knowledge/db_types/%s/rules/%s/";

    private static final String customRuleKnowledgePath = "/sqle/v1/rule_knowledge/db_types/%s/custom_rules/%s/";

    private static final String projectPath = "/v1/dms/projects";

    private static final String dataSourcePath = "/v1/dms/projects/%s/db_services";

    private static final String schemaPath = "/sqle/v1/projects/%s/instances/%s/schemas";

    private static final String sqlAnalysisPath = "/sqle/v1/sql_analysis";

    public HttpClientUtil(SQLESettings settings) {
        String protocol = "http://";
        if (settings.isEnableHttps()) {
            protocol = "https://";
        }
        this.settings = settings;
        this.token = settings.getToken();
        this.uriHead = protocol + settings.getSQLEAddr();
    }

    public String Login() throws Exception {
        Map<String, String> req = new HashMap<>();
        req.put("username", settings.getUserName());
        req.put("password", settings.getPassword());
        Gson gson = new Gson();
        String reqJson = gson.toJson(req);

        String formatStr = String.format("{\"session\":%s}", reqJson);

        JsonObject resp = sendPostJson(uriHead + loginPath, formatStr);
        if (resp.get("code").getAsInt() != 0) {
            throw new Exception("login failed,Please check user name and password,then click Test Connection button. " + resp.get("message").getAsString());
        }
        String tokenResp = resp.get("data").getAsJsonObject().get("token").getAsString();
        this.token = "Bearer " + tokenResp;
        settings.setToken(token);
        return token;
    }

    public ArrayList<String> GetDBTypes() throws Exception {
        if (token == null || token.equals("")) {
            Login();
        }

        JsonObject resp = sendGet(uriHead + driversPath);
        if (resp.get("code").getAsInt() != 0) {
            throw new Exception("get db type failed: " + resp.get("message").getAsString());
        }
        JsonArray array = resp.get("data").getAsJsonObject().get("driver_name_list").getAsJsonArray();
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            list.add(array.get(i).getAsString());
        }
        return list;
    }

    public HashMap<String, String> GetProjectList() throws Exception {
        if (token == null || token.equals("")) {
            Login();
        }

        String reqPath = String.format("%s?page_index=%s&page_size=%s", projectPath, "1", "999999");
        JsonObject resp = sendGet(uriHead + reqPath);
        if (resp.get("code").getAsInt() != 0) {
            throw new Exception("get project name failed: " + resp.get("message").getAsString());
        }

        Gson gson = new Gson();
        Type datasetListType = new TypeToken<Collection<SQLEProjectNameListResult>>() {
        }.getType();

        JsonElement jsonObject = resp.get("data");
        List<SQLEProjectNameListResult> ProjectNameList = gson.fromJson(jsonObject, datasetListType);
        HashMap<String, String> list = new HashMap<>();
        for (SQLEProjectNameListResult sqleProjectNameListResult : ProjectNameList) {
            list.put(sqleProjectNameListResult.getName(), sqleProjectNameListResult.getUID());
        }

        return list;
    }

    public ArrayList<String> GetDataSourceNameList(String projectID, String dbType) throws Exception {
        if (token == null || token.equals("")) {
            Login();
        }

        String dataSourcePath = String.format(HttpClientUtil.dataSourcePath, projectID);
        String encodedDbType = URLEncoder.encode(dbType, "UTF-8");
        String reqPath = String.format("%s?filter_by_db_type=%s&page_index=%s&page_size=%s", dataSourcePath, encodedDbType, "1", "999999");
        JsonObject resp = sendGet(uriHead + reqPath);

        if (resp.get("code").getAsInt() != 0) {
            throw new Exception("get data source list failed: " + resp.get("message").getAsString());
        }

        Gson gson = new Gson();
        Type datasetlisttype = new TypeToken<Collection<SQLEDataSourceNameListResult>>() {
        }.getType();

        JsonElement jsonObject = resp.get("data");
        List<SQLEDataSourceNameListResult> dataSourceNameListResultList = gson.fromJson(jsonObject, datasetlisttype);
        ArrayList<String> list = new ArrayList<>();
        for (SQLEDataSourceNameListResult dataSourceNameListResult : dataSourceNameListResultList) {
            list.add(dataSourceNameListResult.getName());
        }

        return list;
    }

    public ArrayList<String> GetSchemaList(String projectName, String dataSourceName) throws Exception {
        if (token == null || token.equals("")) {
            Login();
        }

        String reqPath = String.format(HttpClientUtil.schemaPath, projectName, dataSourceName);
        JsonObject resp = sendGet(uriHead + reqPath);

        if (resp.get("code").getAsInt() != 0) {
            throw new Exception("get schema name list: " + resp.get("message").getAsString());
        }

        JsonArray array = resp.get("data").getAsJsonObject().get("schema_name_list").getAsJsonArray();
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            list.add(array.get(i).getAsString());
        }

        return list;
    }

    public List<SQLESQLAnalysisResult> GetSQLAnalysis(String sql, String projectName, String dataSourceName, String schemaName) throws Exception {
        if (token == null || token.equals("")) {
            Login();
        }

        String encodedSql = URLEncoder.encode(sql, StandardCharsets.UTF_8);
        String reqPath = String.format("%s%s?project_name=%s&instance_name=%s&schema_name=%s&sql=%s", uriHead, sqlAnalysisPath, projectName, dataSourceName, schemaName, encodedSql);
        JsonObject resp = sendGet(reqPath);

        int code = resp.get("code").getAsInt();
        if (code == 8002) {
            LOG.warn("[sqlAnalysis]community version does not support sql analysis");
            return new LinkedList<>();
        }
        if (code != 0) {
            LOG.warn("[sqlAnalysis]get sql analysis failed: " + resp.get("message").getAsString());
            return new LinkedList<>();
        }

        JsonElement jsonObject = resp.get("data");

        Type datasetListType = new TypeToken<Collection<SQLESQLAnalysisResult>>() {
        }.getType();

        Gson gson = new Gson();

        return gson.fromJson(jsonObject, datasetListType);
    }

    public enum AuditType {
        SQL, MyBatis;
    }

    public SQLEAuditResult AuditSQL(String[] contents, AuditType type, String projectName, String instanceName, String schemaName) throws Exception {
        if (token == null || token.equals("")) {
            Login();
        }

        Map<String, Object> req = new HashMap<>();
        req.put("instance_type", settings.getDBType());
        req.put("file_contents", contents);
        req.put("project_name", projectName);
        req.put("instance_name", instanceName);
        req.put("schema_name", schemaName);

        switch (type) {
            case SQL:
                req.put("sql_type", "sql");
                break;
            case MyBatis:
                req.put("sql_type", "mybatis");
                break;
        }

        Gson gson = new Gson();
        String reqJson = gson.toJson(req);
        JsonObject resp = sendPostJson(uriHead + auditPath, reqJson);
        if (resp.get("code").getAsInt() != 0) {
            throw new Exception("audit failed: " + resp.get("message").getAsString());
        }

        JsonObject data = resp.get("data").getAsJsonObject();
        return gson.fromJson(data, new SQLEAuditResult().getClass());
    }

    public String GetRuleKnowledge(String dbType, String ruleName) throws Exception {
        if (token == null || token.isEmpty()) {
            Login();
        }

        // sqle服务端默认只能解析%20表示的空格
        // 注意:java 的 URLEncoder.encode 方法默认将空格转换为+号
        String encodeDbType = dbType.replace(" ", "%20");
        String knowledge = GetOriginRuleKnowledge(encodeDbType, ruleName);
        if (knowledge == null) {
            knowledge = GetCustomRuleKnowledge(encodeDbType, ruleName);
        }

        if (null == knowledge || knowledge.isEmpty()) {
            knowledge = "知识库获取失败或该规则未配置知识库";
        }

        return knowledge;
    }

    public String GetOriginRuleKnowledge(String dbType, String ruleName) throws Exception {
        if (token == null || token.isEmpty()) {
            Login();
        }

        String reqPath = String.format(ruleKnowledgePath, dbType, ruleName);
        JsonObject resp = sendGet(uriHead + reqPath);
        if (resp.get("code").getAsInt() == 8003) {
            return "SQLE社区版不支持查看规则知识库";
        }

        if (resp.get("code").getAsInt() != 0) {
            throw new Exception("get rule knowledge failed: " + resp.get("message").getAsString());
        }

        JsonObject data = resp.get("data").getAsJsonObject();
        return data.get("knowledge_content").getAsString();
    }

    public String GetCustomRuleKnowledge(String dbType, String ruleName) throws Exception {
        if (token == null || token.isEmpty()) {
            Login();
        }

        String reqPath = String.format(customRuleKnowledgePath, dbType, ruleName);
        JsonObject resp = sendGet(uriHead + reqPath);
        if (resp.get("code").getAsInt() == 8003) {
            return "SQLE社区版不支持查看规则知识库";
        }
        if (resp.get("code").getAsInt() != 0) {
            throw new Exception("get rule knowledge failed: " + resp.get("message").getAsString());
        }

        JsonObject data = resp.get("data").getAsJsonObject();
        return data.get("knowledge_content").getAsString();
    }

    private JsonObject sendGet(String path) throws Exception {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", token);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
            Login();
            return sendGet(path);
        }
        String respStr = response.toString();
        if (responseCode != HttpStatus.SC_OK) {
            throw new Exception("response code != 200, message: " + respStr);
        }
        return new JsonParser().parse(respStr).getAsJsonObject();
    }

    private JsonObject sendPostJson(String path, String request) throws Exception {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", token);
        conn.setRequestProperty("Content-Type", "application/json");
        String result = "";

        conn.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.write(request.getBytes(StandardCharsets.UTF_8));
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
            Login();
            return sendPostJson(path, request);
        }
        String respStr = response.toString();
        if (responseCode != HttpStatus.SC_OK) {
            throw new Exception("response code != 200, message: " + respStr);
        }
        return new JsonParser().parse(respStr).getAsJsonObject();
    }


}
