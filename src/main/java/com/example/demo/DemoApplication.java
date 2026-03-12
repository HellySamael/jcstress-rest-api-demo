package com.example.demo;

import com.example.demo.counter.ConcurrentHashMapCounter;
import com.example.demo.counter.HashMapCounter;
import com.example.demo.counter.LongAdderCounter;
import com.example.demo.counter.PizzaCounter;
import com.example.demo.counter.RacyDbCounter;
import com.example.demo.counter.SafeDbCounter;
import com.example.demo.counter.SyncCounter;
import com.example.demo.counter.ThreadSafeConcurentHashMapCounter;
import io.javalin.Javalin;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;

public final class DemoApplication {

    private DemoApplication() {
    }

    public static void main(String[] args) {

        // Choix de l'implémentation via premier argument CLI ou variable d'environnement VOTE_IMPL
        // Exemples d'options : "hashmap", "sync", "concurrent", "longadder", "threadsafe" (par défaut)
        String impl = args.length > 0 ? args[0] : System.getenv().getOrDefault("VOTE_IMPL", "safedb");
        PizzaCounter voteCounter = switch (impl.toLowerCase()) {
            case "hashmap" -> new HashMapCounter();
            case "sync" -> new SyncCounter();
            case "concurrent" -> new ConcurrentHashMapCounter();
            case "longadder" -> new LongAdderCounter();
            case "racydb"     -> new RacyDbCounter("pizza_votes");
            case "safedb"     -> new SafeDbCounter("pizza_votes");
            case "threadsave" -> new ThreadSafeConcurentHashMapCounter();
            default -> new ThreadSafeConcurentHashMapCounter();
        };

        // Final copy to allow capture in lambdas
        final String implName = impl;

        // Initialize and configure Javalin
        // Serve static resources from src/main/resources/public at '/'
        Javalin app = Javalin.create(DemoApplication::configure);

        // Simple CORS support for local demos (UI on another origin/port).
        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
            ctx.header("Access-Control-Max-Age", "3600");
        });

        app.options("/*", ctx -> ctx.status(204));

        // Start on port 7070
        app.post("/votes/{itemId}", ctx -> {
            String itemId = ctx.pathParam("itemId");
            int newCount = voteCounter.vote(itemId);
            ctx.json(java.util.Collections.singletonMap("votes", newCount));
        });

        // Get all vote counts
        app.get("/votes", ctx -> {
            ctx.json(voteCounter.getVotes());
        });

        // Get vote count for a specific item
        app.get("/votes/{itemId}", ctx -> {
            String itemId = ctx.pathParam("itemId");
            ctx.json(java.util.Collections.singletonMap("votes", voteCounter.getVotesFor(itemId)));
        });

        // Info: which implementation is running (useful for demo)
        app.get("/info", ctx -> {
            ctx.json(java.util.Collections.singletonMap("impl", implName));
        });

        // Reset all votes
        app.delete("/votes", ctx -> {
            voteCounter.resetVotes();
            ctx.status(204); // No Content
        });

        app.start(7070);

        System.out.println("Javalin application started on port 7070.");
        System.out.println("Using vote counter implementation: " + implName);
        System.out.println("--- Likes API ---");
        System.out.println("Try: POST http://localhost:7070/likes to increment a like.");
        System.out.println("Try: GET http://localhost:7070/likes to get the current like count.");
        System.out.println("--- Votes API ---");
        System.out.println("Try: POST http://localhost:7070/votes/star1 to vote for star1.");
        System.out.println("Try: POST http://localhost:7070/votes/star2 to vote for star2.");
        System.out.println("Try: GET http://localhost:7070/votes to get all vote counts.");
        System.out.println("Try: GET http://localhost:7070/votes/star1 to get votes for star1.");
    }

    private static void configure(JavalinConfig config) {
        config.staticFiles.add(staticFileConfig -> {
            staticFileConfig.hostedPath = "/";      // serve at root
            staticFileConfig.directory = "public";  // folder in resources
            staticFileConfig.location = Location.CLASSPATH; // classpath:/public
        });

        config.requestLogger.http((ctx, ms) -> {
            // Log all requests
            System.out.println("Request: " + ctx.req().getMethod() + " " + ctx.req().getRequestURI() + " took " + ms + " ms");
        });
    }
}
