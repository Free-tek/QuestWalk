package com.botosofttechnologies.questwalk;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    TextView signUp, forgotPassword;
    EditText email, password;
    Button login;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // User is signed in
            startActivity(intent);
            finish();
        } else {
            // No user is signed in
            //do nothing
        }

        initUi();


    }

    private void initUi() {
        signUp = (TextView) findViewById(R.id.signUp);
        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);


        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });


        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setMessage("Loading...");
                progressDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.logo));
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.show();
                String $email = String.valueOf(email.getText());
                String $password = String.valueOf(password.getText());
                signIn($email, $password);
            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            startActivity(intent);
            finish();
        } else {
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }



    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void signIn(String email, String password) {
        if (!validateForm()) {
            Toast.makeText(LoginActivity.this, "Please fill the required form", Toast.LENGTH_SHORT).show();
            return;
        }
        final Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);


        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            final FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(intent);
                        } else if(!isNetworkAvailable()){
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Please check your internet connection",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Wrong login details, please try again",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;


        String $email = email.getText().toString();
        if (TextUtils.isEmpty($email)) {
            email.setError("Required.");
            valid = false;
        } else {
            email.setError(null);
        }

        String $password = password.getText().toString();
        if (TextUtils.isEmpty($password)) {
            password.setError("Required.");
            valid = false;
        } else {
            password.setError(null);
        }

        return valid;
    }
}
