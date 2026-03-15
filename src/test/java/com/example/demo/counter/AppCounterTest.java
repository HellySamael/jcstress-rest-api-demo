package com.example.demo.counter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.counter.jmm.AppCounter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppCounterTest {

    private AppCounter counter;

    @BeforeEach
    void setUp() {
        counter = new AppCounter();
    }

    @Test
    void voteIncrementsAndReturnsNewValue() {
        int v1 = counter.vote("margherita");
        int v2 = counter.vote("margherita");
        assertEquals(1, v1);
        assertEquals(2, v2);
        assertEquals(2, counter.getVotesFor("margherita"));
    }

    @Test
    void getVotesForUnknownPizzaReturnsZero() {
        assertEquals(0, counter.getVotesFor("pepperoni"));
    }

    @Test
    void resetVotesClearsCounts() {
        counter.vote("quattro");
        counter.vote("quattro");
        assertEquals(2, counter.getVotesFor("quattro"));
        counter.resetVotes();
        assertEquals(0, counter.getVotesFor("quattro"));
        assertTrue(counter.getVotes().isEmpty());
    }
}
