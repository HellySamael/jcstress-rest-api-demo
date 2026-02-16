package com.example.demo;

import com.example.demo.application.port.in.LikeUseCase;
import com.example.demo.application.port.out.LikeRepository;
import com.example.demo.application.service.LikeService;
import com.example.demo.adapter.out.persistence.InMemoryLikeRepository;
import com.example.demo.domain.LikeCounter; // Added import for LikeCounter

import dagger.Binds;
import dagger.Module;
import dagger.Provides; // Added import for Provides
import jakarta.inject.Singleton;

@Module
public abstract class AppModule {

    @Binds
    @Singleton
    abstract LikeUseCase bindLikeUseCase(LikeService likeService);

    @Binds
    @Singleton
    abstract LikeRepository bindLikeRepository(InMemoryLikeRepository inMemoryLikeRepository);

    // Explicitly provide LikeCounter as a singleton
    @Provides
    @Singleton
    static LikeCounter provideLikeCounter() {
        return new LikeCounter();
    }
}
