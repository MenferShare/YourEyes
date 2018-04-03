package com.example.menfer.youreyes_v;

import net.sf.json.JSONObject;

/**
 * Created by Menfer on 2017/5/3.
 * 软件中常用的固定的值以及表示用户当前状态的值
 */
public class FixedValue {
    final public static String serverIP = "http://192.168.43.208:8000/";     //服务器IP和监听端口
    //final public static String whatServerIP = "http://www.llsevenr.cn:5556/";  //whatseven服务器IP及端口
    final public static String whatServerIP = "http://www.llsevenr.cn:5556/";
    final public static String ConnectionFailed = "ConnectionFailed";         //连接失败
    final public static String ExceptionOccured = "ExceptionOccured";         //出现未知异常
    final public static String photo = "photo";                                  //用户头像文件存储和上传时名称
    final public static String noInput = "NoInput";                              //输入框为空

    public static int connectState = 0;                      //用户是否处于通信状态，志愿者在选择帮助对象后可通信，结束后不可通信


    public static String username = null;
    public static boolean pswChanged = false;   //登录密码是否被修改

    public static double latitude = 0;
    public static double longitude = 0;     //我当前的位置
    public static String myLocation="";        //我当前的地址
    public static String token = "eyJleHAiOjE1MDk4MTYxODMsImlhdCI6MTUwOTgwODk4MywiYWxnIjoiSFMyNTYifQ.eyJpYXQiOjE1MDk4MDg5ODMuMDQ3MTksInVzZXJuYW1lIjoiYWRtaW4ifQ.9_ILsczDEd71S6NMSzXF7_jHWNGmgvmJ9dMvk_Y2uVU";            //登录过后获取的token

    /**
     * 检查联网返回的结果是否是token错误
     * */
    public static boolean tokenError(String result){
        JSONObject jsonObject = JSONObject.fromObject(result);
        if (jsonObject.containsKey("error")){
            String error = jsonObject.getString("error");
            if(error.startsWith("token")){
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }
    }

    /**
     * 检查联网返回的结果是否是用户已存在
     * */
    public static boolean userExist(String result){
        JSONObject jsonObject = JSONObject.fromObject(result);
        if (jsonObject.containsKey("error")){
            String error = jsonObject.getString("error");
            if(error.startsWith("user")){
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }
    }

    /**
     * 检查联网返回的结果是否正确
     * */
    public static boolean statusOK(String result){
        JSONObject jsonObject = JSONObject.fromObject(result);
        if (jsonObject.getString("status")!=null){
            String status = jsonObject.getString("status");
            if(status.equals("ok")){
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }
    }

    /**
     * 获取联网错误后的error字段
     * */
    public static String getError(String result){
        JSONObject jsonObject = JSONObject.fromObject(result);
        String error = "未知错误";
        if (jsonObject.getString("error")!=null) {
            error = jsonObject.getString("error");
        }
        return error;
    }
}
