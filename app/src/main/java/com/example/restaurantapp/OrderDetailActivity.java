package com.example.restaurantapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvTableNumber, tvTotalPrice, tvTimestamp;
    private LinearLayout stepPending, stepPreparing, stepServed, stepPaid;
    private RecyclerView rvItems;
    private Button btnNextStatus;
    private FirebaseFirestore db;
    private String orderId;
    private ListenerRegistration orderListener;
    private Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        db = FirebaseFirestore.getInstance();
        orderId = getIntent().getStringExtra("ORDER_ID");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTableNumber = findViewById(R.id.tvDetailTableNumber);
        tvTotalPrice = findViewById(R.id.tvDetailTotalPrice);
        tvTimestamp = findViewById(R.id.tvOrderTimestamp);
        rvItems = findViewById(R.id.rvOrderItems);
        btnNextStatus = findViewById(R.id.btnNextStatus);

        stepPending = findViewById(R.id.stepPending);
        stepPreparing = findViewById(R.id.stepPreparing);
        stepServed = findViewById(R.id.stepServed);
        stepPaid = findViewById(R.id.stepPaid);

        rvItems.setLayoutManager(new LinearLayoutManager(this));

        if (orderId != null) {
            startListening();
        }

        btnNextStatus.setOnClickListener(v -> advanceStatus());
    }

    private void startListening() {
        orderListener = db.collection("orders").document(orderId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;

                    currentOrder = snapshot.toObject(Order.class);
                    if (currentOrder != null) {
                        updateUI(currentOrder);
                    }
                });
    }

    private void updateUI(Order order) {
        tvTableNumber.setText(String.valueOf(order.getTableNumber()));
        tvTotalPrice.setText(String.format(Locale.getDefault(), "$%.2f", order.getTotalPrice()));
        
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(order.getTimestamp()));
        tvTimestamp.setText(getString(R.string.ordered_at, time));

        ItemsAdapter adapter = new ItemsAdapter(order.getItems());
        rvItems.setAdapter(adapter);

        updateStepper(order.getStatus());
        updateButton(order.getStatus());
    }

    private void updateStepper(String status) {
        resetStepper();
        String[] stages = {"Pending", "Preparing", "Served", "Paid"};
        LinearLayout[] steps = {stepPending, stepPreparing, stepServed, stepPaid};

        int currentIdx = -1;
        for (int i = 0; i < stages.length; i++) {
            if (stages[i].equalsIgnoreCase(status)) {
                currentIdx = i;
                break;
            }
        }

        for (int i = 0; i <= currentIdx; i++) {
            View dot = steps[i].getChildAt(0);
            TextView text = (TextView) steps[i].getChildAt(1);
            
            if (i < currentIdx) {
                dot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Green
                text.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                dot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F4A732"))); // Amber
                text.setTextColor(Color.parseColor("#F4A732"));
            }
        }
    }

    private void resetStepper() {
        LinearLayout[] steps = {stepPending, stepPreparing, stepServed, stepPaid};
        for (LinearLayout step : steps) {
            step.getChildAt(0).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#A0AEC0")));
            ((TextView) step.getChildAt(1)).setTextColor(Color.parseColor("#A0AEC0"));
        }
    }

    private void updateButton(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                btnNextStatus.setText(getString(R.string.start_preparing));
                btnNextStatus.setVisibility(View.VISIBLE);
                break;
            case "preparing":
                btnNextStatus.setText(getString(R.string.mark_as_served));
                btnNextStatus.setVisibility(View.VISIBLE);
                break;
            case "served":
                btnNextStatus.setText(getString(R.string.mark_as_paid));
                btnNextStatus.setVisibility(View.VISIBLE);
                break;
            case "paid":
                btnNextStatus.setVisibility(View.GONE);
                break;
        }
    }

    private void advanceStatus() {
        if (currentOrder == null) return;
        
        String nextStatus;
        switch (currentOrder.getStatus().toLowerCase()) {
            case "pending": nextStatus = "Preparing"; break;
            case "preparing": nextStatus = "Served"; break;
            case "served": nextStatus = "Paid"; break;
            default: return;
        }

        db.collection("orders").document(orderId)
                .update("status", nextStatus)
                .addOnSuccessListener(aVoid -> {
                    createNotification("Status Updated", "Table " + currentOrder.getTableNumber() + " is now " + nextStatus, "StatusChange");
                })
                .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show());
    }

    private void createNotification(String title, String message, String type) {
        String id = db.collection("notifications").document().getId();
        Notification notification = new Notification(id, title, message, type, System.currentTimeMillis(), false);
        db.collection("notifications").document(id).set(notification);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListener != null) orderListener.remove();
    }

    private static class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {
        private final List<String> items;

        ItemsAdapter(List<String> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_detail_dish, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvName.setText(items.get(position));
        }

        @Override
        public int getItemCount() { return items != null ? items.size() : 0; }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            ViewHolder(View v) { super(v); tvName = v.findViewById(R.id.tvDishName); }
        }
    }
}
