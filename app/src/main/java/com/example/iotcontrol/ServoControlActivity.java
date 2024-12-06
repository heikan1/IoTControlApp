package com.example.iotcontrol;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServoControlActivity extends AppCompatActivity {

    private String openTime = ""; // Açılma zamanı
    private String closeTime = ""; // Kapanma zamanı
    private static final String API_URL = "http://<API_BASE_URL>/api/curtain/times"; // C# API'nin URL'si
    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servo_control);

        Button openCurtainButton = findViewById(R.id.openCurtainButton);
        Button closeCurtainButton = findViewById(R.id.closeCurtainButton);
        Button sendTimesButton = findViewById(R.id.sendTimesButton);


        EditText openTimeInput  = findViewById(R.id.openTimeInput);
        EditText closeTimeInput  = findViewById(R.id.closeTimeInput);

        // Aç Butonu
        openCurtainButton.setOnClickListener(v -> {
            sendCurtainCommand("open");
            Toast.makeText(this, "Perde Açıldı", Toast.LENGTH_SHORT).show();
        });

        // Kapat Butonu
        closeCurtainButton.setOnClickListener(v -> {
            sendCurtainCommand("close");
            Toast.makeText(this, "Perde Kapandı", Toast.LENGTH_SHORT).show();
        });


        // Açılma Zamanı Seçimi
        openTimeInput.setOnClickListener(v -> showTimePicker((time) -> {
            openTime = time;
            openTimeInput.setText(openTime); // Seçilen zamanı EditText'e yaz
        }));

        // Kapanma Zamanı Seçimi
        closeTimeInput.setOnClickListener(v -> showTimePicker((time) -> {
            closeTime = time;
            closeTimeInput.setText(closeTime); // Seçilen zamanı EditText'e yaz
        }));

        // Zamanları Gönder Butonu
        sendTimesButton.setOnClickListener(v -> {
            if (!openTime.isEmpty() && !closeTime.isEmpty()) {
                sendTimesToApi(openTime, closeTime);
            } else {
                Toast.makeText(this, "Lütfen tüm zamanları seçin", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTimePicker(TimePickerCallback callback) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    String time = String.format("%02d:%02d", hourOfDay, minuteOfHour);
                    callback.onTimeSelected(time); // Seçilen zamanı geri çağır
                }, hour, minute, true);

        timePickerDialog.show();
    }

    private void sendCurtainCommand(String command) {
        // Aç veya kapat komutunu API'ye gönder
        try {
            JSONObject json = new JSONObject();
            json.put("command", command);

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(API_URL + "/command") // Komut için ayrı bir endpoint
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) {
                    runOnUiThread(() -> Toast.makeText(ServoControlActivity.this, "Komut Gönderildi", Toast.LENGTH_SHORT).show());
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendTimesToApi(String openTime, String closeTime) {
        try {
            JSONObject json = new JSONObject();
            json.put("openTime", openTime);
            json.put("closeTime", closeTime);

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(ServoControlActivity.this, "Zaman Gönderilemedi", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) {
                    runOnUiThread(() -> Toast.makeText(ServoControlActivity.this, "Zaman Gönderildi", Toast.LENGTH_SHORT).show());
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private interface TimePickerCallback {
        void onTimeSelected(String time); // Dönüş tipi String olmalı
    }
}