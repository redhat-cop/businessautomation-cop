package com.redhat.summit2019;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import okhttp3.Credentials;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    private static final OkHttpClient http = new OkHttpClient();
    private static final String BASE_URL = System.getenv().getOrDefault("PAM_BASE_URL", "http://127.0.0.1:8080/kie-server/services/rest/server");
    private static final String CONTAINER_ID = System.getenv().getOrDefault("PAM_CONTAINER_ID", "kafka-jbpm-process_1.0.18-SNAPSHOT");
    private static final String PAGE_SIZE = System.getenv().getOrDefault("BATCH_SIZE", "20");
    private static final String USERNAME = System.getenv().getOrDefault("JBPM_USERNAME", "wbadmin");
    private static final String PASSWORD = System.getenv().getOrDefault("JBPM_PASSWORD", "wbadmin");
    private static final int SLEEP = Integer.parseInt(System.getenv().getOrDefault("RESPONSE_WAIT", "5000"));

    public static void main(String[] args) throws IOException, InterruptedException {
        Headers authHeader = new Headers.Builder()
                .add("Authorization", Credentials.basic(USERNAME, PASSWORD))
                .build();

        URL url = new URL(BASE_URL + "/queries/tasks/instances/pot-owners?status=Ready&page=0&pageSize=" + PAGE_SIZE
                + "&sortOrder=true");

        Request request = new Request.Builder()
                .url(url)
                .headers(authHeader)
                .addHeader("Accept", "application/json")
                .get()
                .build();
        Response response = http.newCall(request).execute();
        String json = response.body() != null ? response.body().string() : "";

        JsonFactory jsonFactory = new JsonFactory();
        JsonParser parser = jsonFactory.createParser(json);

        List<Integer> taskIdList = new ArrayList<>();

        while (parser.nextToken() != null) {
            String field = parser.getCurrentName();

            if ("task-id".equalsIgnoreCase(field)) {
                parser.nextToken();
                taskIdList.add(parser.getIntValue());
            }
        }

        parser.close();

        System.out.println("Collected task IDs: " + taskIdList);

        if (taskIdList.isEmpty()) {
            System.out.println("No task IDs at ready state. Aborting...");
            System.exit(-1);
        }

        Thread.sleep(SLEEP);

        System.out.println("Claiming tasks...");
        for (int taskId : taskIdList) {
            url = new URL(BASE_URL + "/containers/" + CONTAINER_ID + "/tasks/" + taskId + "/states/claimed");
            request = new Request.Builder()
                    .url(url)
                    .headers(authHeader)
                    .put(RequestBody.create(MediaType.get("application/json"), ""))
                    .build();
            response = http.newCall(request).execute();
            if (response.code() != 201) {
                if (response.body() != null) {
                    System.out.println(response.body().string());
                } else {
                    System.out.println("Body is null");
                }
            }
        }

        Thread.sleep(SLEEP);

        System.out.println("Starting tasks...");
        for (int taskId : taskIdList) {
            url = new URL(BASE_URL + "/containers/" + CONTAINER_ID + "/tasks/" + taskId + "/states/started");
            request = new Request.Builder()
                    .url(url)
                    .headers(authHeader)
                    .put(RequestBody.create(MediaType.get("application/json"), ""))
                    .build();
            response = http.newCall(request).execute();
            if (response.code() != 201) {
                if (response.body() != null) {
                    System.out.println(response.body().string());
                } else {
                    System.out.println("Body is null");
                }
            }
        }

        Thread.sleep(SLEEP);

        System.out.println("Completing tasks...");
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int taskId : taskIdList) {
            boolean approved = random.nextBoolean();
            String content = "{\"isApproved\": " + approved + "}";
            url = new URL(BASE_URL + "/containers/" + CONTAINER_ID + "/tasks/" + taskId + "/states/completed");
            request = new Request.Builder()
                    .url(url)
                    .headers(authHeader)
                    .put(RequestBody.create(MediaType.get("application/json"), content))
                    .build();
            response = http.newCall(request).execute();
            if (response.code() != 201) {
                if (response.body() != null) {
                    System.out.println(response.body().string());
                } else {
                    System.out.println("Body is null");
                }
            }
        }
    }
}
