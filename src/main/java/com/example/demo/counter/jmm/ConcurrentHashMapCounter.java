package com.example.demo.counter.jmm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.demo.counter.core.PizzaCounter;

/**
 * Version 3 of the PizzaCounter implementation using ConcurrentHashMap.
 * A non thread-safe implementation of PizzaCounter using ConcurrentHashMap.
 * This class uses a ConcurrentHashMap to manage votes,
 * which allows for concurrent updates without the need for explicit synchronization.
 * The putIfAbsent and compute methods are used to ensure that updates to the vote counts are atomic and thread-safe,
 * but this not prevent race conditions.
 */
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
