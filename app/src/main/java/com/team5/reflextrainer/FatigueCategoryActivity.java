package com.team5.reflextrainer;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class FatigueCategoryActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "category";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fatigue_category);

        findViewById(R.id.cardCardio).setOnClickListener(v -> start(FatigueExercises.Category.CARDIO));
        findViewById(R.id.cardCore).setOnClickListener(v -> start(FatigueExercises.Category.CORE));
        findViewById(R.id.cardLegs).setOnClickListener(v -> start(FatigueExercises.Category.LEGS));
        findViewById(R.id.cardArms).setOnClickListener(v -> start(FatigueExercises.Category.ARMS));

        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());
    }

    private void start(FatigueExercises.Category category) {
        Intent i = new Intent(this, FatigueTrainingActivity.class);
        i.putExtra(EXTRA_CATEGORY, category.name());
        startActivity(i);
        finish();
    }
}
