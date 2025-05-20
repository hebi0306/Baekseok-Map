package com.example.myfirstgooglemap;
import static java.lang.Boolean.TRUE;

import android.content.Context;

import java.io.*;
import java.util.*;

public class Daijkstra {
    private static final int INFINITE = 1000000; // 다익스트라 알고리즘을 위한 초기값
    private static int[] dist; // 가중치(거리)
    private static final int NODE = 22; // 노드(학교 장소) 갯수
    private static int[] parent; // 경로 저장
    private static int spotCnt; // 경로를 지난 노드 수

    private int startNode; // 출발지
    private int endNode; // 목적지
    private String pathNode; // 경로
    private int meter; // 총 거리
    private int trableTime; // 가는 데까지 예상 필요시간

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
        // 장애유무에 따라 간선정보를 다르게 입력합니다.
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
            String[] st = line.split(" ");
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
                int cost = vertex.get(startIdx).calDistance(vertex.get(endIdx));
                adjMatrix[startIdx][endIdx] = cost;
                adjMatrix[endIdx][startIdx] = cost;
            }
        }

        // 최단경로를 저장한다.
        dist = new int[NODE];
        parent = new int[NODE];
        Arrays.fill(dist, INFINITE);
        boolean[] check = new boolean[NODE];
        spotCnt = 0;
        dist[startNode] = 0;

        // 인접행렬 기반 다익스트라 (우선순위큐 없이)
        for (int i = 0; i < NODE; i++) {
            int min = INFINITE;
            int u = -1;
            for (int j = 0; j < NODE; j++) {
                if (!check[j] && dist[j] < min) {
                    min = dist[j];
                    u = j;
                }
            }
            if (u == -1) break;
            check[u] = true;

            for (int v = 0; v < NODE; v++) {
                if (!check[v] && adjMatrix[u][v] < INFINITE) {
                    if (dist[v] > dist[u] + adjMatrix[u][v]) {
                        dist[v] = dist[u] + adjMatrix[u][v];
                        parent[v] = u;
                    }
                }
            }
        }

        Stack<Integer> stack = searchPath(startNode, endNode);

        StringBuilder sb = new StringBuilder();
        while(!stack.isEmpty()){
            int spot = stack.pop();
            // 이미 vertex ID가 1-based이므로 변환하지 않음
            sb.append(spot + " ");
        }

        meter = dist[endNode];
        pathNode = sb.toString();
        trableTime = (int)(meter/1.1); // 초속 1.1m/s로 계산했을 때
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
}
