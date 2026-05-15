package com.example.plantastic.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface PerenualService {
    @GET("v2/species-list")
    Call<PlantResponse> searchPlants(
            @Query("key") String apiKey,
            @Query("q") String query
    );

    @GET("v2/species-list")
    Call<PlantResponse> getSpeciesList(
            @Query("key") String apiKey,
            @Query("page") int page
    );

    @GET("v2/species/details/{id}")
    Call<PlantResponse.PlantData> getSpeciesDetails(
            @Path("id") int id,
            @Query("key") String apiKey
    );

    @GET
    Call<PlantCareGuideResponse> getCareGuide(@Url String url);
}
