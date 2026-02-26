package com.pinguela.rentexpress.rest.api.dto;

import java.math.BigDecimal;

public class VehicleSummaryDTO {

    private Integer vehicleId;
    private String brand;
    private String model;
    private String categoryName;
    private BigDecimal dailyPrice;

    public VehicleSummaryDTO() {}

    public VehicleSummaryDTO(Integer vehicleId, String brand, String model,
                             String categoryName, BigDecimal dailyPrice) {
        this.vehicleId = vehicleId;
        this.brand = brand;
        this.model = model;
        this.categoryName = categoryName;
        this.dailyPrice = dailyPrice;
    }

    public Integer getVehicleId() { return vehicleId; }
    public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getDailyPrice() { return dailyPrice; }
    public void setDailyPrice(BigDecimal dailyPrice) { this.dailyPrice = dailyPrice; }
}
