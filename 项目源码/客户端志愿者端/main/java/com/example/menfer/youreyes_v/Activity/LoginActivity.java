package com.example.menfer.youreyes_v.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.menfer.youreyes_v.FixedValue;
import com.example.menfer.youreyes_v.R;
import com.example.menfer.youreyes_v.YeUtils.Base64Coder;
import com.example.menfer.youreyes_v.YeUtils.UrlUtils;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

/**
 * Created by Menfer on 2017/10/29.
 */
public class LoginActivity extends Activity implements View.OnClickListener{

    private EditText et_username;
    private EditText et_password;
    private Button btn_register;
    private Button btn_login;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==0x123){
                Toast.makeText(LoginActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();

    }

    //初始化控件
    private void initView(){
        et_username = (EditText)findViewById(R.id.et_username);
        et_password = (EditText)findViewById(R.id.et_password);
        btn_login = (Button)findViewById(R.id.btn_login);
        btn_register = (Button)findViewById(R.id.btn_register);
        btn_login.setOnClickListener(this);
        btn_register.setOnClickListener(this);
    }

    //监听控件事件

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //注册
            case R.id.btn_register:
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_login:
                EMClient.getInstance().login(et_username.getText().toString().trim(), et_password.getText().toString().trim(), new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        EMClient.getInstance().groupManager().loadAllGroups();
                        EMClient.getInstance().chatManager().loadAllConversations();
                        //登录自己的服务器
                        new Thread(){
                            @Override
                            public void run() {
                                super.run();
                                JSONObject obj = new JSONObject();
                                obj.put("nickname",et_username.getText().toString().trim());
                                obj.put("password",et_password.getText().toString().trim());
                                String ret = UrlUtils.doPostJson(FixedValue.whatServerIP+"vol/token",obj);
                                if(ret.equals(FixedValue.ExceptionOccured)){
                                    Looper.prepare();
                                    Toast.makeText(LoginActivity.this, "未知异常，请稍后重试", Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }else if(ret.equals(FixedValue.ConnectionFailed)){
                                    Looper.prepare();
                                    Toast.makeText(LoginActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }else if (!FixedValue.statusOK(ret)){
                                    Looper.prepare();
                                    Toast.makeText(LoginActivity.this, FixedValue.getError(ret), Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }else if (FixedValue.tokenError(ret)){
                                    Looper.prepare();
                                    Toast.makeText(LoginActivity.this, "登录信息验证失败，请重新登录", Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }else {
                                    JSONObject resultObj = JSONObject.fromObject(ret);
                                    FixedValue.token = resultObj.getString("token");
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }.start();
                    }
                    @Override
                    public void onError(int i, String s) {
                        EMClient.getInstance().logout(true);
                        Message msg = new Message();
                        msg.what = 0x123;
                        msg.obj = new String("登录失败,请检查用户名和密码");
                        handler.sendMessage(msg);
                    }
                    @Override
                    public void onProgress(int i, String s) {
                    }
                });
                break;
            default:
                break;
        }
    }
}
