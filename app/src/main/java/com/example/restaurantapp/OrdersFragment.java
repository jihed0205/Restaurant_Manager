package com.example.restaurantapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrdersFragment extends Fragment implements OrderAdapter.OnOrderClickListener {

    private RecyclerView rvOrders;
    private TextView tvEmptyOrders, tvTodayOrderCount, tvTodayRevenueSummary;
    private ChipGroup chipGroupStatus;
    private FloatingActionButton fabAddOrder;
    
    private OrderAdapter adapter;
    private List<Order> allOrders = new ArrayList<>();
    private List<Order> filteredOrders = new ArrayList<>();
    
    private FirebaseFirestore db;
    private ListenerRegistration orderListener;
    private String currentFilter = "All";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        db = FirebaseFirestore.getInstance();
        rvOrders = view.findViewById(R.id.rvOrders);
        tvEmptyOrders = view.findViewById(R.id.tvEmptyOrders);
        tvTodayOrderCount = view.findViewById(R.id.tvTodayOrderCount);
        tvTodayRevenueSummary = view.findViewById(R.id.tvTodayRevenueSummary);
        chipGroupStatus = view.findViewById(R.id.chipGroupStatus);
        fabAddOrder = view.findViewById(R.id.fabAddOrder);

        adapter = new OrderAdapter(filteredOrders, this);
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);

        fabAddOrder.setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), AddOrderActivity.class));
            }
        });

        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int id = checkedIds.get(0);
                if (id == R.id.chipAll) currentFilter = "All";
                else if (id == R.id.chipPending) currentFilter = "Pending";
                else if (id == R.id.chipPreparing) currentFilter = "Preparing";
                else if (id == R.id.chipServed) currentFilter = "Served";
                else if (id == R.id.chipPaid) currentFilter = "Paid";
                
                applyFilter();
            }
        });

        startListening();

        return view;
    }

    private void startListening() {
        Query query = db.collection("orders").orderBy("timestamp", Query.Direction.DESCENDING);
        
        orderListener = query.addSnapshotListener((value, error) -> {
            if (getContext() == null) return;
            
            if (error != null) {
                Toast.makeText(getContext(), getString(R.string.listen_failed, error.getMessage()), Toast.LENGTH_SHORT).show();
                return;
            }

            allOrders.clear();
            double todayRevenue = 0;
            int todayCount = 0;
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    Order order = doc.toObject(Order.class);
                    allOrders.add(order);
                    
                    String orderDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(order.getTimestamp()));
                    if (todayDate.equals(orderDate)) {
                        todayCount++;
                        todayRevenue += order.getTotalPrice();
                    }
                }
            }
            
            tvTodayOrderCount.setText(String.valueOf(todayCount));
            tvTodayRevenueSummary.setText(String.format(Locale.getDefault(), "$%.2f", todayRevenue));

            applyFilter();
        });
    }

    private void applyFilter() {
        filteredOrders.clear();
        if (currentFilter.equals("All")) {
            filteredOrders.addAll(allOrders);
        } else {
            for (Order order : allOrders) {
                if (order.getStatus().equalsIgnoreCase(currentFilter)) {
                    filteredOrders.add(order);
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            tvEmptyOrders.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
        } else {
            tvEmptyOrders.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onOrderClick(Order order) {
        if (getContext() != null) {
            Intent intent = new Intent(getActivity(), OrderDetailActivity.class);
            intent.putExtra("ORDER_ID", order.getId());
            startActivity(intent);
        }
    }

    private void showStatusUpdateDialog(Order order) {
        String[] statuses = {"Pending", "Preparing", "Served", "Paid"};
        int currentIdx = -1;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(order.getStatus())) {
                currentIdx = i;
                break;
            }
        }

        final int nextIdx = currentIdx + 1;
        if (nextIdx >= statuses.length) {
            Toast.makeText(getContext(), getString(R.string.order_already_completed), Toast.LENGTH_SHORT).show();
            return;
        }

        String nextStatus = statuses[nextIdx];

        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.update_order_status))
                .setMessage(getString(R.string.move_table_to_status, order.getTableNumber(), nextStatus))
                .setPositiveButton(getString(R.string.update), (dialog, which) -> {
                    updateOrderStatus(order.getId(), nextStatus);
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void updateOrderStatus(String orderId, String newStatus) {
        db.collection("orders").document(orderId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), getString(R.string.status_updated), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), getString(R.string.error_updating, e.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (orderListener != null) {
            orderListener.remove();
        }
    }
}
