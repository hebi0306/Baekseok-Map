package com.example.myfirstgooglemap;
import static java.lang.Boolean.TRUE;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.util.*;

public class Daijkstra {
    private static final int INFINITE = 1000000; // 다익스트라 알고리즘을 위한 초기값
    private static int[] dist; // 가중치(거리)
    private static final int NODE = 139; // 노드(건물 22개 + 코너 포인트 117개) 갯수
    private static int[] parent; // 경로 저장
    private static int spotCnt; // 경로를 지난 노드 수

    private int startNode; // 출발지
    private int endNode; // 목적지
    private String pathNode; // 경로
    private int meter; // 총 거리
    private int trableTime; // 가는 데까지 예상 필요시간
    private int minutes; // 분
    private int seconds; // 초

    // 외부에서 인스턴스 생성을 막기 위한 private 생성자
    private Daijkstra() {
    }

    // 싱글톤 패턴
    private static class Singleton{
        private static final Daijkstra INSTANCE = new Daijkstra();
    }

    // 다익스트라 유일 인스턴스 get Method
    public static Daijkstra getInstance(int start, int end){
        // start와 end는 vertex에서의 인덱스이므로, 실제 ID로 변환
        Singleton.INSTANCE.startNode = start;
        Singleton.INSTANCE.endNode = end;
        return Singleton.INSTANCE;
    }

    public void calDaijkstra(Context context, boolean disabled, ArrayList<Vertex> vertex) throws IOException {
        InputStream is;
        if (disabled == TRUE)
            is = context.getResources().openRawResource(R.raw.edge_disabled);
        else
            is = context.getResources().openRawResource(R.raw.edge);

        // 인접행렬 선언 및 초기화
        int[][] adjMatrix = new int[NODE][NODE];
        for (int i = 0; i < NODE; i++) {
            for (int j = 0; j < NODE; j++) {
                if (i == j) adjMatrix[i][j] = 0;
                else adjMatrix[i][j] = INFINITE;
            }
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            // 주석 라인 무시
            if (line.startsWith("#")) continue;
            
            String[] st = line.split(" ");
            if (st.length < 2) continue;
            
            int start = Integer.parseInt(st[0]);
            int end = Integer.parseInt(st[1]);
            
            // vertex.txt의 ID와 매칭되는 인덱스 찾기
            int startIdx = -1;
            int endIdx = -1;
            for (int i = 0; i < vertex.size(); i++) {
                if (vertex.get(i).id == start) startIdx = i;
                if (vertex.get(i).id == end) endIdx = i;
            }
            
            if (startIdx >= 0 && startIdx < vertex.size() && endIdx >= 0 && endIdx < vertex.size()) {
                // 거리 계산
                int cost = vertex.get(startIdx).calDistance(vertex.get(endIdx));
                
                // 코너 포인트를 포함하는 경로의 경우 거리 보정
                if (vertex.get(startIdx).id > 22 || vertex.get(endIdx).id > 22) {
                    // 코너 포인트 간의 거리는 1.2배로 가중치 부여 (실제 경로가 더 길 수 있음)
                    cost = (int)(cost * 1.2);
                }
                
                // 양방향 연결 설정
                adjMatrix[startIdx][endIdx] = cost;
                adjMatrix[endIdx][startIdx] = cost;
            }
        }

        // 다익스트라 알고리즘 구현
        int[] distance = new int[NODE];
        boolean[] visited = new boolean[NODE];
        int[] previous = new int[NODE];

        // 초기화
        for (int i = 0; i < NODE; i++) {
            distance[i] = INFINITE;
            visited[i] = false;
            previous[i] = -1;
        }
        distance[startNode] = 0;  // 시작점을 실제 선택한 출발지로 설정

        // 다익스트라 알고리즘
        for (int i = 0; i < NODE; i++) {
            int min = INFINITE;
            int u = -1;

            // 방문하지 않은 노드 중 최소 거리를 가진 노드 찾기
            for (int j = 0; j < NODE; j++) {
                if (!visited[j] && distance[j] < min) {
                    min = distance[j];
                    u = j;
                }
            }

            if (u == -1) break;  // 더 이상 방문할 노드가 없음
            visited[u] = true;

            // 인접 노드들의 거리 업데이트
            for (int v = 0; v < NODE; v++) {
                if (!visited[v] && adjMatrix[u][v] != INFINITE) {
                    int newDistance = distance[u] + adjMatrix[u][v];
                    if (newDistance < distance[v]) {
                        distance[v] = newDistance;
                        previous[v] = u;
                    }
                }
            }
        }

        // 경로 복원
        ArrayList<Integer> path = new ArrayList<>();
        int current = endNode;  // 도착점을 실제 선택한 도착지로 설정
        while (current != -1) {
            path.add(0, current);
            current = previous[current];
        }

        // 결과 저장
        this.pathNode = "";
        for (int node : path) {
            this.pathNode += node + " ";
        }
        this.pathNode = this.pathNode.trim();

        meter = distance[endNode];  // 실제 도착지까지의 거리
        trableTime = (int)(meter/1.1); // 초속 1.1m/s로 계산했을 때
        minutes = trableTime / 60; // 분 계산
        seconds = trableTime % 60; // 초 계산
    }

    public static Stack<Integer> searchPath(int startNode, int endNode){
        Stack<Integer> stack = new Stack<>();
        int cur = endNode;

        while(cur != startNode) {
            stack.push(cur);
            spotCnt++;

            cur = parent[cur];
        }
        stack.push(cur);
        spotCnt++;

        return stack;
    }

    public int getMeter() {
        return meter;
    }
    public String getPathNode() { return pathNode; }
    public int getNodeCount() {
        return spotCnt;
    }
    public int getTime() {
        return trableTime;
    }
    public int getMinutes() {
        return minutes;
    }
    public int getSeconds() {
        return seconds;
    }
}
