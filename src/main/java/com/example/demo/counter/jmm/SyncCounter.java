package com.example.demo.counter.jmm;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.counter.core.PizzaCounter;

/**
 * Version 2 of the PizzaCounter implementation using synchronized methods.
 * A thread-safe implementation of PizzaCounter using synchronized methods.
 * Slow but thread-safe implementation of PizzaCounter using HashMap.
 * This class uses synchronization to ensure that only one thread can access the critical sections of code that modify the votes HashMap at a time, preventing race conditions and ensuring thread safety.
 */
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
