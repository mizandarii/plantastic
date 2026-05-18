package com.example.plantastic.api;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public final class PerenualClient {

    private static final String BASE_URL = "https://perenual.com/api/";
    private static final long TIMEOUT_SECONDS = 30L;

    private static volatile Retrofit retrofit;
    private static volatile PerenualService service;

    private PerenualClient() {
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            synchronized (PerenualClient.class) {
                if (retrofit == null) {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                            .callTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static PerenualService getService() {
        if (service == null) {
            synchronized (PerenualClient.class) {
                if (service == null) {
                    service = getRetrofit().create(PerenualService.class);
                }
            }
        }
        return service;
    }
}
