package com.example.restaurantapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private TextView tvTotalRevenue, tvTodayRevenue, tvTotalOrders, tvActiveOrders;
    private BarChart barChart;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        db = FirebaseFirestore.getInstance();
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvTodayRevenue = view.findViewById(R.id.tvTodayRevenue);
        tvTotalOrders = view.findViewById(R.id.tvTotalOrders);
        tvActiveOrders = view.findViewById(R.id.tvActiveOrders);
        barChart = view.findViewById(R.id.barChart);

        setupChart();
        loadDashboardData();

        return view;
    }

    private void setupChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.getLegend().setEnabled(false);
        
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setDrawAxisLine(true);
        xAxis.setTextColor(Color.WHITE);
        
        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisLeft().setGridColor(Color.parseColor("#2C3E50"));
        barChart.getAxisRight().setEnabled(false);
    }

    private void loadDashboardData() {
        db.collection("orders").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (getContext() == null) return;
            
            double totalRev = 0;
            double todayRev = 0;
            int totalOrd = 0;
            int activeOrd = 0;
            
            Map<String, Integer> statusCounts = new HashMap<>();
            statusCounts.put("Pending", 0);
            statusCounts.put("Preparing", 0);
            statusCounts.put("Served", 0);
            statusCounts.put("Paid", 0);

            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Order order = doc.toObject(Order.class);
                totalRev += order.getTotalPrice();
                totalOrd++;

                String orderDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(order.getTimestamp()));
                if (todayDate.equals(orderDate)) {
                    todayRev += order.getTotalPrice();
                }

                String status = order.getStatus();
                if (status != null && statusCounts.containsKey(status)) {
                    statusCounts.put(status, statusCounts.get(status) + 1);
                }
                
                if (status != null && (status.equals("Pending") || status.equals("Preparing"))) {
                    activeOrd++;
                }
            }

            tvTotalRevenue.setText(String.format(Locale.getDefault(), "$%.2f", totalRev));
            todayRev = Math.max(0, todayRev);
            tvTodayRevenue.setText(String.format(Locale.getDefault(), "$%.2f", todayRev));
            tvTotalOrders.setText(String.valueOf(totalOrd));
            tvActiveOrders.setText(String.valueOf(activeOrd));

            updateChart(statusCounts);
        }).addOnFailureListener(e -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), getString(R.string.error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateChart(Map<String, Integer> statusCounts) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        String[] statuses = {"Pending", "Preparing", "Served", "Paid"};
        for (int i = 0; i < statuses.length; i++) {
            Integer count = statusCounts.get(statuses[i]);
            entries.add(new BarEntry(i, count != null ? count : 0));
            labels.add(statuses[i]);
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(new int[]{
                Color.parseColor("#FF9800"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#F4A732")
        });
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.invalidate();
        barChart.animateY(1000);
    }
}
