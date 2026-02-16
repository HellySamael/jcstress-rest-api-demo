package com.example.demo.application.port.out;

import com.example.demo.domain.LikeCounter;

public interface LikeRepository {
    void save(LikeCounter likeCounter);
    LikeCounter get();
}
