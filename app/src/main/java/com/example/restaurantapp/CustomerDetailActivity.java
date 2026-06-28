package com.example.restaurantapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerDetailActivity extends AppCompatActivity {

    private TextView tvInitials, tvName, tvTier, tvPoints, tvSpent, tvVisits, tvNextTier;
    private ProgressBar pbTier;
    private RecyclerView rvHistory;
    private FirebaseFirestore db;
    private String customerId;
    private Customer currentCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);

        db = FirebaseFirestore.getInstance();
        customerId = getIntent().getStringExtra("CUSTOMER_ID");

        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvInitials = findViewById(R.id.tvDetailInitials);
        tvName = findViewById(R.id.tvDetailName);
        tvTier = findViewById(R.id.tvDetailTier);
        tvPoints = findViewById(R.id.tvDetailPoints);
        tvSpent = findViewById(R.id.tvDetailSpent);
        tvVisits = findViewById(R.id.tvDetailVisits);
        tvNextTier = findViewById(R.id.tvNextTierInfo);
        pbTier = findViewById(R.id.pbTierProgress);
        rvHistory = findViewById(R.id.rvOrderHistory);
        Button btnAddPoints = findViewById(R.id.btnAddPoints);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        if (customerId != null) {
            loadCustomerData();
            loadOrderHistory();
        }

        btnAddPoints.setOnClickListener(v -> addManualPoints());
    }

    private void loadCustomerData() {
        db.collection("customers").document(customerId).addSnapshotListener((snapshot, e) -> {
            if (e != null || snapshot == null || !snapshot.exists()) return;
            currentCustomer = snapshot.toObject(Customer.class);
            if (currentCustomer != null) {
                updateUI(currentCustomer);
            }
        });
    }

    private void updateUI(Customer c) {
        tvName.setText(c.getName());
        tvTier.setText(getString(R.string.member_format, c.getTier().toUpperCase()));
        tvPoints.setText(String.valueOf(c.getLoyaltyPoints()));
        tvSpent.setText(String.format(Locale.getDefault(), "$%.2f", c.getTotalSpent()));
        tvVisits.setText(String.valueOf(c.getTotalVisits()));

        if (c.getName() != null && !c.getName().isEmpty()) {
            String[] parts = c.getName().split(" ");
            StringBuilder initials = new StringBuilder();
            if (parts.length > 0 && !parts[0].isEmpty()) initials.append(parts[0].charAt(0));
            if (parts.length > 1 && !parts[1].isEmpty()) initials.append(parts[1].charAt(0));
            tvInitials.setText(initials.toString().toUpperCase());
        }

        int points = c.getLoyaltyPoints();
        int nextGoal;
        String nextTier;
        if (points < 100) { nextGoal = 100; nextTier = "Silver"; }
        else if (points < 300) { nextGoal = 300; nextTier = "Gold"; }
        else if (points < 600) { nextGoal = 600; nextTier = "Platinum"; }
        else { nextGoal = points; nextTier = "Platinum"; }

        int remaining = nextGoal - points;
        if (remaining > 0) {
            tvNextTier.setText(getString(R.string.next_tier_format, remaining, nextTier));
            pbTier.setMax(nextGoal);
            pbTier.setProgress(points);
        } else {
            tvNextTier.setText(getString(R.string.top_tier));
            pbTier.setMax(100);
            pbTier.setProgress(100);
        }
    }

    private void loadOrderHistory() {
        db.collection("orders")
                .whereEqualTo("customerId", customerId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> orders = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        orders.add(doc.toObject(Order.class));
                    }
                    OrderAdapter adapter = new OrderAdapter(orders, null);
                    rvHistory.setAdapter(adapter);
                });
    }

    private void addManualPoints() {
        if (currentCustomer == null) return;
        int newPoints = currentCustomer.getLoyaltyPoints() + 50;
        db.collection("customers").document(customerId).update("loyaltyPoints", newPoints)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, getString(R.string.points_added), Toast.LENGTH_SHORT).show());
    }
}
