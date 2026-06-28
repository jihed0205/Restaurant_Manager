package com.example.restaurantapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.UUID;

public class AddPromotionActivity extends AppCompatActivity {

    private EditText etTitle, etCode, etValue, etMinOrder;
    private Spinner spinnerType;
    private Button btnExpiry, btnSave;
    private CheckBox cbActive;
    private FirebaseFirestore db;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_promotion);

        db = FirebaseFirestore.getInstance();
        etTitle = findViewById(R.id.etPromoTitle);
        etCode = findViewById(R.id.etPromoCode);
        etValue = findViewById(R.id.etDiscountValue);
        etMinOrder = findViewById(R.id.etMinOrder);
        spinnerType = findViewById(R.id.spinnerDiscountType);
        btnExpiry = findViewById(R.id.btnPickExpiry);
        btnSave = findViewById(R.id.btnSavePromotion);
        cbActive = findViewById(R.id.cbPromoActive);

        setupSpinner();

        btnExpiry.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> savePromotion());
    }

    private void setupSpinner() {
        String[] types = {"Percentage", "Fixed"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
            btnExpiry.setText(selectedDate);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void savePromotion() {
        String title = etTitle.getText().toString().trim();
        String code = etCode.getText().toString().trim().toUpperCase();
        String valStr = etValue.getText().toString().trim();
        String minStr = etMinOrder.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();
        boolean active = cbActive.isChecked();

        if (title.isEmpty() || code.isEmpty() || valStr.isEmpty() || minStr.isEmpty() || selectedDate.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double value = Double.parseDouble(valStr);
        double minOrder = Double.parseDouble(minStr);

        String id = UUID.randomUUID().toString();
        Promotion promo = new Promotion(id, title, code, type, value, minOrder, selectedDate, active, 0);

        btnSave.setEnabled(false);
        db.collection("promotions").document(id).set(promo)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Promotion created", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
