package sdcn.project.ecommerce_client_distributed;

// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import sdcn.project.ecommerce_client_distributed.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveCanceledListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * This shows how to change the camera position for the map.
 */
public class MapsActivity extends AppCompatActivity implements
        OnCameraMoveStartedListener,
        OnCameraMoveListener,
        OnCameraMoveCanceledListener,
        OnCameraIdleListener,
        OnMapReadyCallback, CurrentMapsFragment.onSomeEventListener
{

    private LatLng markerLatLng = new LatLng(-2.113318844042216, -79.90096078741612);
    //private LatLng userLatLng;
    private static final int DEFAULT_ZOOM = 15;
    private Button button_selectPlace;
    private static final String TAG = MapsActivity.class.getName();

    private boolean wasLocationSet = false;

    /**
     * The amount by which to scroll the camera. Note that this amount is in raw pixels, not dp
     * (density-independent pixels).
     */
    private static final int SCROLL_BY_PX = 100;

    private GoogleMap map;
    private boolean isCanceled = false;
    private ActionMenuItemView action_geolocate;

    // ===================
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);



        // _______________Toolbar___________________ [BEGIN -->]
        // set the toolbar on the layout as the app bar for the activity
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbarMapsActivity);
        setSupportActionBar(myToolbar);
        // _______________Toolbar___________________ [<-- END]

        // get intent value
        Bundle b = getIntent().getBundleExtra("getExtraLatLng");

        // check if the intent value is not null
        if (b!=null)
        {
            //System.out.println("bundle is not null");
            if (!b.isEmpty())
            {
                markerLatLng =b.getParcelable("bundleLatLng");
                wasLocationSet = true;
                //System.out.println("markerLatLng: " + markerLatLng.latitude + " " +markerLatLng + " "+ markerLatLng.toString()  );
            }
        }
        else
        {
            //System.out.println("bundle IS null");
        }
        //

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        button_selectPlace = (Button) findViewById(R.id.button_selectPlace);

        // launch the Fragment, sending the actual marker location
        button_selectPlace.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //System.out.println("button_selectPlace");
                showFragment();
            }
        });

        // set an action to the toolbar button
        //action_geolocate =  findViewById(R.id.action_geolocate);
        //String strLocationTitle = "Ubicación actual";
        //action_geolocate.setText(strLocationTitle);
        /*
        action_geolocate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                System.out.println("onMenuItemClick");
                // ======================

                // Turn on the My Location layer and the related control on the map.
                updateLocationUI();

                // Get the current location of the device and set the position of the map.
                getDeviceLocation();
            }
        });

         */

        // ==============================

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }
    /////////////////////////////////////
    // launch the "CurrentMapsFragments" fragment
    private void showFragment()
    {
        getSupportFragmentManager()
                .beginTransaction()
                .add(
                    R.id.fragment_container,
                    new CurrentMapsFragment(markerLatLng))
                .commit();
        //confirmAddress.show(fm, "fragment_edit_name");
    }
