package com.pinguela.rentexpress.rest.api.dto;

import java.util.List;

public class RecommendationResponseDTO {

    private List<Integer> recommendedVehicleIds;
    private String explanation;

    public RecommendationResponseDTO() {}

    public RecommendationResponseDTO(List<Integer> recommendedVehicleIds, String explanation) {
        this.recommendedVehicleIds = recommendedVehicleIds;
        this.explanation = explanation;
    }

    public List<Integer> getRecommendedVehicleIds() { return recommendedVehicleIds; }
    public void setRecommendedVehicleIds(List<Integer> recommendedVehicleIds) {
        this.recommendedVehicleIds = recommendedVehicleIds;
    }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
