package com.example.hamstercare;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.content.Context.MODE_PRIVATE;

public class MainFragment extends Fragment {

    View view;
    private SharedPreferences mPreferences;
    private String sharedPrefFile = "com.example.android.mainsharedprefs";
    public static final String NAME_KEY = "NAME_KEY";
    public static final String IMAGE_KEY = "IMAGE_KEY";
    String name;
    Bitmap imageBitmap;
    TextView hamsterName;
    ImageView hamsterDpMain;

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

        // Get a reference to the sharedPreferences object
        mPreferences = getContext().getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        // Retrieve the value using the key, and set a default when there is none
        String defaultValue = getResources().getString(R.string.default_hamster_name);

        // Display hamster name and profile picture
        name = mPreferences.getString(NAME_KEY, defaultValue);
        hamsterName.setText(name);
        imageBitmap = decodeBase64(mPreferences.getString(IMAGE_KEY, defaultValue));
        hamsterDpMain.setImageBitmap(imageBitmap);

        return view;
    }

    // method for base64 to bitmap
    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }

}
