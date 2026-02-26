package com.example.demo.application.port.out;

import com.example.demo.domain.VoteCounter;

public interface VoteRepository {
    VoteCounter get();
    void save(VoteCounter voteCounter);
}
