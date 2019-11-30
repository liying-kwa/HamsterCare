package com.example.hamstercare;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    String foodLevel;
    String waterLevel;

    private ActionBar bottomNavigationBar;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create Notification Channels needed
        createNotificationChannel(PRIMARY_FOOD_CHANNEL_ID);
        createNotificationChannel(PRIMARY_WATER_CHANNEL_ID);

        // Set up bottom navigation bar
        bottomNavigationBar = getSupportActionBar();
        BottomNavigationView navigation = findViewById(R.id.navigationView);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigationBar.setTitle("HamsterCare");
        loadFragment(new MainFragment());

        // Get Firebase Realtime Database References
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference foodAmt = database.getReference("foodAmt");
        final DatabaseReference waterAmt = database.getReference("waterAmt");

        // Check food value and send notifications accordingly
        foodAmt.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String foodValue = dataSnapshot.getValue(String.class);
                foodLevel = foodValue;
                if (foodValue.equals("full")) {
                    mFoodNotifyManager.cancel(FOOD_NOTIFICATION_ID);
                } else if (foodValue.equals("mid")) {
                    mFoodNotifyManager.cancel(FOOD_NOTIFICATION_ID);
                } else if (foodValue.equals("low")) {
                    sendNotification(FOOD_NOTIFICATION_ID);
                } else if (foodValue.equals("empty")) {
                    sendNotification(FOOD_NOTIFICATION_ID);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("jinghan", "Failed to read value.", error.toException());
            }
        });

        // Check water value and send notifications accordingly
        waterAmt.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String waterValue = dataSnapshot.getValue(String.class);
                waterLevel = waterValue;
                if (waterValue.equals("full")) {
                    mWaterNotifyManager.cancel(WATER_NOTIFICATION_ID);
                } else if (waterValue.equals("mid")) {
                    mWaterNotifyManager.cancel(WATER_NOTIFICATION_ID);
                } else if (waterValue.equals("low")) {
                    sendNotification(WATER_NOTIFICATION_ID);
                } else if (waterValue.equals("empty")) {
                    sendNotification(WATER_NOTIFICATION_ID);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("jinghan", "Failed to read value.", error.toException());
            }
        });

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.feed:
                    bottomNavigationBar.setTitle("HamsterCare");
                    loadFragment(new MainFragment());
                    return true;
                case R.id.photo:
                    bottomNavigationBar.setTitle("Photo");
                    loadFragment(new PhotoFragment());
                    return true;
                case R.id.settings:
                    bottomNavigationBar.setTitle("Settings");
                    loadFragment(new SettingsFragment());
                    return true;
            }
            return false;
        }
    };

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.help) {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            mWaterNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        } else {
            mFoodNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
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
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this,
                NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String primaryChannelId = PRIMARY_FOOD_CHANNEL_ID;
        String contentTitle =  "Food is Low";
        String contentText = "Food is low, please top-up!";
        if (foodLevel.equals("empty")) {
            contentTitle = "Food is EMPTY";
            contentText = "Food is running out, please top-up!!";
        }

        if (NOTIFICATION_ID==WATER_NOTIFICATION_ID) {
            primaryChannelId = PRIMARY_WATER_CHANNEL_ID;
            contentTitle =  "Water is Low";
            contentText = "Water is low, please top-up!";
            if (waterLevel.equals("empty")) {
                contentTitle = "Water is EMPTY";
                contentText = "Water is running out, please top-up!!";
            }
        }
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, primaryChannelId)
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
