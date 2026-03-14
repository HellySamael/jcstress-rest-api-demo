package com.example.demo.counter.jmm;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import com.example.demo.counter.core.PizzaCounter;

import static java.util.stream.Collectors.toMap;

/**
 * Version 4 of the PizzaCounter implementation using LongAdder.
 * A non thread-safe implementation of PizzaCounter using LongAdder.
 * This class uses a ConcurrentHashMap to manage votes, where each vote count is represented by a LongAdder.
 * The computeIfAbsent method is used to ensure that a LongAdder is created for each pizza flavor when it is first voted for, and the increment method is called to update the vote count atomically.
 * However, since the intValue method is called separately to retrieve the current vote count, there is a potential for a race condition where the returned value may not reflect the most up-to-date count if another thread increments the LongAdder between the increment and intValue calls.
 * Therefore, while the LongAdder provides atomic increments, the overall implementation does not guarantee a consistent view of the vote counts when accessed concurrently by multiple threads.
 */
public final class LongAdderCounter implements PizzaCounter {

    private final Map<String, LongAdder> votes = new ConcurrentHashMap<>();

    @Override
    public int vote(String pizza) {
        // computeIfAbsent guarantees a single LongAdder per key.
        // increment() is atomic on the LongAdder itself, but intValue() is a
        // *separate* read — another thread may increment between the two calls,
        // making the returned value unreliable (TOCTOU race on the return value).
        // ⚠️ LongAdder does NOT provide an atomic incrementAndGet(), so this
        // implementation cannot return a consistent per-vote result without
        // additional synchronisation.
        votes.computeIfAbsent(pizza, key -> new LongAdder()).increment();
        return votes.get(pizza).intValue(); // ⚠️ racy read — may reflect other threads' increments
    }

    @Override
    public Map<String, Integer> getVotes() {
        return Collections.unmodifiableMap(votes.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().intValue())));
    }

    @Override
    public int getVotesFor(String pizza) {
        return votes.getOrDefault(pizza, new LongAdder()).intValue();
    }

    @Override
    public void resetVotes() {
        votes.clear();
    }
}
