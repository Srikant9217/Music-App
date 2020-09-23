package com.example.music.BottomSheets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.music.Model.ArtistModel;
import com.example.music.Model.PlaylistModel;
import com.example.music.Model.SongModel;
import com.example.music.Model.UserModel;
import com.example.music.R;
import com.example.music.ui.library.fragmentTabs.Artist.FavouriteArtists;
import com.example.music.ui.library.fragmentTabs.Playlist.FavouriteSongs;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BottomSheetPlaylists extends BottomSheetDialogFragment {
    private BottomSheetListener listener;
    private Integer playlistPosition;
    private PlaylistModel playlist;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet, container, false);
        final ListView listView = v.findViewById(R.id.list_view_options);

        playlistPosition = getArguments().getInt("position");

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
        userReference.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            UserModel user = postSnapshot.getValue(UserModel.class);
                            ArrayList<PlaylistModel> playlists = user.getPlaylists();
                            playlist = playlists.get(playlistPosition);

                        }

                        ArrayList<String> options = new ArrayList<>();
                        options.add("Edit");
                        options.add("Delete");
                        options.add("View Playlist");

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                                android.R.layout.simple_list_item_1,
                                options);

                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                listener.onOptionClicked(i, playlistPosition, playlist);
                                dismiss();
                            }
                        });
                        userReference.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        return v;
    }


    public interface BottomSheetListener {
        void onOptionClicked(int option, int position, PlaylistModel playlist);
    }

    public void setBottomSheetListener(BottomSheetListener listener) {
        this.listener = listener;
    }
}