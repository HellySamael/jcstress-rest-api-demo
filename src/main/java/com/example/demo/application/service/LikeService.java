package com.example.demo.application.service;

import com.example.demo.application.port.in.LikeUseCase;
import com.example.demo.application.port.out.LikeRepository;
import com.example.demo.domain.LikeCounter;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class LikeService implements LikeUseCase {

    private final LikeRepository likeRepository;

    @Inject
    public LikeService(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    @Override
    public void like() {
        LikeCounter likeCounter = likeRepository.get();
        likeCounter.increment();
        likeRepository.save(likeCounter);
    }

    @Override
    public int getLikes() {
        return likeRepository.get().getCount();
    }
}
