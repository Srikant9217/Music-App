package com.example.music.ui.library.fragmentTabs;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.music.Adapter.PlaylistAdapter;
import com.example.music.Dialogs.DialogAddPlaylist;
import com.example.music.Model.PlaylistModel;
import com.example.music.Model.UserModel;
import com.example.music.R;
import com.example.music.ui.Playlist.PlaylistFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class PlaylistsFragment extends Fragment implements PlaylistAdapter.OnItemClickListener, DialogAddPlaylist.DialogPlaylistListener {
    private ImageView buttonAddPlaylist;
    private RecyclerView recyclerView;
    private PlaylistAdapter adapter;
    private ProgressBar progressBarRecycler;

    private ArrayList<PlaylistModel> playlistList;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private FirebaseStorage firebaseStorage;
    private DatabaseReference databaseReference;
    private ValueEventListener dbListener1;
    private ValueEventListener dbListener2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlists, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buttonAddPlaylist = view.findViewById(R.id.add_playlist);
        progressBarRecycler = view.findViewById(R.id.recycler_view_progress_bar);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        playlistList = new ArrayList<>();

        adapter = new PlaylistAdapter(getActivity(), playlistList);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        firebaseStorage = FirebaseStorage.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            dbListener1 = databaseReference.orderByChild("userId").equalTo(currentUser.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            playlistList.clear();
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                GenericTypeIndicator<ArrayList<PlaylistModel>> indicator = new GenericTypeIndicator<ArrayList<PlaylistModel>>() {
                                };
                                ArrayList<PlaylistModel> playlists = postSnapshot.child("playlists").getValue(indicator);
                                if (playlists != null && !playlists.isEmpty()) {
                                    for (PlaylistModel playlist : playlists) {
                                        playlist.setKey(postSnapshot.getKey());
                                        playlistList.add(playlist);
                                    }
                                }
                            }
                            adapter.notifyDataSetChanged();
                            progressBarRecycler.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBarRecycler.setVisibility(View.INVISIBLE);
                        }
                    });
        }else {
            progressBarRecycler.setVisibility(View.INVISIBLE);
        }
        buttonAddPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPlaylist();
            }
        });
    }

    private void addPlaylist() {
        if (currentUser == null) {
            Toast.makeText(getActivity(), "Please Login to create Playlist", Toast.LENGTH_SHORT).show();
        }else {
            DialogAddPlaylist addPlaylist = new DialogAddPlaylist();
            addPlaylist.setListener(this);
            addPlaylist.show(getActivity().getSupportFragmentManager(), "Add Playlist");
        }
    }

    @Override
    public void createPlaylist(String playlistName) {
        final PlaylistModel playlist = new PlaylistModel(playlistName, null);

        dbListener2 = databaseReference.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            UserModel user = postSnapshot.getValue(UserModel.class);
                            ArrayList<PlaylistModel> playlists = user.getPlaylists();
                            if (playlists == null) {
                                playlists = new ArrayList<>();
                            }
                            playlists.add(playlist);
                            user.setPlaylists(playlists);

                            String uploadId = postSnapshot.getKey();
                            databaseReference.child(uploadId).setValue(user);
                            databaseReference.removeEventListener(dbListener2);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onItemClick(int position, View view) {
        Bundle bundle = new Bundle();
        bundle.putInt("playlistPosition", position);
        view.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.playlistFragment, bundle));
        view.callOnClick();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (dbListener1 != null){
            databaseReference.removeEventListener(dbListener1);
        }
    }
}