package com.example.restaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        ImageView ivLogo = findViewById(R.id.ivLogo);
        View tvAppName = findViewById(R.id.tvAppName);
        View tvTagline = findViewById(R.id.tvTagline);

        // Logo animation: Scale 0 to 1 and fade in over 800ms
        ivLogo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(800)
                .start();

        // Text animations: Fade in with 400ms delay
        tvAppName.animate()
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(400)
                .start();

        tvTagline.animate()
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(400)
                .start();

        // Navigate to LoginActivity after 2500ms total
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }, 2500);
    }
}
