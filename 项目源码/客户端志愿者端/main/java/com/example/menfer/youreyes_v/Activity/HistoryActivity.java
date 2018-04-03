package com.example.menfer.youreyes_v.Activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.menfer.youreyes_v.FixedValue;
import com.example.menfer.youreyes_v.R;
import com.example.menfer.youreyes_v.YeUtils.Base64Coder;
import com.example.menfer.youreyes_v.YeUtils.UrlUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Menfer on 2017/11/5.
 */
public class HistoryActivity extends Activity {
    private JSONArray historyList;
    private ListView lv_history;
    private List<Map<String,Object>> historyItems;
    private String[] startTime=new String[100];
    private String[] endTime=new String[100];
    private String[] startPosition=new String[100];
    private String[] endPosition=new String[100];
    private String[] imageBase64=new String[100];
    private String[] bli_username=new String[100];
    private Bitmap[] photo=new Bitmap[100];
    //显示所有历史信息
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==0x123){
                SimpleAdapter simpleAdapter = new SimpleAdapter(HistoryActivity.this,historyItems,
                        R.layout.history_item,
                        new String[]{"startTime","endTime","startPosition","endPosition","username"},
                        new int[]{R.id.item_startTime,R.id.item_endTime,R.id.item_startPosition,R.id.item_endPosition,R.id.item_username});
                lv_history.setAdapter(simpleAdapter);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        historyItems = new ArrayList<Map<String,Object>>();
        lv_history = (ListView)findViewById(R.id.lv_history);
        getHistory();
    }

    //获取该用户的历史公益信息
    private void getHistory(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                JSONObject obj = new JSONObject();
                obj.put("token", FixedValue.token);
                String result = UrlUtils.doPostJson(FixedValue.whatServerIP+"vol/history",obj);
                if(result.equals(FixedValue.ExceptionOccured)){
                    Looper.prepare();
                    Toast.makeText(HistoryActivity.this, "未知异常，请稍后重试", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else if(result.equals(FixedValue.ConnectionFailed)){
                    Looper.prepare();
                    Toast.makeText(HistoryActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else if (!FixedValue.statusOK(result)){
                    Looper.prepare();
                    Toast.makeText(HistoryActivity.this, FixedValue.getError(result), Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else if (FixedValue.tokenError(result)){
                    Looper.prepare();
                    Toast.makeText(HistoryActivity.this, "登录信息验证失败，请重新登录", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else {
                    JSONObject resultObj = JSONObject.fromObject(result);
                    historyList = resultObj.getJSONArray("inf");
                    for (int i=0;i<historyList.size();i++){
                        JSONObject jsonObject = historyList.getJSONObject(i);
                        startTime[i] = jsonObject.getString("startTime");
                        endTime[i] = jsonObject.getString("endTime");
                        startPosition[i] = jsonObject.getString("startPosition");
                        endPosition[i] = jsonObject.getString("endPosition");
                        bli_username[i] = jsonObject.getString("bli_username");
                        imageBase64[i] = jsonObject.getString("image");
                        photo[i] = Base64Coder.GenerateBitmap(imageBase64[i]);
                        Map<String,Object> item = new HashMap<String, Object>();
                        item.put("image",photo[i]);
                        item.put("startTime",startTime[i]);
                        item.put("endTime",endTime[i]);
                        item.put("startPosition",startPosition[i]);
                        item.put("endPosition",endPosition[i]);
                        item.put("username",bli_username[i]);
                        historyItems.add(item);
                    }
                    handler.sendEmptyMessage(0x123);
                }
            }
        }.start();
    }
}
