package com.example.restaurantapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class TablesFragment extends Fragment implements TableAdapter.OnTableClickListener {

    private RecyclerView rvTables;
    private TableAdapter adapter;
    private List<Table> tableList = new ArrayList<>();
    private FirebaseFirestore db;
    private FloatingActionButton fabAddTable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tables, container, false);

        db = FirebaseFirestore.getInstance();
        rvTables = view.findViewById(R.id.rvTables);
        fabAddTable = view.findViewById(R.id.fabAddTable);

        adapter = new TableAdapter(tableList, this);
        rvTables.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvTables.setAdapter(adapter);

        fabAddTable.setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), AddTableActivity.class));
            }
        });

        fetchTables();

        return view;
    }

    private void fetchTables() {
        db.collection("tables").orderBy("tableNumber")
                .addSnapshotListener((value, error) -> {
                    if (getContext() == null) return;
                    if (error != null) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    tableList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Table table = doc.toObject(Table.class);
                            table.setId(doc.getId());
                            tableList.add(table);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onTableClick(Table table) {
        showStatusDialog(table);
    }

    private void showStatusDialog(Table table) {
        String[] options = {"Free", "Occupied", "Reserved"};
        new AlertDialog.Builder(getContext())
                .setTitle("Table #" + table.getTableNumber())
                .setItems(options, (dialog, which) -> {
                    updateTableStatus(table.getId(), options[which]);
                })
                .show();
    }

    private void updateTableStatus(String tableId, String newStatus) {
        db.collection("tables").document(tableId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Status updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to update", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
