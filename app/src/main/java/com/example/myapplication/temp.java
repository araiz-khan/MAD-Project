package com.example.myapplication;


import androidx.appcompat.app.AppCompatActivity;

public class BookingActivity extends AppCompatActivity {


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Declare EditText fields for first name, last name, and age
        EditText firstNameEditText, lastNameEditText, ageEditText, nights;
        Button submitButton;
        EditText roomStyleEditText;


        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_booking); // Link to the activity_booking.xml layout file


            roomStyleEditText = findViewById(R.id.selectedRoom);
            firstNameEditText = findViewById(R.id.firstName);
            lastNameEditText = findViewById(R.id.lastName);
            ageEditText = findViewById(R.id.age);
            submitButton = findViewById(R.id.submitBtn);
            nights = findViewById(R.id.nights);



            submitButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {

                    // Get data from the EditText fields
                    String firstName = firstNameEditText.getText().toString();
                    String lastName = lastNameEditText.getText().toString();
                    String ageStr = ageEditText.getText().toString();
                    String night = nights.getText().toString();
                    String roomStyle = roomStyleEditText.getText().toString();



                    Map<String, Object> user = new HashMap<>();
                    user.put("first", firstName);
                    user.put("last", lastName);
                    user.put("born", age);
                    user.put("nights to stay", night);
                    user.put("room style", roomStyle);

                    // Add data to Firestore
                    db.collection("users")
                            .add(user)


                }
            }
        });
    }


}






public class CheckBookingActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    EditText nameEditText;
    Button checkButton, deleteButton;
    TextView resultTextView;

    String documentIdToDelete = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_booking); // Link to activity_check_booking.xml

        // Initialize UI components
        nameEditText = findViewById(R.id.nameEditText);
        checkButton = findViewById(R.id.checkButton);
        deleteButton = findViewById(R.id.deleteButton);
        resultTextView = findViewById(R.id.resultTextView);

        // Set click listener for checking booking
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredName = nameEditText.getText().toString().trim();

                if (enteredName.isEmpty()) {
                    Toast.makeText(CheckBookingActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.collection("users")
                        .whereEqualTo("first", enteredName)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots.isEmpty()) {
                                resultTextView.setText("No booking found.");
                                documentIdToDelete = null;
                            } else {
                                StringBuilder result = new StringBuilder();
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    String first = doc.getString("first");
                                    String last = doc.getString("last");
                                    Object ageObj = doc.get("born");
                                    String nights = doc.getString("nights to stay");
                                    String style = doc.getString("room style");

                                    result.append("Name: ").append(first).append(" ").append(last)
                                            .append("\nAge: ").append(ageObj != null ? ageObj.toString() : "N/A")
                                            .append("\nNights to stay: ").append(nights)
                                            .append("\nRoom style: ").append(style)
                                            .append("\n\n");

                                    documentIdToDelete = doc.getId(); // Store for deletion
                                }
                                resultTextView.setText(result.toString());
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(CheckBookingActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        // Set click listener for deleting booking
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (documentIdToDelete != null) {
                    db.collection("users").document(documentIdToDelete)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(CheckBookingActivity.this, "Booking deleted", Toast.LENGTH_SHORT).show();
                                resultTextView.setText("");
                                documentIdToDelete = null;
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(CheckBookingActivity.this, "Failed to delete booking", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(CheckBookingActivity.this, "Please check booking first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}