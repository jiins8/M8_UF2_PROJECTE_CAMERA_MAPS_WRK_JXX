package com.example.m8_uf2_projecte_camera_maps_wrk_jxx;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
                            mMap.addMarker(new MarkerOptions()
                                    .position(imageLocation)
                                    .title("Image Marker"));
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



}
