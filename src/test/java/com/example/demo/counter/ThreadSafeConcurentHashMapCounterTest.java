package com.example.demo.counter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThreadSafeConcurentHashMapCounterTest {
    private ThreadSafeConcurentHashMapCounter counter;

    @BeforeEach
    void setUp() {
        counter = new ThreadSafeConcurentHashMapCounter();
    }

    @Test
    @DisplayName("vote uses merge and returns incremented value")
    void voteIncrementsAndReturnsNewValue() {
        int v1 = counter.vote("margherita");
        int v2 = counter.vote("margherita");
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
    @DisplayName("getVotes returns unmodifiable view of internal map")
    void getVotesReturnsUnmodifiableMap() {
        counter.vote("funghi");
        Map<String, Integer> map = counter.getVotes();
        assertEquals(1, map.get("funghi"));
        assertThrows(UnsupportedOperationException.class, () -> map.put("funghi", 10));
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
