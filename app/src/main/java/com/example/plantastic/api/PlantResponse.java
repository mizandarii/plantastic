package com.example.plantastic.api;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlantResponse implements Serializable {
    @SerializedName("data")
    private List<PlantData> data;

    public List<PlantData> getData() { return data != null ? data : new ArrayList<>(); }

    public static class PlantData implements Serializable {
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

        @SerializedName("care_level")
        private String careLevel;

        @SerializedName("poisonous_to_pets")
        private JsonElement poisonousToPets;

        @SerializedName("care_guides")
        private String careGuides;

        public int getId() { return id; }
        public String getCommonName() { return commonName != null ? commonName : "Unknown"; }
        
        public List<String> getScientificName() {
            return convertJsonToList(scientificName);
        }
        
        public List<String> getSunlight() { 
            return convertJsonToList(sunlight);
        }

        /**
         * Returns sunlight level from 1 to 4.
         */
        public int getSunlightLevel() {
                    List<String> list = getSunlight();
                    if (list.isEmpty()) return 2;
                    String sun = list.get(0).toLowerCase();

                    // Try to parse explicit numeric levels if present
                    for (char c : sun.toCharArray()) {
                        if (c >= '1' && c <= '4') {
                            return Character.getNumericValue(c);
                        }
                    }

                    // Common phrase matching
                    if (sun.contains("full sun") || sun.contains("full_sun") || sun.contains("very bright") || sun.contains("high")) return 4;
                    if (sun.contains("part sun") || sun.contains("part_sun") || sun.contains("sun-part shade") || sun.contains("moderate") || sun.contains("medium")) return 3;
                    if (sun.contains("part shade") || sun.contains("part_shade") || sun.contains("partial shade") || sun.contains("low")) return 2;
                    if (sun.contains("full shade") || sun.contains("full_shade") || sun.contains("shade")) return 1;

                    return 2; // default
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
        public String getCareLevel() { return careLevel != null ? careLevel : "Unknown"; }
        public String getCareGuides() { return careGuides; }

        public String getPoisonousToPetsText() {
            if (poisonousToPets == null || poisonousToPets.isJsonNull()) return "Unknown";
            if (poisonousToPets.isJsonPrimitive()) {
                if (poisonousToPets.getAsJsonPrimitive().isBoolean()) {
                    return poisonousToPets.getAsBoolean() ? "Yes" : "No";
                }
                if (poisonousToPets.getAsJsonPrimitive().isNumber()) {
                    return poisonousToPets.getAsInt() != 0 ? "Yes" : "No";
                }
                String value = poisonousToPets.getAsString();
                if (value == null) return "Unknown";
                String normalized = value.trim().toLowerCase();
                if (normalized.equals("1") || normalized.equals("true") || normalized.equals("yes")) return "Yes";
                if (normalized.equals("0") || normalized.equals("false") || normalized.equals("no")) return "No";
                return value;
            }
            return poisonousToPets.toString();
        }
    }

    public static class DefaultImage implements Serializable {
        @SerializedName("thumbnail")
        private String thumbnail;
        public String getThumbnail() { return thumbnail; }
    }
}
