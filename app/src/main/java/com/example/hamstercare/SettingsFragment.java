package com.example.hamstercare;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment {

    View view;
    private SharedPreferences mPreferences;
    private String sharedPrefFile = "com.example.android.mainsharedprefs";
    public static final String NAME_KEY = "NAME_KEY";
    public static final String IMAGE_KEY = "IMAGE_KEY";
    public static final int PICK_IMAGE_REQUEST = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    String name;
    FloatingActionButton chooseImage;
    FloatingActionButton saveChanges;
    EditText editName;
    ImageView hamsterDpSettings;
    Bitmap newImageBitmap;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Find views needed
        chooseImage = view.findViewById(R.id.chooseImage);
        saveChanges = view.findViewById(R.id.saveChanges);
        editName = view.findViewById(R.id.editName);
        hamsterDpSettings = view.findViewById(R.id.hamsterDpSettings);

        // Get a reference to the sharedPreferences object
        mPreferences = getContext().getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        // Retrieve and set up current name and profile picture, and set defaults when there are none
        String defaultValue = getResources().getString(R.string.default_hamster_name);
        String defaultImage = encodeTobase64(drawableToBitmap(getResources()
                .getDrawable(R.drawable.ic_hamster_svgrepo_com)));
        name = mPreferences.getString(NAME_KEY, defaultValue);
        editName.setText(name);
        Bitmap imageBitmap = decodeBase64(mPreferences.getString(IMAGE_KEY, defaultImage));
        hamsterDpSettings.setImageBitmap(imageBitmap);
        newImageBitmap = imageBitmap;

        // Choose image from 3 options - Default, Gallery, or Photo
        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] choices = {"Default", "Gallery", "Photo"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Choose image");
                builder.setItems(choices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // the user clicked on colors[which]
                        if (which == 0) {
                            // Select default image
                            hamsterDpSettings.setImageResource(R.drawable.ic_hamster_svgrepo_com);
                            newImageBitmap = drawableToBitmap(getResources().getDrawable(R.drawable.ic_hamster_svgrepo_com));
                        } else if (which == 1) {
                            // Select image from gallery
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent, PICK_IMAGE_REQUEST);
                        } else if (which == 2) {
                            // Take photo using camera
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                            }
                        }
                    }
                });
                builder.show();
            }
        });

        // Confirm changes button
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save text to sharedPreferences
                String newName = editName.getText().toString();
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString(NAME_KEY, newName);

                // Save image to sharedPreferences
                editor.putString(IMAGE_KEY, encodeTobase64(newImageBitmap));
                editor.apply();
                Toast.makeText(view.getContext(), "Settings saved!", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null ) {
            Bitmap thumbnail = data.getParcelableExtra("data");
            Uri fullPhotoUri = data.getData();
            // bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), fullPhotoUri);
            hamsterDpSettings.setImageURI(fullPhotoUri);
            newImageBitmap = ((BitmapDrawable)hamsterDpSettings.getDrawable()).getBitmap();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            hamsterDpSettings.setImageBitmap(imageBitmap);
            newImageBitmap = imageBitmap;
        }
    }

    // Method encode bitmap to base64
    public static String encodeTobase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }

    // Method to decode base64 to bitmap
    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
