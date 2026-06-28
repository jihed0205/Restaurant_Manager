package com.example.restaurantapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class StaffFragment extends Fragment implements StaffAdapter.OnStaffClickListener {

    private RecyclerView rvStaff;
    private TextView tvEmptyStaff;
    private SearchView searchView;

    private StaffAdapter adapter;
    private final List<Staff> allStaff = new ArrayList<>();
    private final List<Staff> filteredStaff = new ArrayList<>();

    private FirebaseFirestore db;
    private ListenerRegistration staffListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_staff, container, false);

        db = FirebaseFirestore.getInstance();
        rvStaff = view.findViewById(R.id.rvStaff);
        tvEmptyStaff = view.findViewById(R.id.tvEmptyStaff);
        searchView = view.findViewById(R.id.searchViewStaff);
        View fabAddStaff = view.findViewById(R.id.fabAddStaff);

        adapter = new StaffAdapter(filteredStaff, this);
        rvStaff.setLayoutManager(new LinearLayoutManager(getContext()));
        rvStaff.setAdapter(adapter);

        fabAddStaff.setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), AddStaffActivity.class));
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });

        startListening();

        return view;
    }

    private void startListening() {
        staffListener = db.collection("staff").orderBy("name")
                .addSnapshotListener((value, error) -> {
                    if (getContext() == null) return;
                    if (error != null) {
                        Toast.makeText(getContext(), getString(R.string.error_prefix, error.getMessage()), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    allStaff.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Staff staff = doc.toObject(Staff.class);
                            staff.setId(doc.getId());
                            allStaff.add(staff);
                        }
                    }
                    filter(searchView.getQuery().toString());
                });
    }

    private void filter(String query) {
        filteredStaff.clear();
        if (query.isEmpty()) {
            filteredStaff.addAll(allStaff);
        } else {
            for (Staff s : allStaff) {
                if (s.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredStaff.add(s);
                }
            }
        }

        if (filteredStaff.isEmpty()) {
            tvEmptyStaff.setVisibility(View.VISIBLE);
            rvStaff.setVisibility(View.GONE);
        } else {
            tvEmptyStaff.setVisibility(View.GONE);
            rvStaff.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStaffClick(Staff staff) {
        showManagementDialog(staff);
    }

    private void showManagementDialog(Staff staff) {
        String activeAction = staff.isActive() ? getString(R.string.mark_inactive) : getString(R.string.mark_active);
        String[] options = {getString(R.string.change_role), activeAction, getString(R.string.delete_member)};
        
        new AlertDialog.Builder(getContext())
                .setTitle(staff.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: showRoleDialog(staff); break;
                        case 1: updateActiveStatus(staff); break;
                        case 2: deleteStaff(staff.getId()); break;
                    }
                })
                .show();
    }

    private void showRoleDialog(Staff staff) {
        String[] roles = {"Waiter", "Chef", "Manager", "Cashier"};
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.select_role))
                .setItems(roles, (dialog, which) -> 
                    db.collection("staff").document(staff.getId()).update("role", roles[which])
                )
                .show();
    }

    private void updateActiveStatus(Staff staff) {
        db.collection("staff").document(staff.getId()).update("active", !staff.isActive());
    }

    private void deleteStaff(String id) {
        db.collection("staff").document(id).delete();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (staffListener != null) staffListener.remove();
    }
}
