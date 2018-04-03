package com.example.menfer.youreyes_v.YeUtils;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

/**
 * Created by Menfer on 2017/4/27.
 */
public class ToastUtil {
    public static void show(Context context,String msg){
        Looper.prepare();
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        Looper.loop();
    }
}
