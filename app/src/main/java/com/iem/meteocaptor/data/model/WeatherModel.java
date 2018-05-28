package com.iem.meteocaptor.data.model;

import android.graphics.Bitmap;

import java.util.Date;

public class WeatherModel {

    private Date date;
    private double temperature;
    private double humidity;
    private Bitmap screenshot;


    public WeatherModel(Date date,double temperature, double humidity, Bitmap screenshot) {
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

    public Bitmap getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(Bitmap screenshot) {
        this.screenshot = screenshot;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
