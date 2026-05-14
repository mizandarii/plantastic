package com.example.plantastic.api;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlantCareGuideResponse implements Serializable {
    @SerializedName("data")
    private List<CareGuideItem> data;

    public List<CareGuideItem> getData() {
        return data != null ? data : new ArrayList<>();
    }

    public static class CareGuideItem implements Serializable {
        @SerializedName("common_name")
        private String commonName;

        @SerializedName("scientific_name")
        private List<String> scientificName;

        @SerializedName("section")
        private List<CareGuideSection> section;

        public String getCommonName() {
            return commonName != null ? commonName : "Unknown";
        }

        public List<String> getScientificName() {
            return scientificName != null ? scientificName : new ArrayList<>();
        }

        public List<CareGuideSection> getSection() {
            return section != null ? section : new ArrayList<>();
        }
    }

    public static class CareGuideSection implements Serializable {
        @SerializedName("type")
        private String type;

        @SerializedName("description")
        private String description;

        public String getType() {
            return type != null ? type : "";
        }

        public String getDescription() {
            return description != null ? description : "";
        }
    }
}

