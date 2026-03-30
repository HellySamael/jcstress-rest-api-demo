package com.example.demo.domain;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Description;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;

import com.example.demo.counter.distributed.AppDbCounter;

@JCStressTest
@Description("AppDbCounter DB behavior")
@Outcome(id = "1, 2", expect = Expect.ACCEPTABLE, desc = "Correct ordering")
@Outcome(id = "2, 1", expect = Expect.ACCEPTABLE, desc = "Correct ordering")
@Outcome(expect = Expect.FORBIDDEN, desc = "Lost update")
@State
public class AppDbCounterStressTest {

    private final AppDbCounter counter = new AppDbCounter();

    @Actor
    public void actor1(II_Result r) {
        r.r1 = counter.vote("pizza1");
    }

    @Actor
    public void actor2(II_Result r) {
        r.r2 = counter.vote("pizza1");
    }


}
