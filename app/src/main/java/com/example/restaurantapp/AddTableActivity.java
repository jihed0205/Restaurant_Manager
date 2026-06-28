package com.example.restaurantapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.UUID;

public class AddTableActivity extends AppCompatActivity {

    private EditText etTableNumber, etCapacity;
    private Button btnSave;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_table);

        db = FirebaseFirestore.getInstance();
        etTableNumber = findViewById(R.id.etTableNumber);
        etCapacity = findViewById(R.id.etCapacity);
        btnSave = findViewById(R.id.btnSaveTable);

        btnSave.setOnClickListener(v -> saveTable());
    }

    private void saveTable() {
        String numberStr = etTableNumber.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();

        if (numberStr.isEmpty() || capacityStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int number = Integer.parseInt(numberStr);
        int capacity = Integer.parseInt(capacityStr);

        String id = UUID.randomUUID().toString();
        Table table = new Table(id, number, capacity, "Free");

        btnSave.setEnabled(false);
        db.collection("tables").document(id).set(table)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Table added", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
