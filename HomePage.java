package com.woozydeveloper.locationapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.woozydeveloper.locationapp.ui.login.LoginPage;

public class HomePage extends FragmentActivity {
    Button mapButton, staffButton;
    public long last_time_millis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean internet = internetIsConnected();
        if (!internet)
            Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();

        if (internet && user != null) {

            Task<Object> task = firebaseTask("getrole");
            task.addOnCompleteListener(new OnCompleteListener<Object>() {
                @Override
                public void onComplete(@NonNull Task<Object> task) {
                    int role = (int) task.getResult();
                    Class cl;
                    if (role == 1) {
                        cl = AdminPage.class;
                    } else {
                        cl = SelectValues.class;
                    }
                    Intent openEditor = new Intent(getApplicationContext(), cl);
                    openEditor.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(openEditor);
                    finish();
                }
            });
        } else {
            setContentView(R.layout.activity_home_page);
            mapButton = findViewById(R.id.mapButton);
            staffButton = findViewById(R.id.staffButton);
            mapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMap();
                }
            });
            staffButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openStaffClass();
                }
            });
        }
    }

    private void openStaffClass() {
        Intent openStaff = new Intent(this, LoginPage.class);
        startActivity(openStaff);
    }

    public void openMap() {
        Intent openMapActivity = new Intent(this, MapsActivity.class);
        startActivity(openMapActivity);
    }

    public void onBackPressed() {
        if (last_time_millis + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            System.exit(0);
        } else {
            Toast.makeText(getApplicationContext(), "Apasă din nou pentru a părăsi aplicația", Toast.LENGTH_LONG).show();
        }
        last_time_millis = System.currentTimeMillis();
    }

    private Task<Object> firebaseTask(String function) {
        FirebaseFunctions mFunctions;
        mFunctions = FirebaseFunctions.getInstance();

        return mFunctions
                .getHttpsCallable(function)
                .call()
                .continueWith(new Continuation<HttpsCallableResult, Object>() {
                    @Override
                    public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        Object result = task.getResult().getData();
                        return result;
                    }
                });
    }

    public boolean internetIsConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }
}