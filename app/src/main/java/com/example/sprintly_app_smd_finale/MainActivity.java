package com.example.sprintly_app_smd_finale;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private TextView appNameText;
    private String fullText = "Sprintly"; // Whatever your app name is
    private int index = 0;
    private Handler textHandler = new Handler();
    private Runnable textRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        appNameText = findViewById(R.id.appNameText);

        startTypeWriterEffect();
        startSplashScreenTimer();
    }

    private void startTypeWriterEffect() {
        index = 0;
        appNameText.setText("");
        textRunnable = new Runnable() {
            @Override
            public void run() {
                if (index <= fullText.length()) {
                    appNameText.setText(fullText.substring(0, index));
                    index++;
                    textHandler.postDelayed(this, 150); // Speed: 300ms per letter
                }
            }
        };
        textHandler.post(textRunnable);
    }

    private void startSplashScreenTimer() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, onboard1.class);
                startActivity(intent);
                finish();
            }
        }, 2000); // 2 second splash
    }
}

