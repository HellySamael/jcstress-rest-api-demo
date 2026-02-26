package com.example.demo.application.port.in;

import java.util.Map;

public interface VoteUseCase {
    int vote(String itemId);
    Map<String, Integer> getVotes();
    int getVotesFor(String itemId);
    void resetVotes();
}
