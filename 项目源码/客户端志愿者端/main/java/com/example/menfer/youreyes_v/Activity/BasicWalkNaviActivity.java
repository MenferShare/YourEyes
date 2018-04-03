package com.example.menfer.youreyes_v.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.NaviLatLng;
import com.example.menfer.youreyes_v.FixedValue;
import com.example.menfer.youreyes_v.R;
import com.example.menfer.youreyes_v.YeUtils.UrlUtils;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseConstant;

import net.sf.json.JSONObject;


public class BasicWalkNaviActivity extends BaseActivity implements View.OnClickListener{

    private double destLongitude;
    private double destLatitude;
    private double myLongitude;
    private double myLatitude;
    private Button btn_startHelp;
    private Button btn_stopHelp;
    private Button btn_toConnect;
    private String bli_username;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==0x200){
                finish();
            }else if(msg.what==0x123){
                btn_startHelp.setVisibility(View.INVISIBLE);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walknavi);
        //获取传值
        Intent intent = getIntent();
        destLatitude = intent.getDoubleExtra("latitude",0);
        destLongitude = intent.getDoubleExtra("longitude",0);
        myLatitude = intent.getDoubleExtra("myLatitude",0);
        myLongitude = intent.getDoubleExtra("myLongitude",0);
        bli_username = intent.getStringExtra("bli_username");
        //初始化导航界面
        mAMapNaviView = (AMapNaviView) findViewById(R.id.naviMap);
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);
        initButton();

    }

    //初始化按钮
    private void initButton(){
        btn_startHelp = (Button)findViewById(R.id.btn_startHelp);
        btn_startHelp.setOnClickListener(this);
        btn_stopHelp = (Button)findViewById(R.id.btn_stopHelp);
        btn_stopHelp.setOnClickListener(this);
        btn_toConnect = (Button)findViewById(R.id.btn_toConnect);
        btn_toConnect.setOnClickListener(this);
    }

    //按钮点击监听


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_startHelp:
                //点击开始帮助按钮的操作，即已经抵达盲人处
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("token", FixedValue.token);
                        String reachRet = UrlUtils.doPostJson(FixedValue.whatServerIP+"vol/reachBlind",jsonObject);
                        handler.sendEmptyMessage(0x123);
                    }
                }.start();
                break;
            case R.id.btn_stopHelp:
                //结束的操作，帮助完成或取消操作，之后退出导航
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("token", FixedValue.token);
                        if(FixedValue.myLocation.length()==0){
                            jsonObject.put("position","未知地点");
                        }else {
                            jsonObject.put("position",FixedValue.myLocation);
                        }
                        String finishTaskRet = UrlUtils.doPostJson(FixedValue.whatServerIP+"vol/finishTask",jsonObject);
                        handler.sendEmptyMessage(0x200);
                    }
                }.start();
                break;
            case R.id.btn_toConnect:
                //跳转至通讯的操作
                Intent intent = new Intent(BasicWalkNaviActivity.this,ChatActivity.class);
                intent.putExtra(EaseConstant.EXTRA_USER_ID,bli_username);
                intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, EMMessage.ChatType.Chat);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onInitNaviSuccess() {
        mAMapNavi.calculateWalkRoute(new NaviLatLng(myLatitude,myLongitude),new NaviLatLng(destLatitude,destLongitude));
    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {
        super.onCalculateRouteSuccess(ints);
        mAMapNavi.startNavi(NaviType.GPS);
    }

}
