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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ReservationsFragment extends Fragment implements ReservationAdapter.OnReservationClickListener {

    private RecyclerView rvReservations;
    private TextView tvEmptyReservations;
    private ChipGroup chipGroupStatus;
    private FloatingActionButton fabAddReservation;

    private ReservationAdapter adapter;
    private List<Reservation> allReservations = new ArrayList<>();
    private List<Reservation> filteredReservations = new ArrayList<>();

    private FirebaseFirestore db;
    private ListenerRegistration reservationListener;
    private String currentFilter = "All";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservations, container, false);

        db = FirebaseFirestore.getInstance();
        rvReservations = view.findViewById(R.id.rvReservations);
        tvEmptyReservations = view.findViewById(R.id.tvEmptyReservations);
        chipGroupStatus = view.findViewById(R.id.chipGroupResStatus);
        fabAddReservation = view.findViewById(R.id.fabAddReservation);

        adapter = new ReservationAdapter(filteredReservations, this);
        rvReservations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReservations.setAdapter(adapter);

        fabAddReservation.setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), AddReservationActivity.class));
            }
        });

        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int id = checkedIds.get(0);
                if (id == R.id.chipResAll) currentFilter = "All";
                else if (id == R.id.chipResConfirmed) currentFilter = "Confirmed";
                else if (id == R.id.chipResPending) currentFilter = "Pending";
                else if (id == R.id.chipResCancelled) currentFilter = "Cancelled";
                
                applyFilter();
            }
        });

        startListening();

        return view;
    }

    private void startListening() {
        Query query = db.collection("reservations").orderBy("date", Query.Direction.ASCENDING);
        
        reservationListener = query.addSnapshotListener((value, error) -> {
            if (getContext() == null) return;
            if (error != null) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            allReservations.clear();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    Reservation res = doc.toObject(Reservation.class);
                    res.setId(doc.getId());
                    allReservations.add(res);
                }
            }
            applyFilter();
        });
    }

    private void applyFilter() {
        filteredReservations.clear();
        if (currentFilter.equals("All")) {
            filteredReservations.addAll(allReservations);
        } else {
            for (Reservation res : allReservations) {
                if (res.getStatus().equalsIgnoreCase(currentFilter)) {
                    filteredReservations.add(res);
                }
            }
        }

        if (filteredReservations.isEmpty()) {
            tvEmptyReservations.setVisibility(View.VISIBLE);
            rvReservations.setVisibility(View.GONE);
        } else {
            tvEmptyReservations.setVisibility(View.GONE);
            rvReservations.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onReservationClick(Reservation reservation) {
        showManagementDialog(reservation);
    }

    private void showManagementDialog(Reservation res) {
        String[] options = {"Confirm", "Cancel", "Delete"};
        new AlertDialog.Builder(getContext())
                .setTitle("Manage Reservation")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: updateStatus(res.getId(), "Confirmed"); break;
                        case 1: updateStatus(res.getId(), "Cancelled"); break;
                        case 2: deleteReservation(res.getId()); break;
                    }
                })
                .show();
    }

    private void updateStatus(String resId, String newStatus) {
        db.collection("reservations").document(resId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) Toast.makeText(getContext(), "Updated", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteReservation(String resId) {
        db.collection("reservations").document(resId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (reservationListener != null) reservationListener.remove();
    }
}
