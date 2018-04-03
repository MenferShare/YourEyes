package com.example.menfer.youreyes_v.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.menfer.youreyes_v.FixedValue;
import com.example.menfer.youreyes_v.YeUtils.UrlUtils;

import net.sf.json.JSONObject;

/**
 * Created by Menfer on 2017/10/14.
 */
public class LocationService extends Service {

    private AMapLocationClient aMapLocationClient;
    private AMapLocationClientOption aMapLocationClientOption;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initLocation();
        //startLocation();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(LocationService.this, "service", Toast.LENGTH_SHORT).show();
        startLocation();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocation();
        destroyLocaiton();
    }

    //初始化定位
    private void initLocation(){
        //初始化client
        aMapLocationClient = new AMapLocationClient(this.getApplicationContext());
        aMapLocationClientOption = getDefaultOption();
        aMapLocationClient.setLocationOption(aMapLocationClientOption);
        aMapLocationClient.setLocationListener(locationListener);
    }

    //定位设置
    private AMapLocationClientOption getDefaultOption(){
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(true);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }

    //定位监听
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if(null != aMapLocation){
                //获取定位后的操作，在此处执行将当前位置传到服务器的操作
                if(aMapLocation.getLongitude() == 0){
                    //gps信号不好并使用4g网络时有可能出现经纬度一直为0的状况，
                    // 此时处理方法为不对用户位置信息进行更新，但是对于这种情况
                    //暂时没找到解决办法，暂不做优化
                }else {
                    //获取定位成功时的操作
                    final AMapLocation location = aMapLocation;
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            JSONObject obj = new JSONObject();
                            obj.put("longitude",location.getLongitude());
                            obj.put("latitude",location.getLatitude());
                            obj.put("token",FixedValue.token);
                            FixedValue.longitude = location.getLongitude();
                            FixedValue.latitude = location.getLatitude();
                            FixedValue.myLocation = location.getAddress();
                            String tempResult = UrlUtils.doPostJson(FixedValue.whatServerIP+"vol/position",obj);

                            if(!FixedValue.statusOK(tempResult)){
                                Looper.prepare();
                                Toast.makeText(LocationService.this, FixedValue.getError(tempResult), Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }
                    }.start();
                }
            }else {
                //获取定位失败时的操作
                Toast.makeText(LocationService.this,"定位失败", Toast.LENGTH_SHORT).show();
            }
        }
    };

    //开始定位
    private void startLocation(){
        aMapLocationClient.startLocation();
    }

    //停止定位
    private void stopLocation(){
        aMapLocationClient.stopLocation();
    }

    //销毁定位
    private void destroyLocaiton(){
        aMapLocationClient.onDestroy();
        aMapLocationClient = null;
    }
}
