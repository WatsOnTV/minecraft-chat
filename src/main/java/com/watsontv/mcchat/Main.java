package com.watsontv.mcchat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String[] creds = getCreds();
        new LoginTest("localhost", 25565, creds[0],  creds[1], false).start();
        //MCServerInfo serverInfo = new MCServerInfo("localhost", 25565);
        //JsonObject authResponse = new Authentication().mojangAuth(creds[0],  creds[1]);
        //System.out.println(authResponse.toString());
    }

    static String[] getCreds() throws IOException {
        String creds;
        try (BufferedReader br = new BufferedReader(new FileReader("credentials.txt"))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            creds = sb.toString().strip();
        }
        return creds.split(":");
    }
}
