package com.example.m8_uf2_projecte_camera_maps_wrk_jxx;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ImageButton backBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });

        retrieveAndAddMarkers();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    private void retrieveAndAddMarkers() {
        CollectionReference imagesRef = FirebaseFirestore.getInstance().collection("images");

        imagesRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Image> imageList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Image image = document.toObject(Image.class);
                            imageList.add(image);

                            LatLng imageLocation = new LatLng(image.getLatitude(), image.getLongitude());
                            addCustomMarker(imageLocation, image.getImageUrl());
                        }
                    } else {
                        Toast.makeText(this, "Failed to retrieve image data from Firestore", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error retrieving images", e);
                    Toast.makeText(this, "Error retrieving images: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void addCustomMarker(LatLng location, String imageUrl) {
        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .apply(new RequestOptions().override(100, 100))
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(resource);

                        mMap.addMarker(new MarkerOptions()
                                .position(location)
                                .title("Image Marker")
                                .icon(bitmapDescriptor));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }
}
