package com.example.demo.domain;

import com.example.demo.counter.SafeDbCounter;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.IIII_Result;

/**
 * ✅ SafeDbCounter — thread-safe DB-backed counter, 100% SQL solution.
 *
 * Unlike RacyDbCounter, concurrency is handled entirely by the database:
 *
 *   1. SELECT votes FROM table WHERE pizza = ? FOR UPDATE
 *      — Acquires an exclusive pessimistic row lock (PostgreSQL).
 *        Concurrent transactions on the same row BLOCK until COMMIT.
 *
 *   2. UPDATE table SET votes = votes + 1 WHERE pizza = ?
 *      — Atomic increment under the lock; no lost updates possible.
 *
 *   3. COMMIT — releases the lock.
 *
 * Isolation: each @State instance creates its own UUID-suffixed table
 * via SafeDbCounter() — workers never share state across iterations.
 *
 * @Before seeds "item1" and "item2" at 0 before every iteration so that
 * SELECT FOR UPDATE always finds an existing row to lock.
 *
 * Expected: ONLY correct orderings {1,2}/{2,1} per item — no FORBIDDEN results.
 */
@JCStressTest
@Description("✅ SafeDbCounter — SELECT FOR UPDATE + UPDATE. Pessimistic locking, no lost updates.")
@Outcome(id = "1, 2, 1, 2", expect = Expect.ACCEPTABLE, desc = "Both items correctly incremented.")
@Outcome(id = "2, 1, 1, 2", expect = Expect.ACCEPTABLE, desc = "Both items correctly incremented.")
@Outcome(id = "1, 2, 2, 1", expect = Expect.ACCEPTABLE, desc = "Both items correctly incremented.")
@Outcome(id = "2, 1, 2, 1", expect = Expect.ACCEPTABLE, desc = "Both items correctly incremented.")
@Outcome(expect = Expect.FORBIDDEN, desc = "Should never happen — pessimistic lock prevents lost updates.")
@State
public class SafeDbCounterStressTest {

    // Each @State instance owns its own isolated UUID-suffixed table
    private final SafeDbCounter counter = new SafeDbCounter();

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
