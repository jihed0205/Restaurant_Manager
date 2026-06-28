package com.example.restaurantapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddOrderActivity extends AppCompatActivity {

    private EditText etTableNumber, etCustPhoneSearch;
    private TextView tvSelectedItemsSummary, tvOrderTotalPrice;
    private Button btnSelectItems, btnSaveOrder, btnSearchCust;
    private FirebaseFirestore db;

    private List<MenuItem> fullMenu = new ArrayList<>();
    private List<MenuItem> selectedItems = new ArrayList<>();
    private String[] menuNames;
    private boolean[] checkedItems;
    private String selectedCustomerId = "";
    private Customer linkedCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);

        db = FirebaseFirestore.getInstance();

        etTableNumber = findViewById(R.id.etTableNumber);
        etCustPhoneSearch = findViewById(R.id.etCustPhoneSearch);
        tvSelectedItemsSummary = findViewById(R.id.tvSelectedItemsSummary);
        tvOrderTotalPrice = findViewById(R.id.tvOrderTotalPrice);
        btnSelectItems = findViewById(R.id.btnSelectItems);
        btnSaveOrder = findViewById(R.id.btnSaveOrder);
        btnSearchCust = findViewById(R.id.btnSearchCust);

        loadMenu();

        btnSelectItems.setOnClickListener(v -> showItemSelectionDialog());
        btnSaveOrder.setOnClickListener(v -> saveOrder());
        btnSearchCust.setOnClickListener(v -> searchCustomer());
    }

    private void searchCustomer() {
        String phone = etCustPhoneSearch.getText().toString().trim();
        if (phone.isEmpty()) return;

        db.collection("customers").whereEqualTo("phone", phone).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        linkedCustomer = queryDocumentSnapshots.getDocuments().get(0).toObject(Customer.class);
                        if (linkedCustomer != null) {
                            linkedCustomer.setId(queryDocumentSnapshots.getDocuments().get(0).getId());
                            selectedCustomerId = linkedCustomer.getId();
                            String text = "Linked: " + linkedCustomer.getName();
                            btnSearchCust.setText(text);
                            Toast.makeText(this, getString(R.string.customer_linked), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.customer_not_found), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMenu() {
        db.collection("menuItems")
                .whereEqualTo("available", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fullMenu.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        fullMenu.add(doc.toObject(MenuItem.class));
                    }
                    prepareDialogData();
                })
                .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show());
    }

    private void prepareDialogData() {
        menuNames = new String[fullMenu.size()];
        checkedItems = new boolean[fullMenu.size()];
        for (int i = 0; i < fullMenu.size(); i++) {
            menuNames[i] = fullMenu.get(i).getName() + " ($" + String.format(Locale.getDefault(), "%.2f", fullMenu.get(i).getPrice()) + ")";
        }
    }

    private void showItemSelectionDialog() {
        if (menuNames == null || menuNames.length == 0) {
            Toast.makeText(this, getString(R.string.menu_loading_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_dishes));
        builder.setMultiChoiceItems(menuNames, checkedItems, (dialog, which, isChecked) -> {
            checkedItems[which] = isChecked;
        });

        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            updateSelectedItems();
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();
    }

    private void updateSelectedItems() {
        selectedItems.clear();
        StringBuilder summary = new StringBuilder();
        double total = 0;

        for (int i = 0; i < checkedItems.length; i++) {
            if (checkedItems[i]) {
                MenuItem item = fullMenu.get(i);
                selectedItems.add(item);
                summary.append("• ").append(item.getName()).append("\n");
                total += item.getPrice();
            }
        }

        tvSelectedItemsSummary.setText(summary.length() > 0 ? summary.toString() : getString(R.string.no_items_selected));
        tvOrderTotalPrice.setText(String.format(Locale.getDefault(), "$%.2f", total));
    }

    private void saveOrder() {
        String tableStr = etTableNumber.getText().toString().trim();
        if (tableStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.enter_table_number), Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, getString(R.string.select_at_least_one), Toast.LENGTH_SHORT).show();
            return;
        }

        int tableNumber = Integer.parseInt(tableStr);
        double total = 0;
        List<String> itemNames = new ArrayList<>();
        for (MenuItem item : selectedItems) {
            itemNames.add(item.getName());
            total += item.getPrice();
        }
        final double finalTotal = total;

        String orderId = UUID.randomUUID().toString();
        Order newOrder = new Order(
                orderId,
                tableNumber,
                itemNames,
                finalTotal,
                "Pending",
                System.currentTimeMillis()
        );
        newOrder.setCustomerId(selectedCustomerId);

        btnSaveOrder.setEnabled(false);
        db.collection("orders").document(orderId).set(newOrder)
                .addOnSuccessListener(aVoid -> {
                    if (!selectedCustomerId.isEmpty()) {
                        updateLoyaltyPoints(finalTotal);
                    }
                    createNotification("New Order", "Order #" + orderId.substring(0, 5) + " placed for Table " + tableNumber, "NewOrder");
                    Toast.makeText(this, getString(R.string.order_placed_success), Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSaveOrder.setEnabled(true);
                    Toast.makeText(this, getString(R.string.error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateLoyaltyPoints(double spent) {
        if (linkedCustomer == null) return;
        
        int earnedPoints = (int) spent;
        linkedCustomer.setLoyaltyPoints(linkedCustomer.getLoyaltyPoints() + earnedPoints);
        linkedCustomer.setTotalSpent(linkedCustomer.getTotalSpent() + spent);
        linkedCustomer.setTotalVisits(linkedCustomer.getTotalVisits() + 1);
        
        db.collection("customers").document(selectedCustomerId).set(linkedCustomer);
    }

    private void createNotification(String title, String message, String type) {
        String id = db.collection("notifications").document().getId();
        Notification notification = new Notification(id, title, message, type, System.currentTimeMillis(), false);
        db.collection("notifications").document(id).set(notification);
    }
}
