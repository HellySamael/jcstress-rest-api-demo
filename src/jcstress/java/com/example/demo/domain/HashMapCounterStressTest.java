package com.example.demo.domain;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.IIII_Result;

import com.example.demo.counter.jmm.HashMapCounter;


@JCStressTest
@Description("❌ HashMap — neither atomic nor thread-safe. Demonstrates lost updates.")
@Outcome(id = "1, 2, 1, 2", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(id = "2, 1, 1, 2", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(id = "1, 2, 2, 1", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(id = "2, 1, 2, 1", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(expect = Expect.FORBIDDEN, desc = "Race condition: lost update detected.")
@State
public class HashMapCounterStressTest {

    private final HashMapCounter counter = new HashMapCounter();

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
