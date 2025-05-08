package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog; // Import AlertDialog
import android.app.DatePickerDialog;
import android.content.DialogInterface; // Import DialogInterface
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Update_Data extends AppCompatActivity {

    private EditText surnameEditText, ageEditText;
    private EditText styleEditText; // Keep as EditText to display selected style
    private TextView nightsTextView;
    private TextView totalPriceTextView;
    private EditText checkInDateEditText, checkOutDateEditText;
    private Button updateButton;
    private FirebaseFirestore db;
    private String documentIdToUpdate; // This will be the user's UID

    // Date format for storing/parsing dates (consistent with BookingActivity)
    private SimpleDateFormat dateFormatStorage = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    // Date format for displaying dates to the user
    private SimpleDateFormat dateFormatDisplay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Variables to hold the selected dates as strings
    private String selectedCheckInDateStr = "";
    private String selectedCheckOutDateStr = "";

    // Array of room styles for the dialog
    private final String[] roomStyles = {"Deluxe", "Premium", "Executive"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_data); // Make sure this is the correct layout file

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        surnameEditText = findViewById(R.id.surnameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        styleEditText = findViewById(R.id.styleEditText); // Initialize styleEditText

        // Initialize TextViews for nights and total price
        nightsTextView = findViewById(R.id.nightsTextView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);

        // Initialize date EditTexts
        checkInDateEditText = findViewById(R.id.checkInDateEditText);
        checkOutDateEditText = findViewById(R.id.checkOutDateEditText);

        updateButton = findViewById(R.id.updateButton);

        // Fetch booking data for the current user to pre-fill the form
        fetchBookingForCurrentUser();

        // Set up click listeners for date selection
        if (checkInDateEditText != null) {
            checkInDateEditText.setOnClickListener(v -> pickDate(true));
        }
        if (checkOutDateEditText != null) {
            checkOutDateEditText.setOnClickListener(v -> pickDate(false));
        }

        // Set up click listener for room style selection
        if (styleEditText != null) {
            styleEditText.setOnClickListener(v -> showRoomSelectionDialog());
            // Make it non-focusable so clicking opens the dialog, not the keyboard
            styleEditText.setFocusable(false);
            styleEditText.setFocusableInTouchMode(false);
        }


        // Set up update button click listener
        updateButton.setOnClickListener(v -> {
            if (documentIdToUpdate != null) {
                updateBooking();
            } else {
                Toast.makeText(this, "No booking found to update.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to show room style selection dialog
    private void showRoomSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Room Style");
        builder.setItems(roomStyles, (dialog, which) -> {
            // 'which' is the index of the selected item
            String selectedStyle = roomStyles[which];
            styleEditText.setText(selectedStyle); // Update the EditText text
            recalculateNightsAndPrice(); // Recalculate price based on the new style
        });
        builder.show();
    }


    // Fetch booking for the current user using their UID
    private void fetchBookingForCurrentUser() {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        documentIdToUpdate = uid; // Set documentIdToUpdate to the user's UID

        // Directly access the user's document using their UID
        db.collection("users")
                .document(uid)  // The document ID is the UID of the user
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        // Document found, pre-fill form with user data
                        surnameEditText.setText(snapshot.getString("last"));

                        // Handle age as Long from Firestore, convert to String for EditText
                        Long ageLong = snapshot.getLong("born");
                        if (ageLong != null) {
                            ageEditText.setText(String.valueOf(ageLong));
                        } else {
                            ageEditText.setText(""); // Or a default value
                        }

                        // Pre-fill date fields
                        String checkInDateFromDb = snapshot.getString("checkInDate");
                        String checkOutDateFromDb = snapshot.getString("checkOutDate");

                        if (checkInDateFromDb != null) {
                            selectedCheckInDateStr = checkInDateFromDb; // Store for potential update
                            try {
                                // Format for display
                                Date date = dateFormatStorage.parse(checkInDateFromDb);
                                checkInDateEditText.setText(dateFormatDisplay.format(date));
                            } catch (ParseException e) {
                                Log.e(TAG, "Error parsing check-in date from DB", e);
                                checkInDateEditText.setText(checkInDateFromDb + " (Parse Error)"); // Show raw if parsing fails
                            }
                        }

                        if (checkOutDateFromDb != null) {
                            selectedCheckOutDateStr = checkOutDateFromDb; // Store for potential update
                            try {
                                // Format for display
                                Date date = dateFormatStorage.parse(checkOutDateFromDb);
                                checkOutDateEditText.setText(dateFormatDisplay.format(date));
                            } catch (ParseException e) {
                                Log.e(TAG, "Error parsing check-out date from DB", e);
                                checkOutDateEditText.setText(checkOutDateFromDb + " (Parse Error)"); // Show raw if parsing fails
                            }
                        }

                        // Pre-fill room style EditText
                        String roomStyleFromDb = snapshot.getString("room style");
                        if (roomStyleFromDb != null) {
                            styleEditText.setText(roomStyleFromDb);
                        } else {
                            styleEditText.setText("");
                        }

                        // Pre-fill nights TextView (will be recalculated on date change)
                        Long nightsLong = snapshot.getLong("numberOfNights");
                        if (nightsLong != null) {
                            nightsTextView.setText("Total Number of Nights: " + String.valueOf(nightsLong));
                        } else {
                            nightsTextView.setText("Total Number of Nights: N/A");
                        }

                        // Pre-fill total price TextView
                        Long totalPriceLong = snapshot.getLong("totalPrice");
                        if (totalPriceLong != null) {
                            totalPriceTextView.setText("Total Price: Rs." + String.valueOf(totalPriceLong));
                        } else {
                            totalPriceTextView.setText("Total Price: N/A");
                        }


                    } else {
                        Toast.makeText(this, "No booking found for this user", Toast.LENGTH_SHORT).show();
                        clearInputFields();
                        documentIdToUpdate = null;
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching booking", e);
                    Toast.makeText(this, "Failed to fetch booking", Toast.LENGTH_SHORT).show();
                    clearInputFields();
                    documentIdToUpdate = null;
                });
    }

    // Method to show date picker dialog
    private void pickDate(boolean isCheckIn) {
        Calendar calendar = Calendar.getInstance();
        // Set the initial date of the picker to the currently selected date if available
        Date initialDate = null;
        try {
            if (isCheckIn && !selectedCheckInDateStr.isEmpty()) {
                initialDate = dateFormatStorage.parse(selectedCheckInDateStr);
            } else if (!isCheckIn && !selectedCheckOutDateStr.isEmpty()) {
                initialDate = dateFormatStorage.parse(selectedCheckOutDateStr);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing initial date for picker", e);
            initialDate = null; // Fallback to current date if parsing fails
        }

        if (initialDate != null) {
            calendar.setTime(initialDate);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    // Store date in a consistent format for calculation and storage
                    String dateForStorage = dateFormatStorage.format(selectedDate.getTime());
                    // Format date for display
                    String dateForDisplay = dateFormatDisplay.format(selectedDate.getTime());

                    if (isCheckIn) {
                        selectedCheckInDateStr = dateForStorage; // Store for calculation/update
                        if (checkInDateEditText != null) {
                            checkInDateEditText.setText(dateForDisplay); // Display formatted date
                        }
                    } else {
                        selectedCheckOutDateStr = dateForStorage; // Store for calculation/update
                        if (checkOutDateEditText != null) {
                            checkOutDateEditText.setText(dateForDisplay); // Display formatted date
                        }
                    }

                    // Recalculate nights and update the TextViews display
                    recalculateNightsAndPrice();

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    // Method to recalculate nights and update the TextViews display
    private void recalculateNightsAndPrice() {
        long numberOfNights = 0;
        long totalPrice = 0;
        int pricePerNight = 0;

        // Get the current room style from the EditText for recalculation
        String currentStyle = styleEditText.getText().toString().trim();

        // Determine price per night based on the current style
        switch (currentStyle) {
            case "Deluxe":
                pricePerNight = 6000;
                break;
            case "Premium":
                pricePerNight = 10500;
                break;
            case "Executive":
                pricePerNight = 15000;
                break;
            default:
                // Handle invalid style - maybe show a warning or set price to 0
                pricePerNight = 0;
                Log.w(TAG, "Invalid room style entered: " + currentStyle);
                break;
        }


        if (!selectedCheckInDateStr.isEmpty() && !selectedCheckOutDateStr.isEmpty()) {
            try {
                Date checkInDate = dateFormatStorage.parse(selectedCheckInDateStr);
                Date checkOutDate = dateFormatStorage.parse(selectedCheckOutDateStr);

                // Ensure check-out is after check-in
                if (checkOutDate.after(checkInDate)) {
                    long diffInMillis = checkOutDate.getTime() - checkInDate.getTime();
                    numberOfNights = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

                    // Calculate total price based on recalculated nights and current style price
                    totalPrice = pricePerNight * numberOfNights;

                    // Update the nightsTextView display
                    if (nightsTextView != null) {
                        nightsTextView.setText("Total Number of Nights: " + String.valueOf(numberOfNights));
                    }

                    // Update the totalPriceTextView display
                    if (totalPriceTextView != null) {
                        totalPriceTextView.setText("Total Price: Rs." + String.valueOf(totalPrice));
                    }


                } else {
                    // If dates are invalid, update TextViews or show error
                    if (nightsTextView != null) {
                        nightsTextView.setText("Total Number of Nights: Invalid Dates");
                    }
                    if (totalPriceTextView != null) {
                        totalPriceTextView.setText("Total Price: Invalid Dates");
                    }
                    Toast.makeText(this, "Check-out date must be after check-in date.", Toast.LENGTH_SHORT).show();
                }

            } catch (ParseException e) {
                Log.e(TAG, "Error recalculating nights and price", e);
                if (nightsTextView != null) {
                    nightsTextView.setText("Total Number of Nights: Error");
                }
                if (totalPriceTextView != null) {
                    totalPriceTextView.setText("Total Price: Error");
                }
                Toast.makeText(this, "Error calculating nights and price.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // If dates are not both selected, update TextViews
            if (nightsTextView != null) {
                nightsTextView.setText("Total Number of Nights: Select Dates");
            }
            if (totalPriceTextView != null) {
                totalPriceTextView.setText("Total Price: Select Dates");
            }
        }
    }


    // Update the booking data in Firestore
    private void updateBooking() {
        String newSurname = surnameEditText.getText().toString().trim();
        String newAgeStr = ageEditText.getText().toString().trim();
        String newStyle = styleEditText.getText().toString().trim(); // Get style from EditText

        // Use the stored selected dates
        String newCheckInDateStr = selectedCheckInDateStr;
        String newCheckOutDateStr = selectedCheckOutDateStr;

        if (newSurname.isEmpty() || newAgeStr.isEmpty() || newStyle.isEmpty()
                || newCheckInDateStr.isEmpty() || newCheckOutDateStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields and select dates", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse the age
        Long newAge;
        try {
            newAge = Long.parseLong(newAgeStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid age", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newAge < 18) {
            Toast.makeText(this, "You must be 18+ to book", Toast.LENGTH_SHORT).show();
            return;
        }

        // Recalculate nights and total price based on the potentially new dates and style
        long numberOfNights = 0;
        long totalPrice = 0;
        int pricePerNight = 0;

        // Determine price per night based on the selected style
        switch (newStyle) {
            case "Deluxe":
                pricePerNight = 6000;
                break;
            case "Premium":
                pricePerNight = 10500;
                break;
            case "Executive":
                pricePerNight = 15000;
                break;
            default:
                Toast.makeText(this, "Invalid room style", Toast.LENGTH_SHORT).show();
                return; // Stop update if style is invalid
        }

        try {
            Date checkInDate = dateFormatStorage.parse(newCheckInDateStr);
            Date checkOutDate = dateFormatStorage.parse(newCheckOutDateStr);

            // Ensure check-out is after check-in
            if (checkOutDate.after(checkInDate)) {
                long diffInMillis = checkOutDate.getTime() - checkInDate.getTime();
                numberOfNights = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

                // Calculate total price
                totalPrice = pricePerNight * numberOfNights;

            } else {
                Toast.makeText(this, "Check-out date must be after check-in date.", Toast.LENGTH_SHORT).show();
                return; // Stop update if dates are invalid
            }

        } catch (ParseException e) {
            Log.e(TAG, "Error parsing dates for update", e);
            Toast.makeText(this, "Error processing dates.", Toast.LENGTH_SHORT).show();
            return; // Stop update if date parsing fails
        }

        // Prepare data for update
        Map<String, Object> updates = new HashMap<>();
        updates.put("last", newSurname);
        updates.put("born", newAge);
        updates.put("checkInDate", newCheckInDateStr); // Update check-in date
        updates.put("checkOutDate", newCheckOutDateStr); // Update check-out date
        updates.put("numberOfNights", numberOfNights); // Update calculated nights
        updates.put("totalPrice", totalPrice); // Update calculated total price
        updates.put("room style", newStyle); // Update room style

        // Update the user's booking document in Firestore
        db.collection("users").document(documentIdToUpdate)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Booking updated successfully", Toast.LENGTH_SHORT).show();
                    // Optionally, re-fetch data to update the displayed fields
                    // fetchBookingForCurrentUser();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update booking", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Update failed", e);
                });
    }

    // Clear all input fields and reset date strings
    private void clearInputFields() {
        surnameEditText.setText("");
        ageEditText.setText("");
        styleEditText.setText(""); // Clear style EditText
        checkInDateEditText.setText("");
        checkOutDateEditText.setText("");
        nightsTextView.setText("Total Number of Nights:"); // Clear Nights TextView
        totalPriceTextView.setText("Total Price: Rs."); // Clear Total Price TextView
        selectedCheckInDateStr = "";
        selectedCheckOutDateStr = "";
    }
}
