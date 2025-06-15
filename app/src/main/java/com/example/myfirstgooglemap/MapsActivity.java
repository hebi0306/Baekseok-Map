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

/**
 * 백석대학교 캠퍼스 맵을 표시하고 경로 안내를 제공하는 메인 액티비티
 * 
 * 주요 기능:
 * 1. 구글 맵을 통한 캠퍼스 지도 표시
 * 2. 건물, 주차장, 흡연 구역 마커 표시
 * 3. 최단 경로 계산 및 표시
 * 4. 건물 정보 표시
 * 5. 장애인 경로 지원
 */
public class MapsActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    /**
     * 앱에서 사용되는 상수들을 정의하는 내부 클래스
     */
    private static class Constants {
        /** 전체 노드 수 */
        static final int TOTAL_NODES = 139;
        
        /** 캠퍼스 중심 좌표 (위도, 경도) */
        static final LatLng CAMPUS_CENTER = new LatLng(36.8400, 127.1840);
        
        /** 지도 표시 범위 제한 (남서쪽, 북동쪽 좌표) */
        static final LatLngBounds CAMPUS_BOUNDS = new LatLngBounds(
            new LatLng(36.8370, 127.1800),  // SW bounds
            new LatLng(36.8430, 127.1890)   // NE bounds
        );
        
        /** 주차장 좌표 목록 (위도, 경도) */
        static final double[] PARKING_POINTS = {
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

        /** 흡연 구역 좌표 목록 (위도, 경도) */
        static final double[] SMOKING_AREA_POINTS = {
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

        /** 건물 좌표 목록 (위도, 경도) */
        static final double[] BUILDING_POINTS = {
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
    }

    // View 선언
    /** 주차장 버튼 레이아웃 */
    private LinearLayout btn_parking_layout, btn_smoke_layout;
    /** 경로 찾기 버튼 */
    private ImageButton imgbtn_find_route, imgbtn_search, imgbtn_find_route_daijkstra;
    /** 슬라이딩 패널 레이아웃 */
    private RelativeLayout layout_slide_up;
    /** 하단 버튼 레이아웃 */
    private LinearLayout layout_bottom_btns, layout_search_line_1, layout_search_line_2, layout_search_line_3;
    /** 슬라이딩 패널 */
    private SlidingUpPanelLayout layout_slide;
    /** 건물 검색 자동완성 텍스트뷰 */
    private AutoCompleteTextView autotext_building, autotext_building_from, autotext_building_to;
    /** 건물 정보 표시 텍스트뷰들 */
    private TextView text_building_no, text_building_name, text_building_name_eng, text_path, text_meter;
    /** 이미지뷰들 */
    private ImageView img_search, img_building_photo_1, img_smoke, img_parking;
    /** 계단/엘리베이터 체크박스 */
    private CheckBox Ckbox_stair, Ckbox_elevator;

    // 변수 선언
    /** 구글 맵 객체 */
    private GoogleMap mMap;
    /** 지도 중심 좌표 */
    private LatLng center;
    /** 건물명 어댑터 */
    private ArrayAdapter<String> stringadt_building;
    /** 강의실명 어댑터 */
    private ArrayAdapter<String> stringadt_rooms;
    /** 마커 리스트들 */
    private ArrayList<Marker> markers_smoking, markers_building, markers_parking;
    /** 경로 폴리라인 리스트 */
    private ArrayList<Polyline> polylines;
    /** 강의실명 리스트 */
    private ArrayList<String> ROOM_NAMES = new ArrayList<>();
    /** 정점(Vertex) 리스트 */
    private static ArrayList<Vertex> vertex = new ArrayList<>(Constants.TOTAL_NODES);
    /** 건물명 리스트 */
    private static ArrayList<String> BUILDING_NAMES = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // 초기화 작업 수행
        initializeLists();
        initializeCheckboxes();
        initializeMap();
        initializeViews();
        setupAdapters();
        setupClickListeners();
    }

    /**
     * 리스트 초기화
     */
    private void initializeLists() {
        markers_smoking = new ArrayList<>();
        markers_building = new ArrayList<>();
        markers_parking = new ArrayList<>();
        vertex = new ArrayList<>();
        polylines = new ArrayList<>();
        ROOM_NAMES = new ArrayList<>();
    }

    /**
     * 체크박스 초기화 및 설정
     */
    private void initializeCheckboxes() {
        try {
            Ckbox_stair = findViewById(R.id.Ckbox_stair);
            Ckbox_elevator = findViewById(R.id.Ckbox_elevator);
            
            if (Ckbox_stair != null && Ckbox_elevator != null) {
                Ckbox_stair.setChecked(false);
                Ckbox_elevator.setChecked(false);
                setupCheckBoxListeners();
            } else {
                Log.e("MapsActivity", "Failed to initialize checkboxes");
            }
        } catch (Exception e) {
            Log.e("MapsActivity", "Error initializing checkboxes: " + e.getMessage());
        }
    }

    /**
     * 지도 초기화 및 설정
     */
    private void initializeMap() {
        try {
            setVertex();
            loadRoomData();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "초기화 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        center = Constants.CAMPUS_CENTER;
    }

    /**
     * View 초기화
     */
    private void initializeViews() {
        // View 초기화 코드
        btn_parking_layout = findViewById(R.id.btn_parking_layout);
        btn_smoke_layout = findViewById(R.id.btn_smoke_layout);
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
    }

    /**
     * 어댑터 설정
     */
    private void setupAdapters() {
        stringadt_building = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, BUILDING_NAMES);
        stringadt_rooms = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ROOM_NAMES);
        autotext_building.setAdapter(stringadt_building);
        autotext_building_from.setAdapter(stringadt_rooms);
        autotext_building_to.setAdapter(stringadt_rooms);
    }

    /**
     * 정점(Vertex) 데이터 로드
     * vertex.txt 파일에서 건물 정보를 읽어와 Vertex 객체 생성
     */
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

    /**
     * 강의실 데이터 로드
     * room.txt 파일에서 강의실 정보를 읽어옴
     */
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

    /**
     * 구글 맵이 준비되면 호출되는 콜백
     * 지도 설정 및 마커 표시
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupMapSettings();
        setupMarkers();
        mMap.setOnMarkerClickListener(this);
    }

    /**
     * 지도 기본 설정
     */
    private void setupMapSettings() {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(center));
        mMap.setMinZoomPreference(17);
        mMap.setMaxZoomPreference(19);
        mMap.setLatLngBoundsForCameraTarget(Constants.CAMPUS_BOUNDS);
    }

    /**
     * 마커 설정
     */
    private void setupMarkers() {
        setupParkingMarkers();
        setupSmokingAreaMarkers();
        setupBuildingMarkers();
    }

    /**
     * 주차장 마커 설정
     */
    private void setupParkingMarkers() {
        BitmapDescriptor bitmap_parking = GetBitmapDescriptor(R.drawable.ic_mark_parking, 70);
        for(int i = 0; i < Constants.PARKING_POINTS.length; i+=2) {
            Marker m = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(Constants.PARKING_POINTS[i], Constants.PARKING_POINTS[i+1]))
                .icon(bitmap_parking)
                .visible(false));
            markers_parking.add(m);
        }
    }

    /**
     * 흡연 구역 마커 설정
     */
    private void setupSmokingAreaMarkers() {
        BitmapDescriptor bitmap_smoking = GetBitmapDescriptor(R.drawable.ic_mark_smoking, 70);
        for(int i = 0; i < Constants.SMOKING_AREA_POINTS.length; i+=2) {
            Marker m = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(Constants.SMOKING_AREA_POINTS[i], Constants.SMOKING_AREA_POINTS[i+1]))
                .icon(bitmap_smoking)
                .visible(false));
            markers_smoking.add(m);
        }
    }

    /**
     * 건물 마커 설정
     */
    private void setupBuildingMarkers() {
        BitmapDescriptor bitmap_building = GetBitmapDescriptor(R.drawable.ic_mark_building, 60);
        for(Vertex v : vertex) {
            if (v.id <= 22) { // 건물인 경우에만 마커 표시
                Marker m = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(v.latitude, v.longitude))
                    .icon(bitmap_building)
                    .visible(true));
                m.setTag(v.name);
                markers_building.add(m);
            }
        }
    }

    /**
     * 마커 표시/숨김 토글
     */
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

    /**
     * 마커 아이콘 생성
     */
    private BitmapDescriptor GetBitmapDescriptor(@DrawableRes int id, int size) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth() * size / 100,
                vectorDrawable.getIntrinsicHeight() * size / 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * 키보드 숨김
     */
    private void HideKeyboard(){
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 위치 문자열을 노드 번호로 변환
     */
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

    /**
     * 경로 찾기 버튼 클릭 처리
     */
    private void ClickFindRouteBtn(){
        HideKeyboard();
        layout_slide.setPanelHeight(0);
        layout_search_line_1.setVisibility(View.INVISIBLE);
        layout_search_line_2.setVisibility(View.VISIBLE);
        autotext_building_to.setText(autotext_building.getText());
        autotext_building.setText("");
    }

    /**
     * 건물 정보 표시
     */
    private void DisplayBuildingInfo(){
        String text = autotext_building.getText().toString();
        int index;
        Context context = getApplicationContext();
        if((index = convert(text)) != -1){
            HideKeyboard();
            layout_slide.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

            // 경로 정보 초기화
            initPathDisplay();

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

    /**
     * 화면 초기화
     */
    public void initDisplay(){
        HideKeyboard();
        layout_slide.setPanelHeight(0);
        layout_search_line_1.setVisibility(View.VISIBLE);
        layout_search_line_2.setVisibility(View.INVISIBLE);
        layout_bottom_btns.setVisibility(View.VISIBLE);
    }

    /**
     * 경로 표시 초기화
     */
    public void initPathDisplay() {
        initPolylines();
        layout_search_line_3.setVisibility(View.INVISIBLE);
        text_path.setText("");
        text_meter.setText("");
    }

    /**
     * 폴리라인 초기화
     */
    public void initPolylines(){
        for(Polyline p : polylines){
            p.remove();
        }
    }

    /**
     * 뒤로가기 버튼 처리
     */
    @Override
    public void onBackPressed() {
        if(layout_search_line_2.getVisibility() == View.VISIBLE){
            initPathDisplay();
        }
        else if(layout_search_line_3.getVisibility() == View.VISIBLE){
            initPathDisplay();
        }
        else{
            super.onBackPressed();
        }
    }

    /**
     * 마커 클릭 이벤트 처리
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        String tag = (String) marker.getTag();
        if(tag != null){
            autotext_building.setText(tag);
            DisplayBuildingInfo();
        }

        return false;
    }

    /**
     * 클릭 리스너 설정
     */
    private void setupClickListeners() {
        // 주차장 마커 토글
        btn_parking_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToggleMarkersVisibility(markers_parking);
                if(markers_parking.get(0).isVisible()){
                    btn_parking_layout.setBackgroundResource(R.drawable.rounded_parking_active_background);
                }
                else{
                    btn_parking_layout.setBackgroundResource(R.drawable.rounded_white_button_background);
                }
            }
        });
        
        // 흡연구역 마커 토글
        btn_smoke_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToggleMarkersVisibility(markers_smoking);
                if(markers_smoking.get(0).isVisible()){
                    btn_smoke_layout.setBackgroundResource(R.drawable.rounded_smoke_active_background);
                }
                else{
                    btn_smoke_layout.setBackgroundResource(R.drawable.rounded_white_button_background);
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

        imgbtn_find_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClickFindRouteBtn();
            }
        });

        imgbtn_find_route_daijkstra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String start = autotext_building_from.getText().toString();
                String end = autotext_building_to.getText().toString();
                if (!start.isEmpty() && !end.isEmpty()) {
                    calculateAndDisplayPath(start, end);
                }
            }
        });

        autotext_building_from.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HideKeyboard();
            }
        });

        autotext_building_to.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HideKeyboard();
            }
        });

        Ckbox_stair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recalculatePath();
            }
        });

        Ckbox_elevator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recalculatePath();
            }
        });

        // 슬라이드 업 패널 상태 변경 리스너
        layout_slide.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                // 패널이 슬라이드될 때 호출
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    // 패널이 닫혔을 때
                    initDisplay();
                }
            }
        });
    }

    /**
     * 경로 계산 및 표시
     */
    private void calculateAndDisplayPath(String startLocation, String endLocation) {
        if (startLocation.isEmpty() || endLocation.isEmpty()) {
            return;
        }

        // 건물명과 호수 분리
        String[] startParts = parseLocation(startLocation);
        String[] endParts = parseLocation(endLocation);
        
        // 건물명으로 vertex 찾기
        int start = convert(startParts[0]);
        int end = convert(endParts[0]);
        
        if (start == -1 || end == -1) {
            Toast.makeText(this, "출발지 또는 도착지를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        initPolylines();

        // 층 정보 파싱 및 시간 계산
        int startFloor = parseFloor(startLocation);
        int endFloor = parseFloor(endLocation);
        int floorTime = calculateFloorTime(startFloor, endFloor);

        HideKeyboard();
        Dijkstra path = Dijkstra.getInstance(start, end);

        try {
            setVertex();
            path.calculateShortestPath(getApplicationContext(), !Ckbox_stair.isChecked(), vertex);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "경로 계산 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 경로 그리기
        drawPath(vertex, path.getPathNode());

        // 경로 정보 표시
        displayPathInfo(path.getPathNode());
        
        // 거리와 시간 계산 및 표시
        displayDistanceAndTime(path.getMeter(), path.getTime(), floorTime);
        
        // 결과 패널 표시
        layout_search_line_3.setVisibility(View.VISIBLE);
    }

    /**
     * 층수 파싱
     */
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

    /**
     * 위치 문자열 파싱
     */
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

    /**
     * 층간 이동 시간 계산
     */
    private int calculateFloorTime(int startFloor, int endFloor) {
        int floorDiff = Math.abs(startFloor - endFloor);
        if (floorDiff == 0) return 0;

        // 엘레베이터 체크박스가 체크된 경우에만 엘레베이터 시간 적용
        if (Ckbox_elevator.isChecked()) {
            return floorDiff * 15;  // 엘레베이터는 층당 15초
        }
        
        // 엘레베이터가 체크되지 않은 경우 계단 시간 적용
        return floorDiff * 30;  // 계단은 층당 30초
    }

    /**
     * 경로 그리기
     */
    private void drawPath(ArrayList<Vertex> vertex, String pathNode) {
        if (pathNode == null || pathNode.isEmpty()) return;

        String[] nodes = pathNode.split(" ");
        for (int i = 0; i < nodes.length - 1; i++) {
            int preVertexNum = Integer.parseInt(nodes[i]);
            int postVertexNum = Integer.parseInt(nodes[i + 1]);
            
            PolylineOptions options = createPolylineOptions(
                vertex.get(preVertexNum),
                vertex.get(postVertexNum)
            );
            
            polylines.add(mMap.addPolyline(options));
        }
    }

    /**
     * 폴리라인 옵션 생성
     */
    private PolylineOptions createPolylineOptions(Vertex start, Vertex end) {
        boolean isBuildingPath = start.id <= 22 && end.id <= 22;
        int color = isBuildingPath ? Color.RED : Color.BLUE;
        int width = isBuildingPath ? 8 : 6;
        
        return new PolylineOptions()
            .add(new LatLng(start.latitude, start.longitude),
                 new LatLng(end.latitude, end.longitude))
            .width(width)
            .color(color)
            .pattern(Arrays.asList(new Dash(30), new Gap(20)));
    }

    /**
     * 경로 정보 표시
     */
    private void displayPathInfo(String pathNode) {
        String[] nodes = pathNode.split(" ");
        StringBuilder pathInfo = new StringBuilder("최단 경로: ");
        
        for (int i = 0; i < nodes.length; i++) {
            int vertexIndex = Integer.parseInt(nodes[i]);
            Vertex currentVertex = vertex.get(vertexIndex);
            
            if (currentVertex.id <= 22) {
                pathInfo.append(currentVertex.name);
                if (i < nodes.length - 1 && hasNextBuilding(nodes, i)) {
                    pathInfo.append(" → ");
                }
            }
        }
        
        text_path.setText(pathInfo.toString());
    }

    /**
     * 다음 건물 존재 여부 확인
     */
    private boolean hasNextBuilding(String[] nodes, int currentIndex) {
        for(int j = currentIndex + 1; j < nodes.length; j++) {
            int nextVertexIndex = Integer.parseInt(nodes[j]);
            if(vertex.get(nextVertexIndex).id <= 22) {
                return true;
            }
        }
        return false;
    }

    /**
     * 거리와 시간 정보 표시
     */
    private void displayDistanceAndTime(int distance, int pathTime, int floorTime) {
        int totalSeconds = calculateTotalTime(distance, pathTime, floorTime);
        int totalMinutes = totalSeconds / 60;
        int remainingSeconds = totalSeconds % 60;
        
        text_meter.setText(String.format("%dm (%d분 %d초)", 
            distance, totalMinutes, remainingSeconds));
    }

    /**
     * 총 이동 시간 계산
     */
    private int calculateTotalTime(int distance, int pathTime, int floorTime) {
        if (Ckbox_stair.isChecked()) {
            double kickboardSpeed = 4.17; // m/s (15km/h)
            return (int)(distance / kickboardSpeed) + floorTime;
        }
        return pathTime + floorTime;
    }

    /**
     * 체크박스 리스너 설정
     */
    private void setupCheckBoxListeners() {
        Ckbox_stair.setOnCheckedChangeListener((buttonView, isChecked) -> recalculatePath());
        Ckbox_elevator.setOnCheckedChangeListener((buttonView, isChecked) -> recalculatePath());
    }

    /**
     * 경로 재계산
     */
    private void recalculatePath() {
        String startLocation = autotext_building_from.getText().toString();
        String endLocation = autotext_building_to.getText().toString();
        if (!startLocation.isEmpty() && !endLocation.isEmpty()) {
            calculateAndDisplayPath(startLocation, endLocation);
        }
    }
}