package com.iem.meteocaptor.data.manager;

import com.iem.meteocaptor.data.model.WeatherModel;

import java.util.ArrayList;
import java.util.List;

public class WeatherManager {

    private static WeatherManager instance  = null;
    private WeatherModel lastMeasure;
    private ArrayList<WeatherModel> archive = new ArrayList<>();

    public static WeatherManager getInstance() {
        if (instance == null) {
            instance = new WeatherManager();
        }
        return instance;
    }

    public void setLastMeasure(WeatherModel measure)
    {
        this.lastMeasure = measure;
    }

    public  WeatherModel getLastMeasure()
    {
        return lastMeasure;
    }

    public void setArchive(ArrayList<WeatherModel> archive)
    {
        this.archive = archive;
    }

    public List<WeatherModel> getArchive()
    {
        return archive;
    }

    public void addNewCapture(WeatherModel model){
        archive.add(model);
    }

}
