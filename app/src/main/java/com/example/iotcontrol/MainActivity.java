package com.example.iotcontrol;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.util.Log;
public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1002;
    private LocationManager locationManager;

    private static final String API_URL = "http://apiaplication-dev.eba-mmrm3b2n.us-east-1.elasticbeanstalk.com/api/VibrationSensorDatas/recieveGPS"; // C# API'nin URL'si
    private final OkHttpClient httpClient = new OkHttpClient();

    private void requestLocationPermission() {
        // Check if foreground location permission is granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        }
        // Check if background location permission is granted
        else if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }, BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permissions are granted, start location updates
            getLocationUpdates();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted for foreground location
                requestLocationPermission();  // Check for background permission
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Background location permission granted
                getLocationUpdates();
            } else {
                Toast.makeText(this, "Background location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void getLocationUpdates() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Request location updates for foreground
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        }

        // You may also need a foreground service for continuous location updates in the background
    }
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Toast.makeText(MainActivity.this, "Lat: " + latitude + ", Lng: " + longitude, Toast.LENGTH_SHORT).show();
            //konum degisince apiye gonder
            sendToAPI(latitude,longitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(@NonNull String provider) {}

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            Toast.makeText(MainActivity.this, "GPS is turned off. Please enable it.", Toast.LENGTH_SHORT).show();
        }
    };
    private void startLocationService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private static final String TAG = "API_SEND";
    private void sendToAPI(double Lattitude, double Longitude){
        try {
            JSONObject json = new JSONObject();
            json.put("enlem",String.valueOf(Lattitude));
            json.put("boylam",String.valueOf(Longitude));

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(API_URL) // Komut için ayrı bir endpoint
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "API gönderimi başarısız: " + e.getMessage(), e);
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Gönderim Başarısız", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        // Başarılı gönderim durumu
                        Log.d(TAG, "API gönderimi başarılı: " + response.message());
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Komut Gönderildi", Toast.LENGTH_SHORT).show());
                    } else {
                        // Sunucudan gelen hata durumu
                        Log.w(TAG, "API gönderimi sırasında hata: " + response.code() + " - " + response.message());
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Gönderim Hatası: " + response.message(), Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "JSON oluşturulurken hata: " + e.getMessage(), e);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        requestLocationPermission();

        Button btnServo = findViewById(R.id.btnServo);
        Button btnSleepTracking = findViewById(R.id.btnSleepTracking);
        Button btnVibrationSensor = findViewById(R.id.btnVibrationSensor);

        btnServo.setOnClickListener(v -> startActivity(new Intent(this, ServoControlActivity.class)));
        btnSleepTracking.setOnClickListener(v -> startActivity(new Intent(this, SleepTrackingActivity.class)));
        btnVibrationSensor.setOnClickListener(v -> startActivity(new Intent(this, VibrationSensorActivity.class)));
    }
}