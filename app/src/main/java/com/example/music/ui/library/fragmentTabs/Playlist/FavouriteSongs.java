package com.example.music.ui.library.fragmentTabs.Playlist;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.music.MainActivity;
import com.example.music.Model.SongModel;
import com.example.music.Model.UserModel;
import com.example.music.Storage.StorageUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavouriteSongs {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private StorageUtil storage;

    private DatabaseReference databaseReference;
    private DatabaseReference songDatabaseRef;
    private ValueEventListener dbListener1;

    private static FavouriteSongs instance;
    private ArrayList<SongModel> favouriteList;

    public static FavouriteSongs getInstance(Context context) {
        if (instance == null) {
            instance = new FavouriteSongs(context);
        }
        return instance;
    }

    public static void setInstance() {
        instance = null;
    }

    public FavouriteSongs(Context context) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        songDatabaseRef = FirebaseDatabase.getInstance().getReference("Song");

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        storage = new StorageUtil(context);

        updateFavouriteSongList();
    }

    private void updateFavouriteSongList() {
        if (currentUser != null) {
            dbListener1 = databaseReference.orderByChild("userId").equalTo(currentUser.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                UserModel user = postSnapshot.getValue(UserModel.class);
                                ArrayList<SongModel> songs = user.getFavouriteSongs();
                                if (songs == null) {
                                    songs = new ArrayList<>();
                                }
                                storage.storeFavouriteSongs(songs);
                                databaseReference.removeEventListener(dbListener1);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    public boolean isFavourite(SongModel song) {
        favouriteList = storage.loadFavouriteSongs();
        if (favouriteList != null) {
            for (SongModel currentSong : favouriteList) {
                if (currentSong.getTitle().equals(song.getTitle())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isFavouriteAll(ArrayList<SongModel> songs) {
        for (SongModel song : songs) {
            if (!isFavourite(song)) {
                return false;
            }
        }
        return true;
    }

    public boolean favouriteOrUnFavourite(SongModel song, Context context) {
        if (currentUser != null) {
            if (isFavourite(song)) {
                removeFavouriteSong(song);
            } else {
                addFavouriteSong(song);
                return true;
            }
        } else {
            Toast.makeText(context, "Please Login", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void favourite(SongModel song) {
        if (!isFavourite(song)) {
            addFavouriteSong(song);
        }
    }

    public void unFavourite(SongModel song) {
        if (isFavourite(song)) {
            removeFavouriteSong(song);
        }
    }

    public void addFavouriteSong(final SongModel song) {
        databaseReference.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            UserModel user = postSnapshot.getValue(UserModel.class);
                            ArrayList<SongModel> songs = user.getFavouriteSongs();
                            if (songs == null) {
                                songs = new ArrayList<>();
                            }
                            songs.add(song);
                            storage.storeFavouriteSongs(songs);
                            user.setFavouriteSongs(songs);

                            String uploadId = postSnapshot.getKey();
                            databaseReference.child(uploadId).setValue(user);
                            databaseReference.removeEventListener(this);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void removeFavouriteSong(final SongModel song) {
        databaseReference.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            UserModel user = postSnapshot.getValue(UserModel.class);
                            ArrayList<SongModel> songs = user.getFavouriteSongs();
                            if (songs == null) {
                                songs = new ArrayList<>();
                            }
                            int position = -1;
                            for (int i = 0; i < songs.size(); i++) {
                                if (songs.get(i).getTitle().equals(song.getTitle())) {
                                    position = i;
                                    break;
                                }
                            }
                            songs.remove(position);
                            storage.storeFavouriteSongs(songs);
                            user.setFavouriteSongs(songs);

                            String uploadId = postSnapshot.getKey();
                            databaseReference.child(uploadId).setValue(user);
                            databaseReference.removeEventListener(this);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void favouriteAll(final ArrayList<SongModel> currentSongs) {
        databaseReference.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            UserModel user = postSnapshot.getValue(UserModel.class);
                            ArrayList<SongModel> songs = user.getFavouriteSongs();
                            if (songs == null) {
                                songs = new ArrayList<>();
                            }
                            for (SongModel song : currentSongs) {
                                if (!isFavourite(song)) {
                                    songs.add(song);
                                }
                            }
                            storage.storeFavouriteSongs(songs);
                            user.setFavouriteSongs(songs);

                            String uploadId = postSnapshot.getKey();
                            databaseReference.child(uploadId).setValue(user);
                            databaseReference.removeEventListener(this);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void unFavouriteAll(final ArrayList<SongModel> currentSongs) {
        databaseReference.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            UserModel user = postSnapshot.getValue(UserModel.class);
                            ArrayList<SongModel> songs = user.getFavouriteSongs();
                            if (songs == null) {
                                songs = new ArrayList<>();
                            }
                            for (SongModel song : currentSongs) {
                                if (isFavourite(song)) {
                                    int position = -1;
                                    for (int i = 0; i < songs.size(); i++) {
                                        if (songs.get(i).getTitle().equals(song.getTitle())) {
                                            position = i;
                                            break;
                                        }
                                    }
                                    songs.remove(position);
                                }
                            }
                            storage.storeFavouriteSongs(songs);
                            user.setFavouriteSongs(songs);

                            String uploadId = postSnapshot.getKey();
                            databaseReference.child(uploadId).setValue(user);
                            databaseReference.removeEventListener(this);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
