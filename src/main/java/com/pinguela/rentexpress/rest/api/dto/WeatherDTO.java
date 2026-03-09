package com.pinguela.rentexpress.rest.api.dto;

public class WeatherDTO {

    private String city;
    private double temp;
    private double tempMin;
    private double tempMax;
    private int humidity;
    private String description;
    private String icon;
    /** Si false, el servicio externo no está disponible (graceful degradation). */
    private boolean available = true;
    /** Mensaje amigable cuando available es false. */
    private String message;

    public WeatherDTO() {}

    public WeatherDTO(String city, double temp, double tempMin, double tempMax,
                      int humidity, String description, String icon) {
        this.city = city;
        this.temp = temp;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.humidity = humidity;
        this.description = description;
        this.icon = icon;
    }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public double getTemp() { return temp; }
    public void setTemp(double temp) { this.temp = temp; }

    public double getTempMin() { return tempMin; }
    public void setTempMin(double tempMin) { this.tempMin = tempMin; }

    public double getTempMax() { return tempMax; }
    public void setTempMax(double tempMax) { this.tempMax = tempMax; }

    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
