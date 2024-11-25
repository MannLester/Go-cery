package com.example.mobcompfinalproj;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 1001;
    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private BarcodeScanner scanner;
    private ProcessCameraProvider cameraProvider;
    private String lastScannedCode = ""; // Add this to track last scanned code
    private long lastScanTime = 0; // Add this to track time between scans
    private static final long SCAN_COOLDOWN_MS = 2000; // 2 seconds cooldown between scans

    @ExperimentalGetImage
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.viewFinder);

        // Request camera permission
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        }

        // Initialize barcode scanner
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        scanner = BarcodeScanning.getClient(options);

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    @ExperimentalGetImage
    private void startCamera() {
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(this).get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImage);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("MainActivity", "Error starting camera: ", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @ExperimentalGetImage
    private void analyzeImage(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage,
                    imageProxy.getImageInfo().getRotationDegrees());

            Task<List<Barcode>> result = scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String rawValue = barcode.getRawValue();
                            if (rawValue != null) {
                                long currentTime = System.currentTimeMillis();
                                // Only show Toast if it's a new code or enough time has passed
                                if (!rawValue.equals(lastScannedCode) &&
                                        (currentTime - lastScanTime) > SCAN_COOLDOWN_MS) {
                                    lastScannedCode = rawValue;
                                    lastScanTime = currentTime;
                                    runOnUiThread(() -> {
                                        Toast.makeText(MainActivity.this,
                                                "Scanned: " + rawValue,
                                                Toast.LENGTH_SHORT).show();
                                    });
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Log.e("MainActivity", "Barcode scanning failed: ", e))
                    .addOnCompleteListener(task -> imageProxy.close());
        } else {
            imageProxy.close();
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @ExperimentalGetImage
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this,
                        "Camera permission is required for QR scanning",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraProvider != null) {
            cameraProvider.unbindAll(); // Stop camera when app is paused
        }
        lastScannedCode = "";
        lastScanTime = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) {
            cameraProvider.unbindAll(); // Ensure camera is stopped
        }
        if (scanner != null) {
            scanner.close(); // Close the scanner
        }
        if (cameraExecutor != null) {
            cameraExecutor.shutdown(); // Shutdown the executor
        }
    }
}