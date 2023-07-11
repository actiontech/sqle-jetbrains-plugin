package com.actiontech.sqle.util;

import com.actiontech.sqle.config.SQLEAuditResult;
import com.actiontech.sqle.config.SQLEProjectNameListResult;
import com.actiontech.sqle.config.SQLESettings;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpStatus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpClientUtil {

    private String uriHead;
    private String token;
    private SQLESettings settings;

    private static final String loginPath = "/v1/login";
    private static final String auditPath = "/v1/sql_audit";
    private static final String driversPath = "/v1/configurations/drivers";

    private static final String projectPath = "/v1/projects";

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
        JsonObject resp = sendPostJson(uriHead + loginPath, reqJson);
        if (resp.get("code").getAsInt() != 0) {
            throw new Exception("login failed: " + resp.get("message").getAsString());
        }
        token = resp.get("data").getAsJsonObject().get("token").getAsString();
        settings.setToken(token);
        return token;
    }

    public ArrayList<String> GetDBTypes() throws Exception {
        if (token == null || token.equals("")) {
            Login();
        }

        JsonObject resp = sendGet(uriHead + driversPath);
        if (resp.get("code").getAsInt() != 0) {
            throw new Exception("login failed: " + resp.get("message").getAsString());
        }
        JsonArray array = resp.get("data").getAsJsonObject().get("driver_name_list").getAsJsonArray();
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            list.add(array.get(i).getAsString());
        }
        return list;
    }

    public ArrayList<String> GetProjectList() throws Exception {
        if (token == null || token.equals("")) {
            Login();
        }

        String reqPath = String.format("%s?page_index=%s&page_size=%s", projectPath, "1", "999999");
        JsonObject resp = sendGet(uriHead + reqPath);
        if (resp.get("code").getAsInt() != 0) {
            throw new Exception("login failed: " + resp.get("message").getAsString());
        }

        Gson gson = new Gson();
        Type datasetListType = new TypeToken<Collection<SQLEProjectNameListResult>>() {
        }.getType();

        JsonElement jsonObject = resp.get("data");
        List<SQLEProjectNameListResult> ProjectNameList = gson.fromJson(jsonObject, datasetListType);
        ArrayList<String> list = new ArrayList<>();
        for (SQLEProjectNameListResult sqleProjectNameListResult : ProjectNameList) {
            list.add(sqleProjectNameListResult.getName());
        }

        return list;
    }

    public enum AuditType {
        SQL, MyBatis;
    }

    public SQLEAuditResult AuditSQL(String sql, AuditType type) throws Exception {
        if (token == null || token.equals("")) {
            Login();
        }

        Map<String, String> req = new HashMap<>();
        req.put("instance_type", settings.getDBType());
        req.put("sql_content", sql);

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

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
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
