package com.example.demo.domain;

import com.example.demo.counter.SafeDbCounter;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.IIII_Result;

/**
 * ✅ SafeDbCounter — thread-safe DB-backed counter.
 *
 * Unlike RacyDbCounter, the increment is fully atomic at the DB level:
 *
 *   1. MERGE INTO table ... KEY(pizza) VALUES (?, 0)   ← ensure row exists (no-op if already present)
 *   2. UPDATE table SET votes = votes + 1 WHERE pizza = ?  ← atomic increment, row-level lock
 *
 * H2 (and any ACID-compliant DB) serialises concurrent UPDATEs on the same row
 * using row-level locking — no two threads can read the same value simultaneously.
 * No SELECT before the UPDATE means no race window.
 *
 * Isolation: each @State instance creates its own UUID-suffixed table.
 * @Before resets the table to empty before every iteration.
 *
 * Expected: ONLY correct orderings {1,2}/{2,1} per item — no FORBIDDEN results.
 */
@JCStressTest
@Description("✅ SafeDbCounter — atomic DB increment (UPDATE votes = votes + 1). No lost updates.")
@Outcome(id = "1, 2, 1, 2", expect = Expect.ACCEPTABLE, desc = "Both items correctly incremented.")
@Outcome(id = "2, 1, 1, 2", expect = Expect.ACCEPTABLE, desc = "Both items correctly incremented.")
@Outcome(id = "1, 2, 2, 1", expect = Expect.ACCEPTABLE, desc = "Both items correctly incremented.")
@Outcome(id = "2, 1, 2, 1", expect = Expect.ACCEPTABLE, desc = "Both items correctly incremented.")
@Outcome(expect = Expect.FORBIDDEN, desc = "Should never happen — atomic UPDATE prevents lost updates.")
@State
public class SafeDbCounterStressTest {

    // Each @State instance owns its own isolated table (UUID suffix)
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

