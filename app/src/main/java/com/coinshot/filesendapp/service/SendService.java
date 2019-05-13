package com.coinshot.filesendapp;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface SendService {
    @Multipart
    @POST("/api/test/file")
    Call<Response> postFile(@Part MultipartBody.Part file);

}
