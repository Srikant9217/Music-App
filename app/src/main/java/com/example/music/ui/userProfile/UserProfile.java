package com.example.music.ui.userProfile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.music.BottomSheets.BottomSheetUserProfile;
import com.example.music.Dialogs.DialogEditProfileName;
import com.example.music.MainActivity;
import com.example.music.Model.UserModel;
import com.example.music.R;
import com.example.music.Storage.StorageUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfile extends Fragment implements DialogEditProfileName.DialogEditProfileNameListener,
        BottomSheetUserProfile.BottomSheetListener {

    private TextView textViewName;
    private ImageView editMenu;
    private TextView playlistCount;
    private TextView favouriteCount;
    private TextView followingCount;

    private UserModel user;

    private DatabaseReference databaseRef;
    private ValueEventListener dbListener;

    private StorageUtil storage;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_profile, container, false);

        editMenu = v.findViewById(R.id.edit_menu);
        textViewName = v.findViewById(R.id.text_view_name);
        playlistCount = v.findViewById(R.id.playlist_value);
        favouriteCount = v.findViewById(R.id.favourites_value);
        followingCount = v.findViewById(R.id.following_value);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        databaseRef = FirebaseDatabase.getInstance().getReference("Users");
        storage = new StorageUtil(getActivity());

        dbListener = databaseRef.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            user = dataSnapshot.getValue(UserModel.class);
                            textViewName.setText(user.getName());
                            if (user.getPlaylists() != null) {
                                playlistCount.setText(String.valueOf(user.getPlaylists().size()));
                            }
                            if (user.getFavouriteSongs() != null) {
                                favouriteCount.setText(String.valueOf(user.getFavouriteSongs().size()));
                            }
                            if (user.getFavouriteArtists() != null) {
                                followingCount.setText(String.valueOf(user.getFavouriteArtists().size()));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        editMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBottomMenu();
            }
        });

        return v;
    }

    private void openBottomMenu() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user);

        BottomSheetUserProfile bottomSheetUserProfile = new BottomSheetUserProfile();
        bottomSheetUserProfile.setBottomSheetListener(this);
        bottomSheetUserProfile.setArguments(bundle);
        bottomSheetUserProfile.show(getChildFragmentManager(), "BottomSheetUserProfile");
    }

    @Override
    public void editName(final String name) {
        if (name != null) {
            databaseRef.orderByChild("userId").equalTo(currentUser.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                UserModel user = dataSnapshot.getValue(UserModel.class);
                                user.setName(name);
                                textViewName.setText(name);
                                String uploadId = dataSnapshot.getKey();
                                databaseRef.child(uploadId).setValue(user);
                                databaseRef.removeEventListener(this);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onOptionClicked(int option, UserModel userModel) {
        switch (option) {
            case 0:
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", userModel);

                DialogEditProfileName dialog = new DialogEditProfileName();
                dialog.setListener(this);
                dialog.setArguments(bundle);
                dialog.show(getActivity().getSupportFragmentManager(), "Set Profile Name");
                break;
            case 1:
                firebaseAuth.signOut();
                MainActivity.signOut();
                storage.clearCachedUserStorage();
                getActivity().getSupportFragmentManager().popBackStack();
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (dbListener != null) {
            databaseRef.removeEventListener(dbListener);
        }
    }
}