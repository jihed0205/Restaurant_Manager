package com.example.restaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextView tvProfileEmail;
    private EditText etProfileName;
    private Button btnUpdateProfile, btnResetPassword, btnProfileLogout;
    
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        }

        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        etProfileName = view.findViewById(R.id.etProfileName);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
        btnResetPassword = view.findViewById(R.id.btnResetPassword);
        btnProfileLogout = view.findViewById(R.id.btnProfileLogout);

        loadUserData();

        btnUpdateProfile.setOnClickListener(v -> updateName());
        btnResetPassword.setOnClickListener(v -> sendPasswordReset());
        btnProfileLogout.setOnClickListener(v -> logout());

        return view;
    }

    private void loadUserData() {
        if (userId == null) return;

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        
                        tvProfileEmail.setText(email);
                        etProfileName.setText(name);
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), getString(R.string.error_loading_profile, e.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateName() {
        String newName = etProfileName.getText().toString().trim();
        if (newName.isEmpty()) {
            if (getContext() != null) {
                Toast.makeText(getContext(), getString(R.string.name_cannot_be_empty), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        db.collection("users").document(userId)
                .update("name", newName)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), getString(R.string.name_updated_success), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), getString(R.string.failed_to_update_name, e.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendPasswordReset() {
        String email = tvProfileEmail.getText().toString();
        if (email.isEmpty()) return;

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), getString(R.string.password_reset_sent, email), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), getString(R.string.error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logout() {
        auth.signOut();
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}
