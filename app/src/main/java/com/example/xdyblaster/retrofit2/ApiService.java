package com.example.xdyblaster.retrofit2;

import com.example.xdyblaster.entity.User;
import com.example.xdyblaster.retrofit2.response.BaseResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {

    @GET("user/{userId}")
    Observable<BaseResponse<User>> login(@Path("userId") String userId);

    @GET("JustinRoom/JSCKit/master/capture/output.json")
    Observable<String> getVersionInfo();

}
