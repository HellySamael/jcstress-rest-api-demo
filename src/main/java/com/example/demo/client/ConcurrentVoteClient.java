package com.example.demo.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

public class ConcurrentVoteClient {

    private static final String BASE_URL = "http://localhost:7070";
    private static final int DEFAULT_NUM_CONCURRENT_REQUESTS = 100;
    private static final int DEFAULT_NUM_TOTAL_REQUESTS = 10000;
    private static final List<String> ITEMS = List.of("item1", "item2", "item3", "item4");

    public static void main(String[] args) throws Exception {
        int numConcurrentRequests = DEFAULT_NUM_CONCURRENT_REQUESTS;
        int numTotalRequests = DEFAULT_NUM_TOTAL_REQUESTS;

        if (args.length > 0) {
            try {
                numConcurrentRequests = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid number for concurrent requests: " + args[0] + ". Using default: " + DEFAULT_NUM_CONCURRENT_REQUESTS);
            }
        }
        if (args.length > 1) {
            try {
                numTotalRequests = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid number for total requests: " + args[1] + ". Using default: " + DEFAULT_NUM_TOTAL_REQUESTS);
            }
        }

        System.out.println("Running Vote client with " + numConcurrentRequests + " concurrent requests and " + numTotalRequests + " total requests.");
        System.out.println("Voting on items: " + ITEMS);

        HttpClient client = HttpClient.newBuilder()
                .executor(Executors.newFixedThreadPool(numConcurrentRequests))
                .build();

        // Reset votes first
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/votes"))
                .DELETE()
                .build();
        client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

        ExecutorService requestExecutor = Executors.newFixedThreadPool(numConcurrentRequests);
        AtomicInteger successCount = new AtomicInteger(0);

        long startTime = System.nanoTime();

        for (int i = 0; i < numTotalRequests; i++) {
            final String itemId = ITEMS.get(i % ITEMS.size());
            requestExecutor.submit(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/votes/" + itemId))
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) { 
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    System.err.println("Request failed: " + e.getMessage());
                }
            });
        }

        requestExecutor.shutdown();
        requestExecutor.awaitTermination(1, TimeUnit.MINUTES);

        long endTime = System.nanoTime();
        long durationMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        System.out.println("Sent " + successCount.get() + " successful POST /votes/{itemId} requests in " + durationMillis + " ms.");

        // Fetch the final vote counts
        System.out.println("Fetching final vote counts...");
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/votes"))
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("Final vote counts: " + getResponse.body());
        
        System.out.println("Expected total votes: " + numTotalRequests);
    }
}
