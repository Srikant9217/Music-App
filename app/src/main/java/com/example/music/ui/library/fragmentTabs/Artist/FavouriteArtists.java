package com.example.music.ui.library.fragmentTabs.Artist;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.music.Model.ArtistModel;
import com.example.music.Model.SongModel;
import com.example.music.Model.UserModel;
import com.example.music.StorageUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavouriteArtists {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private StorageUtil storage;

    private DatabaseReference userReference;
    private DatabaseReference artistDatabaseRef;
    private ValueEventListener dbListener1;
    private ValueEventListener dbListener2;

    private static FavouriteArtists instance;

    public static FavouriteArtists getInstance(Context context) {
        if (instance == null) {
            instance = new FavouriteArtists(context);
        }
        return instance;
    }

    public static void setInstance(){
        instance = null;
    }

    public FavouriteArtists(Context context) {
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        artistDatabaseRef = FirebaseDatabase.getInstance().getReference("Artists");

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        storage = new StorageUtil(context);

        updateFavouriteArtistsList();
    }

    private void updateFavouriteArtistsList() {
        if (currentUser != null) {
            dbListener1 = userReference.orderByChild("userId").equalTo(currentUser.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                UserModel user = postSnapshot.getValue(UserModel.class);
                                ArrayList<ArtistModel> artists = user.getFavouriteArtists();
                                if (artists == null) {
                                    artists = new ArrayList<>();
                                }
                                storage.storeFavouriteArtists(artists);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    public boolean isFavourite(ArtistModel artist) {
        ArrayList<ArtistModel> favouriteList = storage.loadFavouriteArtists();
        for (ArtistModel currentArtist : favouriteList) {
            if (currentArtist.getName().equals(artist.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean favourite(ArtistModel artist, Context context) {
        if (currentUser != null) {
            if (isFavourite(artist)) {
                removeFavouriteArtist(artist);
                updateFavouriteArtistsList();
            } else {
                addFavouriteArtist(artist);
                updateFavouriteArtistsList();
                return true;
            }
        } else {
            Toast.makeText(context, "Please Login", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void addFavouriteArtist(final ArtistModel artist) {
        dbListener2 = userReference.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            UserModel user = postSnapshot.getValue(UserModel.class);
                            ArrayList<ArtistModel> artists = user.getFavouriteArtists();
                            if (artists == null) {
                                artists = new ArrayList<>();
                            }
                            artists.add(artist);
                            user.setFavouriteArtists(artists);

                            String uploadId = postSnapshot.getKey();
                            userReference.child(uploadId).setValue(user);
                            userReference.removeEventListener(dbListener2);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public void removeFavouriteArtist(final ArtistModel artist) {
        dbListener2 = userReference.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            UserModel user = postSnapshot.getValue(UserModel.class);
                            ArrayList<ArtistModel> artists = user.getFavouriteArtists();
                            if (artists == null) {
                                artists = new ArrayList<>();
                            }
                            int position = -1;
                            for (int i = 0; i < artists.size(); i++) {
                                if (artists.get(i).getName().equals(artist.getName())) {
                                    position = i;
                                    break;
                                }
                            }
                            artists.remove(position);

                            user.setFavouriteArtists(artists);

                            String uploadId = postSnapshot.getKey();
                            userReference.child(uploadId).setValue(user);
                            userReference.removeEventListener(dbListener2);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
