package com.example.demo.counter.jmm;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import com.example.demo.counter.core.PizzaCounter;

import static java.util.stream.Collectors.toMap;


public final class LongAdderCounter implements PizzaCounter {

    private final Map<String, LongAdder> votes = new ConcurrentHashMap<>();

    @Override
    public int vote(String pizza) {
        votes.computeIfAbsent(pizza, key -> new LongAdder()).increment();
        return votes.get(pizza).intValue();
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
