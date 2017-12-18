package com.isep.newmapstest;

import android.app.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Response;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class PlaceDetails extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    /** ********************************************************************************************
     *  Variables declaration
     * *********************************************************************************************
     */

    // ID of the place to show
    private String placeID;
    // Place to show
    private Place shownPlace;

    // Name of the class
    private static final String TAG = NewMaps.class.getSimpleName();
    // Variable to use when a reference to the current activity is needed as a parameter in a nested method (in this case "this" does not refer to the current activity)
    Activity thisActivity = this;

    // The entry points to the Maps & Places API.
    private GoogleMap mMap;                     // Google maps object
    private GoogleApiClient mGoogleApiClient;   // Connexion to google maps API
    private GeoDataClient mGeoDataClient;       // Gives access to places API database
    private PlaceDetectionClient mPlaceDetectionClient; // Allow to find the places around the user
    private FusedLocationProviderClient mFusedLocationProviderClient; // An object to retrieve current position


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        // Retrieve the ID of the event to display
        placeID = getIntent().getStringExtra( "placeID");

        /** **************************************************
         *  Setting up the toolbar
         *  **************************************************
         */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // To add a return button (arrow) on the left of the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Smart City Traveller");


        /** ***************************************************
         *  Creating an instance of the google API client
         *  ***************************************************
         */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeID).setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                            shownPlace = places.get(0);
                            Log.i(TAG, "Place found: " + shownPlace.getName());
                            setDescription();
                        } else {
                            Log.e(TAG, "Place not found");
                        }
                        places.release();
                    }
                });

    }


    /** ********************************************************************************************
     * Method to add icons to the toolbar
     * *********************************************************************************************
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_maps, menu);
        return true;
    }


    /** ********************************************************************************************
     * Function that activates the return button on the toolbar to actually perform a return action.
     * *********************************************************************************************
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    /** ********************************************************************************************
     * Defining the actions to perform when the buttons of the toolbar are clicked
     * *********************************************************************************************
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private void setDescription(){
        // Getting the widget for the place title and replacing its content by the title we want
        TextView title_view = (TextView) findViewById(R.id.Title);
        String title = (String) shownPlace.getName();
        title_view.setText(title);

        // setting the address
        TextView address_view = (TextView) findViewById(R.id.Address);
        String address = (String) shownPlace.getAddress();
        address_view.setText(address);

        // setting the ratings
        TextView ratings_view = (TextView) findViewById(R.id.Ratings);
        String ratings = Float.toString(shownPlace.getRating());
        ratings_view.setText(ratings);

        // Setting the phone number
        TextView phone_view = (TextView) findViewById(R.id.Phone);
        String phone = (String) shownPlace.getPhoneNumber();
        phone_view.setText(phone);

        // Setting the WebSite URL
        TextView webSite_view = (TextView) findViewById(R.id.webSite);
        Uri webSite = shownPlace.getWebsiteUri();
        webSite_view.setText(webSite.toString());


    }



}
