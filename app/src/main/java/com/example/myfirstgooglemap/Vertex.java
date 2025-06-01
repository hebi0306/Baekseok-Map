package com.example.myfirstgooglemap;

/**
 * 건물과 위치 정보를 나타내는 정점(Vertex) 클래스
 * 위도, 경도, 건물 정보 등을 포함하며, 두 지점 간의 거리 계산 기능을 제공합니다.
 */
public class Vertex {
    /** 지구 반경 (미터) */
    private static final double EARTH_RADIUS = 6371000;
    
    /** 위도 */
    public final double latitude;
    
    /** 경도 */
    public final double longitude;
    
    /** 건물 ID */
    public final int id;
    
    /** 건물명(한글) */
    public final String name;
    
    /** 건물명(영문) */
    public final String name_eng;
    
    /** 흡연 구역 여부 */
    public final boolean is_smoke;
    
    /** 주차 구역 여부 */
    public final boolean is_parking;
    
    /** 층수 */
    public final int floor;

    /**
     * Vertex 객체를 생성합니다.
     * 
     * @param latitude 위도
     * @param longitude 경도
     * @param id 건물 ID
     * @param name 건물명(한글)
     * @param name_eng 건물명(영문)
     * @param is_smoke 흡연 구역 여부
     * @param is_parking 주차 구역 여부
     * @param floor 층수
     */
    public Vertex(double latitude, double longitude, int id, String name, String name_eng, 
                 boolean is_smoke, boolean is_parking, int floor) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
        this.name = name;
        this.name_eng = name_eng;
        this.is_smoke = is_smoke;
        this.is_parking = is_parking;
        this.floor = floor;
    }

    /**
     * 두 지점 간의 거리를 계산합니다 (Haversine 공식 사용)
     * 
     * @param other 다른 Vertex 객체
     * @return 두 지점 간의 거리 (미터)
     */
    public int calDistance(Vertex other) {
        double lat1 = Math.toRadians(this.latitude);
        double lon1 = Math.toRadians(this.longitude);
        double lat2 = Math.toRadians(other.latitude);
        double lon2 = Math.toRadians(other.longitude);

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                  Math.cos(lat1) * Math.cos(lat2) *
                  Math.sin(dLon/2) * Math.sin(dLon/2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        
        return (int)(EARTH_RADIUS * c);
    }

    @Override
    public String toString() {
        return String.format("Vertex{id=%d, name='%s', lat=%.6f, lon=%.6f}", 
            id, name, latitude, longitude);
    }
}