package com.example.menfer.youreyes_v.Activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.model.Text;
import com.example.menfer.youreyes_v.FixedValue;
import com.example.menfer.youreyes_v.R;
import com.example.menfer.youreyes_v.YeUtils.Base64Coder;
import com.example.menfer.youreyes_v.YeUtils.RoundImageView;
import com.example.menfer.youreyes_v.YeUtils.UrlUtils;

import net.sf.json.JSONObject;

/**
 * Created by Menfer on 2017/11/4.
 */
public class VolunteerActivity extends Activity {
    private String nickname;
    private String name;
    private String gender;
    private String teltphone;
    private String email;
    private String imageBase64;
    private Bitmap photo;

    private TextView tv_nickname;
    private TextView tv_name;
    private TextView tv_gender;
    private TextView tv_telephone;
    private TextView tv_email;
    private RoundImageView riv_photo;
    private Button btn_yes;

    //用于显示信息的handler
    Handler handle = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==0x123){
                tv_nickname.setText("用户名： "+nickname);
                tv_name.setText("姓   名： "+name);
                tv_gender.setText("性   别： "+gender);
                tv_telephone.setText("电   话： "+teltphone);
                tv_email.setText("邮   箱： "+email);
                riv_photo.setImageBitmap(photo);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer);
        initView();
        getAndShowInfo();
    }

    private void initView(){
        tv_nickname = (TextView)findViewById(R.id.volunteer_nickname);
        tv_name = (TextView)findViewById(R.id.volunteer_name);
        tv_gender = (TextView)findViewById(R.id.volunteer_gender);
        tv_telephone = (TextView)findViewById(R.id.volunteer_telephone);
        tv_email = (TextView)findViewById(R.id.volunteer_email);
        riv_photo = (RoundImageView)findViewById(R.id.volunteer_image);
        btn_yes = (Button)findViewById(R.id.volunteer_button);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

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
                    Toast.makeText(VolunteerActivity.this, "未知异常，请稍后重试", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else if(result.equals(FixedValue.ConnectionFailed)){
                    Looper.prepare();
                    Toast.makeText(VolunteerActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else if (FixedValue.tokenError(result)){
                    Looper.prepare();
                    Toast.makeText(VolunteerActivity.this, "登录信息验证失败，请重新登录", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else if (FixedValue.statusOK(result)){
                    JSONObject resultObj = JSONObject.fromObject(result);
                    imageBase64 = resultObj.getString("image");
                    teltphone = resultObj.getString("phone");
                    email = resultObj.getString("email");
                    name = resultObj.getString("name");
                    gender = resultObj.getString("gender");
                    nickname = resultObj.getString("nickname");
                    photo = Base64Coder.GenerateBitmap(imageBase64);
                    handle.sendEmptyMessage(0x123);
                }else {
                    Looper.prepare();
                    Toast.makeText(VolunteerActivity.this, "信息错误，请重新登录", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }.start();
    }
}
