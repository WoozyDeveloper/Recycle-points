package com.woozydeveloper.locationapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import java.util.ArrayList;
import java.util.Map;


public class AdminPage extends FragmentActivity {
    Button buttons[];
    Button buttonLogOut;
    TextView text;
    ArrayList institutions;
    LinearLayout ll;
    LinearLayout.LayoutParams lp;
    String username;
    public long last_time_millis;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        text = (TextView) findViewById(R.id.textview);
        ll = (LinearLayout) findViewById(R.id.adminLinearLayout);
        lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        logOutButton();
        showInstitutions();

    }

    public void logOutButton(){
        buttonLogOut = findViewById(R.id.buttonLogOut);
        buttonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth mFirebaseAuth;
                mFirebaseAuth = FirebaseAuth.getInstance();
                mFirebaseAuth.signOut();

                Intent intent = new Intent(getApplicationContext(), HomePage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showInstitutions() {
        if (internetIsConnected()) {
            final Task<Object> task = firebaseTask("setupnames");
            task.addOnCompleteListener(new OnCompleteListener<Object>() {
                @Override
                public void onComplete(@NonNull Task<Object> task) {
                    institutions = (ArrayList) task.getResult();
                    for (int i = 0; i < institutions.size(); ++i) {
                        Map<String, Object> mymap = (Map<String, Object>) institutions.get(i);
                        Button myButton = new Button(getApplicationContext());
                        username = mymap.get("name").toString();
                        myButton.setText(username);
                        myButton.setId(i);
                        ll.addView(myButton, lp);
                    }
                }
            });
        } else {
            Toast.makeText(getApplicationContext(),R.string.no_internet_connection, Toast.LENGTH_LONG).show();
        }
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
    public void onBackPressed() {
        if (last_time_millis + 2000 > System.currentTimeMillis()) {
            finish();
            System.exit(0);
        } else {
            Toast.makeText(getApplicationContext(), "Apasă din nou pentru a părăsi aplicația", Toast.LENGTH_LONG).show();
        }
        last_time_millis = System.currentTimeMillis();
    }
}