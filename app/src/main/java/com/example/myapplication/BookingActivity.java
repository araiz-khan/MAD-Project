package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class BookingActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    EditText firstNameEditText, lastNameEditText, ageEditText, roomStyleEditText;
    TextView checkInText, checkOutText;
    Button submitButton, pickCheckInBtn, pickCheckOutBtn;

    // References to the parent LinearLayouts for handling clicks and selection state
    LinearLayout layoutDeluxe, layoutPremium, layoutExecutive;

    // ImageView references (can keep if needed for displaying images, but clicks are on LinearLayouts)
    ImageView imgDeluxe, imgPremium, imgExecutive;

    // Store dates as strings initially, will parse for calculation
    String checkInDateStr = "";
    String checkOutDateStr = "";
    String selectedRoomStyle = ""; // Variable to store the selected room style

    // Date format for parsing and displaying
    private SimpleDateFormat dateFormatDisplay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    // Using a consistent format for storing/parsing dates for calculation
    private SimpleDateFormat dateFormatStorage = new SimpleDateFormat("yyyy-MM-dd", Locale.US);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking); // Make sure this is the correct layout file

        // Initialize UI components
        firstNameEditText = findViewById(R.id.firstName);
        lastNameEditText = findViewById(R.id.lastName);
        ageEditText = findViewById(R.id.age);

        // Initialize the hidden EditText - add a check if it's null later
        roomStyleEditText = findViewById(R.id.selectedRoom);

        checkInText = findViewById(R.id.checkInText);
        checkOutText = findViewById(R.id.checkOutText);
        pickCheckInBtn = findViewById(R.id.pickCheckIn);
        pickCheckOutBtn = findViewById(R.id.pickCheckOut);
        submitButton = findViewById(R.id.submitBtn);

        // Initialize LinearLayouts for room selection
        layoutDeluxe = findViewById(R.id.layoutDeluxe);
        layoutPremium = findViewById(R.id.layoutPremium);
        layoutExecutive = findViewById(R.id.layoutExecutive);

        // Initialize ImageViews (if needed for displaying images)
        imgDeluxe = findViewById(R.id.imgDeluxe);
        imgPremium = findViewById(R.id.imgPremium);
        imgExecutive = findViewById(R.id.imgExecutive);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Book Now");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Set click listeners on the LinearLayouts for room selection
        if (layoutDeluxe != null) { // Add null checks for safety
            layoutDeluxe.setOnClickListener(v -> selectRoom("Deluxe", layoutDeluxe));
        }
        if (layoutPremium != null) {
            layoutPremium.setOnClickListener(v -> selectRoom("Premium", layoutPremium));
        }
        if (layoutExecutive != null) {
            layoutExecutive.setOnClickListener(v -> selectRoom("Executive", layoutExecutive));
        }


        if (pickCheckInBtn != null) {
            pickCheckInBtn.setOnClickListener(v -> pickDate(true));
        }
        if (pickCheckOutBtn != null) {
            pickCheckOutBtn.setOnClickListener(v -> pickDate(false));
        }
        if (submitButton != null) {
            submitButton.setOnClickListener(v -> submitData());
        }


    }

    // Method to handle room selection logic
    private void selectRoom(String room, LinearLayout clickedLayout) {
        // Store the selected room style in the variable
        selectedRoomStyle = room;

        // Attempt to update the hidden EditText, but with a null check
        if (roomStyleEditText != null) {
            roomStyleEditText.setText(room);
        } else {
            Log.w(TAG, "roomStyleEditText is null, cannot set text.");
            // This warning will appear in Logcat if findViewById(R.id.selectedRoom) failed
        }


        // Deselect all room layouts by setting their selected state to false
        if (layoutDeluxe != null) layoutDeluxe.setSelected(false);
        if (layoutPremium != null) layoutPremium.setSelected(false);
        if (layoutExecutive != null) layoutExecutive.setSelected(false);


        // Select the clicked layout by setting its selected state to true
        if (clickedLayout != null) {
            clickedLayout.setSelected(true);
        }


        // The background drawable (room_selector_bg) on the LinearLayout
        // will automatically update based on the selected state (true/false).
        // You do NOT need to manually set backgrounds or change drawables here.
    }


    // Method to show date picker dialog with validation
    private void pickDate(boolean isCheckIn) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, 0); // Set time to 00:00 for accurate date comparison
                    selectedCalendar.set(Calendar.MINUTE, 0);
                    selectedCalendar.set(Calendar.SECOND, 0);
                    selectedCalendar.set(Calendar.MILLISECOND, 0);

                    Calendar todayCalendar = Calendar.getInstance();
                    todayCalendar.set(Calendar.HOUR_OF_DAY, 0); // Set time to 00:00 for accurate date comparison
                    todayCalendar.set(Calendar.MINUTE, 0);
                    todayCalendar.set(Calendar.SECOND, 0);
                    todayCalendar.set(Calendar.MILLISECOND, 0);


                    Date selectedDate = selectedCalendar.getTime();
                    Date today = todayCalendar.getTime();


                    if (isCheckIn) {
                        // Validate Check-in Date (must be today or in the future)
                        if (selectedDate.before(today)) {
                            Toast.makeText(this, "Check-in date cannot be in the past.", Toast.LENGTH_SHORT).show();
                            return; // Do not update date if invalid
                        }

                        // If Check-out date is already selected, ensure check-in is not after check-out
                        if (!checkOutDateStr.isEmpty()) {
                            try {
                                Date checkOutDate = dateFormatStorage.parse(checkOutDateStr);
                                if (selectedDate.after(checkOutDate)) {
                                    Toast.makeText(this, "Check-in date cannot be after Check-out date.", Toast.LENGTH_SHORT).show();
                                    return; // Do not update date if invalid
                                }
                            } catch (ParseException e) {
                                Log.e(TAG, "Error parsing check-out date for validation", e);
                                // Continue even if parsing fails, as the primary validation is done
                            }
                        }

                        // If validation passes, update check-in date
                        checkInDateStr = dateFormatStorage.format(selectedDate); // Store for calculation
                        if (checkInText != null) {
                            checkInText.setText("Check-in: " + dateFormatDisplay.format(selectedDate)); // Display formatted date
                        }

                    } else { // isCheckOut
                        // Validate Check-out Date (must be after Check-in Date)
                        if (checkInDateStr.isEmpty()) {
                            Toast.makeText(this, "Please select Check-in date first.", Toast.LENGTH_SHORT).show();
                            return; // Do not update date if check-in is not selected
                        }

                        try {
                            Date checkInDate = dateFormatStorage.parse(checkInDateStr);

                            if (selectedDate.before(checkInDate) || selectedDate.equals(checkInDate)) {
                                Toast.makeText(this, "Check-out date must be after Check-in date.", Toast.LENGTH_SHORT).show();
                                return; // Do not update date if invalid
                            }

                        } catch (ParseException e) {
                            Log.e(TAG, "Error parsing check-in date for validation", e);
                            Toast.makeText(this, "Error validating dates. Please re-select Check-in.", Toast.LENGTH_SHORT).show();
                            return; // Do not update date if check-in parsing fails
                        }

                        // If validation passes, update check-out date
                        checkOutDateStr = dateFormatStorage.format(selectedDate); // Store for calculation
                        if (checkOutText != null) {
                            checkOutText.setText("Check-out: " + dateFormatDisplay.format(selectedDate)); // Display formatted date
                        }
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    // Method to handle submission logic
    private void submitData() {
        String firstName = firstNameEditText != null ? firstNameEditText.getText().toString().trim() : "";
        String lastName = lastNameEditText != null ? lastNameEditText.getText().toString().trim() : "";
        String ageStr = ageEditText != null ? ageEditText.getText().toString().trim() : "";

        // Use the selectedRoomStyle variable for validation and data
        String roomStyle = selectedRoomStyle;


        if (firstName.isEmpty() || lastName.isEmpty() || ageStr.isEmpty() || roomStyle.isEmpty()
                || checkInDateStr.isEmpty() || checkOutDateStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields and select a room and dates", Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid age input", Toast.LENGTH_SHORT).show();
            return;
        }

        if (age < 18) {
            Toast.makeText(this, "You must be 18+ to book", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate the number of nights
        long numberOfNights = 0; // Default to 0 nights
        try {
            Date checkInDate = dateFormatStorage.parse(checkInDateStr);
            Date checkOutDate = dateFormatStorage.parse(checkOutDateStr);

            // Re-validate dates here just in case, though pickDate should handle this
            if (checkOutDate.before(checkInDate) || checkOutDate.equals(checkInDate)) {
                Toast.makeText(this, "Check-out date must be after Check-in date.", Toast.LENGTH_SHORT).show();
                return; // Stop submission if dates are invalid
            }

            long diffInMillis = checkOutDate.getTime() - checkInDate.getTime();
            numberOfNights = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

        } catch (ParseException e) {
            Toast.makeText(this, "Error parsing selected dates.", Toast.LENGTH_SHORT).show();
            e.printStackTrace(); // Log the error
            return; // Stop submission if date parsing fails
        }


        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        // Room prices based on the selectedRoomStyle
        int pricePerNight;
        switch (roomStyle) {
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
                // This case should theoretically not be reached if roomStyle is empty checked above
                Toast.makeText(this, "Invalid room style selected", Toast.LENGTH_SHORT).show();
                return;
        }

        // Calculate total price: Price per night * Number of nights
        long totalPrice = pricePerNight * numberOfNights;

        // Create final copies of the variables to guarantee that they won't change.
        final String finalFirstName = firstName;
        final String finalLastName = lastName;
        final int finalAge = age;
        final String finalRoomStyle = roomStyle;
        final long finalNumberOfNights = numberOfNights;
        final long finalTotalPrice = totalPrice;
        final String finalUserId = userId;

        // Check if user already booked in the "users" collection
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Toast.makeText(this, "You can only book one room", Toast.LENGTH_SHORT).show();
                    } else {
                        // If user has not booked, proceed to show confirmation dialog
                        showConfirmationDialog(finalFirstName, finalLastName, finalAge, finalRoomStyle, finalNumberOfNights, finalTotalPrice, finalUserId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to check booking", e);
                    Toast.makeText(this, "Error checking booking status", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to show booking confirmation dialog
    private void showConfirmationDialog(String firstName, String lastName, int age, String roomStyle,
                                        long numberOfNights, long totalPrice, String userId) { // Added numberOfNights and changed totalPrice to long

        // Format dates for display in the dialog
        String checkInDisplay = "";
        String checkOutDisplay = "";
        try {
            checkInDisplay = dateFormatDisplay.format(dateFormatStorage.parse(checkInDateStr));
            checkOutDisplay = dateFormatDisplay.format(dateFormatStorage.parse(checkOutDateStr));
        } catch (ParseException e) {
            Log.e(TAG, "Error formatting dates for dialog", e);
            checkInDisplay = checkInDateStr; // Fallback to storage format if formatting fails
            checkOutDisplay = checkOutDateStr;
        }


        new AlertDialog.Builder(this)
                .setTitle("Confirm Booking")
                .setMessage("Name: " + firstName + " " + lastName + "\n"
                        + "Room: " + roomStyle + "\n"
                        + "Nights: " + numberOfNights + "\n" // Display number of nights
                        + "Total Price: Rs." + totalPrice + "\n\n"
                        + "Check-in: " + checkInDisplay + "\n" // Use formatted dates for dialog
                        + "Check-out: " + checkOutDisplay + "\n\n" // Use formatted dates for dialog
                        + "Confirm booking?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    Map<String, Object> booking = new HashMap<>();
                    booking.put("first", firstName);
                    booking.put("last", lastName);
                    booking.put("born", age);
                    booking.put("room style", roomStyle);
                    // Store dates in a consistent format for easy parsing later (e.g., in CheckBookingActivity)
                    booking.put("checkInDate", checkInDateStr);
                    booking.put("checkOutDate", checkOutDateStr);
                    booking.put("numberOfNights", numberOfNights); // Store number of nights
                    booking.put("totalPrice", totalPrice); // Store total price

                    db.collection("users").document(userId).set(booking)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Reservation Confirmed", Toast.LENGTH_SHORT).show();
                                // Optionally navigate to another activity or clear the form here
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error saving booking", e);
                                Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Handle back button press on the action bar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
