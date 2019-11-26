package com.example.hamstercare;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.content.Context.MODE_PRIVATE;

public class PhotoFragment extends Fragment {

    View view;
    private SharedPreferences mPreferences;
    private String sharedPrefFile = "com.example.android.mainsharedprefs";
    public static final String NAME_KEY = "NAME_KEY";
    String name;
    TextView hamsterName2;
    FloatingActionButton fab;
    ProgressBar photoProgressBar;
    ImageView hamsterPhoto;


    public PhotoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_photo, container, false);

        // Find views needed
        hamsterName2 = view.findViewById(R.id.hamsterName2);
        fab = view.findViewById(R.id.fab);
        photoProgressBar = view.findViewById(R.id.photoProgressBar);
        hamsterPhoto = view.findViewById(R.id.hamsterPhoto);

        // Edit name
        // Get a reference to the sharedPreferences object
        mPreferences = getContext().getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        // Retrieve the value using the key, and set a default when there is none
        String defaultValue = getResources().getString(R.string.default_hamster_name);
        name = mPreferences.getString(NAME_KEY, defaultValue);
        hamsterName2.setText("Hello, " + name + "!");

        // Get Database References
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference takePic = database.getReference("takePic");
        final DatabaseReference picUrl = database.getReference("picUrl");

        // Before photo is loaded at start-up
        photoProgressBar.setVisibility(View.VISIBLE);

        // Update image every time photo is taken
        picUrl.addValueEventListener(new ImEvtListener());

        // Photo Tab
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePic.setValue("true");
                photoProgressBar.setVisibility(View.VISIBLE);
                hamsterPhoto.setVisibility(View.GONE);
            }
        });

        return view;
    }

    // create inner classes for event listeners
    // we can't use anonymous classes because we need to reference non-final vars
    private class ImEvtListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            String url = dataSnapshot.getValue(String.class);

            // Edit Name


            // Get Database References
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference httpsRef = storage.getReferenceFromUrl(url);

            final ProgressBar photoProgressBar = view.findViewById(R.id.photoProgressBar);
            final ImageView hamsterPhoto = view.findViewById(R.id.hamsterPhoto);

            GlideApp.with(getActivity())
                    .load(httpsRef)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            photoProgressBar.setVisibility(View.GONE);
                            hamsterPhoto.setVisibility(View.VISIBLE);
                            Toast.makeText(getActivity(), "Failed to load image. Please try again later.", Toast.LENGTH_LONG).show();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            photoProgressBar.setVisibility(View.GONE);
                            hamsterPhoto.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .into(hamsterPhoto);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e("mickey1356", "couldn't read database");
        }
    }
}
