package com.woozydeveloper.locationapp;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.api.Context;
import com.google.api.Distribution;

public class PopContactInfo extends Activity {
    @Override
    protected void onCreate(Bundle savedBundleInstance) {
        super.onCreate(savedBundleInstance);
        setContentView(R.layout.pop_contact_info);
        getWindow().setLayout(800,800);

        LinearLayout ll = findViewById(R.id.popll);
        String newline = System.getProperty("line.separator");

        Bundle info = getIntent().getExtras();
        String name = (String) info.get("name");
        String person = (String) info.get("person");
        String phone = (String) info.get("phone");
        String email = (String) info.get("email");

        TextView textname = new TextView(this);
        textname.setText(name);
        textname.setTypeface(null, Typeface.BOLD);
        textname.setGravity(Gravity.CENTER);
        TextView text = new TextView(this);
        text.setText("E-mail: " + email + newline + "Persoană de contact: " + person + newline + "Număr de telefon: " + phone);

        ll.addView(textname);
        ll.addView(text);

    }
}
