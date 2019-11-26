package com.example.hamstercare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ActionBar bottomNavigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Set up bottom navigation bar
        bottomNavigationBar = getSupportActionBar();
        BottomNavigationView navigation = findViewById(R.id.navigationView);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigationBar.setTitle("HamsterCare");
        loadFragment(new MainFragment());


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
                case R.id.help:
                    bottomNavigationBar.setTitle("Help");
                    loadFragment(new HelpFragment());
                    return true;
                case R.id.about:
                    bottomNavigationBar.setTitle("About Us");
                    loadFragment(new AboutFragment());
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




}
