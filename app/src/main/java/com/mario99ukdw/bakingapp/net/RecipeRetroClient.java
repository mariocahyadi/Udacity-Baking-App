package com.mario99ukdw.bakingapp.net;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by mario99ukdw on 09.08.2017.
 */

public class RecipeRetroClient {
    private static final String ROOT_URL = "https://d17h27t6h515a5.cloudfront.net/";

    /**
     * Get Retrofit Instance
     */
    private static Retrofit getRetrofitInstance() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new LoggingInterceptor())
                .build();

        return new Retrofit.Builder()
                .baseUrl(ROOT_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Get API Service
     *
     * @return API Service
     */
    public static RecipeApiService getApiService() {
        return getRetrofitInstance().create(RecipeApiService.class);
    }
}