/////////////////////////////////////
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;

        map.setOnCameraIdleListener(this);
        map.setOnCameraMoveStartedListener(this);
        map.setOnCameraMoveListener(this);
        map.setOnCameraMoveCanceledListener(this);
        // We will provide our own zoom controls.
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        if (wasLocationSet)
        {
            // Show Guayaquil by default
            map.moveCamera(
                    CameraUpdateFactory
                            .newLatLngZoom(
                                    markerLatLng,
                                    DEFAULT_ZOOM)
            );
        }
        else
        {
            // Show Guayaquil by default
            map.moveCamera(
                    CameraUpdateFactory
                            .newLatLngZoom(
                                    new LatLng(-2.113318844042216, -79.90096078741612)
                                    , DEFAULT_ZOOM)
            );
        }



        // ======================


    }



    @Override
    public void onCameraMoveStarted(int reason)
    {
        if (!isCanceled) {
            map.clear();
        }

        String reasonText = "UNKNOWN_REASON";
        switch (reason) {
            case OnCameraMoveStartedListener.REASON_GESTURE:
                reasonText = "GESTURE";
                break;
            case OnCameraMoveStartedListener.REASON_API_ANIMATION:
                reasonText = "API_ANIMATION";
                break;
            case OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION:
                reasonText = "DEVELOPER_ANIMATION";
                break;
        }
        Log.d(TAG, "onCameraMoveStarted(" + reasonText + ")");
        // addCameraTargetToPath();
    }

    /**
     * "onCameraMove" gets me every location when the camera is on motion.
     */
    @Override
    public void onCameraMove() {
        // When the camera is moving, add its target to the current path we'll draw on the map.
        Log.d(TAG, "onCameraMove");
    }

    @Override
    public void onCameraMoveCanceled() {
        // When the camera stops moving, add its target to the current path, and draw it on the map.
        isCanceled = true;  // Set to clear the map when dragging starts again.
        Log.d(TAG, "onCameraMoveCancelled");
    }

    /**
     * "onCameraIdle" gets me the last location on the map, the location when i stop the map camera.
     */
    @Override
    public void onCameraIdle()
    {
        isCanceled = false;  // Set to *not* clear the map when dragging starts again.
        Log.d(TAG, "onCameraIdle");
        CameraPosition currentCameraPosition = map.getCameraPosition();
        markerLatLng = new LatLng(
                currentCameraPosition.target.latitude,
                currentCameraPosition.target.longitude);
        // currentCameraPosition.target; gives me (lat/lng)
        System.out.println("onCameraIdle (.target.latitude): "+ currentCameraPosition.target);
    }

    private void addCameraTargetToPath()
    {
        LatLng target = map.getCameraPosition().target;
    }


    // "someEvent" is a interface from the fragment "CurrentMapsFragment", it is use for
    //  get the values of "LatLng latLong" that indicates the user coordinates location
    @Override
    public void someEvent(LatLng latLong)
    {
        Intent intent = new Intent();
        intent.putExtra("editTextValue", latLong);
        System.out.println("someEvent MapsActivity: " + latLong.toString());
        setResult(RESULT_OK, intent);
        finish();
    }



    // ================================= get current location methods

    // current location variables
    private boolean locationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Location lastKnownLocation;


    // Request runtime permissions in your app, giving the user the opportunity to allow or deny
    // location permission. The following code checks whether the user has granted fine location
    // permission. If not, it requests the permission:
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    // Request runtime permissions in your app, giving the user the opportunity to allow or deny
    // location permission. The following code checks whether the user has granted fine location
    // permission. If not, it requests the permission:
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        // super added
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }



    private void updateLocationUI()
    {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }




    private void getDeviceLocation()
    {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted)
            {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Location> task)
                    {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null)
                            {
                                markerLatLng = new LatLng(
                                        lastKnownLocation.getLatitude()
                                        , lastKnownLocation.getLongitude()
                                );
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(
                                                lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()),
                                                map.getCameraPosition().zoom)
                                );
                            }
                        }
                        else
                        {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(
                                CameraUpdateFactory
                                    .newLatLngZoom(
                                        new LatLng(-2.113318844042216, -79.90096078741612)
                                        , DEFAULT_ZOOM)
                            );
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        }
        catch (SecurityException e)
        {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }






    // inflate the app bar with the toolbar wanted model
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.menu.menu_location, menu);
        return super.onCreateOptionsMenu(menu);
    }




    // set actions to the app bar buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_geolocate) {
            // User chose the "current location" item, show the app settings UI...
            //String strLocationTitle = "Ubicación actual";
            //action_geolocate.setText(strLocationTitle);
            System.out.println("action_geolocate");
            // ======================

            // Turn on the My Location layer and the related control on the map.
            updateLocationUI();

            // Get the current location of the device and set the position of the map.
            getDeviceLocation();
            return true;
        }// If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        System.out.println("onOptionsItemSelected");
        return super.onOptionsItemSelected(item);
    }



/*


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_geolocate:
                // User chose the "Settings" item, show the app settings UI...


                System.out.println("action_geolocate");
                // ======================

                // Turn on the My Location layer and the related control on the map.
                updateLocationUI();

                // Get the current location of the device and set the position of the map.
                getDeviceLocation();
                return true;


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.

                System.out.println("onOptionsItemSelected");

                return super.onOptionsItemSelected(item);

        }
    }



 */

}