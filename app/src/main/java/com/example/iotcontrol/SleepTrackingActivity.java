package com.example.iotcontrol;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SleepTrackingActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private TextView nfcStatus;
    private boolean isTracking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_tracking);

        nfcStatus = findViewById(R.id.nfcStatus);
        Button startSleepButton = findViewById(R.id.startSleepButton);
        Button stopSleepButton = findViewById(R.id.stopSleepButton);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            nfcStatus.setText("NFC desteklenmiyor");
            return;
        }

        // Uyku Takibini Başlat
        startSleepButton.setOnClickListener(v -> {
            isTracking = true;
            startPeriodicCheck(); // 10 dakikalık kontrolü başlat
            Toast.makeText(this, "Uyku takibi başlatıldı", Toast.LENGTH_SHORT).show();
        });

        // Uyku Takibini Durdur
        stopSleepButton.setOnClickListener(v -> {
            isTracking = false;
            sendToApi("wake", "User woke up");
            Toast.makeText(this, "Uyku takibi durduruldu", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);

        if (!isTracking || nfcAdapter == null) return;

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            byte[] id = tag.getId();
            String macAddress = bytesToHex(id);

            // MAC adresi kontrolü
            String phoneMacAddress = "c4:a4:51:bf:9a:f7"; // Telefon MAC adresi
            if (macAddress.equals(phoneMacAddress)) {
                sendToApi("sleep", "User started sleeping");
                nfcStatus.setText("Uyku başladı: " + macAddress);
            } else {
                nfcStatus.setText("NFC Eşleşmesi başarısız!");
            }
        }
    }

    private void startPeriodicCheck() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isTracking) {
                    sendToApi("check", "NFC kart kontrol ediliyor"); // API'ye kontrol bilgisi gönder
                    handler.postDelayed(this, 1 * 60 * 1000); // 10 dakika
                }
            }
        }, 1 * 60 * 1000); // İlk gecikme 10 dakika
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private void sendToApi(String status, String message) {
        // API'ye veri gönder (Örnek bir Toast eklenmiştir)
        Toast.makeText(this, "Durum Gönderildi: " + status, Toast.LENGTH_SHORT).show();
    }
}