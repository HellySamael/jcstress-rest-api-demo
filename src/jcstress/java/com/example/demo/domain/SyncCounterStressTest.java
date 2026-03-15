package com.example.demo.domain;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.IIII_Result;

import com.example.demo.counter.jmm.SyncCounter;


@JCStressTest
@Description("✅ synchronized — thread-safe. Correct but potentially slow under contention.")
@Outcome(id = "1, 2, 1, 2", expect = Expect.ACCEPTABLE, desc = "Both items correctly incremented.")
@Outcome(id = "2, 1, 1, 2", expect = Expect.ACCEPTABLE, desc = "Both items correctly incremented.")
@Outcome(id = "1, 2, 2, 1", expect = Expect.ACCEPTABLE, desc = "Both items correctly incremented.")
@Outcome(id = "2, 1, 2, 1", expect = Expect.ACCEPTABLE, desc = "Both items correctly incremented.")
@Outcome(expect = Expect.FORBIDDEN, desc = "Should never happen with synchronized!")
@State
public class SyncCounterStressTest {

    private final SyncCounter counter = new SyncCounter();

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
