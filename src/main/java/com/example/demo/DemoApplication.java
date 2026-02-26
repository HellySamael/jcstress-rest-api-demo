package com.example.demo;

import com.example.demo.adapter.in.web.LikeController;
import com.example.demo.adapter.in.web.VoteController;
import io.javalin.Javalin;

public class DemoApplication {

    public static void main(String[] args) {
        // Build the dependency graph manually
        DIContainer container = new DIContainer();

        // Get the controllers from the container
        LikeController likeController = container.getLikeController();
        VoteController voteController = container.getVoteController();

        // Initialize and configure Javalin
        Javalin app = Javalin.create(config -> {
            config.requestLogger.http((ctx, ms) -> {
                // Log all requests
                System.out.println("Request: " + ctx.req().getMethod() + " " + ctx.req().getRequestURI() + " took " + ms + " ms");
            });
        }).start(7070); // Start on port 7070

        // Register routes from the controllers
        likeController.registerRoutes(app);
        voteController.registerRoutes(app);

        System.out.println("Javalin application started on port 7070.");
        System.out.println("--- Likes API ---");
        System.out.println("Try: POST http://localhost:7070/likes to increment a like.");
        System.out.println("Try: GET http://localhost:7070/likes to get the current like count.");
        System.out.println("--- Votes API ---");
        System.out.println("Try: POST http://localhost:7070/votes/star1 to vote for star1.");
        System.out.println("Try: POST http://localhost:7070/votes/star2 to vote for star2.");
        System.out.println("Try: GET http://localhost:7070/votes to get all vote counts.");
        System.out.println("Try: GET http://localhost:7070/votes/star1 to get votes for star1.");
    }
}

