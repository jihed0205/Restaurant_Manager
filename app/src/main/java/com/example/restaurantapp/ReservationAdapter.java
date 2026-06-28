package com.example.restaurantapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    public interface OnReservationClickListener {
        void onReservationClick(Reservation reservation);
    }

    private List<Reservation> reservationList;
    private OnReservationClickListener listener;

    public ReservationAdapter(List<Reservation> reservationList, OnReservationClickListener listener) {
        this.reservationList = reservationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation res = reservationList.get(position);
        
        holder.tvCustomerName.setText(res.getCustomerName());
        holder.tvDateTime.setText(res.getDate() + " • " + res.getTime());
        holder.tvDetails.setText(res.getGuestCount() + " Guests • Table #" + String.format("%02d", res.getTableNumber()));
        
        holder.tvStatus.setText(res.getStatus().toUpperCase());
        updateStatusStyle(holder.tvStatus, res.getStatus());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReservationClick(res);
            }
        });
    }

    private void updateStatusStyle(TextView tvStatus, String status) {
        switch (status.toLowerCase()) {
            case "confirmed":
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50)); // Green
                break;
            case "pending":
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF757575)); // Gray
                break;
            case "cancelled":
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFB71C1C)); // Red
                break;
            default:
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF1B2A3B));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvStatus, tvDateTime, tvDetails;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvResCustomerName);
            tvStatus = itemView.findViewById(R.id.tvResStatus);
            tvDateTime = itemView.findViewById(R.id.tvResDateTime);
            tvDetails = itemView.findViewById(R.id.tvResDetails);
        }
    }
}
