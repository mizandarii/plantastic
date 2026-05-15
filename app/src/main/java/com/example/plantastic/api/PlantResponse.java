package com.example.plantastic.api;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class PlantResponse implements Serializable {
    @SerializedName("data")
    private List<PlantData> data;

    public List<PlantData> getData() { return data != null ? data : new ArrayList<>(); }

    @SuppressWarnings("unused")
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

        @SerializedName("description")
        private String description;

        public int getId() { return id; }
        public String getCommonName() { return commonName != null ? commonName : "Unknown"; }
        
        public List<String> getScientificName() {
            return convertJsonToList(scientificName);
        }
        
        public List<String> getSunlight() { 
            return convertJsonToList(sunlight);
        }

        /**
         * Returns the highest sunlight level from 1 to 4.
         * Mapping: full_shade = 1, part_shade = 2, sun_part_shade = 3, full_sun = 4
         */
        public int getSunlightLevel() {
                    List<String> list = getSunlight();
                    if (list.isEmpty()) return 2;

                    int maxLevel = 0;
                    for (String sun : list) {
                        int level = mapSunlightToLevel(sun);
                        if (level > maxLevel) {
                            maxLevel = level;
                        }
                    }

                    return maxLevel > 0 ? maxLevel : 2; // default to 2 if no valid mapping found
        }

        private int mapSunlightToLevel(String sunlight) {
                    if (sunlight == null) return 0;
                    String normalized = sunlight.toLowerCase().trim();

                    // Exact mapping as specified
                    if (normalized.equals("full_sun") || normalized.contains("full sun")) return 4;
                    if (normalized.equals("sun_part_shade") || normalized.contains("sun-part shade")) return 3;
                    if (normalized.equals("part_shade") || normalized.contains("part shade")) return 2;
                    if (normalized.equals("full_shade") || normalized.contains("full shade")) return 1;

                    // Additional fallbacks for common variations
                    if (normalized.contains("very bright") || normalized.contains("high") || normalized.contains("bright")) return 4;
                    if (normalized.contains("moderate") || normalized.contains("medium")) return 3;
                    if (normalized.contains("partial") || normalized.contains("low")) return 2;
                    if (normalized.contains("shade") || normalized.contains("dim")) return 1;

                    return 0; // unknown
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
        public String getDescription() { return description != null ? description : ""; }

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

    @SuppressWarnings("unused")
    public static class DefaultImage implements Serializable {
        @SerializedName("thumbnail")
        private String thumbnail;
        public String getThumbnail() { return thumbnail; }
    }
}
