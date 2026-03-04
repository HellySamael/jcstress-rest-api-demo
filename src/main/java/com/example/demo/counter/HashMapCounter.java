package com.example.demo.counter;

import java.util.HashMap;
import java.util.Map;

/**
 * Version 1 of the PizzaCounter implementation using HashMap.
 * A non-thread-safe implementation of PizzaCounter using HashMap.
 * This class is intentionally not thread-safe to demonstrate race conditions when accessed by multiple threads concurrently.
 */
public final class HashMapCounter implements PizzaCounter {

    private final HashMap<String, Integer> votes = new HashMap<>();

    @Override
    public int vote(String pizza) {
        Integer v = votes.get(pizza);
        votes.put(pizza, v == null ? 1 : v + 1);
        return  votes.get(pizza);
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
