package com.example.demo.domain;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.IIII_Result;

import com.example.demo.counter.distributated.RacyDbCounter;


@JCStressTest
@Description("❌ RacyDbCounter — DB-backed non-atomic read-modify-write. Lost updates guaranteed.")
@Outcome(id = "1, 2, 1, 2", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(id = "2, 1, 1, 2", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(id = "1, 2, 2, 1", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(id = "2, 1, 2, 1", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(expect = Expect.FORBIDDEN, desc = "Race condition: lost update — two actors got the same value.")
@State
public class RacyDbCounterStressTest {
    private final RacyDbCounter counter = new RacyDbCounter();

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
