package com.example.restaurantapp;

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

public class CustomersFragment extends Fragment implements CustomerAdapter.OnCustomerClickListener {

    private TextView tvEmpty;
    private SearchView searchView;
    private CustomerAdapter adapter;
    private final List<Customer> allCustomers = new ArrayList<>();
    private final List<Customer> filteredCustomers = new ArrayList<>();
    private FirebaseFirestore db;
    private ListenerRegistration listener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customers, container, false);

        db = FirebaseFirestore.getInstance();
        RecyclerView rvCustomers = view.findViewById(R.id.rvCustomers);
        tvEmpty = view.findViewById(R.id.tvEmptyCustomers);
        searchView = view.findViewById(R.id.searchViewCustomer);
        View fab = view.findViewById(R.id.fabAddCustomer);

        adapter = new CustomerAdapter(filteredCustomers, this);
        rvCustomers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCustomers.setAdapter(adapter);

        fab.setOnClickListener(v -> {
            if (getActivity() != null) startActivity(new Intent(getActivity(), AddCustomerActivity.class));
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
        listener = db.collection("customers").orderBy("name")
                .addSnapshotListener((value, error) -> {
                    if (getContext() == null) return;
                    if (error != null) {
                        Toast.makeText(getContext(), getString(R.string.error_prefix, error.getMessage()), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    allCustomers.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Customer c = doc.toObject(Customer.class);
                            c.setId(doc.getId());
                            allCustomers.add(c);
                        }
                    }
                    filter(searchView.getQuery().toString());
                });
    }

    private void filter(String query) {
        filteredCustomers.clear();
        if (query.isEmpty()) {
            filteredCustomers.addAll(allCustomers);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Customer c : allCustomers) {
                if (c.getName().toLowerCase().contains(lowerQuery) || c.getPhone().contains(query)) {
                    filteredCustomers.add(c);
                }
            }
        }
        tvEmpty.setVisibility(filteredCustomers.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCustomerClick(Customer customer) {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), CustomerDetailActivity.class);
            intent.putExtra("CUSTOMER_ID", customer.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listener != null) listener.remove();
    }
}
