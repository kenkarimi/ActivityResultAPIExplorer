package com.example.activityresultapiexplorer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    Button bNetworkSettingsStartActivityForResult, bLocationServicesStartActivityForResult, bNetworkSettingsActivityResultAPI, bLocationServicesActivityResultAPI;
    TextView tvNetworkSettingsStartActivityForResult, tvLocationServicesStartActivityForResult, tvNetworkSettingsActivityResultAPI, tvLocationServicesActivityResultAPI;
    ConnectivityManager connectivityManager;
    Network network;
    NetworkCapabilities networkCapabilities;
    private boolean gps_enabled, network_enabled, passive_enabled;
    private static int NETWORK_SETTINGS_CODE = 1;
    private static int LOCATION_SERVICES_SETTINGS_CODE = 2;
    private LocationRequest mLocationRequest;
    private enum Method {
        DEPRECATED,
        CURRENT,
        DEFAULT
    }
    private Method method = Method.DEFAULT;
    private ActivityResultLauncher<Intent> networkSettingsActivityResultLauncher;
    private ActivityResultLauncher<PendingIntent> locationServicesSettingsActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bNetworkSettingsStartActivityForResult = (Button) findViewById(R.id.bNetworkSettingsStartActivityForResult);
        tvNetworkSettingsStartActivityForResult = (TextView) findViewById(R.id.tvNetworkSettingsStartActivityForResult);
        bLocationServicesStartActivityForResult = (Button) findViewById(R.id.bLocationServicesStartActivityForResult);
        tvLocationServicesStartActivityForResult = (TextView) findViewById(R.id.tvLocationServicesStartActivityForResult);
        bNetworkSettingsActivityResultAPI = (Button) findViewById(R.id.bNetworkSettingsActivityResultAPI);
        tvNetworkSettingsActivityResultAPI = (TextView) findViewById(R.id.tvNetworkSettingsActivityResultAPI);
        bLocationServicesActivityResultAPI = (Button) findViewById(R.id.bLocationServicesActivityResultAPI);
        tvLocationServicesActivityResultAPI = (TextView) findViewById(R.id.tvLocationServicesActivityResultAPI);

        bNetworkSettingsStartActivityForResult.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                method = Method.DEPRECATED;
                checkNetworkReachability();
            }
        });
        bLocationServicesStartActivityForResult.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                method = Method.DEPRECATED;
                checkLocationServices();
            }
        });

        bNetworkSettingsActivityResultAPI.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                method = Method.CURRENT;
                checkNetworkReachability();
            }
        });
        bLocationServicesActivityResultAPI.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                method = Method.CURRENT;
                checkLocationServices();
            }
        });

        networkSettingsActivityResultLauncher = registerForActivityResult(new NetworkSettingsActivityResultContract(), new ActivityResultCallback<Void>() {
            @Override
            public void onActivityResult(Void result) {
                //method is Method.CURRENT
                network = connectivityManager.getActiveNetwork(); //To ensure user is still on the same network provider.
                networkCapabilities = connectivityManager.getNetworkCapabilities(network);

                if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    tvNetworkSettingsActivityResultAPI.setText("Connected!");
                } else {
                    tvNetworkSettingsActivityResultAPI.setText("Not Connected!");
                }
            }
        });

        locationServicesSettingsActivityResultLauncher = registerForActivityResult(new LocationServicesSettingsActivityResultContract(), new ActivityResultCallback<Integer>() {
            @Override
            public void onActivityResult(Integer result) { //resultCode returned from parseResult
                //method is Method.CURRENT
                if(result == Activity.RESULT_OK) {
                    tvLocationServicesActivityResultAPI.setText("Location Services has been turned On from Settings!");
                } else if(result == Activity.RESULT_CANCELED) {
                    tvLocationServicesActivityResultAPI.setText("Location Services Off!");
                }
            }
        });
    }

    private void checkNetworkReachability(){
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        network = connectivityManager.getActiveNetwork();
        networkCapabilities = connectivityManager.getNetworkCapabilities(network);

        if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            if(method.equals(Method.DEPRECATED)) {
                tvNetworkSettingsStartActivityForResult.setText("Connected!");
            } else if(method.equals(Method.CURRENT)) {
                tvNetworkSettingsActivityResultAPI.setText("Connected!");
            }
        } else {
            if(method.equals(Method.DEPRECATED)) {
                Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                startActivityForResult(settings, NETWORK_SETTINGS_CODE);
            } else if(method.equals(Method.CURRENT)) {
                Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                networkSettingsActivityResultLauncher.launch(settings);
            }
        }
    }

    private void checkLocationServices() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER); //most common source of location errors
        passive_enabled = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);

        if(!gps_enabled || !network_enabled || !passive_enabled){
            //Location Services Off and Location Request settings not confirmed
            confirmLocationServicesSettings();
        } else if(gps_enabled && network_enabled && passive_enabled) {
            //Location Services On and location Request settings not confirmed
            confirmLocationServicesSettings();
        }
    }

    private void confirmLocationServicesSettings(){
        /**
         * Needs to implement 'com.google.android.gms:play-services-location:21.2.0' to use location services and import the components below like LocationRequest.Builder, LocationSettingsRequest.Builder, SettingsClient etc.
         */
        LocationRequest.Builder builder = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000); //priority, intervalMillis are the only arguments of Builder() but can also be changed below with builder.setPriority & builder.setIntervalMillis along with other additions such as .setWaitForAccurateLocation .setMinUpdateIntervalMillis which was previously setFastestInterval before it was deprecated.
        builder.setWaitForAccurateLocation(false);
        builder.setMinUpdateIntervalMillis(1000);
        builder.setMaxUpdateDelayMillis(0); //default is also 0.
        mLocationRequest = builder.build();

        LocationSettingsRequest.Builder builder2 = new LocationSettingsRequest.Builder();
        builder2.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder2.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(MainActivity.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequest);

        task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if(method.equals(Method.DEPRECATED)) {
                    tvLocationServicesStartActivityForResult.setText("Location Services already On!");
                } else if(method.equals(Method.CURRENT)) {
                    tvLocationServicesActivityResultAPI.setText("Location Services already On!");
                }
            }
        });
        task.addOnFailureListener(MainActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        if(method.equals(Method.DEPRECATED)) {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MainActivity.this, LOCATION_SERVICES_SETTINGS_CODE);
                        } else if(method.equals(Method.CURRENT)) {
                            /**
                             * Can't be used because resolvable.getResolution() sends a PendingIntent instead of an Intent to the ActivityResultContract
                             * This on it's own isn't the problem as you just need to change ActivityResultLauncher<Intent> to ActivityResultLauncher<PendingIntent>
                             * The contract also needs to change to ActivityResultContract<PendingIntent, Integer> to match the types in createIntent and parseResults.
                             * The problem though is createIntent expects its return type to be of type Intent and there's now way to type-cast a PendingIntent into an Intent.
                             * As such, we'll stick to using the resolvable.startResolutionForResult & onActivityResult for the foreseeable future as there's no indication that its deprecated even though Gemini is telling me that it is as its not crossed out on the editor.
                             */

                            /*ResolvableApiException resolvable = (ResolvableApiException) e;
                            locationServicesSettingsActivityResultLauncher.launch(resolvable.getResolution());*/

                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MainActivity.this, LOCATION_SERVICES_SETTINGS_CODE);
                        }
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                        Toast.makeText(MainActivity.this, "Location Services Error", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //method is Method.DEPRECATED
        if(requestCode == NETWORK_SETTINGS_CODE) {
            network = connectivityManager.getActiveNetwork(); //To ensure user is still on the same network provider.
            networkCapabilities = connectivityManager.getNetworkCapabilities(network);

            if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                tvNetworkSettingsStartActivityForResult.setText("Connected!");
            } else {
                tvNetworkSettingsStartActivityForResult.setText("Not Connected!");
            }
        } else if(requestCode == LOCATION_SERVICES_SETTINGS_CODE) {
            if (resultCode == Activity.RESULT_OK) { //Location services finally enabled
                tvLocationServicesStartActivityForResult.setText("Location Services has been turned On from Settings!");
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //Let the alert dialog start or continue showing since location services was not enabled in settings
                tvLocationServicesStartActivityForResult.setText("Location Services Off!");
            }
        }
    }
}