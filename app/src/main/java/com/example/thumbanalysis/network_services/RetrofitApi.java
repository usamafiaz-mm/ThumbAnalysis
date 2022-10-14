package com.example.thumbanalysis.network_services;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitApi {
    public static RetrofitInterface getClient() {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        Retrofit adapter = new Retrofit.Builder()
                .baseUrl("https://ssdms.seld.gos.pk/").client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create()) //Set the Root URL
                .build(); //Finally building the adapter

        //Creating object for our interface
        RetrofitInterface api = adapter.create(RetrofitInterface.class);


        return  api;
    }

}
