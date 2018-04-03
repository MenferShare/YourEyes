package com.example.menfer.youreyes_v.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Text;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.example.menfer.youreyes_v.FixedValue;
import com.example.menfer.youreyes_v.R;
import com.example.menfer.youreyes_v.Service.LocationService;
import com.example.menfer.youreyes_v.YeUtils.Base64Coder;
import com.example.menfer.youreyes_v.YeUtils.RoundImageView;
import com.example.menfer.youreyes_v.YeUtils.ToastUtil;
import com.example.menfer.youreyes_v.YeUtils.UrlUtils;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Exchanger;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,GeocodeSearch.OnGeocodeSearchListener {

    MapView mapView = null;
    private UiSettings uiSettings;
    private MyLocationStyle myLocationStyle;
    private CameraUpdate cameraUpdate;
    private AMap aMap;
    private Handler handler;
    private Handler handlerInf;
    private GeocodeSearch geocodeSearch;
    private List<Marker> markers;
    private String nickname;
    private String imageBase64;
    private Bitmap photo;
    private RoundImageView riv_photo;
    private TextView tv_nickname;
    private NavigationView navigationView;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigationView = (NavigationView)findViewById(R.id.nav_view);
        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main);
        tv_nickname = (TextView)headerLayout.findViewById(R.id.tv_nicknameInNav);
        riv_photo = (RoundImageView)headerLayout.findViewById(R.id.riv_photoInNav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //地图显示与操作
        mapView = (MapView)findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(this);
        setLocationStyle();
        setCamera();
        aMap.setMyLocationEnabled(true);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.moveCamera(cameraUpdate);
        setUiSettingsStyle();
        startLocationService();
        getAndShowInfo();
        getPoints();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handlerInf.sendEmptyMessage(0x126);
            }
        }, 1000, 2000);

        //用于显示志愿者信息和注销的handler
        handlerInf = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what==0x124){
                    tv_nickname.setText(nickname);
                    riv_photo.setMaxWidth(100);
                    riv_photo.setMaxHeight(100);
                    riv_photo.setImageBitmap(photo);
                }else if(msg.what==0x125){
                    Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                    startActivity(intent);
                }else if(msg.what==0x126){
                    if(FixedValue.latitude!=0){
                        getPoints();
                    }/*else {
                        Toast.makeText(MainActivity.this, "location", Toast.LENGTH_SHORT).show();
                    }*/
                }
            }
        };

        //获取handle发送的消息将用户显示在地图上
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 0x123){
                    List<LatLng> latLngs = new ArrayList<LatLng>();
                    List<String> names = new ArrayList<String>();
                    JSONArray jsonArray = JSONArray.fromObject(msg.obj);
                    for(int i = 0; i<jsonArray.size();i++){
                        JSONObject obj = JSONObject.fromObject(jsonArray.get(i));
                        LatLng lat = new LatLng(obj.getDouble("latitude"),obj.getDouble("longitude"));
                        latLngs.add(lat);
                        names.add(obj.getString("username"));
                    }
                    markPoints(latLngs,names);
                }
            }
        };

        AMap.OnInfoWindowClickListener listener = new AMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                if(FixedValue.longitude != 0 && FixedValue.latitude != 0){
                    double latitude = marker.getPosition().latitude;
                    double longitude = marker.getPosition().longitude;
                    Intent intent = new Intent(MainActivity.this,PersonActivity.class);
                    Bundle data = new Bundle();
                    data.putString("nickname",marker.getTitle());
                    data.putDouble("latitude",latitude);
                    data.putDouble("longitude",longitude);
                    intent.putExtras(data);
                    startActivity(intent);
                }else {
                    Toast.makeText(MainActivity.this, "定位失败，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            }
        };


        //定位服务
        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if(location.getLongitude()!=0){
                    FixedValue.latitude = location.getLatitude();
                    FixedValue.longitude = location.getLongitude();
                    FixedValue.myLocation = "成都市";
                    JSONObject obj = new JSONObject();
                    obj.put("longitude",location.getLongitude());
                    obj.put("latitude",location.getLatitude());
                    obj.put("token",FixedValue.token);
                    String tempResult = UrlUtils.doPostJson(FixedValue.whatServerIP+"vol/position",obj);
                    if(!FixedValue.statusOK(tempResult)){
                        Looper.prepare();
                        Toast.makeText(MainActivity.this, FixedValue.getError(tempResult), Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }
        });
//绑定信息窗点击事件
        aMap.setOnInfoWindowClickListener(listener);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //左侧导航栏的按钮
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_inf) {
            //个人信息
            Intent intent = new Intent(MainActivity.this,VolunteerActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            //退出登录
            EMClient.getInstance().logout(true, new EMCallBack() {
                @Override
                public void onSuccess() {
                    handlerInf.sendEmptyMessage(0x125);
                }

                @Override
                public void onError(int i, String s) {
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, "登出失败，请稍后重试", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

                @Override
                public void onProgress(int i, String s) {

                }
            });
        } else if (id == R.id.nav_history) {
            //我的足迹
            Intent intent = new Intent(MainActivity.this,HistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_about) {
            //关于
            Intent intent = new Intent(MainActivity.this,AboutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        EMClient.getInstance().logout(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    //设置定位蓝点的style
    private void setLocationStyle(){
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(2000);
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//只定位一次。
        myLocationStyle.showMyLocation(true);
    }

    //设置UiSetting的状态
    private void setUiSettingsStyle(){
        uiSettings = aMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomPosition(1);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setScaleControlsEnabled(true);
    }

    //设置camera
    private void setCamera(){
        cameraUpdate = CameraUpdateFactory.zoomTo(15);
    }

    //启动传递当前定位的服务
    private void startLocationService(){
        final Intent locationIntent = new Intent(MainActivity.this, LocationService.class);
        startService(locationIntent);
    }

    //将获取的点标记在地图上
    private void markPoints(List<LatLng> latLngs, List<String> names){
        for(int i=0;i<latLngs.size();i++){
            Marker marker = aMap.addMarker(new MarkerOptions().position(latLngs.get(i)).title(names.get(i)));
        }
    }

    //从服务器获取用户点
    private void getPoints(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                JSONObject obj = new JSONObject();
                obj.put("token", FixedValue.token);
                String result = UrlUtils.doPostJson(FixedValue.whatServerIP+"vol/blindPosition",obj);
                if(result.equals(FixedValue.ConnectionFailed)){
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else if(result.equals(FixedValue.ExceptionOccured)){
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, "未知错误，请稍后重试", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else if(FixedValue.tokenError(result)){
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, "登录状态异常，请重新登录", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else if(!FixedValue.statusOK(result)){
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, "您的定位信息有误", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else{
                    JSONObject jsonObj = JSONObject.fromObject(result);
                    JSONArray jsonArray = jsonObj.getJSONArray("position");
                    Message msg = new Message();
                    msg.what = 0x123;
                    msg.obj = jsonArray;
                    handler.sendMessage(msg);
                }
            }
        }.start();
    }

    //获取地址
    private void getAddress(LatLng latLng){
        LatLonPoint point = new LatLonPoint(latLng.latitude,latLng.longitude);
        RegeocodeQuery query = new RegeocodeQuery(point,200,GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(query);
    }

    //获取志愿者信息放在导航栏
    private void getAndShowInfo(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                JSONObject obj = new JSONObject();
                obj.put("token", FixedValue.token);
                String result = UrlUtils.doPostJson(FixedValue.whatServerIP+"vol/inf",obj);
                if(result.equals(FixedValue.ExceptionOccured)){
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, "未知异常，请稍后重试", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else if(result.equals(FixedValue.ConnectionFailed)){
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else if (FixedValue.tokenError(result)){
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, "登录信息验证失败，请重新登录", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else if (FixedValue.statusOK(result)){
                    JSONObject resultObj = JSONObject.fromObject(result);
                    imageBase64 = resultObj.getString("image");
                    nickname = resultObj.getString("nickname");
                    photo = Base64Coder.GenerateBitmap(imageBase64);
                    handlerInf.sendEmptyMessage(0x124);
                }else {
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, "信息错误，请重新登录", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }.start();
    }

    //地理编码查询回调
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        //do nothing
    }

    //逆地理编码查询回调
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int code) {
        if(code == 1000){
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null){
                String address = result.getRegeocodeAddress().getFormatAddress();
                //后续操作暂时不写
            }else {
                //do nothing
            }
        }else {
            //do nothing
        }
    }
}
