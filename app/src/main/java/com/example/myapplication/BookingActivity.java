package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BookingActivity extends AppCompatActivity {

    // Declare FirebaseFirestore instance
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Declare EditText fields for first name, last name, and age
    EditText firstNameEditText, lastNameEditText, ageEditText,nights;
    Button submitButton;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking); // Link to the activity_booking.xml layout file

        // Initialize the views
        firstNameEditText = findViewById(R.id.firstName);
        lastNameEditText = findViewById(R.id.lastName);
        ageEditText = findViewById(R.id.age);
        submitButton = findViewById(R.id.submitBtn);
        nights=findViewById(R.id.nights);
        // Set onClickListener for the submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get data from the EditText fields
                String firstName = firstNameEditText.getText().toString();
                String lastName = lastNameEditText.getText().toString();
                String ageStr = ageEditText.getText().toString();
                String night= nights.getText().toString();
                // Validate the input data
                if (firstName.isEmpty() || lastName.isEmpty() || ageStr.isEmpty()) {
                    // If any field is empty, show a toast message
                    Toast.makeText(BookingActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        // Parse the age to an integer
                        int age = Integer.parseInt(ageStr);

                        // Display the collected data in a Toast
                        String userData = "Name: " + firstName + " " + lastName + "\nAge: " + age +"  nights:"+night;
                        Toast.makeText(BookingActivity.this, userData, Toast.LENGTH_LONG).show();

                        // Now save the data to Firestore
                        Map<String, Object> user = new HashMap<>();
                        user.put("first", firstName);
                        user.put("last", lastName);
                        user.put("born", age);
                        user.put("nights to stay", night);
                                // Add data to Firestore
                        db.collection("users")
                                .add(user)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                        Toast.makeText(BookingActivity.this, "Data saved successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error adding document", e);
                                        Toast.makeText(BookingActivity.this, "Error saving data", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } catch (NumberFormatException e) {
                        // If age is not a valid number, show an error message
                        Toast.makeText(BookingActivity.this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
