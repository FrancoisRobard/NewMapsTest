package com.isep.newmapstest;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

// Import Google API Client
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
// Maps API Imports
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionApi;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
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

    // Variable to store the current position of the camera (= display window)
    private CameraPosition mCameraPosition;

    // The entry points to the Places API.
    private GoogleApiClient mGoogleApiClient;
    private GeoDataClient mGeoDataClient; // Gives access to places API database
    private PlaceDetectionClient mPlaceDetectionClient; // Allow to find the places around the user

    // An object to retrieve current position
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // Default position for the Camera (Paris)
    private final LatLng mDefaultLocation = new LatLng(48.864716, 	2.349014);
    //Default zoom for the camera
    private static final int DEFAULT_ZOOM = 15;

    // Identifier for the location rights request (usen to identify the response corresponding to this request in the onRequestPermissionsResult method
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;

    // Flag to indicates if the access to location was granted (if no we will ask for permission, if refused we will use a default position)
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state (saving the last known place and the current camera position)
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;

    // Identifier for the answer to a place picker request
    int PLACE_PICKER_REQUEST = 3;
    // Place Picker elements, usen to launch a google "place picker" activity
    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

    // Variable to use when a reference to the current activity is needed as a parameter in a nested method (in this case "this" does not refer to the current activity)
    Activity thisActivity = this;






    /** ********************************************************************************************
     *  Function called during the launch of the activity
     *  ********************************************************************************************
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** **************************************************
         * Render the layout (containing the map)
         * ***************************************************
         */
        setContentView(R.layout.activity_new_maps);

        /** **************************************************
         * Retrieve location and camera position from saved instance state.
         * ***************************************************
         */
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        /** **************************************************
         * Construct a GeoDataClient. (to give access to the Places API database)
         * ***************************************************
         */
        mGeoDataClient = Places.getGeoDataClient(this, null);

        /** **************************************************
         * Construct a PlaceDetectionClient. (to allow to find most likely current place)
         * ***************************************************
         */
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        /** **************************************************
         * Construct a FusedLocationProviderClient. (to find th
         * ***************************************************
         */
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


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
        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab);
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


        /** ***************************************************
         *  Allowing the use of the results of the "Autocomplete" Places search bar
         *  ***************************************************
         */
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        // Setting bounds for the results of the requests (here bounds around Paris)
        autocompleteFragment.setBoundsBias(new LatLngBounds(
                new LatLng(48.8, 2.24),
                new LatLng(48.91, 2.43)));
        // Setting a filter for the places type
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT).build();
            // The only filter available for android Places API are :
            //TYPE_FILTER_NONE ,TYPE_FILTER_GEOCODE, TYPE_FILTER_ADDRESS, TYPE_FILTER_ESTABLISHMENT, TYPE_FILTER_REGIONS, TYPE_FILTER_CITIES
            // So to get a more precise filter we need filter the results of the query ourselves
        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


    }


    /** ********************************************************************************************
     * Saves the state of the map when the activity is paused.
     * *********************************************************************************************
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
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
        //LatLng paris = defaultLocation;
        //mMap.addMarker(new MarkerOptions().position(paris).title("Marker in Paris"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(paris,DEFAULT_ZOOM));//newLatLng(placeLatLng));

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents, (FrameLayout) findViewById(R.id.map), false);

                TextView title = ((TextView) infoWindow.findViewById(R.id.info_title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }


    /** ********************************************************************************************
     * Gets the current location of the device, and positions the map's camera.
     * *********************************************************************************************
     */
    private void getDeviceLocation() {
        /*****************************************
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         * Here we se the FusedLocationProviderClient to get the current position
         * ***************************************
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /** ********************************************************************************************
     * Prompts the user for permission to use the device location.
     * *********************************************************************************************
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
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
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
        updateLocationUI();
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


                    PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient,null );
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
            case R.id.fab2:
                System.out.println("FAB2 clicked !");
                showCurrentPlace();
                break;
        }
    }


    /** ********************************************************************************************
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     * NOT REALLY WHAT WE WANT HERE /!\/!\/!\
     * *********************************************************************************************
     */
    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final
            Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                                // Set the count, handling cases where less than 5 entries are returned.
                                int count;
                                if (likelyPlaces.getCount() < M_MAX_ENTRIES) {
                                    count = likelyPlaces.getCount();
                                } else {
                                    count = M_MAX_ENTRIES;
                                }

                                int i = 0;
                                mLikelyPlaceNames = new String[count];
                                mLikelyPlaceAddresses = new String[count];
                                mLikelyPlaceAttributions = new String[count];
                                mLikelyPlaceLatLngs = new LatLng[count];

                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    // Build a list of likely places to show the user.
                                    mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                                    mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace()
                                            .getAddress();
                                    mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                                            .getAttributions();
                                    mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                                    i++;
                                    if (i > (count - 1)) {
                                        break;
                                    }
                                }

                                // Release the place likelihood buffer, to avoid memory leaks.
                                likelyPlaces.release();

                                // Show a dialog offering the user the list of likely places, and add a
                                // marker at the selected place.
                                openPlacesDialog();

                            } else {
                                Log.e(TAG, "Exception: %s", task.getException());
                            }
                        }
                    });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            mMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }


    /** ********************************************************************************************
     * Displays a form allowing the user to select a place from a list of likely places.
     * *********************************************************************************************
     */
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = mLikelyPlaceLatLngs[which];
                String markerSnippet = mLikelyPlaceAddresses[which];
                if (mLikelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                mMap.addMarker(new MarkerOptions()
                        .title(mLikelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Position the map's camera at the location of the marker.
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(mLikelyPlaceNames, listener)
                .show();
    }


    /** ********************************************************************************************
     * Updates the map's UI settings based on whether the user has granted location permission.
     * *********************************************************************************************
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
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
