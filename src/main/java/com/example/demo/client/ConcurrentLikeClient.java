package com.example.demo.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentLikeClient {

    private static final String BASE_URL = "http://localhost:7070";
    private static final int NUM_CONCURRENT_REQUESTS = 100; // Number of requests to send concurrently
    private static final int NUM_TOTAL_REQUESTS = 10000; // Total number of POST requests

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .executor(Executors.newFixedThreadPool(NUM_CONCURRENT_REQUESTS))
                .build();

        ExecutorService requestExecutor = Executors.newFixedThreadPool(NUM_CONCURRENT_REQUESTS);
        AtomicInteger successCount = new AtomicInteger(0);

        System.out.println("Starting to send " + NUM_TOTAL_REQUESTS + " POST /likes requests concurrently...");

        long startTime = System.nanoTime();

        for (int i = 0; i < NUM_TOTAL_REQUESTS; i++) {
            requestExecutor.submit(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/likes"))
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 204) { // No Content for successful POST
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    System.err.println("Request failed: " + e.getMessage());
                }
            });
        }

        requestExecutor.shutdown();
        requestExecutor.awaitTermination(1, TimeUnit.MINUTES); // Wait for all requests to finish

        long endTime = System.nanoTime();
        long durationMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        System.out.println("Sent " + successCount.get() + " successful POST /likes requests in " + durationMillis + " ms.");

        // Fetch the final like count
        System.out.println("Fetching final like count...");
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/likes"))
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("Final like count: " + getResponse.body());
    }
}
