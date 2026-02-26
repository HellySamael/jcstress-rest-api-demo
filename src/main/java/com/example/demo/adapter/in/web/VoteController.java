package com.example.demo.adapter.in.web;

import com.example.demo.application.port.in.VoteUseCase;
import io.javalin.Javalin;

public class VoteController {

    private final VoteUseCase voteUseCase;

    public VoteController(VoteUseCase voteUseCase) {
        this.voteUseCase = voteUseCase;
    }

    public void registerRoutes(Javalin app) {
        // Vote for a specific photo/item
        app.post("/votes/{itemId}", ctx -> {
            String itemId = ctx.pathParam("itemId");
            int newCount = voteUseCase.vote(itemId);
            ctx.json(java.util.Collections.singletonMap("votes", newCount));
        });

        // Get all vote counts
        app.get("/votes", ctx -> {
            ctx.json(voteUseCase.getVotes());
        });

        // Get vote count for a specific item
        app.get("/votes/{itemId}", ctx -> {
            String itemId = ctx.pathParam("itemId");
            ctx.json(java.util.Collections.singletonMap("votes", voteUseCase.getVotesFor(itemId)));
        });

        // Reset all votes
        app.delete("/votes", ctx -> {
            voteUseCase.resetVotes();
            ctx.status(204); // No Content
        });
    }
}
