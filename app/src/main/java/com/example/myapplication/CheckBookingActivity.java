package com.example.myapplication;

import android.os.Bundle;
import android.util.Log; // Import Log for logging errors
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CheckBookingActivity extends AppCompatActivity {

    private TextView resultTextView, welcomeTextView;
    private Button checkButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Define the date format expected from Firestore (used in BookingActivity for storage)
    private static final String DATE_FORMAT_STORAGE = "yyyy-MM-dd";
    private SimpleDateFormat dateFormatStorage = new SimpleDateFormat(DATE_FORMAT_STORAGE, Locale.US);

    // Define the date format for display (optional, if you want to reformat dates)
    private SimpleDateFormat dateFormatDisplay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_booking);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        resultTextView = findViewById(R.id.resultTextView);
        welcomeTextView = findViewById(R.id.welcomeTextView); // Welcome text view
        checkButton = findViewById(R.id.checkButton);

        // Set welcome message with the user's email
        // Check if the user is logged in before getting email
        if (mAuth.getCurrentUser() != null) {
            String userEmail = mAuth.getCurrentUser().getEmail();
            welcomeTextView.setText("Welcome, " + userEmail); // Added "Welcome, " for better context
        } else {
            welcomeTextView.setText("Welcome"); // Default welcome if user not logged in (shouldn't happen if this activity requires login)
            // Optionally, redirect to login activity here
        }


        // Set up button click listener
        checkButton.setOnClickListener(v -> fetchBookingData());
    }

    private void fetchBookingData() {
        // Get the current logged-in user's UID
        // Check if user is logged in before proceeding
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return; // Exit the method if user is not logged in
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Fetch the user's booking data from Firestore
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Retrieve the booking information
                            String firstName = document.getString("first");
                            String lastName = document.getString("last");
                            String roomStyle = document.getString("room style");
                            // Fetch check-in and check-out dates (stored in yyyy-MM-dd format)
                            String checkInDateStr = document.getString("checkInDate");
                            String checkOutDateStr = document.getString("checkOutDate");

                            // Fetch the calculated number of nights and total price
                            // Use .get("fieldName") and cast to Long, then to long primitive
                            Long numberOfNightsLong = document.getLong("numberOfNights");
                            Long totalPriceLong = document.getLong("totalPrice");

                            long numberOfNights = (numberOfNightsLong != null) ? numberOfNightsLong : -1; // Use -1 to indicate not found
                            long totalPrice = (totalPriceLong != null) ? totalPriceLong : -1; // Use -1 to indicate not found


                            // --- Date Formatting for Display (Optional) ---
                            String checkInDisplay = "Not available";
                            String checkOutDisplay = "Not available";
                            if (checkInDateStr != null) {
                                try {
                                    Date checkInDate = dateFormatStorage.parse(checkInDateStr);
                                    checkInDisplay = dateFormatDisplay.format(checkInDate);
                                } catch (ParseException e) {
                                    Log.e("CheckBooking", "Error parsing check-in date for display", e);
                                    checkInDisplay = checkInDateStr + " (Parse Error)"; // Show raw string if parsing fails
                                }
                            }
                            if (checkOutDateStr != null) {
                                try {
                                    Date checkOutDate = dateFormatStorage.parse(checkOutDateStr);
                                    checkOutDisplay = dateFormatDisplay.format(checkOutDate);
                                } catch (ParseException e) {
                                    Log.e("CheckBooking", "Error parsing check-out date for display", e);
                                    checkOutDisplay = checkOutDateStr + " (Parse Error)"; // Show raw string if parsing fails
                                }
                            }
                            // --- End Date Formatting ---


                            // Display the booking details
                            StringBuilder resultBuilder = new StringBuilder();
                            resultBuilder.append("Name: ").append(firstName).append(" ").append(lastName).append("\n");
                            resultBuilder.append("Room Style: ").append(roomStyle).append("\n");

                            // Append dates
                            resultBuilder.append("Check-in: ").append(checkInDisplay).append("\n");
                            resultBuilder.append("Check-out: ").append(checkOutDisplay).append("\n");


                            // Append the number of nights and total price if available
                            if (numberOfNights >= 0) {
                                resultBuilder.append("Nights: ").append(numberOfNights).append("\n");
                            } else {
                                resultBuilder.append("Nights: Not available\n");
                            }

                            if (totalPrice >= 0) {
                                resultBuilder.append("Total Price: Rs.").append(totalPrice);
                            } else {
                                resultBuilder.append("Total Price: Not available");
                            }


                            resultTextView.setText(resultBuilder.toString());

                        } else {
                            resultTextView.setText("No booking found for this user.");
                        }
                    } else {
                        // Handle Firestore task failure
                        Log.e("CheckBooking", "Error fetching data", task.getException()); // Use Log.e for errors
                        Toast.makeText(CheckBookingActivity.this, "Error fetching data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any other exceptions during the fetch operation
                    Log.e("CheckBooking", "Error fetching data", e); // Use Log.e for errors
                    Toast.makeText(CheckBookingActivity.this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
