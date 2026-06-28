package com.example.restaurantapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    private List<Order> orderList;
    private OnOrderClickListener listener;

    public OrderAdapter(List<Order> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        
        holder.tvTable.setText(String.valueOf(order.getTableNumber()));
        String shortId = order.getId().substring(0, Math.min(order.getId().length(), 7)).toUpperCase();
        holder.tvId.setText("ID: #" + shortId);
        holder.tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", order.getTotalPrice()));
        
        String status = order.getStatus();
        holder.tvStatus.setText(status.toUpperCase());
        updateStatusStyle(holder.tvStatus, status);

        // Build items summary
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            StringBuilder itemsText = new StringBuilder();
            for (int i = 0; i < order.getItems().size(); i++) {
                itemsText.append("• ").append(order.getItems().get(i));
                if (i < order.getItems().size() - 1) itemsText.append(", ");
            }
            holder.tvItems.setText(itemsText.toString());
        } else {
            holder.tvItems.setText("No items");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    private void updateStatusStyle(TextView tvStatus, String status) {
        switch (status.toLowerCase()) {
            case "pending":
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF757575)); // Gray
                break;
            case "preparing":
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2196F3)); // Blue
                break;
            case "served":
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50)); // Green
                break;
            case "paid":
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF4A732)); // Amber
                break;
            default:
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF757575));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateList(List<Order> newList) {
        this.orderList = newList;
        notifyDataSetChanged();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTable, tvStatus, tvId, tvPrice, tvItems;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTable = itemView.findViewById(R.id.tvOrderTable);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvId = itemView.findViewById(R.id.tvOrderId);
            tvPrice = itemView.findViewById(R.id.tvOrderPrice);
            tvItems = itemView.findViewById(R.id.tvOrderItems);
        }
    }
}
