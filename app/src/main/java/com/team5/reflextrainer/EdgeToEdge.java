package com.team5.reflextrainer;

import android.view.View;
import android.view.ViewGroup;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Android 15+ draws edge-to-edge by default, so top-anchored views can end up
 *  rendered under the status bar and become unclickable there unless they
 *  account for the system bar inset themselves. */
final class EdgeToEdge {
    private EdgeToEdge() { }

    static void applyTopInsetMargin(View view) {
        ViewGroup.MarginLayoutParams baseParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        int baseMarginTop = baseParams.topMargin;
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.topMargin = baseMarginTop + topInset;
            v.setLayoutParams(params);
            return insets;
        });
    }
}
