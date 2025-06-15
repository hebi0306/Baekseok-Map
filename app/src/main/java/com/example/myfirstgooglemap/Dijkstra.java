package com.example.myfirstgooglemap;

import android.content.Context;
import android.util.Log;
import java.io.*;
import java.util.*;

/**
 * 다익스트라 알고리즘을 구현한 클래스
 * 두 지점 간의 최단 경로를 계산하고, 이동 거리와 시간을 제공합니다.
 * 
 * 주요 기능:
 * 1. 최단 경로 계산
 * 2. 이동 거리 계산
 * 3. 예상 이동 시간 계산
 * 4. 장애인 경로 지원
 */
public class Dijkstra {
    /** 무한대를 나타내는 값 (경로가 존재하지 않음을 나타냄) */
    private static final int INFINITY = 1000000;
    
    /** 전체 노드 수 (지도상의 모든 정점 수) */
    private static final int TOTAL_NODES = 139;
    
    /** 평균 보행 속도 (m/s) - 일반인의 평균 보행 속도 기준 */
    private static final double AVERAGE_WALKING_SPEED = 1.1;
    
    /** 코너 포인트 거리 가중치 - 코너를 돌 때 추가되는 거리 비율 */
    private static final double CORNER_WEIGHT = 1.2;

    /** 시작 노드 번호 */
    private int startNode;
    /** 도착 노드 번호 */
    private int endNode;
    /** 최단 경로를 구성하는 노드들의 문자열 표현 */
    private String pathNode;
    /** 총 이동 거리 (미터) */
    private int meter;
    /** 총 이동 시간 (초) */
    private int travelTime;
    /** 이동 시간의 분 단위 */
    private int minutes;
    /** 이동 시간의 초 단위 */
    private int seconds;
    /** 경로 추적을 위한 배열 - 각 노드의 이전 노드를 저장 */
    private int[] previous;

    /**
     * 싱글톤 패턴 구현을 위한 내부 클래스
     * 애플리케이션 전체에서 단일 인스턴스만 사용하도록 보장
     */
    private static class Singleton {
        private static final Dijkstra INSTANCE = new Dijkstra();
    }

    /**
     * private 생성자 - 싱글톤 패턴 구현
     * previous 배열을 초기화
     */
    private Dijkstra() {
        previous = new int[TOTAL_NODES];
    }

    /**
     * Dijkstra 인스턴스를 반환하는 메서드
     * 싱글톤 패턴을 통해 단일 인스턴스만 생성되도록 보장
     * 
     * @param start 시작 노드 번호
     * @param end 도착 노드 번호
     * @return Dijkstra 인스턴스
     */
    public static Dijkstra getInstance(int start, int end) {
        Singleton.INSTANCE.startNode = start;
        Singleton.INSTANCE.endNode = end;
        return Singleton.INSTANCE;
    }

    /**
     * 다익스트라 알고리즘을 실행하여 최단 경로를 계산하는 메인 메서드
     * 
     * @param context Android 컨텍스트
     * @param disabled 장애인 경로 사용 여부 (true: 계단 제외, false: 계단 포함)
     * @param vertex 정점 목록
     * @throws IOException 파일 읽기 오류 발생 시
     */
    public void calculateShortestPath(Context context, boolean disabled, ArrayList<Vertex> vertex) throws IOException {
        // 인접 행렬 초기화
        int[][] adjMatrix = initializeAdjacencyMatrix();
        // 엣지 데이터 로드
        loadEdges(context, disabled, vertex, adjMatrix);
        
        // 거리와 방문 여부 배열 초기화
        int[] distance = new int[TOTAL_NODES];
        boolean[] visited = new boolean[TOTAL_NODES];
        
        // 다익스트라 알고리즘 초기화
        initializeDijkstra(distance, visited);
        // 다익스트라 알고리즘 실행
        runDijkstra(adjMatrix, distance, visited);
        
        // 결과 저장
        saveResults(distance);
    }

    /**
     * 인접 행렬을 초기화하는 메서드
     * 모든 노드 간의 거리를 무한대로 설정하고, 자기 자신과의 거리는 0으로 설정
     * 
     * @return 초기화된 인접 행렬
     */
    private int[][] initializeAdjacencyMatrix() {
        int[][] adjMatrix = new int[TOTAL_NODES][TOTAL_NODES];
        for (int i = 0; i < TOTAL_NODES; i++) {
            Arrays.fill(adjMatrix[i], INFINITY);
            adjMatrix[i][i] = 0;
        }
        return adjMatrix;
    }

