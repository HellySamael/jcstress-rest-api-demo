package com.example.demo.counter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.demo.counter.jmm.LongAdderCounter;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LongAdderCounterTest {
    private LongAdderCounter counter;

    @BeforeEach
    void setUp() {
        counter = new LongAdderCounter();
    }

    @Test
    @DisplayName("vote increments using ConcurrentHashMap and returns new value")
    void voteIncrementsAndReturnsNewValue() {
        int v1 = counter.vote("margherita");
        int v2 = counter.vote("margherita");
        // Attention : impl actuelle: putIfAbsent + put non atomique, mais en mono-thread ça doit marcher
        assertEquals(1, v1);
        assertEquals(2, v2);
        assertEquals(2, counter.getVotesFor("margherita"));
    }

    @Test
    @DisplayName("getVotesFor returns 0 for unknown pizza")
    void getVotesForUnknownPizzaReturnsZero() {
        assertEquals(0, counter.getVotesFor("pepperoni"));
    }

    @Test
    @DisplayName("getVotes returns underlying ConcurrentHashMap")
    void getVotesReturnsUnderlyingMap() {
        counter.vote("funghi");
        Map<String, Integer> map = counter.getVotes();
        assertSame(map, counter.getVotes());
        assertEquals(1, map.get("funghi"));
    }

    @Test
    @DisplayName("resetVotes clears all counts in ConcurrentHashMap")
    void resetVotesClearsCounts() {
        counter.vote("quattro");
        counter.vote("quattro");
        assertEquals(2, counter.getVotesFor("quattro"));
        counter.resetVotes();
        assertEquals(0, counter.getVotesFor("quattro"));
        assertTrue(counter.getVotes().isEmpty());
    }
}
