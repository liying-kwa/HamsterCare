package com.example.hamstercare;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.MODE_PRIVATE;

public class MainFragment extends Fragment {

    View view;

    String name;
    Bitmap imageBitmap;
    String foodLevel;
    String waterLevel;

    Switch autoTopUpSwitch;

    TextView hamsterName;
    ImageView hamsterDpMain;
    TextView mainDescText;
    TextView foodAmtText;
    TextView waterAmtText;
    ImageView foodPicture;
    ImageView waterPicture;
    View foodViewBanner;
    View waterViewBanner;
    CardView foodCardBackground;
    CardView waterCardBackground;
    Button topUpFoodButton;
    Button topUpWaterButton;
    TextView prevFoodTopUpDateTimeText;
    TextView prevWaterTopUpDateTimeText;

    private SharedPreferences mPreferences;
    private String sharedPrefFile = "com.example.android.mainsharedprefs";
    public static final String NAME_KEY = "NAME_KEY";
    public static final String IMAGE_KEY = "IMAGE_KEY";
    public static final String AUTOTOPUP_KEY = "AUTOTOPUP_KEY";


    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main, container, false);

        // Get views needed
        hamsterName = view.findViewById(R.id.hamsterName);
        hamsterDpMain = view.findViewById(R.id.hamsterDpMain);
        mainDescText = view.findViewById(R.id.mainDescText);
        autoTopUpSwitch = view.findViewById(R.id.autoTopUpSwitch);
        foodAmtText = view.findViewById(R.id.foodAmtText);
        waterAmtText = view.findViewById(R.id.waterAmtText);
        foodPicture = view.findViewById(R.id.foodPicture);
        waterPicture = view.findViewById(R.id.waterPicture);
        foodViewBanner = view.findViewById(R.id.foodViewBanner);
        waterViewBanner = view.findViewById(R.id.waterViewBanner);
        foodCardBackground = view.findViewById(R.id.foodCardBackground);
        waterCardBackground = view.findViewById(R.id.waterCardBackground);
        topUpFoodButton = view.findViewById(R.id.topUpFoodButton);
        topUpWaterButton = view.findViewById(R.id.topUpWaterButton);
        prevFoodTopUpDateTimeText = view.findViewById(R.id.prevFoodTopUpDateTimeText);
        prevWaterTopUpDateTimeText = view.findViewById(R.id.prevWaterTopUpDateTimeText);

        // Get Firebase Realtime Database References
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference autoTopUp = database.getReference("autoTopUpSwitch");
        final DatabaseReference foodAmt = database.getReference("foodAmt");
        final DatabaseReference waterAmt = database.getReference("waterAmt");
        final DatabaseReference topUpFood = database.getReference("topUpFood");
        final DatabaseReference topUpWater = database.getReference("topUpWater");
        final DatabaseReference prevFoodTopUpDateTime = database.getReference("prevFoodTopUpDateTime");
        final DatabaseReference prevWaterTopUpDateTime = database.getReference("prevWaterTopUpDateTime");

        // Get a reference to the sharedPreferences object
        mPreferences = getContext().getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        // Retrieve the value using the key, and set a default when there is none
        String defaultValue = getResources().getString(R.string.default_hamster_name);

        // Display hamster name and profile picture
        name = mPreferences.getString(NAME_KEY, defaultValue);
        hamsterName.setText("Hello, " + name + "!");
        mainDescText.setText("Check " + name + "'s food and water levels.");

        String imageName = mPreferences.getString(IMAGE_KEY, defaultValue);

        if (imageName.equals(defaultValue)) {
            hamsterDpMain.setImageResource(R.drawable.ic_hamster_svgrepo_com);
        } else {
            imageBitmap = decodeBase64(mPreferences.getString(IMAGE_KEY, defaultValue));
            hamsterDpMain.setImageBitmap(imageBitmap);
        }

        // Set up auto-top up switch previous value and checked/unchecked listener
        boolean checkedStatus = mPreferences.getBoolean(AUTOTOPUP_KEY, true);
        autoTopUpSwitch.setChecked(checkedStatus);
        autoTopUpSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (autoTopUpSwitch.isChecked()) {
                    autoTopUp.setValue("true");
                } else {
                    autoTopUp.setValue("false");
                }
            }
        });

        // Check food value and set image and text accordingly
        foodAmt.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String foodValue = dataSnapshot.getValue(String.class);
                foodLevel = foodValue;
                if (foodValue.equals("full")) {
                    foodAmtText.setText("Full");
                    foodAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccept));
                    foodPicture.setImageResource(R.drawable.ic_food_full);
                    foodViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorFULL));
                    foodCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorFULLlight));
                    topUpFoodButton.setBackground(getResources().getDrawable(R.drawable.custom_button_grey));
                } else if (foodValue.equals("mid")) {
                    foodAmtText.setText("Mid");
                    foodAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccept));
                    foodPicture.setImageResource(R.drawable.ic_food_mid);
                    foodViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorUIPrimary));
                    foodCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorUIPrimaryLight));
                    topUpFoodButton.setBackground(getResources().getDrawable(R.drawable.custom_button_blue));
                } else if (foodValue.equals("low")) {
                    foodAmtText.setText("Low");
                    foodAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorReject));
                    foodPicture.setImageResource(R.drawable.ic_food_low);
                    foodViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorReject));
                    foodCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorRejectLight));
                    topUpFoodButton.setBackground(getResources().getDrawable(R.drawable.custom_button_red));
                } else if (foodValue.equals("empty")) {
                    foodAmtText.setText("Empty");
                    foodAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorReject));
                    foodPicture.setImageResource(R.drawable.ic_food_empty);
                    foodViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorReject));
                    foodCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorRejectLight));
                    topUpFoodButton.setBackground(getResources().getDrawable(R.drawable.custom_button_red));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("jinghan", "Failed to read value.", error.toException());
            }
        });

        // Set listener for Top-Up Food button
        topUpFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (foodLevel.equals("full")) {
                    Toast.makeText(getActivity(), "Food is full!", Toast.LENGTH_LONG).show();
                } else {
                    topUpFood.setValue("true");
                    Toast.makeText(getActivity(), "Food is added!", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Set listener to update date and time for last top-up of food
        prevFoodTopUpDateTime.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String dateTime = dataSnapshot.getValue(String.class);
                prevFoodTopUpDateTimeText.setText("Last Top-Up: " + dateTime);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("jinghan", "Failed to read value.", error.toException());
            }
        });

        // Check water value and set image and text accordingly
        waterAmt.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String waterValue = dataSnapshot.getValue(String.class);
                waterLevel = waterValue;
                if (waterValue.equals("full")) {
                    waterAmtText.setText("Full");
                    waterAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccept));
                    waterPicture.setImageResource(R.drawable.ic_water_full);
                    waterViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorFULL));
                    waterCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorFULLlight));
                    topUpWaterButton.setBackground(getResources().getDrawable(R.drawable.custom_button_grey));
                } else if (waterValue.equals("mid")) {
                    waterAmtText.setText("Mid");
                    waterAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccept));
                    waterPicture.setImageResource(R.drawable.ic_water_mid);
                    waterViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorUIPrimary));
                    waterCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorUIPrimaryLight));
                    topUpWaterButton.setBackground(getResources().getDrawable(R.drawable.custom_button_blue));
                } else if (waterValue.equals("low")) {
                    waterAmtText.setText("Low");
                    waterAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorReject));
                    waterPicture.setImageResource(R.drawable.ic_water_low);
                    waterViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorReject));
                    waterCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorRejectLight));
                    topUpWaterButton.setBackground(getResources().getDrawable(R.drawable.custom_button_red));
                } else if (waterValue.equals("empty")) {
                    waterAmtText.setText("Empty");
                    waterAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorReject));
                    waterPicture.setImageResource(R.drawable.ic_water_empty);
                    waterViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorReject));
                    waterCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorRejectLight));
                    topUpWaterButton.setBackground(getResources().getDrawable(R.drawable.custom_button_red));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("jinghan", "Failed to read value.", error.toException());
            }
        });

        // Set listener for Top-Up Water button
        topUpWaterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (waterLevel.equals("full")) {
                    Toast.makeText(getActivity(), "Water is full!", Toast.LENGTH_LONG).show();
                } else {
                    topUpWater.setValue("true");
                    Toast.makeText(getActivity(), "Water is added!", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Set listener to update date and time for last top-up of water
        prevWaterTopUpDateTime.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String dateTime = dataSnapshot.getValue(String.class);
                prevWaterTopUpDateTimeText.setText("Last Top-Up: " + dateTime);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("jinghan", "Failed to read value.", error.toException());
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = mPreferences.edit();
        if (autoTopUpSwitch.isChecked()) {
            editor.putBoolean(AUTOTOPUP_KEY, true);
        } else {
            editor.putBoolean(AUTOTOPUP_KEY, false);
        }
        editor.apply();
    }

    // method for base64 to bitmap
    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }

}
