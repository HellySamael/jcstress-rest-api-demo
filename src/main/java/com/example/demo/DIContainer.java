package com.example.demo;

import com.example.demo.adapter.in.web.LikeController;
import com.example.demo.adapter.out.persistence.InMemoryLikeRepository;
import com.example.demo.application.port.in.LikeUseCase;
import com.example.demo.application.port.out.LikeRepository;
import com.example.demo.application.service.LikeService;
import com.example.demo.domain.LikeCounter;

// --- New components for Vote feature ---
import com.example.demo.adapter.in.web.VoteController;
import com.example.demo.adapter.out.persistence.InMemoryVoteRepository;
import com.example.demo.application.port.in.VoteUseCase;
import com.example.demo.application.port.out.VoteRepository;
import com.example.demo.application.service.VoteService;
import com.example.demo.domain.VoteCounter;


public class DIContainer {

    // Existing components for Like feature
    private final LikeCounter likeCounter;
    private final LikeRepository likeRepository;
    private final LikeUseCase likeUseCase;
    private final LikeController likeController;

    // New components for Vote feature
    private final VoteCounter voteCounter;
    private final VoteRepository voteRepository;
    private final VoteUseCase voteUseCase;
    private final VoteController voteController;

    public DIContainer() {
        // --- Existing Like feature ---
        this.likeCounter = new LikeCounter();
        this.likeRepository = new InMemoryLikeRepository(this.likeCounter);
        this.likeUseCase = new LikeService(this.likeRepository);
        this.likeController = new LikeController(this.likeUseCase);

        // --- New Vote feature ---
        this.voteCounter = new VoteCounter();
        this.voteRepository = new InMemoryVoteRepository(this.voteCounter);
        this.voteUseCase = new VoteService(this.voteRepository);
        this.voteController = new VoteController(this.voteUseCase);
    }

    public LikeController getLikeController() {
        return likeController;
    }

    public VoteController getVoteController() {
        return voteController;
    }
}

