package org.itmo;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import java.util.Random;

@JCStressTest
@Outcome(id = "0", expect = Expect.ACCEPTABLE, desc = "No data races detected")
@Outcome(id = "1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Potential data race detected")
@Outcome(id = "2", expect = Expect.FORBIDDEN, desc = "Data race confirmed")
@State
public class BFSDuplicatedVisitTest {
    private final Graph graph;

    public BFSDuplicatedVisitTest() {
        Random r = new Random(42);
        graph = new RandomGraphGenerator().generateGraph(r, 100_000, 500_000);
    }

    @Actor
    public void actor1() {
        graph.parallelBFSCyclicBarrier(0);
    }

    @Arbiter
    public void arbiter(I_Result r) {
        r.r1 = graph.getDuplicatedVisitsCount();
    }
}