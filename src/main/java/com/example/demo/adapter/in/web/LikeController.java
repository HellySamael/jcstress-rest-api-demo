package com.example.demo.adapter.in.web;

import com.example.demo.application.port.in.LikeUseCase;
import io.javalin.Javalin;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Collections;
import java.util.Map;

@Singleton
public class LikeController {

    private final LikeUseCase likeUseCase;

    @Inject
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
    }
}
