package com.example.menfer.youreyes_v.Activity;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menfer.youreyes_v.R;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessageBody;
import com.hyphenate.chat.EMVoiceMessageBody;

import java.io.IOException;
import java.util.List;

/**
 * Created by Menfer on 2017/11/4.
 */
public class ReceiceActivity extends Activity implements View.OnClickListener{
    private Button btn_listen;
    private TextView tv_status;
    private int messageNum;
    private List<EMMessage> messageList;
    String fileUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        initView();
        /*EMConversation conversation = EMClient.getInstance().chatManager().getConversation("111");
        messageNum = conversation.getUnreadMsgCount();
        tv_status.setText(Integer.toString(messageNum));*/
        //注册监听
        EMClient.getInstance().chatManager().addMessageListener(new EMMessageListener() {
            @Override
            public void onMessageReceived(List<EMMessage> list) {
                EMMessage message = list.get(0);
                EMFileMessageBody body = (EMFileMessageBody)message.getBody();
                fileUrl = body.getRemoteUrl();
                String user = message.getUserName();
                String type = message.getType().toString();
                tv_status.setText(type);
            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> list) {

            }

            @Override
            public void onMessageRead(List<EMMessage> list) {

            }

            @Override
            public void onMessageDelivered(List<EMMessage> list) {

            }

            @Override
            public void onMessageRecalled(List<EMMessage> list) {

            }

            @Override
            public void onMessageChanged(EMMessage emMessage, Object o) {

            }
        });
    }

    public void playUrl(String videoUrl)
    {
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(videoUrl);
            mediaPlayer.prepare();//prepare之后自动播放
            mediaPlayer.start();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void initView(){
        btn_listen = (Button)findViewById(R.id.btn_listen);
        tv_status = (TextView)findViewById(R.id.tv_status);
        btn_listen.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_listen:
                playUrl(fileUrl);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
