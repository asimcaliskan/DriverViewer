package com.example.driverviewer;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private String URL = "http://{ip}:{port}";
    private RequestQueue queue = null;
    private JSONArray responseJSONArray = null;
    private SupportMapFragment mapFragment = null;

    private List<JSONObject> jsonObjects = new ArrayList<JSONObject>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        URL = getIntent().getStringExtra("URL");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getIntent().getStringExtra("URL"));
        stringBuilder.append("/get");
        URL = stringBuilder.toString();
        queue = Volley.newRequestQueue(this);
        get();
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        //for (int i=0; i< responseJSON.length(); i++) {

        //}
    }

    private void get() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                response -> {
                    try {
                        JSONObject responseJSON = new JSONObject(response);
                        try {
                            responseJSONArray = responseJSON.getJSONArray("data");
                            for (int i = 0; i < responseJSONArray.length(); i++) {
                                JSONObject jsonObject = responseJSONArray.getJSONObject(i);
                                jsonObjects.add(jsonObject);
                                //String species = jsonObject.getString("time");
                                //Log.d("asda-***************", species);
                            }
                            assert mapFragment != null;
                            mapFragment.getMapAsync(this);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show());
        queue.add(stringRequest);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        String time = null;
        Double latitude = null;
        Double longitude = null;
        Boolean isEyeOpen = null;
        Boolean isLookingForward = null;
        try {
            for (int i = 0; i < jsonObjects.size(); i++) {
                time = jsonObjects.get(i).getString("time");
                latitude = jsonObjects.get(i).getDouble("latitude");
                longitude = jsonObjects.get(i).getDouble("longitude");
                isEyeOpen = jsonObjects.get(i).getBoolean("is_eye_open");
                isLookingForward = jsonObjects.get(i).getBoolean("is_looking_forward");

                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(time).snippet("IS EYE OPEN: " + isEyeOpen.toString() + " IS LOOKING FORWARD: " + isLookingForward));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(15)
                .build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        googleMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Toast.makeText(this,
                marker.getSnippet(),
                Toast.LENGTH_SHORT).show();
        return false;
    }
}
