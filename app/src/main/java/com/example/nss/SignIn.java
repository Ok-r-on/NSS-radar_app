package com.example.nss;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nss.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class SignIn extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    FirebaseDatabase db;
    DatabaseReference databaseReference;
    EditText signin_username,signin_email,signin_password,consignpass;
    Button signupbtn;
    TextView loginredirect;
    String cpass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        db= FirebaseDatabase.getInstance();
        databaseReference = db.getReference("User");

        firebaseAuth=FirebaseAuth.getInstance();
        signin_email=findViewById(R.id.signin_email);
        signin_username=findViewById(R.id.signin_username);
        signin_password=findViewById(R.id.signin_password);
        consignpass=findViewById(R.id.conf_signin_password);
        signupbtn=findViewById(R.id.signupbtn);
        loginredirect=findViewById(R.id.loginredirecttxt);

        cpass = consignpass.getText().toString().trim();

        consignpass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (cpass.length()<6 && cpass.length()>0){
                    consignpass.setError("Less than 6 characters");
                }
                else {
                    // If it doesn't match, clear any previous error message
                    consignpass.setError(null);
                }
            }
        });
        signupbtn.setOnClickListener(view -> {

            String username = signin_username.getText().toString().trim();
            String email = signin_email.getText().toString().trim();
            String pass = signin_password.getText().toString().trim();
            cpass = consignpass.getText().toString().trim();

            if (!email.isEmpty() && !username.isEmpty()) {
                if (!pass.isEmpty() && !cpass.isEmpty()) {
                    if (cpass.equals(pass)) {
                        firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                SignInMethodQueryResult result = task.getResult();
                                List<String> signInMethods = result.getSignInMethods();
                                if (signInMethods != null && !signInMethods.isEmpty()) {
                                            // User already exists
                                    Toast.makeText(SignIn.this, "Username already exists", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignIn.this, LogInActivity.class));
                                } else {
                                            // User doesn't exist, create a new account
                                    firebaseAuth.createUserWithEmailAndPassword(email, cpass).addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            User user = new User(username, email, pass);
                                            databaseReference.child(username).setValue(user).addOnCompleteListener(task11 -> Toast.makeText(SignIn.this, "Sign Up Successful", Toast.LENGTH_SHORT).show());
                                            startActivity(new Intent(SignIn.this, LogInActivity.class));
                                        } else {
                                            Toast.makeText(SignIn.this, "Sign Up Failed: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(SignIn.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        // Check if username already exists
                    } else {
                        Toast.makeText(SignIn.this, "The passwords do not match", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignIn.this, "Please fill in the Password/s field", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SignIn.this, "Please enter the email", Toast.LENGTH_SHORT).show();
            }
        });
        loginredirect.setOnClickListener(view -> startActivity(new Intent(SignIn.this, LogInActivity.class)));
    }
}