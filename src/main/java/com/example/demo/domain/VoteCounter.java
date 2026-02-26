package com.example.demo.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages votes for different items (e.g., photos).
 * This implementation is intentionally not thread-safe to demonstrate race conditions.
 */
public class VoteCounter {

    private final Map<String, Integer> votes = new HashMap<>();

    /**
     * Increments the vote count for a given item ID.
     * This method has a check-then-act race condition.
     * When two threads call it simultaneously for the same itemId, they might both read the
     * same initial value, and one of the increments will be lost.
     */
    public int vote(String itemId) {
        Integer count = votes.get(itemId);
        if (count == null) {
            count = 0;
        }
        // The race condition happens here:
        // Thread A reads count (e.g., 0).
        // Thread B reads count (e.g., 0).
        // Thread A calculates newCount as 1.
        // Thread B calculates newCount as 1.
        int newCount = count + 1;
        votes.put(itemId, newCount);
        // Thread A returns 1.
        // Thread B returns 1. The correct result for one of them should have been 2.
        return newCount;
    }

    public Map<String, Integer> getVotes() {
        return new HashMap<>(votes); // Return a copy for safety
    }

    public int getVotesFor(String itemId) {
        return votes.getOrDefault(itemId, 0);
    }

    public void reset() {
        votes.clear();
    }
}
