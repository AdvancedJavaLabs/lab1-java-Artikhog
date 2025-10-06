package org.itmo;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

@JCStressTest
@Outcome(id = "1", expect = Expect.ACCEPTABLE, desc = "Correct")
@State
public class SimpleTest {

    private int x = 0;

    @Actor
    public void actor1() {
        x = 1;
    }

    @Arbiter
    public void arbiter(I_Result r) {
        r.r1 = x;
    }
}