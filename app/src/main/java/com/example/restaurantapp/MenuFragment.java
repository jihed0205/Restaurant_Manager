package com.example.restaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment {

    private RecyclerView rvMenu;
    private TextView tvEmptyMenu;
    private FloatingActionButton fabAddMenu;
    private ChipGroup chipGroupCategories;
    private MenuAdapter adapter;
    private List<MenuItem> menuList;
    private List<MenuItem> allItems = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentCategory = "All";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        db = FirebaseFirestore.getInstance();
        rvMenu = view.findViewById(R.id.rvMenu);
        tvEmptyMenu = view.findViewById(R.id.tvEmptyMenu);
        fabAddMenu = view.findViewById(R.id.fabAddMenu);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);

        menuList = new ArrayList<>();
        adapter = new MenuAdapter(menuList);
        rvMenu.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMenu.setAdapter(adapter);

        fabAddMenu.setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), AddMenuItemActivity.class));
            }
        });

        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipAll) currentCategory = "All";
                else if (checkedId == R.id.chipStarter) currentCategory = "Starter";
                else if (checkedId == R.id.chipMain) currentCategory = "Main";
                else if (checkedId == R.id.chipDessert) currentCategory = "Dessert";
                else if (checkedId == R.id.chipDrink) currentCategory = "Drink";
                
                applyFilter();
            }
        });

        fetchMenuItems();

        return view;
    }

    private void applyFilter() {
        List<MenuItem> filteredList = new ArrayList<>();
        if (currentCategory.equals("All")) {
            filteredList.addAll(allItems);
        } else {
            for (MenuItem item : allItems) {
                if (item.getCategory().equalsIgnoreCase(currentCategory)) {
                    filteredList.add(item);
                }
            }
        }

        if (filteredList.isEmpty()) {
            tvEmptyMenu.setVisibility(View.VISIBLE);
            rvMenu.setVisibility(View.GONE);
        } else {
            tvEmptyMenu.setVisibility(View.GONE);
            rvMenu.setVisibility(View.VISIBLE);
        }
        adapter.updateList(filteredList);
    }

    private void fetchMenuItems() {
        db.collection("menuItems")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (getContext() == null) return;
                        
                        if (error != null) {
                            Toast.makeText(getContext(), getString(R.string.listen_failed, error.getMessage()), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        allItems.clear();
                        if (value != null) {
                            for (QueryDocumentSnapshot doc : value) {
                                MenuItem item = doc.toObject(MenuItem.class);
                                allItems.add(item);
                            }
                        }

                        applyFilter();
                    }
                });
    }
}
