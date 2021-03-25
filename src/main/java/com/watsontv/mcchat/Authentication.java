package com.watsontv.mcchat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.watsontv.mcchat.exceptions.InvalidCredentialsException;
import com.watsontv.mcchat.exceptions.MinecraftNotOwnedException;
import com.watsontv.mcchat.exceptions.UserMigratedException;

import javax.naming.ServiceUnavailableException;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Authenticates with either Mojang, Minecraft or Microsoft, depending on the type of account. */
public class Authentication {

    /** Authenticates with Mojang and returns a JSONObject containing the user profile, or a JSONObject containing
     * the error which occurred. TODO Further error checking, found at https://wiki.vg/Authentication */
    public JsonObject mojangAuth(String username, String password) {
        JsonObject response;
        try {
            if(username.isEmpty() || password.isEmpty()){
                throw new InvalidCredentialsException("Username or passwords field(s) left blank");
            }
            String url = "https://authserver.mojang.com/authenticate";
            URL myUrl = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) myUrl.openConnection();
            String query = "{\"agent\":{\"name\":\"Minecraft\", \"version\": 1}, \"username\":\"" + username + "\"," +
                    "\"password\":\"" + password + "\"}";
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            con.setDoInput(true);

            DataOutputStream output = new DataOutputStream(con.getOutputStream());
            output.writeBytes(query);
            output.close();
            BufferedReader reader;
            if (con.getResponseCode() >= 200 && con.getResponseCode() <= 300) {
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

            response = new Gson().fromJson(content.toString(), JsonObject.class);

            if (con.getResponseCode() >= 400) {
                if (response.get("error").getAsString().equals("ForbiddenOperationException")) {
                    if(response.get("errorMessage").getAsString().equals("Invalid credentials. Account migrated, use email as username.")){
                        throw new UserMigratedException("Account migrated, use email as username.");
                    }
                } else {

                }
            }
        }catch (ProtocolException e) {
            response = new JsonObject();
            response.addProperty("error", "ProtocolException");
            response.addProperty("errorMessage", e.getMessage());
        }catch (MalformedURLException e) {
            response = new JsonObject();
            response.addProperty("error", "MalformedURLException");
            response.addProperty("errorMessage", e.getMessage());
        }catch (IOException e) {
            response = new JsonObject();
            response.addProperty("error", "IOException");
            response.addProperty("errorMessage", e.getMessage());
        }catch (InvalidCredentialsException e) {
            response = new JsonObject();
            response.addProperty("error", "InvalidCredentialsException");
            response.addProperty("errorMessage", e.getMessage());
        } catch (UserMigratedException e) {
            response = new JsonObject();
            response.addProperty("error", "UserMigratedException");
            response.addProperty("errorMessage", e.getMessage());
        }

        return response;
    }

    /** Performs Microsoft Authentication when given the credentials. Returns a JSONObject with either the profile
     * of the user or the error which occurred. TODO Figure out how to deal with 2fa */
    public JsonObject microsoftAuth(String username, String password) throws IOException {
        /* The auth flow for Microsoft accounts is much more complex than Mojang/Minecraft accounts:
         * 1) We log the user into their Microsoft account, using their username and password and receive a code from MS
         * 2) We use that code to get an authorization token from Microsoft
         * 3) We next authenticate with Xbox Live with the auth token, which gives us a token and UHS
         *    Note: I cannot find any documentation on the UHS and have no idea what it stands for, but its required
         * 4) We authenticate with XSTS with the Xbox Live token, which gives an XSTS token
         * 5) We finally authenticate with Minecraft after giving the USH and XSTS token, which gives us a new
         *    access token for Minecraft
         * 6) We need to check if the account actually owns Minecraft, and if it does...
         * 7) We can finally get the profile of the account, which gives the information we need.
         * 8) We return the Minecraft access token, the username and the UUID of the user (the user "profile").
         */
        JsonObject response;
        try {
            if(username.isEmpty() || password.isEmpty()){
                throw new InvalidCredentialsException("Username or passwords field(s) left blank");
            }
            String msCode = getMSCode(username, password);
            response = microsoftAuthFromMSCode(msCode);
        }catch (InvalidCredentialsException e) {
            response = new JsonObject();
            response.addProperty("error", "InvalidCredentialsException");
            response.addProperty("errorMessage", e.getMessage());
        }
        return response;
    }

