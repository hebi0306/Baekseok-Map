package com.example.myfirstgooglemap;

import android.content.Context;
import android.util.Log;
import java.io.*;
import java.util.*;

/**
 * 다익스트라 알고리즘을 구현한 클래스
 * 두 지점 간의 최단 경로를 계산하고, 이동 거리와 시간을 제공합니다.
 */
public class Dijkstra {
    /** 무한대를 나타내는 값 */
    private static final int INFINITY = 1000000;
    
    /** 전체 노드 수 */
    private static final int TOTAL_NODES = 139;
    
    /** 평균 보행 속도 (m/s) */
    private static final double AVERAGE_WALKING_SPEED = 1.1;
    
    /** 코너 포인트 거리 가중치 */
    private static final double CORNER_WEIGHT = 1.2;

    private int startNode;
    private int endNode;
    private String pathNode;
    private int meter;
    private int travelTime;
    private int minutes;
    private int seconds;
    private int[] previous; // 경로 추적을 위한 배열

    // 싱글톤 패턴
    private static class Singleton {
        private static final Dijkstra INSTANCE = new Dijkstra();
    }

    private Dijkstra() {
        previous = new int[TOTAL_NODES];
    }

    /**
     * Dijkstra 인스턴스를 반환합니다.
     * 
     * @param start 시작 노드
     * @param end 도착 노드
     * @return Dijkstra 인스턴스
     */
    public static Dijkstra getInstance(int start, int end) {
        Singleton.INSTANCE.startNode = start;
        Singleton.INSTANCE.endNode = end;
        return Singleton.INSTANCE;
    }

    /**
     * 다익스트라 알고리즘을 실행하여 최단 경로를 계산합니다.
     * 
     * @param context 컨텍스트
     * @param disabled 계단 사용 여부
     * @param vertex 정점 목록
     * @throws IOException 파일 읽기 오류 발생 시
     */
    public void calculateShortestPath(Context context, boolean disabled, ArrayList<Vertex> vertex) throws IOException {
        int[][] adjMatrix = initializeAdjacencyMatrix();
        loadEdges(context, disabled, vertex, adjMatrix);
        
        int[] distance = new int[TOTAL_NODES];
        boolean[] visited = new boolean[TOTAL_NODES];
        
        initializeDijkstra(distance, visited);
        runDijkstra(adjMatrix, distance, visited);
        
        saveResults(distance);
    }

    private int[][] initializeAdjacencyMatrix() {
        int[][] adjMatrix = new int[TOTAL_NODES][TOTAL_NODES];
        for (int i = 0; i < TOTAL_NODES; i++) {
            Arrays.fill(adjMatrix[i], INFINITY);
            adjMatrix[i][i] = 0;
        }
        return adjMatrix;
    }

    private void loadEdges(Context context, boolean disabled, ArrayList<Vertex> vertex, int[][] adjMatrix) throws IOException {
        int resourceId = disabled ? R.raw.path_with_stairs : R.raw.path_without_stairs;
        try (InputStream is = context.getResources().openRawResource(resourceId);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue;
                
                String[] parts = line.split(" ");
                if (parts.length < 2) continue;
                
                int start = Integer.parseInt(parts[0]);
                int end = Integer.parseInt(parts[1]);
                
                int startIdx = findVertexIndex(vertex, start);
                int endIdx = findVertexIndex(vertex, end);
                
                if (startIdx >= 0 && endIdx >= 0) {
                    int cost = calculateEdgeCost(vertex.get(startIdx), vertex.get(endIdx));
                    adjMatrix[startIdx][endIdx] = cost;
                    adjMatrix[endIdx][startIdx] = cost;
                }
            }
        }
    }

    private int findVertexIndex(ArrayList<Vertex> vertex, int id) {
        for (int i = 0; i < vertex.size(); i++) {
            if (vertex.get(i).id == id) return i;
        }
        return -1;
    }

    private int calculateEdgeCost(Vertex v1, Vertex v2) {
        int cost = v1.calDistance(v2);
        if (v1.id > 22 || v2.id > 22) {
            cost = (int)(cost * CORNER_WEIGHT);
        }
        return cost;
    }

    private void initializeDijkstra(int[] distance, boolean[] visited) {
        Arrays.fill(distance, INFINITY);
        Arrays.fill(visited, false);
        Arrays.fill(previous, -1);
        distance[startNode] = 0;
    }

    private void runDijkstra(int[][] adjMatrix, int[] distance, boolean[] visited) {
        for (int i = 0; i < TOTAL_NODES; i++) {
            int u = findMinDistanceNode(distance, visited);
            if (u == -1) break;
            
            visited[u] = true;
            updateNeighbors(u, adjMatrix, distance, visited);
        }
    }

    private int findMinDistanceNode(int[] distance, boolean[] visited) {
        int min = INFINITY;
        int minIndex = -1;
        
        for (int i = 0; i < TOTAL_NODES; i++) {
            if (!visited[i] && distance[i] < min) {
                min = distance[i];
                minIndex = i;
            }
        }
        return minIndex;
    }

    private void updateNeighbors(int u, int[][] adjMatrix, int[] distance, boolean[] visited) {
        for (int v = 0; v < TOTAL_NODES; v++) {
            if (!visited[v] && adjMatrix[u][v] != INFINITY) {
                int newDistance = distance[u] + adjMatrix[u][v];
                if (newDistance < distance[v]) {
                    distance[v] = newDistance;
                    previous[v] = u;
                }
            }
        }
    }

    private void saveResults(int[] distance) {
        ArrayList<Integer> path = new ArrayList<>();
        int current = endNode;
        while (current != -1) {
            path.add(0, current);
            current = previous[current];
        }

        StringBuilder pathBuilder = new StringBuilder();
        for (int node : path) {
            pathBuilder.append(node).append(" ");
        }
        this.pathNode = pathBuilder.toString().trim();

        this.meter = distance[endNode];
        this.travelTime = (int)(meter / AVERAGE_WALKING_SPEED);
        this.minutes = travelTime / 60;
        this.seconds = travelTime % 60;
    }

    // Getter 메서드들
    public int getMeter() { return meter; }
    public String getPathNode() { return pathNode; }
    public int getTime() { return travelTime; }
    public int getMinutes() { return minutes; }
    public int getSeconds() { return seconds; }
}
