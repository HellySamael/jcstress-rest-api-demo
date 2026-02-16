package com.example.demo.domain;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

/**
 * A JCStress test for the LikeCounter to expose race conditions.
 */
@JCStressTest
@Outcome(id = "2", expect = Expect.ACCEPTABLE, desc = "Both threads incremented, result is 2.")
@Outcome(id = "1", expect = Expect.FORBIDDEN, desc = "One increment was lost, result is 1 (race condition).")
@State
public class LikeCounterStressTest {

    private final LikeCounter counter = new LikeCounter();

    @Actor
    public void actor1() {
        counter.increment();
    }

    @Actor
    public void actor2() {
        counter.increment();
    }

    @Arbiter
    public void arbiter(I_Result r) {
        r.r1 = counter.getCount();
    }
}
