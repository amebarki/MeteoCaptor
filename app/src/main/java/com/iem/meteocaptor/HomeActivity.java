package com.iem.meteocaptor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class HomeActivity extends AppCompatActivity {


    private ImageView splashScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        splashScreen = findViewById(R.id.home_image_view);
        splashScreen.setImageResource(R.drawable.splash_screen);
    }
}
