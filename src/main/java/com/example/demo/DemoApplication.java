package com.example.demo;

import com.example.demo.adapter.in.web.LikeController;
import io.javalin.Javalin;

public class DemoApplication {

    public static void main(String[] args) {
        // Build Dagger graph
        AppComponent appComponent = DaggerAppComponent.create();

        // Get the LikeController from the Dagger graph
        LikeController likeController = appComponent.likeController();

        // Initialize and configure Javalin
        Javalin app = Javalin.create(config -> {
            config.requestLogger.http((ctx, ms) -> {
                // Log all requests
                System.out.println("Request: " + ctx.req().getMethod() + " " + ctx.req().getRequestURI() + " took " + ms + " ms");
            });
        }).start(7070); // Start on port 7070

        // Register routes from the LikeController
        likeController.registerRoutes(app);

        System.out.println("Javalin application started on port 7070.");
        System.out.println("Try: POST http://localhost:7070/likes to increment a like.");
        System.out.println("Try: GET http://localhost:7070/likes to get the current like count.");
    }
}
