package com.example.demo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConcurrentVoteClient {

    private static final String BASE_URL = "http://localhost:7070";
    private static final int DEFAULT_NUM_CONCURRENT_REQUESTS = 100;
    private static final int DEFAULT_NUM_TOTAL_REQUESTS = 10000;
    private static final List<String> ITEMS = List.of("margherita", "pepperoni", "funghi", "quattro");

    private ConcurrentVoteClient(){}

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
        boolean terminated = requestExecutor.awaitTermination(1, TimeUnit.MINUTES);
        if (!terminated) {
            System.err.println("Warning: request executor did not terminate within 1 minute. Forcing shutdown.");
            requestExecutor.shutdownNow();
        }

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
        String body = getResponse.body();
        System.out.println("Final vote counts: " + body);

        // Nouveau check : sommer les votes renvoyés par le serveur et comparer au total attendu
        int sumVotes = sumVotesFromJson(body);
        System.out.println("Expected total votes: " + numTotalRequests);
        if (sumVotes != numTotalRequests) {
            System.err.println("[MISMATCH] Total votes returned by server (" + sumVotes + ") != expected total requests (" + numTotalRequests + ").");
            System.err.println("Successful POSTs observed: " + successCount.get());
        } else {
            System.out.println("[OK] Total votes match expected total requests.");
        }

        // Also print if successful HTTP responses differ from expected total
        if (successCount.get() != numTotalRequests) {
            System.err.println("[WARNING] Number of successful HTTP responses (" + successCount.get() + ") differs from expected total requests (" + numTotalRequests + ").");
        }

        // Fetch the final vote counts (already printed)
        System.out.println("Done.");
    }

    // Petite méthode utilitaire qui extrait tous les nombres du JSON d'objet simple et les somme.
    private static int sumVotesFromJson(String json) {
        if (json == null || json.isBlank()) return 0;
        // Simple extraction des entiers : recherche de ":\s*<nombre>" ; marche pour des réponses comme {"a":1,"b":2}
        Pattern p = Pattern.compile(":\\s*([0-9]+)");
        Matcher m = p.matcher(json);
        int sum = 0;
        while (m.find()) {
            try {
                sum += Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return sum;
    }
}
