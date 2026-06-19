package com.example.project1.network;

import android.content.Context;

import com.example.project1.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/";

    public static ApiService create(Context context) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    String token = new SessionManager(context).getToken();

                    Request.Builder request = chain.request().newBuilder();

                    if (!token.isEmpty()) {
                        request.addHeader("Authorization", "Bearer " + token);
                    }

                    return chain.proceed(request.build());
                })
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);
    }
}
