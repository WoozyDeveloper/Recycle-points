package com.woozydeveloper.locationapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    public ArrayList markers;
    String newline = System.getProperty("line.separator");
    Button button;
    Map <String, Integer> idMap; //primul este id-ul markerului, al doilea este indicele în arraylist

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        button = findViewById(R.id.buttonAboutColors);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setAdapter();
        getMarkers();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, PopColorsInfo.class));
            }
        });
    }

    void getMarkers() {
        if (internetIsConnected()) {
            final Task<Object> task = firebaseTask("getinfo");
            task.addOnCompleteListener(new OnCompleteListener<Object>() {
                @Override
                public void onComplete(@NonNull Task<Object> task) {
                    markers = (ArrayList) task.getResult();
                    addMarkersOnMap();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(),R.string.no_internet_connection, Toast.LENGTH_LONG).show();
        }
    }

    void addMarkersOnMap() {
        idMap = new HashMap<>();
        for (int i = 0; i < markers.size(); ++i) {
            Map<String, Object> mymap = (Map<String, Object>) markers.get(i);
            double latt = Double.parseDouble(mymap.get("latitude").toString());
            double lngt = Double.parseDouble(mymap.get("longitude").toString());
            String name = mymap.get("name").toString();
            ArrayList<Integer> percents = (ArrayList<Integer>)mymap.get("percents");
            LatLng latLng = new LatLng(latt, lngt);
            MarkerOptions options = new MarkerOptions().position(latLng).title(name);
            options.snippet("Baterii și acumulatori: " + percents.get(0) + "%" + newline +
                    "Electrice și electronice: " + percents.get(1) + "%" + newline +
                    "Becuri și neoane: " + percents.get(2) + "%");

            Marker mark = mMap.addMarker(options);
            String markId = mark.getId();
            idMap.put(markId, i);
        }
    }


    /**
     * acest adapter este pentru a putea afișa informațiile pe mai multe linii
     * fără ca acestea să fie trunchiate
     * <p>
     * sursa: https://stackoverflow.com/a/31852074/9235932
     */
    void setAdapter() {
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Context context = getApplicationContext(); //or getActivity(), YourActivity.this, etc.
                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());


                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                TextView bottom = new TextView(context);
                bottom.setText("Apasă pentru detalii de contact");
                bottom.setTypeface(null, Typeface.ITALIC);

                info.addView(title);
                info.addView(snippet);
                info.addView(bottom);

                final String marker_id = marker.getId();

                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        int myId =  idMap.get(marker_id);

                        Intent intent = new Intent(MapsActivity.this, PopContactInfo.class);

                        Map<String, Object> mymap = (Map<String, Object>) markers.get(myId);
                        Bundle bundle = new Bundle();
                        for (Map.Entry<String, Object> entry : mymap.entrySet()) {
                            bundle.putString(entry.getKey(), entry.getValue().toString());
                        }
                        intent.putExtras(bundle);
                        startActivity(intent);
                        marker.hideInfoWindow();
                    }
                });

                return info;
            }


        });
    }

    /**
     * această metodă este utilizată pentru a folosi alte culori decât cele din BitmapDescriptorFactory
     * din nefericire, nu se poate tine cont de Saturatie sau Valoare din sistemul HSV
     * ci doar de Hue
     */
    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }


    private Task<Object> firebaseTask(String function) {
        FirebaseFunctions mFunctions;
        mFunctions = FirebaseFunctions.getInstance();

        return mFunctions
                .getHttpsCallable(function)
                .call()
                .continueWith(new Continuation<HttpsCallableResult, Object>() {
                    @Override
                    public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        Object result = task.getResult().getData();
                        return result;
                    }
                });
    }

    public boolean internetIsConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }

}