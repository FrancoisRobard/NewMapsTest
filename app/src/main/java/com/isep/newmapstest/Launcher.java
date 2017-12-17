package com.isep.newmapstest;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class Launcher extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Identifying the element corresponding to our button
        Button btnMap = (Button) findViewById(R.id.button_display_map);
        btnMap.setOnClickListener(this);

        //Identifying the element corresponding to our button
        Button btnPlacePick = (Button) findViewById(R.id.button_display_place_picker);
        //Setting a click listener to make an action when the button is clicked
        btnPlacePick.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.button_display_map:
                // An intent is a set of data that can be passed from an app component to another
                // (each app component is running in a "sandbox" that lets it do an access only what we granted it. An intent is a way of communication between those components)
                Intent nMap = new Intent(Launcher.this, NewMaps.class);
                double accessibleKmsAroundUser = 1;
                nMap.putExtra("accessibleKms", 2);
                startActivity(nMap);
                break;
            case R.id.button_display_place_picker:
                Intent plcpck = new Intent(Launcher.this, PlacePicker.class);
                startActivity(plcpck);
                break;
        }
    }
}
