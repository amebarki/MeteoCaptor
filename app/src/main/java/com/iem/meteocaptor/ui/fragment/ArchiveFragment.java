package com.iem.meteocaptor.ui.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.iem.meteocaptor.R;
import com.iem.meteocaptor.data.model.WeatherModel;
import com.iem.meteocaptor.tools.CustomItemClickListener;
import com.iem.meteocaptor.ui.adapter.WeatherAdapter;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by iem on 30/04/2018.
 */

public class ArchiveFragment extends Fragment {

    private ArrayList<WeatherModel> listMeasure;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter recyclerViewAdapter;
    private RecyclerView.LayoutManager recyclerViewManager;
    private Context context;


    public static ArchiveFragment newInstance() {
        Bundle args = new Bundle();
        ArchiveFragment fragment = new ArchiveFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.context = null;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listMeasure = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_archive, container, false);
        this.initList();
        this.setRecyclerView(view);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Archive");

    }

    private void setRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.fragment_archive_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        final WeatherAdapter weatherAdapter = new WeatherAdapter(listMeasure, this, new CustomItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Toast.makeText(context, "position : " + position, Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(weatherAdapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void initList() {
        listMeasure = new ArrayList<>();
        listMeasure.add(new WeatherModel(new Date(),Math.random() * 100, Math.random() * 100, "Screenshot" + Math.random() * 1000));
        listMeasure.add(new WeatherModel(new Date(),Math.random() * 100, Math.random() * 100, "Screenshot" + Math.random() * 1000));
        listMeasure.add(new WeatherModel(new Date(),Math.random() * 100, Math.random() * 100, "Screenshot" + Math.random() * 1000));
        listMeasure.add(new WeatherModel(new Date(),Math.random() * 100, Math.random() * 100, "Screenshot" + Math.random() * 1000));
        listMeasure.add(new WeatherModel(new Date(),Math.random() * 100, Math.random() * 100, "Screenshot" + Math.random() * 1000));
        listMeasure.add(new WeatherModel(new Date(),Math.random() * 100, Math.random() * 100, "Screenshot" + Math.random() * 1000));
        listMeasure.add(new WeatherModel(new Date(),Math.random() * 100, Math.random() * 100, "Screenshot" + Math.random() * 1000));
        listMeasure.add(new WeatherModel(new Date(),Math.random() * 100, Math.random() * 100, "Screenshot" + Math.random() * 1000));
    }

}
