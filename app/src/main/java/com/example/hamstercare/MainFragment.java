package com.example.hamstercare;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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

import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

public class MainFragment extends Fragment {

    View view;

    String name;
    Bitmap imageBitmap;
    TextView hamsterName;
    ImageView hamsterDpMain;
    TextView mainDescText;

    Switch autoTopUpSwitch;

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

    //In MainActivity, create a constant for the notification channel ID.
    // Every notification channel must be associated with an ID that is unique within your package.
    // You use this channel ID later, to post your notifications.
    private static final String PRIMARY_FOOD_CHANNEL_ID = "primary_food_notification_channel";
    private static final String PRIMARY_WATER_CHANNEL_ID = "primary_water_notification_channel";
    //The Android system uses the NotificationManager class to deliver notifications to the user.
    // In MainActivity.java, create a member variable to store the NotificationManager object.
    private NotificationManager mFoodNotifyManager;
    private NotificationManager mWaterNotifyManager;
    //You need to associate the notification with a notification ID so that your code can update
    // or cancel the notification in the future. In MainActivity.java, create a constant for the notification ID:
    private static final int FOOD_NOTIFICATION_ID = 0;
    private static final int WATER_NOTIFICATION_ID = 1;


    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main, container, false);

        // Create Notification Channels needed
        createNotificationChannel(PRIMARY_FOOD_CHANNEL_ID);
        createNotificationChannel(PRIMARY_WATER_CHANNEL_ID);

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
                if (foodValue.equals("full")) {
                    foodAmtText.setText("Full");
                    foodAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccept));
                    foodPicture.setImageResource(R.drawable.ic_food_full);
                    foodViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorUIPrimary));
                    foodCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorUIPrimaryLight));
                    topUpFoodButton.setBackground(getResources().getDrawable(R.drawable.custom_button_blue));
                    //cancel notification
                    mFoodNotifyManager.cancel(FOOD_NOTIFICATION_ID);
                } else if (foodValue.equals("mid")) {
                    foodAmtText.setText("Mid");
                    foodAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccept));
                    foodPicture.setImageResource(R.drawable.ic_food_mid);
                    foodViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorUIPrimary));
                    foodCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorUIPrimaryLight));
                    topUpFoodButton.setBackground(getResources().getDrawable(R.drawable.custom_button_blue));
                    mFoodNotifyManager.cancel(FOOD_NOTIFICATION_ID);
                } else if (foodValue.equals("low")) {
                    foodAmtText.setText("Low");
                    foodAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorReject));
                    foodPicture.setImageResource(R.drawable.ic_food_low);
                    foodViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorReject));
                    foodCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorRejectLight));
                    topUpFoodButton.setBackground(getResources().getDrawable(R.drawable.custom_button_red));
                    sendNotification(FOOD_NOTIFICATION_ID);
                } else if (foodValue.equals("empty")) {
                    foodAmtText.setText("Empty");
                    foodAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorReject));
                    foodPicture.setImageResource(R.drawable.ic_food_empty);
                    foodViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorReject));
                    foodCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorRejectLight));
                    topUpFoodButton.setBackground(getResources().getDrawable(R.drawable.custom_button_red));
                    sendNotification(FOOD_NOTIFICATION_ID);
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
                topUpFood.setValue("true");
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
                if (waterValue.equals("full")) {
                    waterAmtText.setText("Full");
                    waterAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccept));
                    waterPicture.setImageResource(R.drawable.ic_water_full);
                    waterViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorUIPrimary));
                    waterCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorUIPrimaryLight));
                    topUpWaterButton.setBackground(getResources().getDrawable(R.drawable.custom_button_blue));
                    //cancel notification
                    mWaterNotifyManager.cancel(WATER_NOTIFICATION_ID);
                } else if (waterValue.equals("mid")) {
                    waterAmtText.setText("Mid");
                    waterAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccept));
                    waterPicture.setImageResource(R.drawable.ic_water_mid);
                    waterViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorUIPrimary));
                    waterCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorUIPrimaryLight));
                    topUpWaterButton.setBackground(getResources().getDrawable(R.drawable.custom_button_blue));
                    mWaterNotifyManager.cancel(WATER_NOTIFICATION_ID);
                } else if (waterValue.equals("low")) {
                    waterAmtText.setText("Low");
                    waterAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorReject));
                    waterPicture.setImageResource(R.drawable.ic_water_low);
                    waterViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorReject));
                    waterCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorRejectLight));
                    topUpWaterButton.setBackground(getResources().getDrawable(R.drawable.custom_button_red));
                    sendNotification(WATER_NOTIFICATION_ID);
                } else if (waterValue.equals("empty")) {
                    waterAmtText.setText("Empty");
                    waterAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorReject));
                    waterPicture.setImageResource(R.drawable.ic_water_empty);
                    waterViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorReject));
                    waterCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorRejectLight));
                    topUpWaterButton.setBackground(getResources().getDrawable(R.drawable.custom_button_red));
                    sendNotification(WATER_NOTIFICATION_ID);
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
                topUpWater.setValue("true");
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

    //Create a method stub for the sendNotification() method:
    private void sendNotification(int NOTIFICATION_ID) {
        if (NOTIFICATION_ID == WATER_NOTIFICATION_ID) {
            //NotificationCompat.Builder notifyBuilder = getWaterNotificationBuilder();
            NotificationCompat.Builder notifyBuilder = getNotificationBuilder(WATER_NOTIFICATION_ID);
            mWaterNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());
        } else if (NOTIFICATION_ID == FOOD_NOTIFICATION_ID) {
            //NotificationCompat.Builder notifyBuilder = getFoodNotificationBuilder();
            NotificationCompat.Builder notifyBuilder = getNotificationBuilder(FOOD_NOTIFICATION_ID);
            mFoodNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());
        }
    }

    private void createNotificationChannel(String PRIMARY_CHANNEL_ID)
    {
        if (PRIMARY_CHANNEL_ID.equals(PRIMARY_WATER_CHANNEL_ID)) {
            mWaterNotifyManager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
        } else {
            mFoodNotifyManager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Create a NotificationChannel
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                    "Mascot Notification", NotificationManager
                    .IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notification from Mascot");
            if (PRIMARY_CHANNEL_ID.equals(PRIMARY_WATER_CHANNEL_ID)) {
                mWaterNotifyManager.createNotificationChannel(notificationChannel);
            } else {
                mFoodNotifyManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    private NotificationCompat.Builder getNotificationBuilder(int NOTIFICATION_ID){
        Intent notificationIntent = new Intent(getActivity(), MainActivity.class);

        PendingIntent notificationPendingIntent = PendingIntent.getActivity(getContext(),
                NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String primaryChannelId = PRIMARY_FOOD_CHANNEL_ID;
        String contentTitle =  "Food is Low";
        String contentText = "Food is low, please top up!";

        if (NOTIFICATION_ID==WATER_NOTIFICATION_ID) {
            primaryChannelId = PRIMARY_WATER_CHANNEL_ID;
            contentTitle =  "Water is Low";
            contentText = "Water is low, please top up!";
        }
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(getContext(), primaryChannelId)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_logo_icon)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        return notifyBuilder;
    }

}
