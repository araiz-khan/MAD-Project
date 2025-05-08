package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    Button btnBooking, btn_chk_book, btnAboutUs, btnLogout, btn_update_book, btn_del_book;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentUser = mAuth.getCurrentUser();

        btnBooking = findViewById(R.id.btnBooking);
        btnLogout = findViewById(R.id.btnLogout);
        btn_chk_book = findViewById(R.id.btn_chk_book);
        btnAboutUs = findViewById(R.id.btnAboutUs);
        btn_update_book = findViewById(R.id.btn_update_book);
        btn_del_book = findViewById(R.id.btn_del_book);

        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Welcome " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
        }

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnBooking.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BookingActivity.class);
            startActivity(intent);
        });

        btn_chk_book.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CheckBookingActivity.class);
            startActivity(intent);
        });

        btn_update_book.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Update_Data.class);
            startActivity(intent);
        });

        btnAboutUs.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutUsActivity.class);
            startActivity(intent);
        });

        // Handle the Delete Booking button click
        btn_del_book.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Booking")
                .setMessage("Are you sure you want to delete your room booking?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Instantiate the DeleteBooking class and delete the booking
                    DeleteBooking deleteBooking = new DeleteBooking(MainActivity.this);
                    deleteBooking.deleteBooking();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
