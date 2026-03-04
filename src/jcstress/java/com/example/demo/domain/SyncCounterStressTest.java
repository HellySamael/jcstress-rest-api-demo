package com.example.demo.domain;

import com.example.demo.counter.SyncCounter;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.IIII_Result;

/**
 * ✅ SyncCounter — thread-safe via synchronized.
 *
 * Every method is declared synchronized: only one thread at a time can execute
 * vote(). The entire read-modify-write is protected by the instance monitor
 * → no race condition possible.
 *
 * Trade-off: high contention under heavy load (all threads block each other),
 * but correctness is guaranteed.
 *
 * Expected: ONLY {1,2} / {2,1} orderings for each item → all ACCEPTABLE.
 * Any other result would be a JCStress bug or JVM anomaly.
 */
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
