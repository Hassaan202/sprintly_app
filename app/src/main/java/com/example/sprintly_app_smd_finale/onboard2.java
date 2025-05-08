package com.example.sprintly_app_smd_finale;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;  // ← missing import
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton; // ← missing import

public class onboard2 extends AppCompatActivity {

    private FloatingActionButton nextButton1;  // Field hona chahiye yahan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboard2);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nextButton1 = findViewById(R.id.nextButton2); // Button find karo
        nextButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(onboard2.this, onboard3.class); // Dashboard pe jao
                startActivity(intent);
                finish(); // is page ko close kar do
            }
        });
    }
}
