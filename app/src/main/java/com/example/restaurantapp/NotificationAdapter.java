package com.example.restaurantapp;

import android.content.res.ColorStateList;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notificationList;

    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                notification.getTimestamp(), 
                System.currentTimeMillis(), 
                DateUtils.MINUTE_IN_MILLIS);
        holder.tvTime.setText(timeAgo);

        // Styling based on read status
        if (notification.isRead()) {
            holder.card.setCardBackgroundColor(ColorStateList.valueOf(0xFF1B2A3B)); // Dark navy
        } else {
            holder.card.setCardBackgroundColor(ColorStateList.valueOf(0xFF223044)); // Slightly brighter navy
        }

        // Icon and color based on type
        int iconRes = R.drawable.ic_notifications;
        int colorRes = R.color.colorPrimary; // Default amber

        if ("NewOrder".equals(notification.getType())) {
            iconRes = R.drawable.ic_receipt;
            colorRes = android.R.color.holo_orange_light; // Amber
        } else if ("StatusChange".equals(notification.getType())) {
            iconRes = R.drawable.ic_restaurant;
            colorRes = android.R.color.holo_blue_light; // Blue
        } else if ("NewReservation".equals(notification.getType())) {
            iconRes = R.drawable.ic_event;
            colorRes = android.R.color.holo_green_light; // Green
        } else if ("LowStock".equals(notification.getType())) {
            iconRes = R.drawable.ic_bar_chart;
            colorRes = android.R.color.holo_red_light; // Red
        }

        holder.ivIcon.setImageResource(iconRes);
        holder.ivIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), colorRes)));
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        ImageView ivIcon;
        TextView tvTitle, tvMessage, tvTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardNotification);
            ivIcon = itemView.findViewById(R.id.ivNotificationIcon);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
        }
    }
}
