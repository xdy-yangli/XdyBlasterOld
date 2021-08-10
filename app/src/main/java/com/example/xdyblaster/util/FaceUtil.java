package com.example.xdyblaster.util;

import com.google.gson.Gson;

import java.util.LinkedHashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FaceUtil {
    private static String path = "http://pda.99mb.net:9010/api/pda/checkAllowDetonate";

    public FaceUtil() {
        super();
    }

    public static Response sendFaceMessage(LinkedHashMap<String, String> params) {
        Gson gson = new Gson();
        String jsonImgList = "";
        jsonImgList = gson.toJson(params);
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(jsonImgList, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(path)
                .addHeader("access-token","5464821313131")
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
    }



}
