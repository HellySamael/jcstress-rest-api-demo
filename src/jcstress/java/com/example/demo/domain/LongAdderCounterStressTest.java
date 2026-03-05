package com.example.demo.domain;

import com.example.demo.counter.LongAdderCounter;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.IIII_Result;

/**
 * ❌ LongAdderCounter — deceptive: increment is atomic but the return value is NOT.
 *
 * LongAdder is designed for high-contention counting, but the implementation here
 * separates the increment from reading the result:
 *
 *   votes.computeIfAbsent(pizza, k -> new LongAdder()).increment(); // atomic ✅
 *   return votes.get(pizza).intValue();                             // separate read ❌
 *
 * Between increment() and intValue(), another thread may have already incremented
 * the same counter, so two actors can both observe the same value (e.g. both return 2).
 *
 * Expected: FORBIDDEN results — two actors on the same item returning the same value.
 */
@JCStressTest
@Description("❌ LongAdder increment()+intValue() — increment is atomic, returned value is NOT.")
@Outcome(id = "1, 2, 1, 2", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(id = "2, 1, 1, 2", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(id = "1, 2, 2, 1", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(id = "2, 1, 2, 1", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(expect = Expect.FORBIDDEN, desc = "Race condition: increment() and intValue() are not atomic together.")
@State
public class LongAdderCounterStressTest {

    private final LongAdderCounter counter = new LongAdderCounter();

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
