package com.example.restaurantapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    public interface OnCustomerClickListener {
        void onCustomerClick(Customer customer);
    }

    private final List<Customer> customerList;
    private final OnCustomerClickListener listener;

    public CustomerAdapter(List<Customer> customerList, OnCustomerClickListener listener) {
        this.customerList = customerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer customer = customerList.get(position);
        
        holder.tvName.setText(customer.getName());
        holder.tvPoints.setText(String.format(Locale.getDefault(), "%d Points", customer.getLoyaltyPoints()));
        holder.tvStats.setText(String.format(Locale.getDefault(), "$%.2f Spent • %d Visits", customer.getTotalSpent(), customer.getTotalVisits()));
        
        holder.tvTier.setText(customer.getTier().toUpperCase());
        updateTierStyle(holder.tvTier, customer.getTier());

        // Initials Avatar
        if (customer.getName() != null && !customer.getName().isEmpty()) {
            String[] parts = customer.getName().split(" ");
            StringBuilder initials = new StringBuilder();
            if (parts.length > 0 && !parts[0].isEmpty()) initials.append(parts[0].charAt(0));
            if (parts.length > 1 && !parts[1].isEmpty()) initials.append(parts[1].charAt(0));
            holder.tvInitials.setText(initials.toString().toUpperCase());
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCustomerClick(customer);
        });
    }

    private void updateTierStyle(TextView tvTier, String tier) {
        int color;
        switch (tier.toLowerCase()) {
            case "platinum": color = Color.parseColor("#E5E4E2"); break;
            case "gold": color = Color.parseColor("#FFD700"); break;
            case "silver": color = Color.parseColor("#C0C0C0"); break;
            case "bronze": color = Color.parseColor("#CD7F32"); break;
            default: color = Color.parseColor("#757575"); break;
        }
        tvTier.setBackgroundTintList(ColorStateList.valueOf(color));
        tvTier.setTextColor(tier.equalsIgnoreCase("platinum") || tier.equalsIgnoreCase("gold") ? Color.BLACK : Color.WHITE);
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    static class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvName, tvTier, tvPoints, tvStats;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tvCustomerInitials);
            tvName = itemView.findViewById(R.id.tvCustomerName);
            tvTier = itemView.findViewById(R.id.tvCustomerTier);
            tvPoints = itemView.findViewById(R.id.tvCustomerPoints);
            tvStats = itemView.findViewById(R.id.tvCustomerStats);
        }
    }
}
