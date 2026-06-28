package com.example.restaurantapp;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PromotionsFragment extends Fragment implements PromotionAdapter.OnPromotionClickListener {

    private RecyclerView rvPromotions;
    private TextView tvEmpty;
    private ChipGroup chipGroup;
    private PromotionAdapter adapter;
    private final List<Promotion> allPromos = new ArrayList<>();
    private final List<Promotion> filteredPromos = new ArrayList<>();
    private FirebaseFirestore db;
    private ListenerRegistration listener;
    private String currentFilter = "All";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_promotions, container, false);

        db = FirebaseFirestore.getInstance();
        rvPromotions = view.findViewById(R.id.rvPromotions);
        tvEmpty = view.findViewById(R.id.tvEmptyPromotions);
        chipGroup = view.findViewById(R.id.chipGroupPromoFilter);
        View fab = view.findViewById(R.id.fabAddPromotion);

        adapter = new PromotionAdapter(filteredPromos, this);
        rvPromotions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPromotions.setAdapter(adapter);

        fab.setOnClickListener(v -> {
            if (getActivity() != null) startActivity(new Intent(getActivity(), AddPromotionActivity.class));
        });

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int id = checkedIds.get(0);
                if (id == R.id.chipPromoAll) currentFilter = "All";
                else if (id == R.id.chipPromoActive) currentFilter = "Active";
                else if (id == R.id.chipPromoExpired) currentFilter = "Expired";
                applyFilter();
            }
        });

        startListening();
        return view;
    }

    private void startListening() {
        listener = db.collection("promotions").addSnapshotListener((value, error) -> {
            if (getContext() == null || error != null || value == null) return;
            allPromos.clear();
            for (QueryDocumentSnapshot doc : value) {
                Promotion p = doc.toObject(Promotion.class);
                p.setId(doc.getId());
                allPromos.add(p);
            }
            applyFilter();
        });
    }

    private void applyFilter() {
        filteredPromos.clear();
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        
        for (Promotion p : allPromos) {
            boolean isExpired = isDateBefore(p.getExpiryDate(), today);
            if (currentFilter.equals("All")) filteredPromos.add(p);
            else if (currentFilter.equals("Active") && p.isActive() && !isExpired) filteredPromos.add(p);
            else if (currentFilter.equals("Expired") && isExpired) filteredPromos.add(p);
        }

        tvEmpty.setVisibility(filteredPromos.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();
    }

    private boolean isDateBefore(String dateStr, String todayStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.parse(dateStr).before(sdf.parse(todayStr));
        } catch (Exception e) { return false; }
    }

    @Override
    public void onPromotionClick(Promotion promo) {
        String[] options = {"Copy Code", "Delete Promotion"};
        new AlertDialog.Builder(getContext())
                .setTitle(promo.getTitle())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) copyToClipboard(promo.getCode());
                    else deletePromo(promo.getId());
                }).show();
    }

    @Override
    public void onToggleActive(Promotion promo, boolean isActive) {
        db.collection("promotions").document(promo.getId()).update("active", isActive);
    }

    private void copyToClipboard(String code) {
        if (getContext() == null) return;
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Promo Code", code);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), "Code copied", Toast.LENGTH_SHORT).show();
    }

    private void deletePromo(String id) {
        db.collection("promotions").document(id).delete();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listener != null) listener.remove();
    }
}
