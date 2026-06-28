package com.example.restaurantapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.List;
import java.util.Locale;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder> {

    public interface OnPromotionClickListener {
        void onPromotionClick(Promotion promotion);
        void onToggleActive(Promotion promotion, boolean isActive);
    }

    private final List<Promotion> promotionList;
    private final OnPromotionClickListener listener;

    public PromotionAdapter(List<Promotion> promotionList, OnPromotionClickListener listener) {
        this.promotionList = promotionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PromotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_promotion, parent, false);
        return new PromotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromotionViewHolder holder, int position) {
        Promotion promo = promotionList.get(position);
        
        holder.tvTitle.setText(promo.getTitle());
        holder.tvCode.setText(promo.getCode());
        
        String valText = promo.getDiscountType().equals("Percentage") ? 
                String.format(Locale.getDefault(), "%.0f%% OFF", promo.getDiscountValue()) :
                String.format(Locale.getDefault(), "$%.2f OFF", promo.getDiscountValue());
        holder.tvValue.setText(valText);
        
        holder.tvUsage.setText("Used " + promo.getUsageCount() + " times");
        holder.tvExpiry.setText("Expires on " + promo.getExpiryDate());
        
        holder.switchActive.setChecked(promo.isActive());

        holder.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) listener.onToggleActive(promo, isChecked);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPromotionClick(promo);
        });
    }

    @Override
    public int getItemCount() {
        return promotionList.size();
    }

    static class PromotionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCode, tvValue, tvUsage, tvExpiry;
        SwitchMaterial switchActive;

        public PromotionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvPromoTitle);
            tvCode = itemView.findViewById(R.id.tvPromoCode);
            tvValue = itemView.findViewById(R.id.tvPromoValue);
            tvUsage = itemView.findViewById(R.id.tvPromoUsage);
            tvExpiry = itemView.findViewById(R.id.tvPromoExpiry);
            switchActive = itemView.findViewById(R.id.switchPromoActive);
        }
    }
}
