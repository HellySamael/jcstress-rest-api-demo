package com.example.demo.adapter.out.persistence;

import com.example.demo.application.port.out.LikeRepository;
import com.example.demo.domain.LikeCounter;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class InMemoryLikeRepository implements LikeRepository {

    private final LikeCounter likeCounter;

    @Inject
    public InMemoryLikeRepository(LikeCounter likeCounter) {
        this.likeCounter = likeCounter;
    }

    @Override
    public void save(LikeCounter likeCounter) {
        // In this in-memory implementation, we directly operate on the shared instance
        // so save operation might not do much beyond ensuring it's the same instance.
        // For simplicity, we'll assume the provided likeCounter is the same instance
        // or we're just making sure our internal reference is up-to-date.
        // In a real scenario, this would persist the state.
    }

    @Override
    public LikeCounter get() {
        return likeCounter;
    }
}
