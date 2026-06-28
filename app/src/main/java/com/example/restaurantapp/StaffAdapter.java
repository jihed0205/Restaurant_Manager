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

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {

    public interface OnStaffClickListener {
        void onStaffClick(Staff staff);
    }

    private final List<Staff> staffList;
    private final OnStaffClickListener listener;

    public StaffAdapter(List<Staff> staffList, OnStaffClickListener listener) {
        this.staffList = staffList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        Staff staff = staffList.get(position);
        
        holder.tvName.setText(staff.getName());
        
        String activeStatus = staff.isActive() ? 
                holder.itemView.getContext().getString(R.string.mark_active).replace("Mark ", "") : 
                holder.itemView.getContext().getString(R.string.mark_inactive).replace("Mark ", "");
                
        holder.tvShift.setText(holder.itemView.getContext().getString(R.string.shift_format, staff.getShift(), activeStatus));
        holder.tvRole.setText(staff.getRole().toUpperCase());

        if (staff.getName() != null && !staff.getName().isEmpty()) {
            String[] parts = staff.getName().split(" ");
            StringBuilder initials = new StringBuilder();
            if (parts.length > 0 && !parts[0].isEmpty()) initials.append(parts[0].charAt(0));
            if (parts.length > 1 && !parts[1].isEmpty()) initials.append(parts[1].charAt(0));
            holder.tvInitials.setText(initials.toString().toUpperCase());
        }

        updateRoleStyle(holder.tvRole, staff.getRole());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStaffClick(staff);
            }
        });
    }

    private void updateRoleStyle(TextView tvRole, String role) {
        int color;
        switch (role.toLowerCase()) {
            case "waiter": color = Color.parseColor("#2196F3"); break; 
            case "chef": color = Color.parseColor("#F44336"); break; 
            case "manager": color = Color.parseColor("#F4A732"); break; 
            case "cashier": color = Color.parseColor("#4CAF50"); break;
            default: color = Color.parseColor("#757575"); break;
        }
        tvRole.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    static class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvName, tvShift, tvRole;

        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tvStaffInitials);
            tvName = itemView.findViewById(R.id.tvStaffName);
            tvShift = itemView.findViewById(R.id.tvStaffShift);
            tvRole = itemView.findViewById(R.id.tvStaffRole);
        }
    }
}
