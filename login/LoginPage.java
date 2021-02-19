package com.woozydeveloper.locationapp.ui.login;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.woozydeveloper.locationapp.AdminPage;
import com.woozydeveloper.locationapp.R;
import com.woozydeveloper.locationapp.SelectValues;
import com.woozydeveloper.locationapp.ui.login.LoginViewModel;
import com.woozydeveloper.locationapp.ui.login.LoginViewModelFactory;

import java.util.ArrayList;

public class LoginPage extends AppCompatActivity {

    /**
     * FIREBASE
     */
    FirebaseAuth mFirebaseAuth;

    private LoginViewModel loginViewModel;
    private String myUsername="";
    private String myPassword="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        /**
         * FIREBASE
         */
        mFirebaseAuth = FirebaseAuth.getInstance();

        final EditText usernameEditText=findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                myUsername = usernameEditText.getText().toString();
                loginViewModel.loginDataChanged(myUsername, passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });



        loginButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myUsername = usernameEditText.getText().toString();
                myPassword = passwordEditText.getText().toString();


                /**
                 * FIREBASE CONNECTION
                 */

                if (!myUsername.isEmpty()&&!myPassword.isEmpty()) {
                    mFirebaseAuth.signInWithEmailAndPassword(myUsername+"@actionlab.ro",myPassword).addOnCompleteListener(LoginPage.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()){
                                makeMeShake(loginButton,50,5);
                                usernameEditText.setText("");
                                passwordEditText.setText("");
                                loginButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                Toast.makeText(getApplicationContext(), "Încearcă din nou", Toast.LENGTH_LONG).show();
                            } else {

                                String welcome = "Welcome " + myUsername + "!";
                                Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
                                
                                Task <Object> task0 = firebaseTask("getrole");
                                
                                task0.addOnCompleteListener(new OnCompleteListener<Object>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Object> task) {
                                        int role = (int) task.getResult();

                                        if (role==1) {
                                            Intent openAdmin = new Intent(getApplicationContext(), AdminPage.class);
                                            openAdmin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(openAdmin);
                                            finish();
                                        } else {
                                            Intent openEditor = new Intent(getApplicationContext(), SelectValues.class);
                                            openEditor.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(openEditor);
                                            finish();
                                        }
                                    }
                                });
                                


                            }
                        }
                    });
                }

            }

        });
    }
    public static View makeMeShake(Button view, int duration, int offset) {
        Animation anim = new TranslateAnimation(-offset,offset,0,0);
        anim.setDuration(duration);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(5);
        view.startAnimation(anim);
        return view;

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

}
