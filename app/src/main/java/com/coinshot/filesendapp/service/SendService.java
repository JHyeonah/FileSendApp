package com.coinshot.filesendapp.service;

import com.coinshot.filesendapp.model.Response;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface SendService {
    @Multipart
    @POST("/api/test/file")
    Call<Response> postFile(@Part MultipartBody.Part file);

    @GET("/api/test/file/{filename}")
    Call<Void> getFile(@Path("filename") String filename);

}
