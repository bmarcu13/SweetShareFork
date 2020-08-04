package com.example.sweetshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.example.sweetshare.util;

public class Signup extends AppCompatActivity {

    private EditText fullNameField, emailField, passField, repPassField;
    private Button signupButton;
    private TextView toLoginButton;
    private FirebaseAuth fAuth;
    private ConstraintLayout loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        fullNameField = findViewById(R.id.NameField);
        emailField = findViewById(R.id.EmailField);
        passField = findViewById(R.id.PasswordField);
        repPassField = findViewById(R.id.RepPasswordField);
        signupButton = findViewById(R.id.SignupButton);

        fAuth = FirebaseAuth.getInstance();

        if (fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        // Creating account in Firebase
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fullName = fullNameField.getText().toString().trim();
                String email = emailField.getText().toString().trim();
                String pass = passField.getText().toString();
                String passConfirmation = repPassField.getText().toString();

                if (fullName.isEmpty()) {
                    fullNameField.setError("This field can't be empty");
                    return;
                }
                if (email.isEmpty()) {
                    emailField.setError("This field can't be empty");
                    return;
                }
                if (pass.isEmpty()){
                    passField.setError("This field can't be empty");
                    return;
                }
                if (passConfirmation.isEmpty()) {
                    repPassField.setError("This field can't be empty");
                    return;
                }

                if (!pass.equals(passConfirmation)) {
                    passField.setError("The passwords don't match");
                    return;
                }
                if (pass.length() < 6) {
                    passField.setError("Password must be at least 6 characters long");
                    return;
                }

                loadingLayout.setVisibility(ConstraintLayout.VISIBLE);

                fAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Signup.this, "Account created.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                        else {
                            Toast.makeText(Signup.this, "Error:" + task.getException(), Toast.LENGTH_SHORT).show();
                            loadingLayout.setVisibility(ConstraintLayout.INVISIBLE);
                        }
                    }
                });
            }
        });

        // Displaying focus bar
        util.setInputFieldActivityStatus(findViewById(R.id.EmailField), findViewById(R.id.EmailFieldActiveBar));
        util.setInputFieldActivityStatus(findViewById(R.id.NameField), findViewById(R.id.NameFieldBar));
        util.setInputFieldActivityStatus(findViewById(R.id.PasswordField), findViewById(R.id.PasswordFieldBar));
        util.setInputFieldActivityStatus(findViewById(R.id.RepPasswordField), findViewById(R.id.RepPasswordFieldBar));

    }

    public void toLoginActivity(View view) {
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }
}