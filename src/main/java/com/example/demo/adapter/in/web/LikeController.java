package com.example.demo.adapter.in.web;

import com.example.demo.application.port.in.LikeUseCase;
import io.javalin.Javalin;

import java.util.Collections;
import java.util.Map;

public class LikeController {

    private final LikeUseCase likeUseCase;

    public LikeController(LikeUseCase likeUseCase) {
        this.likeUseCase = likeUseCase;
    }

    public void registerRoutes(Javalin app) {
        app.post("/likes", ctx -> {
            likeUseCase.like();
            ctx.status(204); // No Content
        });

        app.get("/likes", ctx -> {
            int likes = likeUseCase.getLikes();
            Map<String, Integer> response = Collections.singletonMap("likes", likes);
            ctx.json(response);
        });

        app.delete("/likes", ctx -> {
            likeUseCase.resetLikes();
            ctx.status(204); // No Content
        });
    }
}
