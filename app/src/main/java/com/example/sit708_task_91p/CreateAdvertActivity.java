package com.example.sit708_task_91p;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.location.Location;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.android.libraries.places.api.Places;

public class CreateAdvertActivity extends Activity {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1000;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private RadioGroup radioPostType;
    private RadioButton radioLost, radioFound;
    private EditText editTextName, editTextPhone, editTextDescription, editTextDate, editTextLocation;
    private Button buttonSave, buttonGetCurrentLocation;
    private double Latitude;
    private double Longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advert);

        radioPostType = findViewById(R.id.radioGroupPostType);
        radioLost = findViewById(R.id.radioLost);
        radioFound = findViewById(R.id.radioFound);
        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextDate = findViewById(R.id.editTextDate);
        editTextLocation = findViewById(R.id.editTextLocation);
        buttonSave = findViewById(R.id.buttonSave);
        editTextLocation = findViewById(R.id.editTextLocation);
        buttonGetCurrentLocation = findViewById(R.id.buttonGetCurrentLocation);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyDb7TLex6gjHVmsOMZUYwzqQcUDMsgMY0Y");
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up the Autocomplete intent
        editTextLocation.setFocusable(false);
        editTextLocation.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(CreateAdvertActivity.this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });

        // GetCurrentLocation Button Click Listener
        buttonGetCurrentLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else {
                getLocation();
            }
        });

        buttonSave.setOnClickListener(v -> {
            String postType = radioLost.isChecked() ? "Lost" : "Found";
            String name = editTextName.getText().toString();
            String phone = editTextPhone.getText().toString();
            String description = editTextDescription.getText().toString();
            String date = editTextDate.getText().toString();
            String location = editTextLocation.getText().toString();

            // Check if all fields are filled
            if (name.isEmpty() || phone.isEmpty() || description.isEmpty() || date.isEmpty() || location.isEmpty()) {
                Toast.makeText(CreateAdvertActivity.this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new LostFoundItem object
            LostFoundItem item = new LostFoundItem(postType + " " + name, description, phone, date, location, Latitude, Longitude);

            // Save the item
            saveItem(item);

            // Clear input fields
            clearFields();

            // Notify the user
            Toast.makeText(CreateAdvertActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveItem(LostFoundItem item) {
        SharedPreferences sharedPreferences = getSharedPreferences("LostFoundPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Retrieve the existing items
        String itemsJson = sharedPreferences.getString("items", "[]");
        Type type = new TypeToken<ArrayList<LostFoundItem>>(){}.getType();
        ArrayList<LostFoundItem> itemList = new Gson().fromJson(itemsJson, type);

        // Add the new item
        itemList.add(item);

        // Save the updated list
        itemsJson = new Gson().toJson(itemList);
        editor.putString("items", itemsJson);
        editor.apply();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.d("Location", "Location: " + location.toString());
                    Latitude = location.getLatitude();
                    Longitude = location.getLongitude();
                    Geocoder geocoder = new Geocoder(CreateAdvertActivity.this, Locale.getDefault());
                    try {
                        Log.d("Location", "Attempting to get address...");
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            // display address
                            Address address = addresses.get(0);
                            double latitude = address.getLatitude();
                            double longitude = address.getLongitude();
                            Log.d("Location", "Address found: " + address.toString());
                            String addressFragments = address.getMaxAddressLineIndex() >= 0 ? address.getAddressLine(0) : "";
                            Log.d("Location", "Address to be set: " + addressFragments);
                            runOnUiThread(() -> {
                                editTextLocation.setText(addressFragments);
                            });
                            Log.d("Location", "Address set to editTextLocation:" + editTextLocation.getText().toString());
                        } else {
                            Toast.makeText(CreateAdvertActivity.this, "No address found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException ioException) {
                        // internet issue
                        Toast.makeText(CreateAdvertActivity.this, "Service not available", Toast.LENGTH_SHORT).show();
                    } catch (IllegalArgumentException illegalArgumentException) {
                        // invalid Latitude and Longitude
                        Toast.makeText(CreateAdvertActivity.this, "Invalid lat long used", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CreateAdvertActivity.this, "Location not detected", Toast.LENGTH_SHORT).show();
                    Log.d("Location", "Location not detected");
                }
            }
        });
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to clear input fields after saving an item
    private void clearFields() {
        editTextName.setText("");
        editTextPhone.setText("");
        editTextDescription.setText("");
        editTextDate.setText("");
        editTextLocation.setText("");
        radioPostType.clearCheck();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                editTextLocation.setText(place.getAddress()); // Set the address to the editText
                if (place.getLatLng() != null) {
                    Latitude = place.getLatLng().latitude;
                    Longitude = place.getLatLng().longitude;
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                Toast.makeText(this, "Address selection canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}