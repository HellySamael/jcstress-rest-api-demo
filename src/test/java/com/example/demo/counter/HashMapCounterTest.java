package com.example.demo.counter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.demo.counter.jmm.HashMapCounter;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HashMapCounterTest {

    private HashMapCounter counter;

    @BeforeEach
    void setUp() {
        counter = new HashMapCounter();
    }

    @Test
    @DisplayName("vote increments from 0 to 1 and returns new value")
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
    @DisplayName("getVotes exposes backing map (non defensive copy)")
    void getVotesExposesBackingMap() {
        counter.vote("funghi");
        Map<String, Integer> map = counter.getVotes();
        assertSame(map, counter.getVotes()); // même instance
        assertEquals(1, map.get("funghi"));
        map.put("funghi", 42); // on modifie directement la map interne
        assertEquals(42, counter.getVotesFor("funghi"));
    }

    @Test
    @DisplayName("resetVotes clears all counts")
    void resetVotesClearsCounts() {
        counter.vote("quattro");
        counter.vote("quattro");
        assertEquals(2, counter.getVotesFor("quattro"));
        counter.resetVotes();
        assertEquals(0, counter.getVotesFor("quattro"));
        assertTrue(counter.getVotes().isEmpty());
    }
}