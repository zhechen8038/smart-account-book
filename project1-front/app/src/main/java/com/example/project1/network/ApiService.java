package com.example.project1.network;

import com.example.project1.model.AccountRecord;
import com.example.project1.model.BillRecognitionResponse;
import com.example.project1.model.LoginRequest;
import com.example.project1.model.LoginResponse;
import com.example.project1.model.RecordSummary;
import com.example.project1.model.RegisterRequest;
import com.example.project1.model.SaveRecordRequest;
import com.example.project1.model.UserResponse;
import com.example.project1.model.UpdateUserRequest;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/users/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/users/register")
    Call<UserResponse> register(@Body RegisterRequest request);

    @GET("api/users/me")
    Call<UserResponse> getCurrentUser();

    @PUT("api/users/me")
    Call<UserResponse> updateCurrentUser(@Body UpdateUserRequest request);

    @GET("api/records")
    Call<List<AccountRecord>> getRecords(@Query("month") String month);

    @GET("api/records/summary")
    Call<RecordSummary> getRecordSummary(@Query("month") String month);

    @GET("api/records/count")
    Call<Map<String, Long>> getRecordCount();

    @POST("api/records")
    Call<AccountRecord> createRecord(@Body SaveRecordRequest request);

    @PUT("api/records/{recordId}")
    Call<AccountRecord> updateRecord(
            @Path("recordId") Long recordId,
            @Body SaveRecordRequest request
    );

    @DELETE("api/records/{recordId}")
    Call<Map<String, String>> deleteRecord(
            @Path("recordId") Long recordId
    );

    @Multipart
    @POST("api/records/recognize")
    Call<BillRecognitionResponse> recognizeBill(
            @Part MultipartBody.Part image
    );
}
