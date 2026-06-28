package com.example.restaurantapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class AddReservationActivity extends AppCompatActivity {

    private EditText etName, etPhone, etGuests, etNotes;
    private Spinner spinnerTable;
    private Button btnDate, btnTime, btnSave;
    private FirebaseFirestore db;
    private List<Table> availableTables = new ArrayList<>();
    private String selectedDate = "", selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reservation);

        db = FirebaseFirestore.getInstance();
        etName = findViewById(R.id.etResCustomerName);
        etPhone = findViewById(R.id.etResPhone);
        etGuests = findViewById(R.id.etResGuestCount);
        etNotes = findViewById(R.id.etResNotes);
        spinnerTable = findViewById(R.id.spinnerResTable);
        btnDate = findViewById(R.id.btnPickDate);
        btnTime = findViewById(R.id.btnPickTime);
        btnSave = findViewById(R.id.btnSaveReservation);

        loadAvailableTables();

        btnDate.setOnClickListener(v -> showDatePicker());
        btnTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveReservation());
    }

    private void loadAvailableTables() {
        db.collection("tables")
                .whereEqualTo("status", "Free")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    availableTables.clear();
                    List<String> tableNames = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Table table = doc.toObject(Table.class);
                        table.setId(doc.getId());
                        availableTables.add(table);
                        tableNames.add("Table #" + String.format("%02d", table.getTableNumber()) + " (Seats " + table.getCapacity() + ")");
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tableNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerTable.setAdapter(adapter);
                });
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            btnDate.setText(selectedDate);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedTime = String.format("%02d:%02d", hourOfDay, minute);
            btnTime.setText(selectedTime);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void saveReservation() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String guestsStr = etGuests.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || guestsStr.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty() || spinnerTable.getSelectedItem() == null) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int guests = Integer.parseInt(guestsStr);
        Table selectedTable = availableTables.get(spinnerTable.getSelectedItemPosition());

        String id = UUID.randomUUID().toString();
        Reservation res = new Reservation(id, name, phone, selectedTable.getId(), 
                selectedTable.getTableNumber(), guests, selectedDate, selectedTime, notes, "Confirmed");

        btnSave.setEnabled(false);
        db.collection("reservations").document(id).set(res)
                .addOnSuccessListener(aVoid -> {
                    // Update table status
                    db.collection("tables").document(selectedTable.getId()).update("status", "Reserved");
                    createNotification("New Reservation", "For " + name + " on " + selectedDate + " at " + selectedTime, "NewReservation");
                    Toast.makeText(this, "Reservation confirmed", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createNotification(String title, String message, String type) {
        String id = db.collection("notifications").document().getId();
        Notification notification = new Notification(id, title, message, type, System.currentTimeMillis(), false);
        db.collection("notifications").document(id).set(notification);
    }
}
