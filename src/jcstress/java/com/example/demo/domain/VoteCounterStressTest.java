package com.example.demo.domain;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.IIII_Result;

@JCStressTest
@Description("Tests race conditions with 4 actors and multiple items.")
@Outcome(id = "1, 2, 1, 2", expect = Expect.ACCEPTABLE, desc = "Both items updated correctly.")
@Outcome(expect = Expect.FORBIDDEN, desc = "Race condition detected!")
@State
public class VoteCounterStressTest {

    private final VoteCounter voteCounter = new VoteCounter();

    @Actor
    public void actor1(IIII_Result r) {
        r.r1 = voteCounter.vote("item1");
    }

    @Actor
    public void actor2(IIII_Result r) {
        r.r2 = voteCounter.vote("item1");
    }

    @Actor
    public void actor3(IIII_Result r) {
        r.r3 = voteCounter.vote("item2");
    }

    @Actor
    public void actor4(IIII_Result r) {
        r.r4 = voteCounter.vote("item2");
    }
}
