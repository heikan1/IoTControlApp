package com.example.iotcontrol;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class LocationService extends Service {

    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    public void onCreate() {
        super.onCreate();

        // Create notification channel for Android 8.0 and above
        createNotificationChannel();

        // Initialize location manager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Create a location listener
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Handle location updates here
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // For example, print the coordinates to Logcat (you could do something else here)
                System.out.println("Latitude: " + latitude + ", Longitude: " + longitude);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Request location updates when service starts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        }

        // Start the service as a foreground service with a persistent notification
        startForeground(1, createNotification());

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;  // Return null since this is not a bound service
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop location updates when service is destroyed
        locationManager.removeUpdates(locationListener);
    }

    // Create a notification channel for Android 8.0 and above (required for foreground service)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "LOCATION_CHANNEL",
                    "Location Tracking",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Create the foreground notification
    private Notification createNotification() {
        return new NotificationCompat.Builder(this, "LOCATION_CHANNEL")
                .setContentTitle("Location Service")
                .setContentText("Tracking location in the background")
                //.setSmallIcon(R.drawable.ic_location) // Replace with your own icon
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
    }
}
