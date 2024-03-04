package com.example.m8_uf2_projecte_camera_maps_wrk_jxx;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import android.widget.ImageButton;

import com.example.m8_uf2_projecte_camera_maps_wrk_jxx.databinding.RvImagesGridBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GalleryActivity extends AppCompatActivity {

    private RvImagesGridBinding binding;
    private FirebaseAuth auth;
    private ImageButton backBtn;
    FirebaseUser user;
    private static final String TAG = "GalleryActivity";
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = RvImagesGridBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else {

            recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            imageAdapter = new ImageAdapter();
            recyclerView.setAdapter(imageAdapter);

            loadImagesFromFirestore();
        }

        backBtn = binding.backBtn;

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadImagesFromFirestore() {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Replace "images" with the actual collection name where you store your images
        db.collection("images")
                .whereEqualTo("userId", userId) // You might need to adjust this based on your data model
                .addSnapshotListener((value, error) -> {
                    if (error != null) {

                        return;
                    }

                    if (value != null) {
                        List<String> imageUrls = new ArrayList<>();

                        for (DocumentChange dc : value.getDocumentChanges()) {
                            // Assuming you have a field named "imageUrl" in your Firestore document
                            String imageUrl = dc.getDocument().getString("imageUrl");
                            imageUrls.add(imageUrl);
                        }

                        // Update the adapter with the new data
                        imageAdapter.setImageUrls(imageUrls);
                    }
                });
    }

}