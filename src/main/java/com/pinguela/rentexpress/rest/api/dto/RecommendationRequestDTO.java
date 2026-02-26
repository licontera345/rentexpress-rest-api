package com.pinguela.rentexpress.rest.api.dto;

import java.util.List;

public class RecommendationRequestDTO {

    private String destination;
    private String passengers;
    private String tripDuration;
    private String roadCondition;
    private List<VehicleSummaryDTO> vehicles;

    public RecommendationRequestDTO() {}

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getPassengers() { return passengers; }
    public void setPassengers(String passengers) { this.passengers = passengers; }

    public String getTripDuration() { return tripDuration; }
    public void setTripDuration(String tripDuration) { this.tripDuration = tripDuration; }

    public String getRoadCondition() { return roadCondition; }
    public void setRoadCondition(String roadCondition) { this.roadCondition = roadCondition; }

    public List<VehicleSummaryDTO> getVehicles() { return vehicles; }
    public void setVehicles(List<VehicleSummaryDTO> vehicles) { this.vehicles = vehicles; }
}
