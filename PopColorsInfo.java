package com.woozydeveloper.locationapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

public class PopColorsInfo extends Activity {
    @Override
    protected void onCreate(Bundle savedBundleInstance) {
        super.onCreate(savedBundleInstance);
        setContentView(R.layout.pop_colors_info);

        getWindow().setLayout(500,500);


    }
}
