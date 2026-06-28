package com.example.restaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;
    private TextView tvToolbarTitle, tvNotificationBadge;
    private View notificationContainer;
    private ListenerRegistration pendingOrdersListener, notificationsListener;
    private int unreadNotificationsCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // 1. Check if user is logged in immediately
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        notificationContainer = findViewById(R.id.notificationContainer);
        tvNotificationBadge = findViewById(R.id.tvNotificationBadge);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        notificationContainer.setOnClickListener(v -> {
            NotificationsBottomSheet bottomSheet = new NotificationsBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "NotificationsBottomSheet");
        });

        if (savedInstanceState == null) {
            loadFragment(new MenuFragment(), "Menu");
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "";
            int itemId = item.getItemId();

            if (itemId == R.id.nav_menu) {
                selectedFragment = new MenuFragment();
                title = "Menu";
            } else if (itemId == R.id.nav_tables) {
                selectedFragment = new TablesFragment();
                title = "Tables";
            } else if (itemId == R.id.nav_orders) {
                selectedFragment = new OrdersFragment();
                title = "Orders";
            } else if (itemId == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
                title = "Dashboard";
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                title = "Profile";
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment, title);
                return true;
            }
            return false;
        });

        setupOrderBadge();
        setupNotificationsListener();
    }

    private void setupNotificationsListener() {
        notificationsListener = db.collection("notifications")
                .whereEqualTo("read", false)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        unreadNotificationsCount = value.size();
                        updateNotificationBadge();
                    }
                });
    }

    private void updateNotificationBadge() {
        if (unreadNotificationsCount > 0) {
            tvNotificationBadge.setText(String.valueOf(unreadNotificationsCount));
            tvNotificationBadge.setVisibility(View.VISIBLE);
        } else {
            tvNotificationBadge.setVisibility(View.GONE);
        }
    }

    public void createNotification(String title, String message, String type) {
        String id = db.collection("notifications").document().getId();
        Notification notification = new Notification(id, title, message, type, System.currentTimeMillis(), false);
        db.collection("notifications").document(id).set(notification);
    }

    private void loadFragment(Fragment fragment, String title) {
        try {
            tvToolbarTitle.setText(title);
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading " + title + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void setupOrderBadge() {
        try {
            BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.nav_orders);
            badge.setBackgroundColor(ContextCompat.getColor(this, R.color.bottom_nav_colors)); 
            badge.setBadgeTextColor(ContextCompat.getColor(this, android.R.color.black));
            badge.setVisible(false);

            pendingOrdersListener = db.collection("orders")
                    .whereEqualTo("status", "Pending")
                    .addSnapshotListener((value, error) -> {
                        if (error != null) return;
                        if (value != null) {
                            int count = value.size();
                            if (count > 0) {
                                badge.setNumber(count);
                                badge.setVisible(true);
                            } else {
                                badge.setVisible(false);
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pendingOrdersListener != null) {
            pendingOrdersListener.remove();
        }
        if (notificationsListener != null) {
            notificationsListener.remove();
        }
    }
}
