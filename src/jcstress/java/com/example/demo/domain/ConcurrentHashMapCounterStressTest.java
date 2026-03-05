package com.example.demo.domain;

import com.example.demo.counter.ConcurrentHashMapCounter;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.IIII_Result;

/**
 * ❌ ConcurrentHashMapCounter — misleading: NOT thread-safe despite ConcurrentHashMap.
 *
 * ConcurrentHashMap guarantees safety for individual operations (get, put),
 * but the compound sequence get() + put() is NOT atomic:
 *
 *   votes.putIfAbsent(pizza, 0);
 *   votes.put(pizza, votes.get(pizza) + 1);  // get() and put() are two separate steps!
 *
 * Two threads can both call get() and read the same value before either calls put(),
 * so one increment is lost (classic ABA / lost-update problem).
 *
 * Expected: FORBIDDEN results (e.g. both actors on item1 return 1).
 */
@JCStressTest
@Description("❌ ConcurrentHashMap get()+put() — compound operation is NOT atomic.")
@Outcome(id = "1, 2, 1, 2", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(id = "2, 1, 1, 2", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(id = "1, 2, 2, 1", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(id = "2, 1, 2, 1", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(expect = Expect.FORBIDDEN, desc = "Race condition: get()+put() not atomic → lost update.")
@State
public class ConcurrentHashMapCounterStressTest {

    private final ConcurrentHashMapCounter counter = new ConcurrentHashMapCounter();

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
