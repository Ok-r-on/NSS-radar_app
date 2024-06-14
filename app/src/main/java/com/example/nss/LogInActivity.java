package com.example.nss;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nss.senior.Senior;
import com.example.nss.volunteer.Volunteer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LogInActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    EditText login_user,login_password,login_email;
    Button loginpbtn;
    FirebaseDatabase db;
    DatabaseReference databaseReference;
    TextView signupredirecttxt;
    private static final String PREFS_NAME = "MyPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        auth=FirebaseAuth.getInstance();
        db= FirebaseDatabase.getInstance();
        databaseReference = db.getReference("User");

        login_user=findViewById(R.id.login_user);
        login_email=findViewById(R.id.login_email);
        login_password=findViewById(R.id.login_password);


        loginpbtn=findViewById(R.id.loginpbtn);
        signupredirecttxt=findViewById(R.id.signupredirecttxt);

        loginpbtn.setOnClickListener(view -> {

            String user = login_user.getText().toString().trim();
            String email = login_email.getText().toString().trim();
            String pass = login_password.getText().toString().trim();

            if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (!pass.isEmpty()) {
                    databaseReference.child(user).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String retrievedName = snapshot.child("name").getValue(String.class);
                                String retrievedEmail = snapshot.child("email").getValue(String.class);
                                String retrievedPass = snapshot.child("pass").getValue(String.class);
                                if (retrievedName.equals(user) && retrievedEmail.equals(email) && retrievedPass.equals(pass)) {
                                    // User exists and credentials match, proceed with login
                                    auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener(authResult -> {
                                        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putString("UserName", retrievedName);
                                        editor.apply();
                                        Toast.makeText(LogInActivity.this, "Logged In", Toast.LENGTH_SHORT).show();
                                        //TODO start vol activity
                                        startActivity(new Intent(LogInActivity.this, Volunteer.class));
                                        finish();
                                        // Start activity as needed
                                    }).addOnFailureListener(e -> Toast.makeText(LogInActivity.this, "Failed To Log In", Toast.LENGTH_SHORT).show());
                                } else {
                                    Toast.makeText(LogInActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(LogInActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(LogInActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    login_password.setError("Password cannot be empty");
                }
            } else {
                login_email.setError("Please enter a valid email");
            }

        });

        signupredirecttxt.setOnClickListener(view -> startActivity(new Intent(LogInActivity.this, SignIn.class)));

    }
}