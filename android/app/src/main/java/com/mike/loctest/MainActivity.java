package com.mike.loctest;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static final String locationProvider = LocationManager.NETWORK_PROVIDER;
    public static final int FIVE_SECONDS = 1000 * 5;

    LocationManager locationManager;
    LocationListener locationListener;

    Location lastKnownLocation;

    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Switch sw = (Switch) findViewById(R.id.switch1);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    // the toggle is enabled
                    locStart();
                } else {
                    // the toggle is disabled
                    locStop();
                }
            }
        });
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        try { lastKnownLocation = locationManager.getLastKnownLocation(locationProvider); }
        catch (SecurityException e) { e.printStackTrace(); }
        tv = findViewById(R.id.textView);
    }

    public void locStart() {

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (isBetterLocation(location, lastKnownLocation)){
                    lastKnownLocation = location;
                }
                sendLoc(lastKnownLocation);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) { }

            @Override
            public void onProviderEnabled(String s) {}

            @Override
            public void onProviderDisabled(String s) { }
        };

        // register the listener with the location manager to receive location updates
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, FIVE_SECONDS, 0, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    public void locStop() {
        locationManager.removeUpdates(locationListener);
        tv.setText("GPS Data");
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     * @return Whether the new Location is better than the old one
     */
    public boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // a new location is always better than no location
            return true;
        }

        // check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > FIVE_SECONDS;
        boolean isSignificantlyOlder = timeDelta < -FIVE_SECONDS;
        boolean isNewer = timeDelta > 0;

        // if it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            // if the new location is more than five seconds older, it must be worse
            return false;
        }

        //check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 10;

        // check if the old and new location are form the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

        // determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider){
            return true;
        }
        return false;
    }

    /** checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null){
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public void sendLoc(Location loc) {
        tv.setText("Lat: " + loc.getLatitude() + "; Lon: " + loc.getLongitude());
    }
}
