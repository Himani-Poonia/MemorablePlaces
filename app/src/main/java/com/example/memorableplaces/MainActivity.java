package com.example.memorableplaces;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import static java.util.Collections.reverse;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> listOfPlaces = new ArrayList<String>();
    static ArrayList<LatLng> locations = new ArrayList<LatLng>();
    static ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.memorableplaces", Context.MODE_PRIVATE);

        ArrayList<String> latitudes = new ArrayList<>();
        ArrayList<String> longitudes = new ArrayList<>();

        listOfPlaces.clear();
        latitudes.clear();
        longitudes.clear();
        locations.clear();

        try {

            listOfPlaces = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("places",ObjectSerializer.serialize(new ArrayList<String>())));

            latitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("latitude",ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("longitude",ObjectSerializer.serialize(new ArrayList<String>())));



        } catch (Exception e) {
            e.printStackTrace();
        }

        if(listOfPlaces.size() > 0 && latitudes.size() > 0 && longitudes.size() > 0) {

            if(listOfPlaces.size() == latitudes.size() && latitudes.size() == longitudes.size()) {

                for(int i = 0;i < latitudes.size(); i++) {

                    locations.add(new LatLng(Double.parseDouble(latitudes.get(i)), Double.parseDouble(longitudes.get(i))));

                }

            }

        } else {   //this means that the app is opened for the first time

            listOfPlaces.add("Add a new place...");
            locations.add(new LatLng(0,0));

        }

        ListView placeListView = findViewById(R.id.placeListView);

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,listOfPlaces);
        placeListView.setAdapter(arrayAdapter);

        placeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("placeNumber",position);
                startActivity(intent);
            }
        });
    }
}