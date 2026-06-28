package com.example.restaurantapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword;
    Button btnRegister;
    TextView tvLogin;
    FirebaseAuth auth;
    FirebaseFirestore db;
    ProgressDialog progressDialog;
    private int currentStep = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.step_creating_account));
        progressDialog.setCancelable(true);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            currentStep = 1;
            progressDialog.setMessage(getString(R.string.step_creating_account));
            progressDialog.show();

            try {
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                triggerEmailAndSave(user, name, email);
                            } else {
                                progressDialog.dismiss();
                                showStatusDialog(getString(R.string.registration_failed), getString(R.string.user_created_null), false);
                            }
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            showStatusDialog(getString(R.string.registration_failed), e.getMessage(), false);
                        });
            } catch (Exception e) {
                progressDialog.dismiss();
                showStatusDialog(getString(R.string.system_error), e.getMessage(), false);
            }
        });

        tvLogin.setOnClickListener(v -> finish());
    }

    private void triggerEmailAndSave(FirebaseUser user, String name, String email) {
        currentStep = 2;
        progressDialog.setMessage(getString(R.string.step_sending_email));
        
        user.sendEmailVerification()
                .addOnSuccessListener(aVoid -> {
                    String emailStatus = getString(R.string.verification_sent);
                    saveToFirestore(user, name, email, emailStatus);
                })
                .addOnFailureListener(e -> {
                    String emailStatus = getString(R.string.email_send_failed, e.getMessage());
                    saveToFirestore(user, name, email, emailStatus);
                });
    }

    private void saveToFirestore(FirebaseUser user, String name, String email, String emailStatus) {
        currentStep = 3;
        progressDialog.setMessage(getString(R.string.step_saving_profile));
        
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);

        db.collection("users").document(user.getUid()).set(userMap)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    EmailService.sendWelcomeEmail(name, email);
                    showStatusDialog(getString(R.string.registration_finished), emailStatus, true);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    String msg = emailStatus + "\n\n" + getString(R.string.profile_save_failed_rules) + ": " + e.getMessage();
                    showStatusDialog(getString(R.string.registration_finished), msg, true);
                });
                
        new android.os.Handler().postDelayed(() -> {
            if (progressDialog.isShowing() && currentStep == 3) {
                progressDialog.dismiss();
                showStatusDialog(getString(R.string.partial_success), emailStatus + "\n\n" + getString(R.string.database_slow_response), true);
            }
        }, 8000);
    }

    private void showStatusDialog(String title, String message, boolean navigateOnClose) {
        if (isFinishing()) return;
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    if (navigateOnClose) {
                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }
}
