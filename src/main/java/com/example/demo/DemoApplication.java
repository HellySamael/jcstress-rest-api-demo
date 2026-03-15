package com.example.demo;

import com.example.demo.counter.core.PizzaCounter;
import com.example.demo.counter.distributated.AppDbCounter;
import com.example.demo.counter.jmm.AppCounter;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;

public final class DemoApplication {

    private DemoApplication() {
    }

    public static void main(String[] args) {
        String impl = args.length > 0 ? args[0] : System.getenv().getOrDefault("VOTE_IMPL", "hashmap");
        PizzaCounter voteCounter = switch (impl.toLowerCase()) {
            case "hashmap", "sync", "concurrent", "threadsafe", "threadsave", "jmm" -> new AppCounter();
            case "racydb", "safedb", "db" -> new AppDbCounter("pizza_votes");
            default -> new AppCounter();
        };
        final String implName = impl;
        Javalin app = Javalin.create(DemoApplication::configure);
        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
            ctx.header("Access-Control-Max-Age", "3600");
        });

        app.options("/*", ctx -> ctx.status(204));
        app.post("/votes/{itemId}", ctx -> {
            String itemId = ctx.pathParam("itemId");
            int newCount = voteCounter.vote(itemId);
            ctx.json(java.util.Collections.singletonMap("votes", newCount));
        });
        app.get("/votes", ctx -> {
            ctx.json(voteCounter.getVotes());
        });
        app.get("/votes/{itemId}", ctx -> {
            String itemId = ctx.pathParam("itemId");
            ctx.json(java.util.Collections.singletonMap("votes", voteCounter.getVotesFor(itemId)));
        });
        app.get("/info", ctx -> {
            ctx.json(java.util.Collections.singletonMap("impl", implName));
        });
        app.delete("/votes", ctx -> {
            voteCounter.resetVotes();
            ctx.status(204);
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
            staticFileConfig.hostedPath = "/";
            staticFileConfig.directory = "public";
            staticFileConfig.location = Location.CLASSPATH;
        });

        config.requestLogger.http((ctx, ms) -> {
            System.out.println("Request: " + ctx.req().getMethod() + " " + ctx.req().getRequestURI() + " took " + ms + " ms");
        });
    }
}
