package com.example.myfirstgooglemap;

import androidx.annotation.DrawableRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import static java.lang.Boolean.FALSE;

import java.util.ArrayList;
import java.io.*;
import java.util.List;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener,OnMapReadyCallback {

    // View 선언
    private ImageButton imgbtn_parking, imgbtn_smoke, imgbtn_find_route, imgbtn_search, imgbtn_find_route_daijkstra;
    private RelativeLayout layout_slide_up;
    private LinearLayout layout_bottom_btns, layout_search_line_1, layout_search_line_2, layout_search_line_3;
    private SlidingUpPanelLayout layout_slide;
    private AutoCompleteTextView autotext_building, autotext_building_from, autotext_building_to;
    private TextView text_building_no, text_building_name, text_building_name_eng, text_path, text_meter;
    private ImageView img_search, img_building_photo_1, img_smoke, img_parking;
    private CheckBox Ckbox_stair;

    // 변수 선언
    private GoogleMap mMap;
    private LatLng center;
    private ArrayAdapter<String> stringadt_building;
    private ArrayAdapter<String> stringadt_rooms;  // 호수 자동완성을 위한 어댑터
    private ArrayList<Marker> markers_smoking, markers_building, markers_parking;
    private ArrayList<Polyline> polylines;
    private ArrayList<String> ROOM_NAMES = new ArrayList<>();  // 호수 목록 저장

    private static final int NODE = 139; // 노드(건물 22개 + 코너 포인트 117개) 갯수
    private static ArrayList<Vertex> vertex = new ArrayList<>(NODE); // vertex 객체배열

    private static final double[] PARKING_POINTS = {
        36.840566, 127.188988,  // 주차장 1
        36.840856, 127.186043,  // 주차장 2
        36.840665, 127.186398,  // 주차장 3
        36.840456, 127.187373,  // 주차장 4
        36.840504, 127.185512,  // 주차장 5
        36.840757, 127.182028,  // 주차장 6
        36.839488, 127.184569,  // 주차장 7
        36.838802, 127.185878,  // 주차장 8
        36.838715, 127.186097   // 주차장 9
    };

    private static final double[] SMOKING_AREA_POINTS ={
            36.840973, 127.187829,  // 흡연장 1
            36.839576, 127.186077,  // 흡연장 2
            36.839403, 127.182200,  // 흡연장 3
            36.839173, 127.183352,  // 흡연장 4
            36.839188, 127.184240,  // 흡연장 5
            36.838499, 127.184256,  // 흡연장 6
            36.838641, 127.181649,  // 흡연장 7
            36.837982, 127.182936,  // 흡연장 8
            36.837246, 127.185478   // 흡연장 9
    };

    private static final double[] BUILDING_POINTS = {
        36.841339, 127.181359, // 정문 (1)
        36.840063, 127.180060, // 후문 (2)
        36.840566, 127.182469, // 학생복지관 (3)
        36.840853, 127.183620, // 목양관 (4)
        36.839510, 127.182595, // 백석홀 (5)
        36.838599, 127.181964, // 은혜관 (6)
        36.842554, 127.185146, // 기숙사 (7)
        36.841756, 127.185800, // 승리관 (8)
        36.837412, 127.182412, // 창조관 (9)
        36.838412, 127.183187, // 자유관 (10)
        36.839297, 127.183466, // 인성관 (11)
        36.840149, 127.184521, // 진리관 (12)
        36.838657, 127.184345, // 지혜관 (14)
        36.839711, 127.184789, // 교수회관 (15)
        36.840138, 127.185277, // 음악관 (16)
        36.841351, 127.187284, // 체육관 (17)
        36.837500, 127.185182, // 글로벌외식산업관 (18)
        36.839301, 127.185902, // 본부동 (19)
        36.837772, 127.184036, // 백석학술정보관 (20)
        36.838814, 127.187597, // 예술대학동 (21)
        36.840853, 127.188424, // 조형관 (22)
        36.842545, 127.185141  // 백석생활관 (24)
    };

    private static final int BUILDING_POINTS_SIZE = 139 * 2; // 전체 노드 수 * (위도 + 경도 = 2)

    private static ArrayList<String> BUILDING_NAMES = new ArrayList<>();

    // 지도 범위 제한을 위한 변수
    private LatLngBounds baekseokBounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // 리스트 초기화
        markers_smoking = new ArrayList<>();
        markers_building = new ArrayList<>();
        markers_parking = new ArrayList<>();
        vertex = new ArrayList<>();
        polylines = new ArrayList<>();
        ROOM_NAMES = new ArrayList<>();

        // 지도 범위 제한 설정
        baekseokBounds = new LatLngBounds(
            new LatLng(36.8370, 127.1800),  // SW bounds
            new LatLng(36.8430, 127.1890)   // NE bounds
        );

        // Vertex 초기화
        try {
            setVertex();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "초기화 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        // 호수 정보 로드
        try {
            loadRoomData();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "호수 정보 로드 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //LatLng 값 할당
        center = new LatLng(36.8400, 127.1840);  // 백석대학교 중심 좌표

        // 뷰 할당
        imgbtn_parking = findViewById(R.id.btn_parking);  // 주차장 버튼
        imgbtn_parking.setImageResource(R.drawable.btn_park_gray);  // 초기 이미지를 회색으로 설정
        imgbtn_smoke = findViewById(R.id.btn_smoke);
        imgbtn_smoke.setImageResource(R.drawable.btn_smoke_gray);  // 초기 이미지를 회색으로 설정
        imgbtn_find_route = findViewById(R.id.btn_find_route);
        imgbtn_find_route_daijkstra = findViewById(R.id.btn_find_route_daijkstra);
        autotext_building = findViewById(R.id.autotext_building);
        autotext_building_from = findViewById(R.id.autotext_building_from);
        autotext_building_to = findViewById(R.id.autotext_building_to);
        imgbtn_search = findViewById(R.id.btn_search);
        layout_slide_up = findViewById(R.id.layout_slide_up);
        layout_bottom_btns = findViewById(R.id.layout_bottom_btns);
        layout_slide = findViewById(R.id.layout_slide);
        layout_slide.setPanelHeight(0);
        text_building_no = findViewById(R.id.bd_number);
        text_building_name = findViewById(R.id.bd_name);
        text_building_name_eng = findViewById(R.id.bd_eng_name);
        text_meter = findViewById(R.id.text_meter);
        text_path = findViewById(R.id.text_path);
        img_search = findViewById(R.id.btn_search);
        img_building_photo_1 = findViewById(R.id.bd_photo_1);
        img_parking = findViewById(R.id.img_parking);
        img_smoke = findViewById(R.id.img_smoke);
        layout_search_line_1 = findViewById(R.id.search_line_1);
        layout_search_line_2 = findViewById(R.id.search_line_2);
        layout_search_line_2.setVisibility(View.INVISIBLE);
        layout_search_line_3 = findViewById(R.id.search_line_3);
        layout_search_line_3.setVisibility(View.INVISIBLE);
        Ckbox_stair = findViewById(R.id.Ckbox_stair);

        //어뎁터 할당
        stringadt_building = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, BUILDING_NAMES);
        stringadt_rooms = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ROOM_NAMES);
        autotext_building.setAdapter(stringadt_building);
        autotext_building_from.setAdapter(stringadt_rooms);
        autotext_building_to.setAdapter(stringadt_rooms);

        // 건물 이름 초기화
        BUILDING_NAMES = new ArrayList<>();
        for (int i = 0; i < BUILDING_POINTS.length / 2; i++) {
            BUILDING_NAMES.add("건물 " + (i + 1));
        }

        // 클릭 리스너 설정
        setupClickListeners();
    }

    public void setVertex() throws IOException {
        BUILDING_NAMES.clear(); // 기존 목록 초기화
        vertex.clear(); // 기존 vertex 목록 초기화
        
        try {
            InputStream is = getResources().openRawResource(R.raw.vertex);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] st = line.split(" ");
                Vertex v = new Vertex(
                    Double.parseDouble(st[0]),  // 위도
                    Double.parseDouble(st[1]),  // 경도
                    Integer.parseInt(st[2]),    // ID
                    st[3],                      // 건물명(한글)
                    st[4],                      // 건물명(영문)
                    Integer.parseInt(st[5]) == 1,  // 흡연 구역
                    Integer.parseInt(st[6]) == 1,  // 주차 구역
                    Integer.parseInt(st[7])     // 층수
                );
                vertex.add(v);
            }
            reader.close();
            
            if (vertex.isEmpty()) {
                throw new IOException("vertex 데이터가 비어있습니다.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MapsActivity", "Vertex 초기화 오류: " + e.getMessage());
            throw new IOException("Vertex 초기화 중 오류 발생: " + e.getMessage());
        }
    }

    // 호수 정보 로드 메서드
    private void loadRoomData() throws IOException {
        try {
            InputStream is = this.getApplicationContext().getResources().openRawResource(R.raw.rooms);
            if (is == null) {
                throw new IOException("rooms 파일을 찾을 수 없습니다.");
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    ROOM_NAMES.add(line.trim());
                }
            }
            
            if (ROOM_NAMES.isEmpty()) {
                throw new IOException("호수 데이터가 비어있습니다.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MapsActivity", "호수 정보 로드 오류: " + e.getMessage());
            throw new IOException("호수 정보 로드 중 오류 발생: " + e.getMessage());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 카메라 위치 변경 및 축소/확대 제한
        mMap.moveCamera(CameraUpdateFactory.newLatLng(center));
        mMap.setMinZoomPreference(17);  // 줌 레벨 조정
        mMap.setMaxZoomPreference(19);  // 줌 레벨 조정

        // 지도 범위 제한
        mMap.setLatLngBoundsForCameraTarget(baekseokBounds);

        // 주차장 마커 설정
        BitmapDescriptor bitmap_parking = GetBitmapDescriptor(R.drawable.ic_mark_parking, 70);
        for(int i = 0; i < PARKING_POINTS.length; i+=2){
            Marker m = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(PARKING_POINTS[i], PARKING_POINTS[i+1]))
                .icon(bitmap_parking)
                .visible(false));
            markers_parking.add(m);
        }

        // 흡연 구역 마커 설정
        BitmapDescriptor bitmap_smoking = GetBitmapDescriptor(R.drawable.ic_mark_smoking, 70);
        for(int i = 0; i < SMOKING_AREA_POINTS.length; i+=2){
            Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(SMOKING_AREA_POINTS[i], SMOKING_AREA_POINTS[i+1])));
            m.setIcon(bitmap_smoking);
            m.setVisible(false);
            markers_smoking.add(m);
        }

        // 건물 마커 설정
        BitmapDescriptor bitmap_building = GetBitmapDescriptor(R.drawable.ic_mark_building, 60);
        for(int i = 0; i < vertex.size(); i++){
            // 건물인 경우에만 마커 표시 (ID가 22 이하인 경우)
            if (vertex.get(i).id <= 22) {
                Marker m = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(vertex.get(i).latitude, vertex.get(i).longitude))
                    .icon(bitmap_building)
                    .visible(true));
                m.setTag(vertex.get(i).name);
                markers_building.add(m);
                Log.d("Marker", "Setting marker " + i + " with name: " + vertex.get(i).name + " at position: " + vertex.get(i).latitude + "," + vertex.get(i).longitude);
            }
        }

        mMap.setOnMarkerClickListener(this);
    }

    private void ToggleMarkersVisibility(ArrayList<Marker> markers){
        int size = markers.size();
        if(markers.get(0).isVisible()){    // 마커가 표시 중이라면 보이지 않게 만들기
            for(int i = 0; i < size; i++){
                markers.get(i).setVisible(false);
            };
        }
        else{                                       // 마커가 표시 중이 아니라면 보이게 만들기
            for(int i = 0; i < size; i++){
                markers.get(i).setVisible(true);
            };
        }
    }

    private BitmapDescriptor GetBitmapDescriptor(@DrawableRes int id, int size) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth() * size / 100,
                vectorDrawable.getIntrinsicHeight() * size / 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void HideKeyboard(){
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public int convert(String spot) {
        int result = -1; // 못 찾았을 경우, -1로 반환합니다.

        // 모든 정점(건물과 코너)을 검색
        for(int i = 0; i < vertex.size(); i++) {
            if(spot.equals(vertex.get(i).name) | spot.equals(vertex.get(i).name_eng) | spot.equals(Integer.toString(vertex.get(i).id))) {
                Log.d("Convert", "Found match for " + spot + " at index " + i + " with name " + vertex.get(i).name + " with ID " + vertex.get(i).id);
                return i;  // 인덱스를 그대로 반환
            }
        }
        Log.d("Convert", "No match found for " + spot);
        return result;
    }

    private void ClickFindRouteBtn(){
        HideKeyboard();
        layout_slide.setPanelHeight(0);
        layout_search_line_1.setVisibility(View.INVISIBLE);
        layout_search_line_2.setVisibility(View.VISIBLE);
        autotext_building_to.setText(autotext_building.getText());
        autotext_building.setText("");
    }

    private void DisplayBuildingInfo(){
        String text = autotext_building.getText().toString();
        int index;
        Context context = getApplicationContext();
        if((index = convert(text)) != -1){
            HideKeyboard();
            layout_slide.setPanelHeight(400);

            // vertex 에서 정보를 가져와 View 값 변경
            layout_bottom_btns.setVisibility(View.GONE);
            text_building_no.setText("No. " + vertex.get(index).id);
            text_building_name.setText(vertex.get(index).name);
            text_building_name_eng.setText(vertex.get(index).name_eng);
            
            int id = context.getResources().getIdentifier("photo_"+vertex.get(index).id + "_1", "drawable", context.getPackageName());
            img_building_photo_1.setImageResource(id);

            if(vertex.get(index).is_smoke){
                img_smoke.setImageResource(R.drawable.ic_smoke_color);
            }
            else{
                img_smoke.setImageResource(R.drawable.ic_smoke_gray);
            }
            if(vertex.get(index).is_parking){
                img_parking.setImageResource(R.drawable.ic_park_color);
            }
            else{
                img_parking.setImageResource(R.drawable.ic_park_gray);
            }

            // 카메라 줌 인
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(vertex.get(index).latitude,  vertex.get(index).longitude)));
        }
    }

    public void initDisplay(){
        HideKeyboard();
        layout_slide.setPanelHeight(0);
        layout_search_line_1.setVisibility(View.VISIBLE);
        layout_search_line_2.setVisibility(View.INVISIBLE);
        layout_search_line_3.setVisibility(View.INVISIBLE);
        layout_bottom_btns.setVisibility(View.VISIBLE);
        initPolylines();
    }

    public void initPolylines(){
        for(Polyline p : polylines){
            p.remove();
        }
    }

    @Override
    public void onBackPressed() {
        if(layout_slide.getPanelHeight() != 0 || layout_search_line_2.getVisibility() == View.VISIBLE){
            initDisplay();
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        String tag = (String) marker.getTag();
        if(tag != null){
            autotext_building.setText(tag);
            initDisplay();
            DisplayBuildingInfo();
        }

        return false;
    }

    private void setupClickListeners() {
        // 주차장 마커 토글
        imgbtn_parking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToggleMarkersVisibility(markers_parking);
                if(markers_parking.get(0).isVisible()){
                    imgbtn_parking.setImageResource(R.drawable.btn_park_color);
                }
                else{
                    imgbtn_parking.setImageResource(R.drawable.btn_park_gray);
                }
            }
        });
        
        // 흡연구역 마커 토글
        imgbtn_smoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToggleMarkersVisibility(markers_smoking);
                if(markers_smoking.get(0).isVisible()){
                    imgbtn_smoke.setImageResource(R.drawable.btn_smoke_color);
                }
                else{
                    imgbtn_smoke.setImageResource(R.drawable.btn_smoke_gray);
                }
            }
        });

        autotext_building.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // 자동완성 클릭 시 키보드 숨기기
                HideKeyboard();
            }
        });

        imgbtn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisplayBuildingInfo();
            }
        });

        imgbtn_find_route_daijkstra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 출발지랑 도착지 넣는 텍스트 뷰에서 값 가져오기
                String startLocation = autotext_building_from.getText().toString();
                String endLocation = autotext_building_to.getText().toString();
                
                // 건물명과 호수 분리
                String[] startParts = parseLocation(startLocation);
                String[] endParts = parseLocation(endLocation);
                
                // 건물명으로 vertex 찾기
                int start = convert(startParts[0]); // 선택한 출발지를 객체배열의 고유번호로 바꿔줍니다.
                int end = convert(endParts[0]); // 선택한 도착지를 객체배열의 고유번호로 바꿔줍니다.
                initPolylines();
                if(start == -1 || end == -1) return;

                // 층 정보 파싱
                int startFloor = parseFloor(startLocation);
                int endFloor = parseFloor(endLocation);
                
                // 층 이동 시간 계산
                int floorTime = calculateFloorTime(startFloor, endFloor);

                HideKeyboard();
                Daijkstra path = Daijkstra.getInstance(start, end); // 다익스트라 singleton 객체 Path를 생성합니다.

                try {
                    setVertex(); // vertex 초기화
                    path.calDaijkstra(getApplicationContext(), !Ckbox_stair.isChecked(), vertex); // 경로 계산
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                String[] pathNode = path.getPathNode().split(" ", 0); // 계산된 경로(String)을 연산을 위해 배열로 만듭니다.

                // 캠퍼스지도상에 그릴 polyLine객체를 pathNode를 토대로 생성합니다.
                drawPath(vertex, path.getPathNode());

                // 경로 정보 표시
                String pathInfo = "최단 경로: "; // 시작 설명 추가
                for (int i = 0; i < pathNode.length; i++) {
                    int vertexIndex = Integer.parseInt(pathNode[i]);
                    // 건물 노드(ID <= 22)만 경로 정보 텍스트에 표시
                    if (vertex.get(vertexIndex).id <= 22) {
                        pathInfo += vertex.get(vertexIndex).name;
                        // 마지막 건물 노드가 아니라면 화살표 추가
                        if (i < pathNode.length - 1) {
                            boolean foundNextBuilding = false;
                            for(int j = i + 1; j < pathNode.length; j++){
                                 int nextVertexIndex = Integer.parseInt(pathNode[j]);
                                 if(vertex.get(nextVertexIndex).id <= 22){
                                     foundNextBuilding = true;
                                     break;
                                 }
                            }
                            if(foundNextBuilding) pathInfo += " → ";
                        }
                    }
                }

                text_path.setText(pathInfo);
                
                // 층 이동 시간을 포함한 총 시간 계산
                int totalSeconds = path.getTime() + floorTime;
                int totalMinutes = totalSeconds / 60;
                int remainingSeconds = totalSeconds % 60;
                
                text_meter.setText(path.getMeter() + "m (" + totalMinutes + "분 " + remainingSeconds + "초)");
                layout_search_line_3.setVisibility(View.VISIBLE);
            }
        });

        imgbtn_find_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClickFindRouteBtn();
            }
        });

        img_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClickFindRouteBtn();
            }
        });
    }

    private void drawPath(ArrayList<Vertex> vertex, String pathNode) {
        if (pathNode == null || pathNode.isEmpty()) return;

        String[] nodes = pathNode.split(" ");
        for (int i = 0; i < nodes.length - 1; i++) {
            int preVertexNum = Integer.parseInt(nodes[i]);
            int postVertexNum = Integer.parseInt(nodes[i + 1]);
            
            // 건물 간의 직접 경로는 빨간색으로 표시
            int color = Color.RED;
            int width = 8;  // 선 두께 증가
            
            // 코너 포인트를 포함하는 경로는 파란색으로 표시
            if (vertex.get(preVertexNum).id > 22 || vertex.get(postVertexNum).id > 22) {
                color = Color.BLUE;
                width = 6;  // 선 두께 증가
            }
            
            // polyline 그리는 코드
            polylines.add(mMap.addPolyline((new PolylineOptions())
                .add(new LatLng(vertex.get(preVertexNum).latitude, vertex.get(preVertexNum).longitude),
                     new LatLng(vertex.get(postVertexNum).latitude, vertex.get(postVertexNum).longitude))
                .width(width)
                .color(color)
                .pattern(Arrays.asList(new Dash(30), new Gap(20)))));  // 점선 패턴 수정
        }
    }

    // 층 정보 파싱 메서드
    private int parseFloor(String location) {
        // 정규식을 사용하여 층 정보 추출 (예: "본부동 101호" -> 1)
        Pattern pattern = Pattern.compile("(\\d{3})호");
        Matcher matcher = pattern.matcher(location);
        if (matcher.find()) {
            String roomNumber = matcher.group(1);
            // 101호 -> 1층, 201호 -> 2층 등으로 변환
            return Integer.parseInt(roomNumber.substring(0, 1));
        }
        return 1; // 기본값 1층
    }

    // 건물명과 호수 분리 메서드
    private String[] parseLocation(String location) {
        // 정규식을 사용하여 건물명과 호수 분리 (예: "본부동 101호" -> ["본부동", "101호"])
        Pattern pattern = Pattern.compile("(.+?)\\s*(\\d{3}호)?$");
        Matcher matcher = pattern.matcher(location);
        if (matcher.find()) {
            String buildingName = matcher.group(1).trim();
            String roomNumber = matcher.group(2);
            return new String[]{buildingName, roomNumber != null ? roomNumber : ""};
        }
        return new String[]{location, ""};
    }

    // 층 이동 시간 계산 메서드
    private int calculateFloorTime(int startFloor, int endFloor) {
        int floorDiff = Math.abs(endFloor - startFloor);
        return floorDiff * 30; // 층당 30초
    }
}