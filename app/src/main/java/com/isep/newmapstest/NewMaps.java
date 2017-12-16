package com.isep.newmapstest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

// Import Google API Client
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
// Maps API Imports
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionApi;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
// Location & Places API imports
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
// Others imports
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class NewMaps extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    /** ********************************************************************************************
     *  Variables declaration
     * *********************************************************************************************
     */

    // Name of the class
    private static final String TAG = NewMaps.class.getSimpleName();

    // Google maps object
    private GoogleMap mMap;

    // The entry points to the Places API.
    private GoogleApiClient mGoogleApiClient;
    private GeoDataClient mGeoDataClient; // Google database on places
    private PlaceDetectionClient mPlaceDetectionClient; // geolocation provider object

    // Place Picker elements, usen to launch a google "place picker" activity
    int PLACE_PICKER_REQUEST = 1;
    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

    // Variable to use when a reference to the current activity is needed as a parameter in a nested method (in this case "this" does not refer to the current activity)
    Activity thisActivity = this;

    // Identifier for the location rights request (usen to identify the response corresponding to this request in the
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    // Default position for the Camera (Paris)
    private final LatLng defaultLocation = new LatLng(48.864716, 	2.349014);
    //Default zoom for the camera
    private static final int DEFAULT_ZOOM = 15;


    /** ********************************************************************************************
     *  Function called during the launch of the activity
     *  ********************************************************************************************
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_maps);

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
         *  Obtain the SupportMapFragment and get notified when the map is ready to be used.
         *  ***************************************************
         */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /** ***************************************************
         *  Setting up the action to perform when the little round floating button is clicked
         *  (for the moment display a toast, but it should be used to get current place and set the camera at a proper zoom on it)
         *  Obtain the SupportMapFragment and get notified when the map is ready to be used.
         *  ***************************************************
         */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        /** ***************************************************
         *  Creating an instance of the google API client
         *  ***************************************************
         */
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

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
     * Builds the map when the Google Play services client is successfully connected.
     * *********************************************************************************************
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /** ********************************************************************************************
     * Defining the action to do when the buttons of the toolbar are clicked
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


    /** ********************************************************************************************
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Paris, France.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     * *********************************************************************************************
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Paris and move the camera
        LatLng paris = defaultLocation;
        mMap.addMarker(new MarkerOptions().position(paris).title("Marker in Paris"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(paris,DEFAULT_ZOOM));//newLatLng(placeLatLng));
    }


    /** ********************************************************************************************
     * Method called when an element is clicked (the "view" element that was clicked is given as a parameter)
     * This methods needs to identify which element was clicked (by comparing the id of the clicked element with known IDs)
     * Depending on the element clicked we can then define the action to perform.
     * *********************************************************************************************
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.fab:
                displayToast("Floating action button clicked");
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    System.out.println("ACCESS_FINE_LOCATION permission missing");
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    // Asking the user the permission to access
                    ActivityCompat.requestPermissions(thisActivity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                    //}
                    //return;
                }else{

                    // Look for the Places around the user
                    List<String> filters=new ArrayList<>();
                    filters.add(String.valueOf(Place.TYPE_STREET_ADDRESS));

                    PlaceFilter placeFilter = new PlaceFilter(true,filters);


                    PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient,null ).;
                    result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                        @Override
                        public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                            System.out.println("onResult called ");
                            boolean cameraMoved = false;
                            for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                float placeLkyhd = placeLikelihood.getLikelihood();
                                Place place = placeLikelihood.getPlace();
                                LatLng placeLatLng = place.getLatLng();
                                    //Moving the camera to first returned place (most likely place) --> would be better with real place !! and blue current place marker
                                    if(!cameraMoved){
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng,DEFAULT_ZOOM));//newLatLng(placeLatLng));
                                        System.out.println("camera moved !");
                                        cameraMoved=true;
                                    }

                                String placeName = (String) place.getName();
                                List<Integer> placeType = place.getPlaceTypes();

                                String typeslist = "";
                                for(int type : placeType){
                                    typeslist=typeslist+";"+type;
                                }
                                System.out.println("Place : "+placeName+" has likelyhood : "+placeLkyhd+" and types : "+typeslist);
                                //Log.i(TAG, String.format("Place '%s' has likelihood: %g",placeLikelihood.getPlace().getName(),placeLikelihood.getLikelihood()));
                                mMap.addMarker(new MarkerOptions().position(placeLatLng).title(placeName));


                            }
                            likelyPlaces.release();
                        }
                    });
                }

                break;
        }
    }


    /** ********************************************************************************************
     * Method called when the result of a permission request is detected.
     * We can then define the action to do when the permission asked was granted, or when it was refused
     * *********************************************************************************************
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the location  task you need to do.
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    /** ********************************************************************************************
     * Method from abstract parent classes onConnectionFailedListener and onMapReadyCallback that must be implemented
     * (But i don't know what to put inside)
     * *********************************************************************************************
     */
    @Override
    public void onConnectionFailed(ConnectionResult result){
        displayToast("Connection failed");
    }
    @Override
    public void onConnectionSuspended(int i) {
        displayToast("Connection  suspended");
    }


    /** ********************************************************************************************
     *  Method is used to display the message in proper manner
     *  ********************************************************************************************"
     * @param message
     */
    private void displayToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        
    }


}
