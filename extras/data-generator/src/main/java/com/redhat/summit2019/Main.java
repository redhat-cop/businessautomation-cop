package com.redhat.summit2019;

import com.redhat.summit2019.generator.DataGenerator;
import okhttp3.Credentials;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.net.URL;

public class Main {

    private static final OkHttpClient http = new OkHttpClient();
    private static final DataGenerator dataGenerator = new DataGenerator();
    private static final String BASE_URL = System.getenv().getOrDefault("PAM_BASE_URL", "http://127.0.0.1:8080/kie-server/services/rest/server");
    private static final String CONTAINER_ID = System.getenv().getOrDefault("PAM_CONTAINER_ID", "kafka-jbpm-process_1.0.18-SNAPSHOT");
    private static final String PROCESS_INSTANCE_ID = System.getenv().getOrDefault("PAM_PROCESS_INSTANCE_ID", "kafka-jbpm-process.claimfund-process");
    private static final String USERNAME = System.getenv().getOrDefault("JBPM_USERNAME", "wbadmin");
    private static final String PASSWORD = System.getenv().getOrDefault("JBPM_PASSWORD", "wbadmin");

    public static void main(String[] args) throws Exception {
        int quantity = 10;
        Headers authHeader = new Headers.Builder()
                .add("Authorization", Credentials.basic(USERNAME, PASSWORD))
                .build();
        URL url = new URL(BASE_URL + "/containers/" + CONTAINER_ID + "/processes/" + PROCESS_INSTANCE_ID + "/instances");

        if (args != null && args.length > 0) {
            quantity = Integer.parseInt(args[0]);
        }

        System.out.println("Generating " + quantity + " new task(s).");
        for (int i = 0; i < quantity; i++) {
            System.out.println("Creating task #" + i);
            String json = dataGenerator.generateJsonData();

            RequestBody body = RequestBody.create(MediaType.get("application/json"), json);
            Request request = new Request.Builder()
                    .url(url)
                    .headers(authHeader)
                    .post(body)
                    .build();
            Response response = http.newCall(request).execute();

            System.out.println("POST: " + response.code() + " " + (response.body() != null ? response.body().string() : "null"));
        }
    }
}
