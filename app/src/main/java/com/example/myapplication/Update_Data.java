package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class Update_Data extends AppCompatActivity {

    private EditText surnameEditText, ageEditText, nightsEditText, styleEditText;
    private Button updateButton, deleteButton;
    private FirebaseFirestore db;
    private String documentIdToUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_data);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        surnameEditText = findViewById(R.id.surnameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        nightsEditText = findViewById(R.id.nightsEditText);
        styleEditText = findViewById(R.id.styleEditText);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);

        // Fetch booking for current user
        fetchBookingForCurrentUser();

        // Update Booking
        updateButton.setOnClickListener(v -> {
            if (documentIdToUpdate != null) {
                updateBooking();
            } else {
                Toast.makeText(this, "No booking found. Please check first.", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete Booking
        deleteButton.setOnClickListener(v -> {
            if (documentIdToUpdate != null) {
                deleteBooking();
            } else {
                Toast.makeText(this, "No booking found. Please check first.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchBookingForCurrentUser() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .whereEqualTo("userId", uid)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        DocumentSnapshot doc = snapshot.getDocuments().get(0);
                        documentIdToUpdate = doc.getId();

                        // Pre-fill form with user data
                        surnameEditText.setText(doc.getString("last"));
                        ageEditText.setText(String.valueOf(Objects.requireNonNull(doc.getLong("born"))));
                        nightsEditText.setText(doc.getString("nights to stay"));
                        styleEditText.setText(doc.getString("room style"));
                    } else {
                        Toast.makeText(this, "No booking found for this user", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching booking", e);
                    Toast.makeText(this, "Failed to fetch booking", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateBooking() {
        String newSurname = surnameEditText.getText().toString().trim();
        String newAgeStr = ageEditText.getText().toString().trim();
        String newNights = nightsEditText.getText().toString().trim();
        String newStyle = styleEditText.getText().toString().trim();

        if (newSurname.isEmpty() || newAgeStr.isEmpty() || newNights.isEmpty() || newStyle.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Long newAge;
        try {
            newAge = Long.parseLong(newAgeStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid age", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(documentIdToUpdate)
                .update(
                        "last", newSurname,
                        "born", newAge,
                        "nights to stay", newNights,
                        "room style", newStyle
                )
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Booking updated", Toast.LENGTH_SHORT).show();
                    clearInputFields();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update booking", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Update failed", e);
                });
    }

    private void deleteBooking() {
        db.collection("users").document(documentIdToUpdate)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Booking deleted", Toast.LENGTH_SHORT).show();
                    clearInputFields();
                    documentIdToUpdate = null;
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete booking", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Delete failed", e);
                });
    }

    private void clearInputFields() {
        surnameEditText.setText("");
        ageEditText.setText("");
        nightsEditText.setText("");
        styleEditText.setText("");
    }
}
