package com.example.demo.counter.jmm;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.demo.counter.core.PizzaCounter;


public final class ThreadSafeConcurentHashMapCounter implements PizzaCounter {

    private final Map<String, Integer> votes = new ConcurrentHashMap<>();

    @Override
    public int vote(String pizza) {
        return votes.merge(pizza, 1, Integer::sum);
    }

    @Override
    public Map<String, Integer> getVotes() {
        return Collections.unmodifiableMap(votes);
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
