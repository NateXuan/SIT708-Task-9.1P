package com.example.sit708_task_91p;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ItemDetailsActivity extends Activity {

    private TextView textViewDetailName;
    private TextView textViewDetailPhone;
    private TextView textViewDetailDescription;
    private TextView textViewDetailDate;
    private TextView textViewDetailLocation;
    private Button buttonRemove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        textViewDetailName = findViewById(R.id.textViewDetailName);
        textViewDetailPhone = findViewById(R.id.textViewDetailPhone);
        textViewDetailDescription = findViewById(R.id.textViewDetailDescription);
        textViewDetailDate = findViewById(R.id.textViewDetailDate);
        textViewDetailLocation = findViewById(R.id.textViewDetailLocation);
        buttonRemove = findViewById(R.id.buttonRemove);

        // Get the intent and the items
        Intent intent = getIntent();
        String title = intent.getStringExtra("ITEM_TITLE");
        String description = intent.getStringExtra("ITEM_DESCRIPTION");
        String phone = intent.getStringExtra("ITEM_PHONE");
        String date = intent.getStringExtra("ITEM_DATE");
        String location = intent.getStringExtra("ITEM_LOCATION");

        // Set the text views
        textViewDetailName.setText(title);
        textViewDetailPhone.setText("Contactor: " + phone);
        textViewDetailDescription.setText("Description: " + description);
        textViewDetailDate.setText("Date: " + date);
        textViewDetailLocation.setText("At " +location);

        int position = intent.getIntExtra("ITEM_POSITION", -1);
        buttonRemove.setOnClickListener(v -> {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("ITEM_REMOVED", true);
            returnIntent.putExtra("ITEM_POSITION", position);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        });

    }
}
