package com.example.restaurantapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.TableViewHolder> {

    public interface OnTableClickListener {
        void onTableClick(Table table);
    }

    private List<Table> tableList;
    private OnTableClickListener listener;

    public TableAdapter(List<Table> tableList, OnTableClickListener listener) {
        this.tableList = tableList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_table, parent, false);
        return new TableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        Table table = tableList.get(position);
        
        holder.tvNumber.setText(String.format("%02d", table.getTableNumber()));
        holder.tvCapacity.setText(table.getCapacity() + " Persons");
        holder.tvStatus.setText(table.getStatus().toUpperCase());

        int bgColor;
        switch (table.getStatus().toLowerCase()) {
            case "occupied":
                bgColor = Color.parseColor("#B71C1C");
                break;
            case "reserved":
                bgColor = Color.parseColor("#1565C0");
                break;
            default: // Free
                bgColor = Color.parseColor("#1B2A3B");
                break;
        }
        holder.cardView.setCardBackgroundColor(ColorStateList.valueOf(bgColor));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTableClick(table);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tableList.size();
    }

    public void updateList(List<Table> newList) {
        this.tableList = newList;
        notifyDataSetChanged();
    }

    static class TableViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvNumber, tvCapacity, tvStatus;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvNumber = itemView.findViewById(R.id.tvTableNumber);
            tvCapacity = itemView.findViewById(R.id.tvTableCapacity);
            tvStatus = itemView.findViewById(R.id.tvTableStatus);
        }
    }
}
