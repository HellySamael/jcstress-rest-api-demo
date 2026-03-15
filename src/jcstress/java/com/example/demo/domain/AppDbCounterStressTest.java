package com.example.demo.domain;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Description;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.IIII_Result;

import com.example.demo.counter.distributated.AppDbCounter;

@JCStressTest
@Description("AppDbCounter DB behavior")
@Outcome(id = "1, 2, 1, 2", expect = Expect.ACCEPTABLE, desc = "Correct ordering")
@Outcome(id = "2, 1, 1, 2", expect = Expect.ACCEPTABLE, desc = "Correct ordering")
@Outcome(id = "1, 2, 2, 1", expect = Expect.ACCEPTABLE, desc = "Correct ordering")
@Outcome(id = "2, 1, 2, 1", expect = Expect.ACCEPTABLE, desc = "Correct ordering")
@Outcome(expect = Expect.FORBIDDEN, desc = "Lost update")
@State
public class AppDbCounterStressTest {

    private final AppDbCounter counter = new AppDbCounter();

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
