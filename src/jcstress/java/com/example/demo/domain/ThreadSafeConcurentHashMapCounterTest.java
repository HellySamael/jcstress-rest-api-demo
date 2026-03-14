package com.example.demo.domain;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.IIII_Result;

import com.example.demo.counter.jmm.ThreadSafeConcurentHashMapCounter;

/**
 * ✅ ThreadSafeConcurrentHashMapCounter — thread-safe via ConcurrentHashMap.merge().
 *
 * ConcurrentHashMap.merge() is an ATOMIC operation: it combines the read,
 * the computation, and the write into a single indivisible step.
 * No race condition is possible on the counter.
 *
 * This is the recommended solution: both correct AND performant.
 * Unlike synchronized, independent items (item1 vs item2) do not contend.
 *
 * Expected: ONLY {1,2} / {2,1} orderings for each item → all ACCEPTABLE.
 */
@JCStressTest
@Description("✅ ConcurrentHashMap.merge() — atomic and performant. The right solution.")
@Outcome(id = "1, 2, 1, 2", expect = Expect.ACCEPTABLE, desc = "Both items correctly incremented.")
@Outcome(id = "2, 1, 1, 2", expect = Expect.ACCEPTABLE, desc = "Both items correctly incremented.")
@Outcome(id = "1, 2, 2, 1", expect = Expect.ACCEPTABLE, desc = "Both items correctly incremented.")
@Outcome(id = "2, 1, 2, 1", expect = Expect.ACCEPTABLE, desc = "Both items correctly incremented.")
@Outcome(expect = Expect.FORBIDDEN, desc = "Should never happen with merge()!")
@State
public class ThreadSafeConcurentHashMapCounterTest {

    private final ThreadSafeConcurentHashMapCounter counter = new ThreadSafeConcurentHashMapCounter();

    @Actor
    public void actor1(IIII_Result r) {
        r.r1 = counter.vote("item1");
    }

    @Actor
    public void actor2(IIII_Result r) {
        r.r2 = counter.vote("item1");
    }

    @Actor
    public void actor3(IIII_Result r) {
        r.r3 = counter.vote("item2");
    }

    @Actor
    public void actor4(IIII_Result r) {
        r.r4 = counter.vote("item2");
    }
}
