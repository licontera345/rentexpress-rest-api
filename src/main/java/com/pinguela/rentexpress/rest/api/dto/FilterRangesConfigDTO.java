package com.pinguela.rentexpress.rest.api.dto;

/**
 * Rangos por defecto para filtros de búsqueda (F12); el frontend puede consumir este endpoint
 * en lugar de hardcodear min/max de año, kilometraje y precio.
 */
public class FilterRangesConfigDTO {

    private RangeConfig manufactureYear;
    private RangeConfig mileage;
    private RangeConfig dailyPrice;

    public FilterRangesConfigDTO() {}

    public FilterRangesConfigDTO(RangeConfig manufactureYear, RangeConfig mileage, RangeConfig dailyPrice) {
        this.manufactureYear = manufactureYear;
        this.mileage = mileage;
        this.dailyPrice = dailyPrice;
    }

    public static class RangeConfig {
        private Integer min;
        private Integer max;

        public RangeConfig() {}

        public RangeConfig(Integer min, Integer max) {
            this.min = min;
            this.max = max;
        }

        public Integer getMin() { return min; }
        public void setMin(Integer min) { this.min = min; }
        public Integer getMax() { return max; }
        public void setMax(Integer max) { this.max = max; }
    }

    public RangeConfig getManufactureYear() { return manufactureYear; }
    public void setManufactureYear(RangeConfig manufactureYear) { this.manufactureYear = manufactureYear; }
    public RangeConfig getMileage() { return mileage; }
    public void setMileage(RangeConfig mileage) { this.mileage = mileage; }
    public RangeConfig getDailyPrice() { return dailyPrice; }
    public void setDailyPrice(RangeConfig dailyPrice) { this.dailyPrice = dailyPrice; }
}
