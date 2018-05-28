package com.iem.meteocaptor.ui.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iem.meteocaptor.R;
import com.iem.meteocaptor.data.manager.WeatherManager;
import com.iem.meteocaptor.data.model.WeatherModel;

import org.w3c.dom.Text;

import java.util.Date;

/**
 * Created by iem on 30/04/2018.
 */

public class LastMeasureFragment extends Fragment {
    private WeatherManager weatherManager = WeatherManager.getInstance();
    private Context context;
    private ImageView screenshot;
    private TextView dateTextView;
    private TextView temperatureTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        this.init();
        View view = inflater.inflate(R.layout.fragment_last_measure, container, false);
        screenshot = view.findViewById(R.id.fragment_last_measure_screenshot);
        screenshot.setImageResource(R.drawable.splash_screen);
        dateTextView = view.findViewById(R.id.fragment_last_measure_textview_date);
        temperatureTextView = view.findViewById(R.id.fragment_last_measure_textview_temperature);
        dateTextView.setText("Date : " + weatherManager.getLastMeasure().getDate());
        temperatureTextView.setText("Temperature : " + weatherManager.getLastMeasure().getTemperature());
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Last Measure Fragment");
    }


    private void init() {
       // weatherManager.setLastMeasure(new WeatherModel(new Date(), Math.random() * 100, Math.random() * 100, "Screenshot" + Math.random() * 1000));
    }

}
