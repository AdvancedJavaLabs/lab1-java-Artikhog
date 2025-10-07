package org.itmo;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;

class Graph {
    private final int V;
    private final ArrayList<Integer>[] adjList;
    private int threadPoolSize;
    private AtomicIntegerArray visited;

    Graph(int vertices) {
        this(vertices, Runtime.getRuntime().availableProcessors());
    }

    Graph(int vertices, int threadPoolSize) {
        this.V = vertices;
        this.threadPoolSize = threadPoolSize;
        this.visited = new AtomicIntegerArray(vertices);
        adjList = new ArrayList[vertices];
        for (int i = 0; i < vertices; ++i) {
            adjList[i] = new ArrayList<>();
        }
    }

    void addEdge(int src, int dest) {
        if (!adjList[src].contains(dest)) {
            adjList[src].add(dest);
        }
    }

    void parallelBFS(int startVertex) {
        this.visited = new AtomicIntegerArray(V);
        ConcurrentLinkedQueue<Integer> currentLevelQueue = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Integer> nextLevelQueue = new ConcurrentLinkedQueue<>();

        visited.set(startVertex, 1);
        currentLevelQueue.add(startVertex);

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        try {
            int level = 0;

            while (!currentLevelQueue.isEmpty()) {
                System.out.println("Processing level " + level + ", verteces: " + currentLevelQueue.size());
                // футуры текущего уровня
                List<Future<?>> futures = new ArrayList<>();
                // запускаем на каждый поток задание на посещение вершин пока очередь не опустеет
                for (int i = 0; i < threadPoolSize; i++) {
                    ConcurrentLinkedQueue<Integer> finalCurrentLevelQueue = currentLevelQueue;
                    ConcurrentLinkedQueue<Integer> finalNextLevelQueue = nextLevelQueue;
                    futures.add(executor.submit(() -> {
                        try {
                            Integer vertex;
                            while ((vertex = finalCurrentLevelQueue.poll()) != null) {
                                for (int neighbor : adjList[vertex]) {
                                    if (visited.compareAndSet(neighbor, 0, 1)) {
                                        finalNextLevelQueue.add(neighbor);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Thread.currentThread().interrupt();
                        }
                    }));
                }

                // Ждем завершения уровня
                for (Future<?> future : futures) {
                    future.get();
                }

                // Меняем очереди местами
                currentLevelQueue = nextLevelQueue;
                nextLevelQueue = new ConcurrentLinkedQueue<>();
                level++;
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
    }

    void parallelBFSCyclicBarrier(int startVertex) {
        this.visited = new AtomicIntegerArray(V);
        AtomicReference<ConcurrentLinkedQueue<Integer>> currentLevelQueue = new AtomicReference<>(new ConcurrentLinkedQueue<>());
        AtomicReference<ConcurrentLinkedQueue<Integer>> nextLevelQueue = new AtomicReference<>(new ConcurrentLinkedQueue<>());

        visited.set(startVertex, 1);
        currentLevelQueue.get().add(startVertex);

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        // Синхронизация на барьере при достижении барьера меняем местами очереди
        CyclicBarrier levelBarrier = new CyclicBarrier(threadPoolSize, () -> {
            currentLevelQueue.set(nextLevelQueue.get());
            nextLevelQueue.set(new ConcurrentLinkedQueue<>());
        });

        try {
            while (!currentLevelQueue.get().isEmpty()) {
                // запускаем на каждый поток задание на посещение вершин пока очередь не опустеет
                for (int i = 0; i < threadPoolSize; i++) {
                    ConcurrentLinkedQueue<Integer> finalCurrentLevelQueue = currentLevelQueue.get();
                    ConcurrentLinkedQueue<Integer> finalNextLevelQueue = nextLevelQueue.get();
                    executor.submit(() -> {
                        try {
                            Integer vertex;
                            while ((vertex = finalCurrentLevelQueue.poll()) != null) {
                                for (int neighbor : adjList[vertex]) {
                                    if (visited.compareAndSet(neighbor, 0, 1)) {
                                        finalNextLevelQueue.add(neighbor);
                                    }
                                }
                            }
                            // синхронизация на барьере
                            levelBarrier.await();
                        } catch (Exception e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                }
            }
        } finally {
            executor.shutdown();
        }
    }

    //Generated by ChatGPT
    void bfs(int startVertex) {
        boolean[] visited = new boolean[V];

        LinkedList<Integer> queue = new LinkedList<>();

        visited[startVertex] = true;
        queue.add(startVertex);

        while (!queue.isEmpty()) {
            startVertex = queue.poll();

            for (int n : adjList[startVertex]) {
                if (!visited[n]) {
                    visited[n] = true;
                    queue.add(n);
                }
            }
        }
    }

    // генерирует дерево с заданным ветвлением
    void generateTree(int branchingFactor) {
        for (int i = 0; i < V; i++) {
            adjList[i].clear();
        }

        Queue<Integer> queue = new LinkedList<>();
        int nextVertex = 1;

        queue.add(0); // корень 0

        while (!queue.isEmpty() && nextVertex < V) {
            int current = queue.poll();
            // генерируем детей по количеству branchFactor
            for (int i = 0; i < branchingFactor && nextVertex < V; i++) {
                addEdge(current, nextVertex);
                queue.add(nextVertex);
                nextVertex++;
            }
        }
    }
    // вывод для визуализации в GraphViz
    void visualizeAsGraphViz() {
        for (int i = 0; i < V; i++) {
            if (!adjList[i].isEmpty()) {
                for (int neighbor : adjList[i]) {
                    System.out.println("  \"" + i + "\" -> \"" + neighbor + "\"");
                }
            }
        }
    }

    void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public boolean isVisited(int vertex) {
        return visited.get(vertex) >= 1;
    }

    public int getVisitedCount() {
        int count = 0;
        for (int i = 0; i < V; i++) {
            if (visited.get(i) == 1) count++;
        }
        return count;
    }

    public int getDuplicatedVisitsCount() {
        int duplicatedVisitsCount = 0;
        for (int i = 0; i < V; i++) {
            if (visited.get(i) > 1) {
                duplicatedVisitsCount++;
            }
        }
        return duplicatedVisitsCount;
    }

    public int getNotVisitedCount() {
        int notVisitedCount = 0;
        for (int i = 0; i < V; i++) {
            if (visited.get(i) == 0) {
                notVisitedCount++;
            }
        }
        return notVisitedCount;
    }
}
