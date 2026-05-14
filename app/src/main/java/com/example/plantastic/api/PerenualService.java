package com.example.plantastic.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface PerenualService {
    @GET("api/species-list")
    Call<PlantResponse> searchPlants(
            @Query("key") String apiKey,
            @Query("q") String query
    );

    @GET
    Call<PlantCareGuideResponse> getCareGuide(@Url String url);
}
