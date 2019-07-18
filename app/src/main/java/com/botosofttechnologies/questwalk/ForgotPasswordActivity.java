package com.botosofttechnologies.questwalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextView forgotPassword, signIn;
    private EditText email;
    private Button send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);


        initUi();


    }

    private void initUi() {

        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        signIn = (TextView) findViewById(R.id.signIn);
        email = (EditText) findViewById(R.id.email);
        send = (Button) findViewById(R.id.send);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LaunchActivity.class);
                startActivity(intent);

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String $email = String.valueOf(email.getText());

                if(!$email.isEmpty()){
                    FirebaseAuth.getInstance().sendPasswordResetEmail($email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("Email Status", "Email sent.");
                                        Toast.makeText(ForgotPasswordActivity.this, "Recovery mail sent successfully!", Toast.LENGTH_SHORT).show();

                                    } else {
                                        Toast.makeText(ForgotPasswordActivity.this, "Invalid email!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }else{
                    Toast.makeText(ForgotPasswordActivity.this, "Enter email address", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
