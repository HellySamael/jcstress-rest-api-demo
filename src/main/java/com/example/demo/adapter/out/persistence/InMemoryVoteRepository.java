package com.example.demo.adapter.out.persistence;

import com.example.demo.application.port.out.VoteRepository;
import com.example.demo.domain.VoteCounter;

public class InMemoryVoteRepository implements VoteRepository {

    private final VoteCounter voteCounter;

    public InMemoryVoteRepository(VoteCounter voteCounter) {
        this.voteCounter = voteCounter;
    }

    @Override
    public VoteCounter get() {
        return this.voteCounter;
    }

    @Override
    public void save(VoteCounter voteCounter) {
        // In this in-memory implementation, the state is shared, so a save is a no-op.
    }
}
