package com.example.myfirstgooglemap;

import android.graphics.Bitmap;

public class Vertex {
    // 기본 정보
    public double latitude;
    public double longitude;
    public int id;
    public String name;
    public String name_eng;
    
    // 시설 정보
    public boolean is_smoke;
    public boolean is_parking;

    // 층 정보
    public int floor;

    // 기본 생성자
    public Vertex() {
    }

    // 모든 정보를 받는 생성자
    public Vertex(double latitude, double longitude, int id, String name, String name_eng, boolean is_smoke, boolean is_parking, int floor) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
        this.name = name;
        this.name_eng = name_eng;
        this.is_smoke = is_smoke;
        this.is_parking = is_parking;
        this.floor = floor;
    }

    // 위치 정보 설정
    private void setLocation(double lati, double longi) {
        this.latitude = lati;
        this.longitude = longi;
    }

    // 기본 정보 설정
    private void setInfo(int id, String name, String name_eng) {
        this.id = id;
        this.name = name;
        this.name_eng = name_eng;
    }

    // 시설 정보 설정
    public void setFacility(boolean is_smoke, boolean is_parking) {
        this.is_smoke = is_smoke;
        this.is_parking = is_parking;
    }

    // 층 정보 설정
    public void setFloor(int floor) {
        this.floor = floor;
    }

    // 층 정보 가져오기
    public int getFloor() {
        return this.floor;
    }

    // 위도와 경도를 기준으로 두 지점 간의 거리측정
    public int calDistance(Vertex p) {
        int R = 6378137; // 지구 반지름
        double dLat = Math.toRadians(p.latitude - this.latitude);
        double dLong = Math.toRadians(p.longitude - this.longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                  Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(p.latitude)) * 
                  Math.sin(dLong / 2) * Math.sin(dLong / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;
        return (int)d; // returns the distance in meter
    }
}