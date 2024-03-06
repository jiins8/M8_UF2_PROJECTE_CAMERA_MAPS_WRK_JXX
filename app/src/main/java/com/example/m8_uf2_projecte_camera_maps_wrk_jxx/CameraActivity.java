package com.example.m8_uf2_projecte_camera_maps_wrk_jxx;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.example.m8_uf2_projecte_camera_maps_wrk_jxx.databinding.ActivityCameraBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    private ActivityCameraBinding binding;
    ImageButton cameraBtn, flipBtn, toggleFlash, backBtn;
    private PreviewView previewView;
    private ImageView previewImageView;
    int cameraOrientation = CameraSelector.LENS_FACING_BACK;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationProviderClient fusedLocationClient;


    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (result) {
            startCamera(cameraOrientation);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationPermission();

        previewView = binding.cameraPreview;
        previewImageView = binding.previewImageView;
        cameraBtn = binding.cameraBtn;
        flipBtn = binding.flipCamera;
        toggleFlash = binding.toggleFlash;

        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startCamera(cameraOrientation);
        }

        flipBtn.setOnClickListener(v -> {
            if (cameraOrientation == CameraSelector.LENS_FACING_BACK) {
                cameraOrientation = CameraSelector.LENS_FACING_FRONT;
            } else {
                cameraOrientation = CameraSelector.LENS_FACING_BACK;
            }
            startCamera(cameraOrientation);
        });

        backBtn = binding.backBtn;

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void startCamera(int cameraOrientation) {
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(this);

        listenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = listenableFuture.get();
                Preview preview = new Preview.Builder().build();


                ImageCapture imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraOrientation)
                        .build();

                cameraProvider.unbindAll();

                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                cameraBtn.setOnClickListener(v -> takePicture(imageCapture));
                toggleFlash.setOnClickListener(v -> setFlashIcon(camera));

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }



    private void takePicture(ImageCapture imageCapture) {
        final File file = new File(getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();

        imageCapture.takePicture(outputFileOptions, Executors.newCachedThreadPool(), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                runOnUiThread(() -> {
                    saveImageToFirestore(file);
                    startCamera(cameraOrientation);
                    showPreview(file.getPath());
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(CameraActivity.this, "Failed to save: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                startCamera(cameraOrientation);
            }
        });
    }

    private void saveImageToFirestore(File file) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        CollectionReference imagesRef = db.collection("images");

        String documentId = imagesRef.document().getId();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + userId + "/" + documentId + ".jpg");
        Uri fileUri = Uri.fromFile(file);

        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Map<String, Object> imageInfo = new HashMap<>();
                        imageInfo.put("imageUrl", uri.toString());
                        imageInfo.put("userId", userId);

                        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                            if (location != null) {
                                imageInfo.put("latitude", location.getLatitude());
                                imageInfo.put("longitude", location.getLongitude());
                            }

                            imagesRef.document(documentId)
                                    .set(imageInfo)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(CameraActivity.this, "Image uploaded to Firestore", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(CameraActivity.this, "Failed to upload image to Firestore", Toast.LENGTH_SHORT).show());
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CameraActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }

    private void showPreview(String imagePath) {
        previewImageView.setVisibility(View.VISIBLE);

        Picasso.get()
                .load(new File(imagePath))
                .into(previewImageView);


        new Handler().postDelayed(() -> {
            previewImageView.setVisibility(View.GONE);
            startCamera(cameraOrientation);
        }, 10000);
    }

    private void setFlashIcon(Camera camera) {
        if (camera.getCameraInfo().hasFlashUnit()) {
            if (camera.getCameraInfo().getTorchState().getValue() == 0) {
                camera.getCameraControl().enableTorch(true);
                toggleFlash.setImageResource(R.drawable.baseline_flash_on_24);
            } else {
                camera.getCameraControl().enableTorch(false);
                toggleFlash.setImageResource(R.drawable.baseline_flash_off_24);
            }
        } else {
            Toast.makeText(CameraActivity.this, "Flash is not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startCamera(cameraOrientation);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera(cameraOrientation);
            } else {
                Toast.makeText(this, "Location permission denied. Unable to proceed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void releaseCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                if (cameraProvider != null) {
                    cameraProvider.unbindAll();
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    protected void onDestroy() {
        releaseCamera();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        releaseCamera();
        super.onStop();
    }
}