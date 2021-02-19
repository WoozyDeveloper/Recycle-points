package com.woozydeveloper.locationapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

public class SelectValues extends AppCompatActivity {
    public static String SHARED_PREFERENCES = "PERCENTS";
    Button buttonMapActivity, buttonLogOut, buttonUpdate, buttonAdmin;
    Button[][] buttons_for_values = new Button[3][6];
    int[] colors = new int[3];
    int[] percents = new int[3];

    public long last_time_millis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_values);

        setUser();
        getValues();

        //region buttons

        //culorile din BitmapDescriptorFactory sunt salvate cu acelasi nume in #colors.xml
        colors[0] = getResources().getColor(R.color.HUE_AZURE);
        colors[1] = getResources().getColor(R.color.HUE_MAGENTA);
        colors[2] = getResources().getColor(R.color.HUE_ORANGE);

        associateButtons();
        setOnClickListeners();
        //endregion
    }

    //region functions

    public void saveData(int cant, int k) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String key = "procent" + k;
        editor.putInt(key, cant);
        String message = String.valueOf(cant);
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.cancel();
        toast.show();
        editor.apply();

        percents[k] = cant;
    }

    public void openMap() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void logOut() {
        FirebaseAuth mFirebaseAuth;
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAuth.signOut();

        Intent intent = new Intent(getApplicationContext(), HomePage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //region buttons functions
    public void associateButtons() {
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 5; ++j) {
                String button_id = "button" + i + j;
                int res_id = getResources().getIdentifier(button_id, "id", getPackageName());
                buttons_for_values[i][j] = findViewById(res_id);
            }

        buttonMapActivity = findViewById(R.id.button6);
        buttonLogOut = findViewById(R.id.button7);
        buttonUpdate = findViewById(R.id.update);
    }

    public void setButtonsColors() {
        int[] checkNumber = new int[3];
        for (int k = 0; k < 3; ++k) {
            checkNumber[k] = percents[k] / 25;
            if (checkNumber[k] != 0) {
                for (int i = 0; i <= checkNumber[k]; ++i) {
                    buttons_for_values[k][i].setBackgroundColor(colors[k]);
                }
                for (int i = checkNumber[k] + 1; i < 5; ++i) {
                    buttons_for_values[k][i].setBackgroundColor(0);
                }
            } else {
                for (int i = 0; i < 5; ++i) {
                    buttons_for_values[k][i].setBackgroundColor(getResources().getColor(R.color.colorAccent));
                }
            }
        }
    }

    public void setOnClickListeners() {
        for (int k = 0; k < 3; ++k) {
            final int l = k;
            buttons_for_values[k][0].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < 5; ++i)
                        buttons_for_values[l][i].setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    saveData(0, l);
                }
            });

            for (int b = 1; b < 5; ++b) {
                final int maxx = b;
                buttons_for_values[l][b].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i <= maxx; ++i)
                            buttons_for_values[l][i].setBackgroundColor(colors[l]);
                        for (int i = maxx + 1; i < 5; ++i)
                            buttons_for_values[l][i].setBackgroundColor(0);
                        saveData(maxx * 25, l);
                    }
                });
            }
        }

        buttonMapActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });
        buttonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateValues();
            }
        });

    }
    //endregion

    //region firebase interaction

    public void setUser() {
        if (internetIsConnected()) {
            Map<String, Object> map = new HashMap<>();
            final Task<Map<String, Object>> taskData = firebaseTask("getinstitutionname", map);
            taskData.addOnCompleteListener(new OnCompleteListener<Map<String, Object>>() {
                @Override
                public void onComplete(@NonNull Task<Map<String, Object>> task) {
                    Map<String, Object> result = task.getResult();
                    if (result != null) {
                        String s = (String) result.get("name");
                        TextView institution = findViewById(R.id.username);
                        institution.setText(s);
                    } else {
                        Toast.makeText(getApplicationContext(), "Nu s-au putut furniza datele", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
        }
    }

    public void getValues() {
        if (internetIsConnected()) {
            Map<String, Object> map = new HashMap<>();
            final Task<Map<String, Object>> task = firebaseTask("getvalues", map);

            task.addOnCompleteListener(new OnCompleteListener<Map<String, Object>>() {
                @Override
                public void onComplete(@NonNull Task<Map<String, Object>> task) {
                    Map <String, Object> result = task.getResult();
                    if (result != null) {
                        if (result.get("percent0")!=null)
                            percents[0] = (int) result.get("percent0");
                        if (result.get("percent1")!=null)
                            percents[1] = (int) result.get("percent1");
                        if (result.get("percent2")!=null)
                            percents[2] = (int) result.get("percent2");

                        setButtonsColors();
                    } else {
                        Toast.makeText(getApplicationContext(), "Nu s-au putut furniza datele", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
        }
    }

    public void updateValues() {
        if (internetIsConnected()) {
            Map<String, Object> map = new HashMap<>();
            map.put("percent0", percents[0]);
            map.put("percent1", percents[1]);
            map.put("percent2", percents[2]);
            firebaseTask("updatevalues", map);
        } else {
            Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
        }
    }
    //endregion

    public void onBackPressed() {
        if (last_time_millis + 2000 > System.currentTimeMillis()) {
            finish();
            System.exit(0);
        } else {
            Toast.makeText(getApplicationContext(), "Apasă din nou pentru a părăsi aplicația", Toast.LENGTH_LONG).show();
        }
        last_time_millis = System.currentTimeMillis();
    }


    private Task<Map<String, Object>> firebaseTask(String function, Map<String, Object> data) {
        FirebaseFunctions mFunctions;
        mFunctions = FirebaseFunctions.getInstance();

        return mFunctions
                .getHttpsCallable(function)
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, Map<String, Object>>() {
                    @Override
                    public Map<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
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

    //endregion

}
