package com.botosofttechnologies.questwalk.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.botosofttechnologies.questwalk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUpActivity extends AppCompatActivity {

    TextView signIn;
    EditText email, phoneNo, username, password, age;
    RadioGroup radiogroup;
    RadioButton female, male;
    Button createAccount;

    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference usersID = database.getReference().child("userId");
    int $count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        initUi();



        usersID.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                $count = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void initUi() {

        signIn = (TextView) findViewById(R.id.signIn);
        email = (EditText) findViewById(R.id.email);
        phoneNo = (EditText) findViewById(R.id.phoneNo);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        age = (EditText) findViewById(R.id.age);
        radiogroup = (RadioGroup) findViewById(R.id.radiogroup);
        female = (RadioButton) findViewById(R.id.female);
        male = (RadioButton) findViewById(R.id.male);
        createAccount = (Button) findViewById(R.id.createAccount);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateForm();
                if(!validateForm()){
                    Toast.makeText(SignUpActivity.this, "Check the form for error", Toast.LENGTH_SHORT).show();

                }else{

                    final Intent intent = new Intent(SignUpActivity.this, NavigationActivity.class);

                    final String $email = String.valueOf(email.getText());
                    final String $password = String.valueOf(password.getText());

                    final AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this).create();
                    alertDialog.setTitle("Terms and Conditions");
                    alertDialog.setMessage("*By using Quest Walk, you agree to allowing the app administrators send you ads ftrom time to time.\n"

                            + "*All rhe data gathered can be used to send you relevant ads as deemed fit by the administrator, in order to avoid irrelevant ads from reaching you\n");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Agree",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    progressDialog = new ProgressDialog(SignUpActivity.this);
                                    progressDialog.setMessage("Creating Account");
                                    progressDialog.setCanceledOnTouchOutside(true);
                                    progressDialog.show();

                                    mAuth.createUserWithEmailAndPassword($email, $password)
                                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {

                                                    if (task.isSuccessful()) {

                                                        FirebaseUser user = mAuth.getCurrentUser();
                                                        saveData();
                                                        sendEmailVerification();
                                                        progressDialog.dismiss();
                                                        startActivity(intent);
                                                        finish();
                                                    }else if(!isNetworkAvailable()){
                                                        Toast.makeText(SignUpActivity.this, "Sign up failed, please check your internet connection",
                                                                Toast.LENGTH_SHORT).show();
                                                    }else {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(SignUpActivity.this, " please try again",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                }
                            });


                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Dismiss",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }

                            });

                    alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(SignUpActivity.this.getResources().getColor(R.color.orange));
                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(SignUpActivity.this.getResources().getColor(R.color.orange));
                        }
                    });


                    alertDialog.show();


                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void saveData() {

        final String uid = mAuth.getCurrentUser().getUid();

        DatabaseReference userId = database.getReference().child("users").child(uid);

        String $email = String.valueOf(email.getText());
        String $phoneNo = String.valueOf(phoneNo.getText());
        String $username = String.valueOf(username.getText());
        String $password = String.valueOf(password.getText());
        String $age = String.valueOf(age.getText());


        userId.child("email").setValue($email);
        userId.child("phoneNo").setValue($phoneNo);
        userId.child("password").setValue($password);
        userId.child("age").setValue($age);
        userId.child("username").setValue($username);
        userId.child("totem").setValue(0);
        userId.child("activeTasks").setValue("false");
        userId.child("nameEnteredTasks").setValue("");

        //lets create a new node on firebase containing each user email as parent and their userID as a child

        usersID.child(String.valueOf($count+1)).child("email").setValue($email);
        usersID.child(String.valueOf($count+1)).child("userId").setValue(uid);


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

        String $phoneno = phoneNo.getText().toString();
        if (TextUtils.isEmpty($phoneno)) {
            phoneNo.setError("Required.");
            valid = false;
        } else {
            phoneNo.setError(null);
        }

        String $username = username.getText().toString();
        if (TextUtils.isEmpty($username)) {
            username.setError("Required.");
            valid = false;
        } else {
            username.setError(null);
        }



        String $age = age.getText().toString();
        if (TextUtils.isEmpty($age)) {
            age.setError("Required.");
            valid = false;
        } else {
            age.setError(null);
        }

        return valid;
    }


    private void sendEmailVerification() {

        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUpActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

}