    /** Performs Microsoft Authentication when given the required MS Code produced by Microsoft */
    public JsonObject microsoftAuthFromMSCode(String msCode){
        JsonObject response;
        try {
            String msToken = getMSToken(msCode);
            Map<String, String> xblResponse = getXBLToken(msToken);
            String xblToken = xblResponse.get("token");
            String uhs = xblResponse.get("uhs");
            String xstsToken = getXSTSToken(xblToken);
            String minecraftToken = getMinecraftToken(uhs, xstsToken);
            boolean accountOwnsMinecraft = checkAccountOwnsMinecraft(minecraftToken);
            response = getMinecraftProfile(minecraftToken);
            System.out.println(accountOwnsMinecraft);
        }catch (MinecraftNotOwnedException e){
            response = new JsonObject();
            response.addProperty("error", "MinecraftNotOwnedException");
            response.addProperty("errorMessage", e.getMessage());
        }catch (IOException e){
            response = new JsonObject();
            response.addProperty("error", "IOException");
            response.addProperty("errorMessage", e.getMessage());
        }
        return response;
    }

    /** Returns the MS Code by logging the user into their Microsoft account with given the credentials.
     *  Huge thanks to Steveice10 for his work on MCAuthLib which is where most of this code is from.
     *  https://github.com/Steveice10/MCAuthLib
     *  Note: This does not handle accounts with 2FA enabled. Users with these accounts must physically sign in and
     *  the code be passed on to getMSToken(..) */
    String getMSCode(String username, String password) throws IOException, InvalidCredentialsException {
        URL MS_LOGIN_ENDPOINT = new URL("https://login.live.com/oauth20_authorize.srf?redirect_uri=https://login.live.com/oauth20_desktop.srf&scope=service::user.auth.xboxlive.com::MBI_SSL&display=touch&response_type=code&locale=en&client_id=00000000402b5328");
        String cookie = "";
        String PPFT = "";
        String urlPost = "";
        Pattern PPFT_PATTERN = Pattern.compile("sFTTag:[ ]?'.*value=\"(.*)\"/>'");
        Pattern URL_POST_PATTERN = Pattern.compile("urlPost:[ ]?'(.+?(?='))");
        Pattern CODE_PATTERN = Pattern.compile("[?|&]code=([\\w.-]+)");

        HttpsURLConnection connection = (HttpsURLConnection) MS_LOGIN_ENDPOINT.openConnection();
        connection.setDoInput(true);
        try (InputStream in = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream()) {
            cookie = connection.getHeaderField("set-cookie");
            String body = inputStreamToString(in);
            Matcher m = PPFT_PATTERN.matcher(body);

            if (m.find()) {
                PPFT = m.group(1);
            } else {
                throw new ServiceUnavailableException("Could not parse response of '" + MS_LOGIN_ENDPOINT + "'.");
            }

            m = URL_POST_PATTERN.matcher(body);
            if (m.find()) {
                urlPost = m.group(1);
            } else {
                throw new ServiceUnavailableException("Could not parse response of '" + MS_LOGIN_ENDPOINT + "'.");
            }
        } catch (ServiceUnavailableException e) {
            e.printStackTrace();
        }

        Map<String, String> map = new HashMap<>();

        map.put("login", username);
        map.put("loginfmt", username);
        map.put("passwd", password);
        map.put("PPFT", PPFT);
        String postData = formMapToString(map);
        String code;

        byte[] bytes = postData.getBytes(StandardCharsets.UTF_8);
        HttpsURLConnection con = (HttpsURLConnection) new URL(urlPost).openConnection();
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        con.setRequestProperty("Content-Length", String.valueOf(bytes.length));
        con.setRequestProperty("Cookie", cookie);
        con.setDoInput(true);
        con.setDoOutput(true);
        OutputStream out = con.getOutputStream();
        out.write(bytes);
        if (con.getResponseCode() != 200 || con.getURL().toString().equals(urlPost)) {
            System.out.println(con.getURL().toString());
            System.out.println(urlPost);
            throw new InvalidCredentialsException("Invalid Microsoft username and/or password");
        }
        Matcher m = CODE_PATTERN.matcher(URLDecoder.decode(con.getURL().toString(), StandardCharsets.UTF_8.name()));
        if (m.find()) {
            code = m.group(1);
        } else {
            throw new IOException("Could not parse response of '" + urlPost + "'.");
        }
        return code;
    }

