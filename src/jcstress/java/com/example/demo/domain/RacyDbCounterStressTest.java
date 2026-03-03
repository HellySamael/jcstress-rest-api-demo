package com.example.demo.domain;

import com.example.demo.counter.RacyDbCounter;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.IIII_Result;

/**
 * ❌ RacyDbCounter — NOT thread-safe: DB-backed read-modify-write without locking.
 *
 * The vote() method performs three separate, non-atomic steps:
 *
 *   1. SELECT votes FROM table WHERE pizza = ?          ← read
 *   2. Thread.sleep(random 1-5ms)                       ← widens the race window
 *   3. MERGE INTO table (pizza, votes) VALUES (?, ?)    ← write back current+1
 *
 * Two threads can both read the same value (e.g. 0), both compute next=1,
 * and both write 1 back — one increment is silently lost.
 *
 * Isolation: each @State instance creates its own table (UUID-suffixed) so that
 * concurrent JCStress workers never share state across iterations.
 * @Before resets the table to empty before every iteration.
 *
 * Expected: FORBIDDEN results (e.g. both actors on item1 both return 1).
 */
@JCStressTest
@Description("❌ RacyDbCounter — DB-backed non-atomic read-modify-write. Lost updates guaranteed.")
@Outcome(id = "1, 2, 1, 2", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(id = "2, 1, 1, 2", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(id = "1, 2, 2, 1", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(id = "2, 1, 2, 1", expect = Expect.ACCEPTABLE, desc = "Correct result — actors serialised by luck.")
@Outcome(expect = Expect.FORBIDDEN, desc = "Race condition: lost update — two actors got the same value.")
@State
public class RacyDbCounterStressTest {

    // Each @State instance owns its own isolated table (UUID suffix)
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
