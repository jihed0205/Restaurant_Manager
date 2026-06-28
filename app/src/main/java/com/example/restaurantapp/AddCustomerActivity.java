package com.example.restaurantapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.UUID;

public class AddCustomerActivity extends AppCompatActivity {

    private EditText etName, etPhone, etEmail;
    private Button btnSave;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customer);

        db = FirebaseFirestore.getInstance();
        etName = findViewById(R.id.etCustName);
        etPhone = findViewById(R.id.etCustPhone);
        etEmail = findViewById(R.id.etCustEmail);
        btnSave = findViewById(R.id.btnSaveCustomer);

        btnSave.setOnClickListener(v -> saveCustomer());
    }

    private void saveCustomer() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        String id = UUID.randomUUID().toString();
        Customer customer = new Customer(id, name, phone, email);

        btnSave.setEnabled(false);
        db.collection("customers").document(id).set(customer)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, getString(R.string.customer_registered), Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, getString(R.string.error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
    }
}
