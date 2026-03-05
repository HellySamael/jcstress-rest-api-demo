package com.example.demo.counter;

import java.util.Map;

public interface PizzaCounter {
    int vote(String pizza);

    Map<String, Integer> getVotes();

    int getVotesFor(String pizza);

    void resetVotes();
}
