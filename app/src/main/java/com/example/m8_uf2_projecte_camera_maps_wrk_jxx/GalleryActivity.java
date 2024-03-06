package com.example.m8_uf2_projecte_camera_maps_wrk_jxx;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.widget.GridView;
import android.widget.ImageButton;

import com.example.m8_uf2_projecte_camera_maps_wrk_jxx.databinding.GvImagesGridBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GalleryActivity extends AppCompatActivity {

    private GvImagesGridBinding binding;
    private FirebaseAuth auth;
    private ImageButton backBtn;
    FirebaseUser user;
    private static final String TAG = "GalleryActivity";
    private GridView gridView;
    private ImageAdapter imageAdapter;
    private List<String> imageUrls;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = GvImagesGridBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        gridView = binding.gridView;

        backBtn = binding.backBtn;

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });
        imageUrls = new ArrayList<>();
        fetchPhotosFromFirestore();
    }
    private void fetchPhotosFromFirestore() {
        db.collection("images")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String imageUrl = document.getString("imageUrl");
                            if (imageUrl != null) {
                                imageUrls.add(imageUrl);
                            }
                        }
                        updateGridView();
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void updateGridView() {
        imageAdapter = new ImageAdapter(this, imageUrls);
        gridView.setAdapter(imageAdapter);
    }
}