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

public class onboard1 extends AppCompatActivity {

    private FloatingActionButton nextButton;  // Field hona chahiye yahan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboard1);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nextButton = findViewById(R.id.nextButton); // Button find karo
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(onboard1.this, onboard2.class); // Dashboard pe jao
                startActivity(intent);
                finish(); // is page ko close kar do
            }
        });
    }
}
