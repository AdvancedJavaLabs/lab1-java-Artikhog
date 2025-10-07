package org.itmo;

import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class ParallelBFSSpeedTestByResource {

    @Test
    public void bfsSpeedTest() throws IOException {
        int vertices = 2_000_000;
        int connections = 10_000_000;
        int[] threadNums = new int[] {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024};
        Random r = new Random(42);
        Graph g = new RandomGraphGenerator().generateGraph(r, vertices, connections);
        try (FileWriter fw = new FileWriter("tmp/thread_test_results.txt")) {
            for (int i = 0; i < threadNums.length; i++) {
                g.setThreadPoolSize(threadNums[i]);
                long parallelTime = executeParallelBfsAndGetTime(g);
                fw.append("Times for " + threadNums[i] + " threads");
                fw.append("\nParallel: " + parallelTime);
                fw.append("\n--------\n");
            }
            fw.flush();
        }
    }

    private long executeParallelBfsAndGetTime(Graph g) {
        long startTime = System.currentTimeMillis();
        g.parallelBFS(0);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
}
