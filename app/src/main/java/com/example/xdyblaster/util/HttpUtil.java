package com.example.xdyblaster.util;

import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class HttpUtil {
    private static String[] PATH = {"http://qq.mbdzlg.com/mbdzlgtxzx/servlet/DzlgMmxzJsonServlert",
            "http://qq.mbdzlg.com/mbdzlgtxzx/servlet/DzlgMmxzJsonServlert",
            "http://qq.mbdzlg.com/mbdzlgtxzx/servlet/DzlgSysbJsonServlert"};
    //  private static String[] PATH = {"http://qq.mbdzlg.com/mbdzlgtxzx/servlet/DzlgMmlxxzJsonServlert",
    //          "http://qq.mbdzlg.com/mbdzlgtxzx/servlet/DzlgMmlxxzJsonServlert"};
//    private static String[] PATH = {"http://47.106.243.39:9990",
//            "http://47.106.243.39:9990/api"};
    //   private static String[] PATH = {"http://192.168.125.115:9990",
    //           "http://192.168.0.105:9990/api"};
    // private static URL[] url = new URL[2];

    public HttpUtil() {
        super();
    }

    // 静态代码块实例化url
//    static {
//        try {
//            url[0] = new URL(PATH[0]);
//            url[1] = new URL(PATH[1]);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//    }

//    /**
//     * 发送消息体到服务端
//     *
//     * @param params
//     * @param encode
//     * @return
//     */

    public static Response sendPostMessage(int urlNum, String head, LinkedHashMap<String, String> params) {


        Gson gson = new Gson();
        String jsonImgList = "";
        jsonImgList = gson.toJson(params);
//        try {
//            jsonImgList = URLEncoder.encode(gson.toJson(params), "UTF-8");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //       String test="{\"fbh\", \"6070309507000\"，\"htid\", \"520101181218003\"，\"sbbh\", \"FooA1111101\"，\"xmbh\", \"520100X18121803\"，\"dwdm\", \"3701004200003\"}";
        //      String test="{\"sbbh\":\"F5300001671\",\"jd\":\"106.59774\",\"dwdm\":\"4526002200001\",\"wd\":\"23.90510\",\"uid\":\"5300421912300A364,5300421912300162A4\"}";
        //     String test="{\"fbh\":\"5300421912300\",\"sbbh\":\"F5300001671\",\"jd\":\"106.59774\",\"dwdm\":\"4526002200001\",\"wd\":\"23.90510\"}";

        try {
            jsonImgList = AndroidDes3Util.encode(jsonImgList, "jadl12345678912345678912");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //  jsonImgList=Base64Encoder.encode(test.getBytes());
        //  jsonImgList="oEWm475y+lcJybxbaESpK/aKBErZmi51n4kYP82DAkvxH0cXR0OKEldkr1mISGlEq4tOPDJYAbHqeoznJMWzRd3X9xSOB46J/C3Kgrs+0T4nMcavhYwv1NO6dt+Npea6XiQxnbsNqDd8QMIjufnnqsEeUbdnXkcHpXFwC8vgO/1omSbdrwX8WQUWg2dz3XSMP385bffwWWzsMxkZVJwyLQ==";
        //  String enc=" ";
        try {
            jsonImgList = URLEncoder.encode(jsonImgList, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        String send = head + jsonImgList;
        RequestBody requestBody = RequestBody.create(send, MediaType.parse("application/x-www-form-urlencoded"));
        Request request = new Request.Builder()
                .url(PATH[urlNum])//attachHttpGetParams(PATH, jsonImgList))
                .post(requestBody)
                .build();
        try {
            Response response = okHttpClient
                    .newCall(request)
                    .execute();
            if (response.isSuccessful())
                return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
//        Call call = okHttpClient.newCall(request);
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.d(TAG, "response: error");
//
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, okhttp3.Response response) throws IOException {
//                Log.e(TAG, "response: " + Objects.requireNonNull(response.body()).string());
//            }
//        });
//        return null
    }

    public static Response sendPostMessage(int urlNum, String head) {

        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(head, MediaType.parse("application/x-www-form-urlencoded"));
        Request request = new Request.Builder()
                .url(PATH[urlNum])//attachHttpGetParams(PATH, jsonImgList))
                .post(requestBody)
                .build();
        try {
            Response response = okHttpClient
                    .newCall(request)
                    .execute();
            if (response.isSuccessful())
                return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
//        Call call = okHttpClient.newCall(request);
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.d(TAG, "response: error");
//
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, okhttp3.Response response) throws IOException {
//                Log.e(TAG, "response: " + Objects.requireNonNull(response.body()).string());
//            }
//        });
//        return null
    }

    public static String attachHttpGetParams(String url, String str) {
        return url + "?" + str;
    }

}
