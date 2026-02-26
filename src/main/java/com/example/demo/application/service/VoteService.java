package com.example.demo.application.service;

import com.example.demo.application.port.in.VoteUseCase;
import com.example.demo.application.port.out.VoteRepository;
import com.example.demo.domain.VoteCounter;

import java.util.Map;

public class VoteService implements VoteUseCase {

    private final VoteRepository voteRepository;

    public VoteService(VoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }

    @Override
    public int vote(String itemId) {
        VoteCounter voteCounter = voteRepository.get();
        int newCount = voteCounter.vote(itemId);
        voteRepository.save(voteCounter);
        return newCount;
    }

    @Override
    public Map<String, Integer> getVotes() {
        return voteRepository.get().getVotes();
    }

    @Override
    public int getVotesFor(String itemId) {
        return voteRepository.get().getVotesFor(itemId);
    }

    @Override
    public void resetVotes() {
        VoteCounter voteCounter = voteRepository.get();
        voteCounter.reset();
        voteRepository.save(voteCounter);
    }
}