    /**
     * 엣지 데이터를 파일에서 로드하는 메서드
     * 장애인 경로 여부에 따라 다른 파일을 사용
     * 
     * @param context Android 컨텍스트
     * @param disabled 장애인 경로 사용 여부
     * @param vertex 정점 목록
     * @param adjMatrix 인접 행렬
     * @throws IOException 파일 읽기 오류 발생 시
     */
    private void loadEdges(Context context, boolean disabled, ArrayList<Vertex> vertex, int[][] adjMatrix) throws IOException {
        // 장애인 경로 여부에 따라 다른 파일 선택
        int resourceId = disabled ? R.raw.path_with_stairs : R.raw.path_without_stairs;
        try (InputStream is = context.getResources().openRawResource(resourceId);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                // 주석 라인 무시
                if (line.startsWith("#")) continue;
                
                // 엣지 데이터 파싱
                String[] parts = line.split(" ");
                if (parts.length < 2) continue;
                
                int start = Integer.parseInt(parts[0]);
                int end = Integer.parseInt(parts[1]);
                
                // 정점 인덱스 찾기
                int startIdx = findVertexIndex(vertex, start);
                int endIdx = findVertexIndex(vertex, end);
                
                // 유효한 정점인 경우 비용 계산 및 인접 행렬 업데이트
                if (startIdx >= 0 && endIdx >= 0) {
                    int cost = calculateEdgeCost(vertex.get(startIdx), vertex.get(endIdx));
                    adjMatrix[startIdx][endIdx] = cost;
                    adjMatrix[endIdx][startIdx] = cost;
                }
            }
        }
    }

    /**
     * 정점 ID로 정점 인덱스를 찾는 메서드
     * 
     * @param vertex 정점 목록
     * @param id 찾을 정점 ID
     * @return 정점 인덱스 (찾지 못한 경우 -1)
     */
    private int findVertexIndex(ArrayList<Vertex> vertex, int id) {
        for (int i = 0; i < vertex.size(); i++) {
            if (vertex.get(i).id == id) return i;
        }
        return -1;
    }

    /**
     * 두 정점 간의 비용을 계산하는 메서드
     * 코너 포인트인 경우 추가 가중치 적용
     * 
     * @param v1 첫 번째 정점
     * @param v2 두 번째 정점
     * @return 계산된 비용
     */
    private int calculateEdgeCost(Vertex v1, Vertex v2) {
        int cost = v1.calDistance(v2);
        // 코너 포인트인 경우 가중치 적용
        if (v1.id > 22 || v2.id > 22) {
            cost = (int)(cost * CORNER_WEIGHT);
        }
        return cost;
    }

    /**
     * 다익스트라 알고리즘 초기화 메서드
     * 거리 배열과 방문 여부 배열을 초기화
     * 
     * @param distance 거리 배열
     * @param visited 방문 여부 배열
     */
    private void initializeDijkstra(int[] distance, boolean[] visited) {
        Arrays.fill(distance, INFINITY);
        Arrays.fill(visited, false);
        Arrays.fill(previous, -1);
        distance[startNode] = 0;
    }

    /**
     * 다익스트라 알고리즘 실행 메서드
     * 최단 경로를 계산하는 핵심 로직
     * 
     * @param adjMatrix 인접 행렬
     * @param distance 거리 배열
     * @param visited 방문 여부 배열
     */
    private void runDijkstra(int[][] adjMatrix, int[] distance, boolean[] visited) {
        for (int i = 0; i < TOTAL_NODES; i++) {
            int u = findMinDistanceNode(distance, visited);
            if (u == -1) break;
            
            visited[u] = true;
            updateNeighbors(u, adjMatrix, distance, visited);
        }
    }

    /**
     * 방문하지 않은 노드 중 최소 거리를 가진 노드를 찾는 메서드
     * 
     * @param distance 거리 배열
     * @param visited 방문 여부 배열
     * @return 최소 거리 노드의 인덱스 (찾지 못한 경우 -1)
     */
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

    /**
     * 현재 노드의 이웃 노드들의 거리를 업데이트하는 메서드
     * 
     * @param u 현재 노드
     * @param adjMatrix 인접 행렬
     * @param distance 거리 배열
     * @param visited 방문 여부 배열
     */
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

    /**
     * 최종 결과를 저장하는 메서드
     * 경로, 거리, 시간 정보를 계산하여 저장
     * 
     * @param distance 거리 배열
     */
    private void saveResults(int[] distance) {
        // 경로 추적
        ArrayList<Integer> path = new ArrayList<>();
        int current = endNode;
        while (current != -1) {
            path.add(0, current);
            current = previous[current];
        }

        // 경로 문자열 생성
        StringBuilder pathBuilder = new StringBuilder();
        for (int node : path) {
            pathBuilder.append(node).append(" ");
        }
        this.pathNode = pathBuilder.toString().trim();

        // 거리와 시간 계산
        this.meter = distance[endNode];
        this.travelTime = (int)(meter / AVERAGE_WALKING_SPEED);
        this.minutes = travelTime / 60;
        this.seconds = travelTime % 60;
    }

    // Getter 메서드들
    /** 총 이동 거리 반환 */
    public int getMeter() { return meter; }
    /** 경로 노드 문자열 반환 */
    public String getPathNode() { return pathNode; }
    /** 총 이동 시간(초) 반환 */
    public int getTime() { return travelTime; }
    /** 이동 시간(분) 반환 */
    public int getMinutes() { return minutes; }
    /** 이동 시간(초) 반환 */
    public int getSeconds() { return seconds; }
}