    String getMSToken(String msCode) throws IOException {
        URL MS_TOKEN_ENDPOINT = new URL("https://login.live.com/oauth20_token.srf");
        HttpsURLConnection connection = (HttpsURLConnection) MS_TOKEN_ENDPOINT.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);

        Map<String, String> map = new HashMap<>();
        map.put("client_id", "00000000402b5328");
        map.put("code", msCode);
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "https://login.live.com/oauth20_desktop.srf");
        map.put("scope", "service::user.auth.xboxlive.com::MBI_SSL");
        String query = formMapToString(map);
        DataOutputStream output = new DataOutputStream(connection.getOutputStream());
        output.writeBytes(query);
        output.close();
        InputStream reader;
        if(connection.getResponseCode() == 200){
            reader = connection.getInputStream();
        }else{
            reader = connection.getErrorStream();
        }

        JsonObject response = new Gson().fromJson(inputStreamToString(reader), JsonObject.class);
        return response.get("access_token").getAsString();
    }

    Map<String, String> getXBLToken(String msToken) throws IOException {
        URL XBL_AUTH_ENDPOINT = new URL("https://user.auth.xboxlive.com/user/authenticate");
        HttpsURLConnection connection = (HttpsURLConnection) XBL_AUTH_ENDPOINT.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type","application/json");
        connection.setAllowUserInteraction(true);
        connection.setRequestProperty("Accept", "application/json");
        Map<String, String> propertiesMap = new HashMap<>();
        propertiesMap.put("AuthMethod", "RPS");
        propertiesMap.put("SiteName", "user.auth.xboxlive.com");
        propertiesMap.put("RpsTicket", msToken);
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("Properties", propertiesMap);
        queryMap.put("RelyingParty", "http://auth.xboxlive.com");
        queryMap.put("TokenType", "JWT");
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String json = gson.toJson(queryMap);
        DataOutputStream output = new DataOutputStream(connection.getOutputStream());
        output.writeBytes(json);
        output.close();
        InputStream reader;
        if(connection.getResponseCode() == 200){
            reader = connection.getInputStream();
        }else{
            reader = connection.getErrorStream();
        }
        JsonObject response = new Gson().fromJson(inputStreamToString(reader), JsonObject.class);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("token", response.get("Token").getAsString());
        responseMap.put("uhs", response.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString());
        return responseMap;
    }

    String getXSTSToken(String xblToken) throws IOException {
        URL XSTS_AUTH_ENDPOINT = new URL("https://xsts.auth.xboxlive.com/xsts/authorize");
        HttpsURLConnection connection = (HttpsURLConnection) XSTS_AUTH_ENDPOINT.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type","application/json");
        connection.setRequestProperty("Accept", "application/json");
        ArrayList<String> tokens = new ArrayList<>();
        tokens.add(xblToken);
        Map<String, Object> propertiesMap = new HashMap<>();
        propertiesMap.put("SandboxId", "RETAIL");
        propertiesMap.put("UserTokens", tokens);
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("Properties", propertiesMap);
        queryMap.put("RelyingParty", "rp://api.minecraftservices.com/");
        queryMap.put("TokenType", "JWT");
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String json = gson.toJson(queryMap);
        DataOutputStream output = new DataOutputStream(connection.getOutputStream());
        output.writeBytes(json);
        output.close();
        InputStream reader;
        if(connection.getResponseCode() == 200){
            reader = connection.getInputStream();
        }else{
            reader = connection.getErrorStream();
        }
        JsonObject response = new Gson().fromJson(inputStreamToString(reader), JsonObject.class);
        return response.get("Token").getAsString();
    }

    String getMinecraftToken(String uhs, String xstsToken) throws IOException {
        URL MC_LOGIN_ENDPOINT = new URL("https://api.minecraftservices.com/authentication/login_with_xbox");
        HttpsURLConnection connection = (HttpsURLConnection) MC_LOGIN_ENDPOINT.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type","application/json");
        connection.setRequestProperty("Accept", "application/json");
        String query = "{\"identityToken\": \"XBL3.0 x=" + uhs + ";" + xstsToken + "\"}";
        DataOutputStream output = new DataOutputStream(connection.getOutputStream());
        output.writeBytes(query);
        output.close();
        InputStream reader;
        if(connection.getResponseCode() == 200){
            reader = connection.getInputStream();
        }else{
            reader = connection.getErrorStream();
        }
        JsonObject response = new Gson().fromJson(inputStreamToString(reader), JsonObject.class);
        System.out.println(response.toString());
        return response.get("access_token").getAsString();
    }

    boolean checkAccountOwnsMinecraft(String minecraftToken) throws IOException {
        URL MC_OWNERSHIP_CHECK_ENDPOINT = new URL("https://api.minecraftservices.com/entitlements/mcstore");
        HttpsURLConnection connection = (HttpsURLConnection) MC_OWNERSHIP_CHECK_ENDPOINT.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + minecraftToken);
        InputStream reader;
        if(connection.getResponseCode() == 200){
            reader = connection.getInputStream();
        }else{
            reader = connection.getErrorStream();
        }
        JsonObject response = new Gson().fromJson(inputStreamToString(reader), JsonObject.class);
        return response.toString().contains("product_minecraft") && response.toString().contains("game_minecraft");
    }

    JsonObject getMinecraftProfile(String minecraftToken) throws IOException, MinecraftNotOwnedException {
        URL MC_PROFILE_ENDPOINT = new URL("https://api.minecraftservices.com/minecraft/profile");
        HttpsURLConnection connection = (HttpsURLConnection) MC_PROFILE_ENDPOINT.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + minecraftToken);
        InputStream reader;
        if(connection.getResponseCode() == 200){
            reader = connection.getInputStream();
        }else{
            reader = connection.getErrorStream();
        }
        JsonObject response = new Gson().fromJson(inputStreamToString(reader), JsonObject.class);
        if(response.toString().contains("NOT_FOUND")){
            throw new MinecraftNotOwnedException("Microsoft Account does not own a copy of Minecraft");
        }
        return response;
    }

    /** Authenticates the current session with Minecraft */
    public static JsonObject authenticateSession(String accessToken, String uuid, String serverHash){
        JsonObject response;
        try{
            URL sessionServer = new URL("https://sessionserver.mojang.com/session/minecraft/join");
            HttpsURLConnection connection = (HttpsURLConnection) sessionServer.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/json");
            Map<String, String> map = new HashMap<>();
            map.put("accessToken", accessToken);
            map.put("selectedProfile", uuid);
            map.put("serverId", serverHash);
            String content = formMapToString(map);
            DataOutputStream output = new DataOutputStream(connection.getOutputStream());
            output.writeBytes(content);
            output.close();
            InputStream reader;
            if(connection.getResponseCode() == 204){
                reader = connection.getInputStream();
            }else{
                reader = connection.getErrorStream();
            }
            response = new Gson().fromJson(inputStreamToString(reader), JsonObject.class);
        }catch (IOException e){
            response = new JsonObject();
            response.addProperty("error", "IOException");
            response.addProperty("errorMessage", e.getMessage());
        }
        return response;
    }

    static String formMapToString(Map<String, String> input) {
        StringBuilder inputString = new StringBuilder();
        for (Map.Entry<String, String> inputField : input.entrySet()) {
            if (inputString.length() > 0) {
                inputString.append("&");
            }
            try {
                inputString.append(URLEncoder.encode(inputField.getKey(), StandardCharsets.UTF_8.toString()));
                inputString.append("=");
                inputString.append(URLEncoder.encode(inputField.getValue(), StandardCharsets.UTF_8.toString()));
            } catch (UnsupportedEncodingException ignored) { }
        }
        return inputString.toString();
    }

    static String inputStreamToString(InputStream inputStream) throws IOException {
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        return textBuilder.toString();
    }
}
