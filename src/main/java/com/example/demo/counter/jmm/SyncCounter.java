package com.example.demo.counter.jmm;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.counter.core.PizzaCounter;


public final class SyncCounter implements PizzaCounter {
    private final HashMap<String, Integer> votes = new HashMap<>();

    @Override
    public synchronized int vote(String pizza) {
        Integer v = votes.get(pizza);
        votes.put(pizza, v == null ? 1 : v + 1);
        return votes.get(pizza);
    }

    @Override
    public synchronized Map<String, Integer> getVotes() {
        return votes;
    }

    @Override
    public synchronized int getVotesFor(String pizza) {
        return votes.getOrDefault(pizza, 0);
    }

    @Override
    public synchronized void resetVotes() {
        votes.clear();
    }
}
