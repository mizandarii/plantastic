package com.example.plantastic.api;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class PlantResponse {
    @SerializedName("data")
    private List<PlantData> data;

    public List<PlantData> getData() { return data != null ? data : new ArrayList<>(); }

    public static class PlantData {
        private int id;
        @SerializedName("common_name")
        private String commonName;
        
        @SerializedName("scientific_name")
        private JsonElement scientificName;
        
        @SerializedName("default_image")
        private DefaultImage defaultImage;
        
        @SerializedName("sunlight")
        private JsonElement sunlight;
        
        @SerializedName("watering")
        private String watering;
        
        @SerializedName("family")
        private String family;

        public int getId() { return id; }
        public String getCommonName() { return commonName != null ? commonName : "Unknown"; }
        
        public List<String> getScientificName() {
            return convertJsonToList(scientificName);
        }
        
        public List<String> getSunlight() { 
            return convertJsonToList(sunlight);
        }

        private List<String> convertJsonToList(JsonElement element) {
            List<String> result = new ArrayList<>();
            if (element == null || element.isJsonNull()) return result;
            if (element.isJsonArray()) {
                for (JsonElement item : element.getAsJsonArray()) {
                    if (item != null && !item.isJsonNull()) result.add(item.getAsString());
                }
            } else if (element.isJsonPrimitive()) {
                result.add(element.getAsString());
            }
            return result;
        }

        public DefaultImage getDefaultImage() { return defaultImage; }
        public String getWatering() { return watering != null ? watering : "average"; }
        public String getFamily() { return family != null ? family : "General"; }
    }

    public static class DefaultImage {
        @SerializedName("thumbnail")
        private String thumbnail;
        public String getThumbnail() { return thumbnail; }
    }
}
