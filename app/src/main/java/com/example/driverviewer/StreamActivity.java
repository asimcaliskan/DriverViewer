package com.example.driverviewer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.location.Location;
import android.os.Bundle;
import android.util.Base64;
import android.util.Size;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class StreamActivity extends AppCompatActivity {
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
    protected FusedLocationProviderClient fusedLocationProviderClient;
    private PreviewView streamPreviewView = null;
    private Button startStopStreamButton = null;
    private Boolean isStreaming = false;
    private Boolean sendJSON = true;
    private Bitmap bitmap = null;
    private RequestQueue queue = null;
    private String URL = "http://192.168.1.52:5000/authenticate/";
    private TextView URL_textView;
    private TextView locationTextView;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationTextView = findViewById(R.id.textview_location);
        locationTextView.setText("LOCATION DATA");

        URL_textView = findViewById(R.id.textview_url);
        URL = getIntent().getStringExtra("URL");
        URL_textView.setText(URL);

        streamPreviewView = findViewById(R.id.preview_view_stream);
        streamPreviewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);

        startStopStreamButton = findViewById(R.id.button_start_stop_stream);
        startStopStreamButton.setOnClickListener(view -> {
            if (isStreaming) {
                startStopStreamButton.setText(R.string.start_stream);
            } else {
                startStopStreamButton.setText(R.string.stop_stream);
            }
            isStreaming = !isStreaming;
        });

        queue = Volley.newRequestQueue(this);

        cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider processCameraProvider = cameraProviderListenableFuture.get();
                bindPreview(processCameraProvider);
            } catch (ExecutionException | InterruptedException exception) {
                toastMaker("Error in cameraProviderListenableFuture.addListener" + exception);
            }
        }, ContextCompat.getMainExecutor(this));


    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        preview.setSurfaceProvider(streamPreviewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview);

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setTargetResolution(new Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();


        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), imageProxy -> {
            if (imageProxy.getFormat() == PixelFormat.RGBA_8888) {
                if (bitmap == null) {
                    bitmap = Bitmap.createBitmap(
                            imageProxy.getWidth(),
                            imageProxy.getHeight(),
                            Bitmap.Config.ARGB_8888
                    );
                }
                bitmap.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());
                if (isStreaming) {
                    try {
                        if (sendJSON){
                            sendJSON = false;
                            post();
                        }
                    } catch (JSONException e) {
                        toastMaker(e.toString());
                        e.printStackTrace();
                    }
                }
            }
            imageProxy.close();
        });
        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }

    private void post() throws JSONException {
        //GET LOCATION <
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationTextView.setText("CHECK LOCATION PERMISSIONS");
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                locationTextView.setText("Latitude:" + latitude + " Longitude:" + longitude);
                //Log.d("TAG", location.toString());
            }
        });
        //GET LOCATION >

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, stream);
        byte[] byteArray = stream.toByteArray();
        String imageString = Base64.encodeToString(byteArray, Base64.DEFAULT);
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt("image", imageString);
        jsonObject.put("latitude", latitude);
        jsonObject.put("longitude", longitude);
        jsonObject.put("time", Calendar.getInstance().getTime().toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonObject, response -> {
            //toastMaker("SUCCESS");
            sendJSON = true;
        }, error -> {
            toastMaker("ERROR " + error);
        });
        queue.add(jsonObjectRequest);
    }

    private void toastMaker(String text) {
        //Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}