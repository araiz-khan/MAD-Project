package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class CheckBookingActivity extends AppCompatActivity {

    EditText nameEditText;
    Button checkButton;
    TextView resultTextView;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_booking);

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText);
        checkButton = findViewById(R.id.checkButton);
        resultTextView = findViewById(R.id.resultTextView);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Check Booking");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredName = nameEditText.getText().toString().trim();

                if (enteredName.isEmpty()) {
                    Toast.makeText(CheckBookingActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Search in Firestore
                db.collection("users")
                        .whereEqualTo("first", enteredName)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (queryDocumentSnapshots.isEmpty()) {
                                    resultTextView.setText("No booking found for this name.");
                                } else {
                                    StringBuilder result = new StringBuilder();
                                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                        String firstName = document.getString("first");
                                        String lastName = document.getString("last");
                                        Long age = document.getLong("born");
                                        String nightsToStay = document.getString("nights to stay"); // Add this line to fetch nights to stay

                                        result.append("Name: ").append(firstName).append(" ").append(lastName)
                                                .append("\nAge: ").append(age)
                                                .append("\nNights to stay: ").append(nightsToStay)
                                                .append("\n\n");
                                    }
                                    resultTextView.setText(result.toString());
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Error getting documents", e);
                                Toast.makeText(CheckBookingActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                            }
                        });


            }

        });

    }
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // or use finish();
        return true;
    }

}
