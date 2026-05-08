package com.example.plantastic.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlantResponse {
    @SerializedName("data")
    private List<PlantData> data;

    public List<PlantData> getData() { return data; }

    public static class PlantData {
        private int id;
        @SerializedName("common_name")
        private String commonName;
        @SerializedName("scientific_name")
        private List<String> scientificName;
        @SerializedName("default_image")
        private DefaultImage defaultImage;

        public int getId() { return id; }
        public String getCommonName() { return commonName; }
        public List<String> getScientificName() { return scientificName; }
        public DefaultImage getDefaultImage() { return defaultImage; }
    }

    public static class DefaultImage {
        @SerializedName("thumbnail")
        private String thumbnail;
        public String getThumbnail() { return thumbnail; }
    }
}
