package com.iem.meteocaptor.data.model;

import java.util.Date;

public class WeatherModel {

    private Date date;
    private double temperature;
    private double humidity;
    private String screenshot;


    public WeatherModel(Date date,double temperature, double humidity, String screenshot) {
        this.date = date;
        this.temperature = temperature;
        this.humidity = humidity;
        this.screenshot = screenshot;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public String getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(String screenshot) {
        this.screenshot = screenshot;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
