package com.example.restaurantapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.UUID;

public class AddMenuItemActivity extends AppCompatActivity {

    private EditText etName, etPrice;
    private Spinner spinnerCategory;
    private CheckBox cbAvailable;
    private Button btnSave;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_menu_item);

        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etItemName);
        etPrice = findViewById(R.id.etItemPrice);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        cbAvailable = findViewById(R.id.cbAvailable);
        btnSave = findViewById(R.id.btnSaveMenu);

        btnSave.setOnClickListener(v -> saveMenuItem());
    }

    private void saveMenuItem() {
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        boolean available = cbAvailable.isChecked();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.invalid_price_format), Toast.LENGTH_SHORT).show();
            return;
        }

        String id = UUID.randomUUID().toString();
        MenuItem menuItem = new MenuItem(id, name, price, category, available);

        btnSave.setEnabled(false);
        db.collection("menuItems").document(id).set(menuItem)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, getString(R.string.item_added_success), Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, getString(R.string.error_adding_item, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
    }
}
