package com.example.restaurantapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.UUID;

public class AddStaffActivity extends AppCompatActivity {

    private EditText etName, etPhone, etEmail;
    private Spinner spinnerRole, spinnerShift;
    private CheckBox cbActive;
    private Button btnSave;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_staff);

        db = FirebaseFirestore.getInstance();
        etName = findViewById(R.id.etStaffName);
        etPhone = findViewById(R.id.etStaffPhone);
        etEmail = findViewById(R.id.etStaffEmail);
        spinnerRole = findViewById(R.id.spinnerStaffRole);
        spinnerShift = findViewById(R.id.spinnerStaffShift);
        cbActive = findViewById(R.id.cbStaffActive);
        btnSave = findViewById(R.id.btnSaveStaff);

        setupSpinners();

        btnSave.setOnClickListener(v -> saveStaff());
    }

    private void setupSpinners() {
        String[] roles = {"Waiter", "Chef", "Manager", "Cashier"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        String[] shifts = {"Morning", "Afternoon", "Night"};
        ArrayAdapter<String> shiftAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, shifts);
        shiftAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShift.setAdapter(shiftAdapter);
    }

    private void saveStaff() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();
        String shift = spinnerShift.getSelectedItem().toString();
        boolean active = cbActive.isChecked();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        String id = UUID.randomUUID().toString();
        Staff staff = new Staff(id, name, phone, email, role, shift, active);

        btnSave.setEnabled(false);
        db.collection("staff").document(id).set(staff)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, getString(R.string.staff_member_added), Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, getString(R.string.error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
    }
}
