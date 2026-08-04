package com.team5.reflextrainer;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Android 15+ draws edge-to-edge by default; without this the header (and its
        // back button) renders under the status bar and becomes unclickable in that region.
        View header = findViewById(R.id.aboutHeader);
        int headerBasePaddingTop = header.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(header, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(v.getPaddingLeft(), headerBasePaddingTop + topInset, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }
}
