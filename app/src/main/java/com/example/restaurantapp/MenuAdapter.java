package com.example.restaurantapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private List<MenuItem> menuList;

    public MenuAdapter(List<MenuItem> menuList) {
        this.menuList = menuList;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem item = menuList.get(position);
        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", item.getPrice()));
        holder.tvCategory.setText(item.getCategory());
        
        if (item.isAvailable()) {
            holder.tvAvailability.setText(holder.itemView.getContext().getString(R.string.available));
            holder.indicatorDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark)));
        } else {
            holder.tvAvailability.setText(holder.itemView.getContext().getString(R.string.unavailable));
            holder.indicatorDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark)));
        }
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public void updateList(List<MenuItem> newList) {
        this.menuList = newList;
        notifyDataSetChanged();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvAvailability, tvCategory;
        View indicatorDot;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMenuName);
            tvPrice = itemView.findViewById(R.id.tvMenuPrice);
            tvAvailability = itemView.findViewById(R.id.tvAvailability);
            tvCategory = itemView.findViewById(R.id.chipCategory);
            indicatorDot = itemView.findViewById(R.id.indicatorDot);
        }
    }
}
