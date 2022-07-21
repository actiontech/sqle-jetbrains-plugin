package com.actiontech.sqle.util;

import com.actiontech.sqle.config.SQLEAuditResult;
import com.actiontech.sqle.config.SQLESettings;
import com.google.api.Http;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpStatus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HttpClientUtil {

    private String uriHead;
    private String token;
    private SQLESettings settings;

    private static final String loginPath = "/v1/login";
    private static final String auditPath = "/v1/sql_audit";
    private static final String driversPath = "/v1/configurations/drivers";

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

    public SQLEAuditResult AuditSQL(String sql) throws Exception {
        if (token == null || token.equals("")) {
            Login();
        }

        Map<String, String> req = new HashMap<>();
        req.put("instance_type", settings.getDBType());
        req.put("sql_content", sql);
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

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
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
        if (responseCode != HttpStatus.SC_OK) {
            throw new Exception("response code != 200, message: " + response.toString());
        }
        return new JsonParser().parse(response.toString()).getAsJsonObject();
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
        wr.writeBytes(request);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
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
        if (responseCode != HttpStatus.SC_OK) {
            throw new Exception("response code != 200, message: " + response.toString());
        }
        return new JsonParser().parse(response.toString()).getAsJsonObject();
    }
}
