package com.example.iotcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnServo = findViewById(R.id.btnServo);
        Button btnSleepTracking = findViewById(R.id.btnSleepTracking);
        Button btnVibrationSensor = findViewById(R.id.btnVibrationSensor);

        btnServo.setOnClickListener(v -> startActivity(new Intent(this, ServoControlActivity.class)));
        btnSleepTracking.setOnClickListener(v -> startActivity(new Intent(this, SleepTrackingActivity.class)));
        btnVibrationSensor.setOnClickListener(v -> startActivity(new Intent(this, VibrationSensorActivity.class)));
    }
}