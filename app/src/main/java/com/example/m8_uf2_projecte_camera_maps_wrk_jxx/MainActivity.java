package com.example.m8_uf2_projecte_camera_maps_wrk_jxx;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.m8_uf2_projecte_camera_maps_wrk_jxx.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Button cameraBtn, galleryBtn, mapsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraBtn = binding.cameraBtn;
        galleryBtn = binding.galleryBtn;
        mapsBtn = binding.mapsBtn;

        cameraBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
            startActivity(intent);
            finish();
        });
        galleryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
            startActivity(intent);
            finish();
        });

        mapsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(intent);
            finish();
        });
    }
}