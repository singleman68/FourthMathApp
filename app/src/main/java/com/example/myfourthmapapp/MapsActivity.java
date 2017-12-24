package com.example.myfourthmapapp;

import android.content.Intent;
import android.location.Geocoder;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    private Marker mPerson;
    //private Double mLatitude;
    //private Double mLongitude;
    private LatLng mPosition;
    private LatLng mOrigen;
    private AddressResultReceiver mResultReceiver;
    private String mAddressOutput;
    private boolean mAddressRequested;

    private EditText mAddressText;
    private Button mRequestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mAddressOutput = "";
        mAddressRequested = false;
        mResultReceiver = new AddressResultReceiver(new Handler());
        mAddressText = (EditText) findViewById(R.id.editAddress);
        mRequestButton = (Button) findViewById(R.id.request);

        // Obtiene el intent desde la actividad anterior
        Intent intent = getIntent();
        // Recupera los datos enviados en los extras
        Double mLatitude = intent.getDoubleExtra("latitude", 0);
        Double mLongitude = intent.getDoubleExtra("longitude", 0);
        mPosition = new LatLng(mLatitude, mLongitude);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // Add a marker in Sydney and move the camera
        CameraPosition objetivo = CameraPosition.builder().target(mPosition).zoom(18).build();
        MarkerOptions markerOptionsCliente = new MarkerOptions()
                .position(mPosition)
                .draggable(true)
                .title("Cliente");
        mPerson = mMap.addMarker(markerOptionsCliente);
        //mMap.addMarker(new MarkerOptions().position(mPosition).title("Marker in position"));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(objetivo));
        mAddressRequested = true;
        mPosition = mPerson.getPosition();
        getAddress();
        mMap.setOnMarkerDragListener(this);
    }

    private void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        Log.i("Intent", "Start");
        LatLng position = new LatLng(mPosition.latitude, mPosition.longitude);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, position);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
    }

    public void requestActions(View view) {
        mAddressText.setEnabled(false);
    }

    private void getAddress() {
        // Determine whether a Geocoder is available.
        if (!Geocoder.isPresent()) {
            //showSnackbar(getString(R.string.no_geocoder_available));
            return;
        }

        // If the user pressed the fetch address button before we had the location,
        // this will be set to true indicating that we should kick off the intent
        // service after fetching the location.
        if (mAddressRequested) {
            Log.i("getAddress", "Start");
            startIntentService();
        }
    }

    private void displayAddressOutput() {
        String newTitle = mAddressOutput;
        setTitle(newTitle);
        mAddressText.setText(newTitle);

    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        if (marker.equals(mPerson)) {
            Toast.makeText(this, "Inicia Arrastre", Toast.LENGTH_SHORT).show();

            mAddressRequested = true;
        }
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        if (marker.equals(mPerson)) {
            Toast.makeText(this, "Buscando ubicación", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        if (marker.equals(mPerson)) {
            Toast.makeText(this, "Finaliza Arrastre", Toast.LENGTH_SHORT).show();

            mPosition = marker.getPosition();

            getAddress();


        }
    }

    private class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         *  Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            Log.i("Address", String.valueOf(mAddressOutput));
            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                showToast(getString(R.string.address_found));
            }

            // Reset. Enable the Fetch Address button and stop showing the progress bar.
            mAddressRequested = false;
        }
    }
}
