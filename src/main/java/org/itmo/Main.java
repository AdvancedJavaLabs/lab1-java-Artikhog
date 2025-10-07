package org.itmo;

public class Main {
    public static void main(String[] args) {
        Graph graph = new Graph(1023);
        graph.generateTree(2);
        graph.parallelBFS(0);
        System.out.println("Visited count: " + graph.getVisitedCount());

        boolean allVisited = true;
        for (int i = 0; i < 5; i++) {
            if (!graph.isVisited(i)) {
                allVisited = false;
                break;
            }
        }
        System.out.println(allVisited);
    }
}
