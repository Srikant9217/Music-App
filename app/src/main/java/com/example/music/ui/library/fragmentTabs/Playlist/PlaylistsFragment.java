package com.example.music.ui.library.fragmentTabs.Playlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Adapter.PlaylistAdapter;
import com.example.music.BottomSheets.BottomSheetArtists;
import com.example.music.BottomSheets.BottomSheetPlaylists;
import com.example.music.Dialogs.DialogAddPlaylist;
import com.example.music.Dialogs.DialogAddSongsToPlaylist;
import com.example.music.Dialogs.DialogEditPlaylist;
import com.example.music.Model.PlaylistModel;
import com.example.music.Model.UserModel;
import com.example.music.R;
import com.example.music.ui.library.fragmentTabs.Artist.ArtistFragment;
import com.example.music.ui.library.fragmentTabs.Artist.FavouriteArtists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PlaylistsFragment extends Fragment implements PlaylistAdapter.OnItemClickListener,
        DialogAddPlaylist.DialogPlaylistListener, BottomSheetPlaylists.BottomSheetListener,
        DialogEditPlaylist.DialogPlaylistListener {
    private ImageView buttonAddPlaylist;
    private RelativeLayout favouritePlaylist;

    private RecyclerView recyclerView;
    private PlaylistAdapter adapter;
    private ProgressBar progressBarRecycler;

    private ArrayList<PlaylistModel> playlistList;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

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
        favouritePlaylist = view.findViewById(R.id.favourites_playlist);
        progressBarRecycler = view.findViewById(R.id.recycler_view_progress_bar);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        playlistList = new ArrayList<>();

        adapter = new PlaylistAdapter(getActivity(), playlistList);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

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
        } else {
            progressBarRecycler.setVisibility(View.INVISIBLE);
        }

        buttonAddPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPlaylist();
            }
        });
        favouritePlaylist.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.favouriteFragment));
    }

    private void addPlaylist() {
        if (currentUser == null) {
            Toast.makeText(getActivity(), "Please Login to create Playlist", Toast.LENGTH_SHORT).show();
        } else {
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
    public void editPlaylistName(final String playlistName, final int position) {
        databaseReference.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            UserModel user = postSnapshot.getValue(UserModel.class);
                            ArrayList<PlaylistModel> playlists = user.getPlaylists();
                            PlaylistModel playlist = playlists.get(position);
                            playlist.setName(playlistName);

                            String uploadId = postSnapshot.getKey();
                            databaseReference.child(uploadId).setValue(user);
                            databaseReference.removeEventListener(this);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void deletePlaylist(final int position) {
        databaseReference.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            UserModel user = postSnapshot.getValue(UserModel.class);
                            ArrayList<PlaylistModel> playlists = user.getPlaylists();
                            playlists.remove(position);

                            String uploadId = postSnapshot.getKey();
                            databaseReference.child(uploadId).setValue(user);
                            databaseReference.removeEventListener(this);
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
    public void onItemLongClicked(int position, PlaylistModel playlistModel) {
        Bundle bundle = new Bundle();
        bundle.putInt("playlistPosition", position);
        bundle.putSerializable("playlist", playlistModel);

        BottomSheetPlaylists bottomSheetPlaylists = new BottomSheetPlaylists();
        bottomSheetPlaylists.setBottomSheetListener(this);
        bottomSheetPlaylists.setArguments(bundle);
        bottomSheetPlaylists.show(getChildFragmentManager(), "BottomSheetPlaylists");
    }

    @Override
    public void onOptionClicked(int option, int position, PlaylistModel playlist) {
        switch (option) {
            case 0:
                Bundle bundle2 = new Bundle();
                bundle2.putString("currentName", playlist.getName());
                DialogEditPlaylist dialogEditPlaylist = new DialogEditPlaylist();
                dialogEditPlaylist.setListener(this);
                dialogEditPlaylist.setArguments(bundle2);
                dialogEditPlaylist.show(getActivity().getSupportFragmentManager(), "Edit Playlist Name");
                break;
            case 1:
                deletePlaylist(position);
                break;
            case 2:
                Bundle bundle = new Bundle();
                bundle.putInt("playlistPosition", position);
                View view = getActivity().findViewById(R.id.recycler_view);
                view.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.playlistFragment, bundle));
                view.callOnClick();
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (dbListener1 != null) {
            databaseReference.removeEventListener(dbListener1);
        }
    }
}