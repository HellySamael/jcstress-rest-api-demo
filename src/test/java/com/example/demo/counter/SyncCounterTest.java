package com.example.demo.counter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SyncCounterTest {
    private SyncCounter counter;

    @BeforeEach
    void setUp() {
        counter = new SyncCounter();
    }

    @Test
    @DisplayName("vote increments count in synchronized method and returns new value")
    void voteIncrementsAndReturnsNewValue() {
        int v1 = counter.vote("margherita");
        int v2 = counter.vote("margherita");
        assertEquals(1, v1);
        assertEquals(2, v2);
        assertEquals(2, counter.getVotesFor("margherita"));
    }

    @Test
    @DisplayName("getVotesFor returns 0 for unknown pizza in synchronized context")
    void getVotesForUnknownPizzaReturnsZero() {
        assertEquals(0, counter.getVotesFor("pepperoni"));
    }

    @Test
    @DisplayName("getVotes returns backing map (still synchronized access)")
    void getVotesReturnsBackingMap() {
        counter.vote("funghi");
        Map<String, Integer> map = counter.getVotes();
        assertSame(map, counter.getVotes());
        assertEquals(1, map.get("funghi"));
    }

    @Test
    @DisplayName("resetVotes clears all counts in synchronized method")
    void resetVotesClearsCounts() {
        counter.vote("quattro");
        counter.vote("quattro");
        assertEquals(2, counter.getVotesFor("quattro"));
        counter.resetVotes();
        assertEquals(0, counter.getVotesFor("quattro"));
        assertTrue(counter.getVotes().isEmpty());
    }
}