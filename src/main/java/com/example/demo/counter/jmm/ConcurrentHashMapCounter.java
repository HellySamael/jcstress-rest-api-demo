package com.example.demo.counter.jmm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.demo.counter.core.PizzaCounter;


public final class ConcurrentHashMapCounter implements PizzaCounter {

    private final Map<String, Integer> votes = new ConcurrentHashMap<>();

    @Override
    public int vote(String pizza) {
        votes.putIfAbsent(pizza, 0);
        votes.put(pizza, votes.get(pizza) + 1);
        return votes.get(pizza);
    }

    @Override
    public Map<String, Integer> getVotes() {
        return votes;
    }

    @Override
    public int getVotesFor(String pizza) {
        return votes.getOrDefault(pizza, 0);
    }

    @Override
    public void resetVotes() {
        votes.clear();
    }
}
