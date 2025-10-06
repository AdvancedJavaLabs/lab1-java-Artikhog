package org.itmo;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;
import org.openjdk.jcstress.infra.results.ZZ_Result;

@JCStressTest
@Outcome(id = "1023", expect = Expect.ACCEPTABLE, desc = "Correct")
@State
public class BFSCorrectnessTest {
    private Graph graph;

    public BFSCorrectnessTest() {
        graph = new Graph(1023);
        graph.generateTree(2);
    }

    @Actor
    public void actor1() {
        graph.parallelBFS(0);
    }

    @Arbiter
    public void arbiter(I_Result r) {
        r.r1 = graph.getVisitedCount();
    }
}