package com.example.demo;

import com.example.demo.adapter.in.web.LikeController;
import com.example.demo.adapter.out.persistence.InMemoryLikeRepository;
import com.example.demo.application.port.in.LikeUseCase;
import com.example.demo.application.port.out.LikeRepository;
import com.example.demo.application.service.LikeService;
import com.example.demo.domain.LikeCounter;

public class DIContainer {

    private final LikeCounter likeCounter;
    private final LikeRepository likeRepository;
    private final LikeUseCase likeUseCase;
    private final LikeController likeController;

    public DIContainer() {
        this.likeCounter = new LikeCounter();
        this.likeRepository = new InMemoryLikeRepository(this.likeCounter);
        this.likeUseCase = new LikeService(this.likeRepository);
        this.likeController = new LikeController(this.likeUseCase);
    }

    public LikeController getLikeController() {
        return likeController;
    }
}
