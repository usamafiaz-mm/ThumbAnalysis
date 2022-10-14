package com.example.thumbanalysis.network_services;

import com.example.thumbanalysis.models.ResponseObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface RetrofitInterface {

    @GET("api/method/frappe.integrations.meta_api.get_url/")
    Call<ResponseObject> getFilePath(@Query("semis_code") String semisCode, @Query("Authorization") String token);

    @GET    Call<ResponseBody> downLoadFile(@Url String semis);

}
