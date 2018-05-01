package com.iem.meteocaptor.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iem.meteocaptor.R;
import com.iem.meteocaptor.data.model.WeatherModel;
import com.iem.meteocaptor.tools.CustomItemClickListener;
import com.iem.meteocaptor.ui.fragment.ArchiveFragment;

import java.util.ArrayList;
import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {


    private ArrayList<WeatherModel> listMeasure;
    private Context context;

    private CustomItemClickListener listener;


    public WeatherAdapter(List<WeatherModel> listMeasure, ArchiveFragment context, CustomItemClickListener listener) {
        this.listMeasure = new ArrayList<>();
        this.listMeasure.addAll(listMeasure);
        this.listener = listener;
    }

    @Override
    public WeatherAdapter.WeatherViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // create a new view
        final View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recycler_item_archive_weather, viewGroup, false);

        final WeatherAdapter.WeatherViewHolder myHolder = new WeatherAdapter.WeatherViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(v, myHolder.getLayoutPosition());
            }
        });

        return myHolder;
    }

    @Override
    public void onBindViewHolder(WeatherAdapter.WeatherViewHolder holder, int position) {
        holder.dateTextView.setText("Date" + listMeasure.get(position).getDate().toString());
        holder.humidityTextView.setText("Humidity" + listMeasure.get(position).getHumidity() +"");
        holder.temperatureTextView.setText("Temperature" + listMeasure.get(position).getTemperature() +"");
       // Picasso.with(context).load(pokemonArrayList.get(position).getSprite()).into(holder.imageView);
    }



    @Override
    public int getItemCount() {
        if (listMeasure == null) {
            return 0;
        }
        return listMeasure.size();
    }



    public static class WeatherViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView temperatureTextView;
        public TextView humidityTextView;
        public WeatherViewHolder(View itemView) {
            super(itemView);
            this.temperatureTextView = itemView.findViewById(R.id.item_archive_temperature) ;
            this.humidityTextView = itemView.findViewById(R.id.item_archive_humidity);
            this.dateTextView = itemView.findViewById(R.id.item_archive_date);
        }
    }



}
