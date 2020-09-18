package com.example.music.ui.library.fragmentTabs.Album;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.music.Model.AlbumModel;
import com.example.music.Model.ArtistModel;
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

public class FavouriteAlbums {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private StorageUtil storage;

    private DatabaseReference userReference;
    private DatabaseReference albumDatabaseRef;
    private ValueEventListener dbListener1;
    private ValueEventListener dbListener2;

    private static FavouriteAlbums instance;

    public static FavouriteAlbums getInstance(Context context) {
        if (instance == null) {
            instance = new FavouriteAlbums(context);
        }
        return instance;
    }

    public static void setInstance(){
        instance = null;
    }

    public FavouriteAlbums(Context context) {
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        albumDatabaseRef = FirebaseDatabase.getInstance().getReference("Albums");

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        storage = new StorageUtil(context);

        updateFavouriteAlbumsList();
    }

    private void updateFavouriteAlbumsList() {
        if (currentUser != null) {
            dbListener1 = userReference.orderByChild("userId").equalTo(currentUser.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                UserModel user = postSnapshot.getValue(UserModel.class);
                                ArrayList<AlbumModel> albums = user.getFavouriteAlbums();
                                if (albums == null) {
                                    albums = new ArrayList<>();
                                }
                                storage.storeFavouriteAlbums(albums);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    public boolean isFavourite(AlbumModel album) {
        ArrayList<AlbumModel> favouriteList = storage.loadFavouriteAlbums();
        for (AlbumModel currentAlbum : favouriteList) {
            if (currentAlbum.getName().equals(album.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean favourite(AlbumModel album, Context context) {
        if (currentUser != null) {
            if (isFavourite(album)) {
                removeFavouriteAlbum(album);
                updateFavouriteAlbumsList();
            } else {
                addFavouriteAlbum(album);
                updateFavouriteAlbumsList();
                return true;
            }
        } else {
            Toast.makeText(context, "Please Login", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void addFavouriteAlbum(final AlbumModel album) {
        dbListener2 = userReference.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            UserModel user = postSnapshot.getValue(UserModel.class);
                            ArrayList<AlbumModel> albums = user.getFavouriteAlbums();
                            if (albums == null) {
                                albums = new ArrayList<>();
                            }
                            albums.add(album);
                            user.setFavouriteAlbums(albums);

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

    public void removeFavouriteAlbum(final AlbumModel album) {
        dbListener2 = userReference.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            UserModel user = postSnapshot.getValue(UserModel.class);
                            ArrayList<AlbumModel> albums = user.getFavouriteAlbums();
                            if (albums == null) {
                                albums = new ArrayList<>();
                            }
                            int position = -1;
                            for (int i = 0; i < albums.size(); i++) {
                                if (albums.get(i).getName().equals(album.getName())) {
                                    position = i;
                                    break;
                                }
                            }
                            albums.remove(position);

                            user.setFavouriteAlbums(albums);

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
