package com.example.myapplication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import android.widget.Toast;
import android.content.Context;

public class DeleteBooking {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Context context;

    public DeleteBooking(Context context) {
        this.context = context;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public void deleteBooking() {
        // Get the current logged-in user's UID
        String userId = mAuth.getCurrentUser().getUid();

        // Delete the booking data from Firestore
        db.collection("users")
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Show success message
                    Toast.makeText(context, "Booking Deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Show error message
                    Toast.makeText(context, "Error deleting booking", Toast.LENGTH_SHORT).show();
                });
    }
}
