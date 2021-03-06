package com.example.music.ui.library.fragmentTabs.Playlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Adapter.SongAdapter;
import com.example.music.BottomSheets.BottomSheetPlaylistSongs;
import com.example.music.Dialogs.DialogAddPlaylist;
import com.example.music.Dialogs.DialogAddSongsToPlaylist;
import com.example.music.MainActivity;
import com.example.music.Model.AlbumModel;
import com.example.music.Model.ArtistModel;
import com.example.music.Model.PlaylistModel;
import com.example.music.Model.SongModel;
import com.example.music.Model.UserModel;
import com.example.music.R;
import com.example.music.ui.library.fragmentTabs.Album.AlbumFragment;
import com.example.music.ui.library.fragmentTabs.Artist.ArtistFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class PlaylistFragment extends Fragment implements SongAdapter.OnItemClickListener,
        DialogAddSongsToPlaylist.DialogAddSongsListener,
        BottomSheetPlaylistSongs.BottomSheetListener {
    private TextView textViewPlaylistTitle;
    private ImageView buttonAddSongsToPlaylist;

    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private ProgressBar progressBarRecycler;

    private ArrayList<SongModel> songList;
    private int position;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private FirebaseStorage firebaseStorage;
    private DatabaseReference databaseReference;
    private DatabaseReference songDatabaseRef;
    private ValueEventListener dbListener1;
    private ValueEventListener dbListener2;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_playlist, container, false);

        textViewPlaylistTitle = v.findViewById(R.id.text_view_playlist_title);
        buttonAddSongsToPlaylist = v.findViewById(R.id.add_songs_to_playlist);

        position = getArguments().getInt("playlistPosition");

        progressBarRecycler = v.findViewById(R.id.recycler_view_progress_bar);
        recyclerView = v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        songList = new ArrayList<>();

        adapter = new SongAdapter(getActivity(), songList);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        firebaseStorage = FirebaseStorage.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        songDatabaseRef = FirebaseDatabase.getInstance().getReference("Song");

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        dbListener1 = databaseReference.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        songList.clear();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            UserModel user = postSnapshot.getValue(UserModel.class);
                            ArrayList<PlaylistModel> playlists = user.getPlaylists();
                            PlaylistModel playlist = playlists.get(position);
                            textViewPlaylistTitle.setText(playlist.getName());
                            ArrayList<String> songs = playlist.getSongs();
                            if (songs == null){
                                progressBarRecycler.setVisibility(View.INVISIBLE);
                                break;
                            }

                            for (String song: songs){
                                songDatabaseRef.orderByChild("title").equalTo(song)
                                        .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                            SongModel song = postSnapshot.getValue(SongModel.class);
                                            song.setKey(postSnapshot.getKey());
                                            songList.add(song);
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
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBarRecycler.setVisibility(View.INVISIBLE);
                    }
                });

        buttonAddSongsToPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });

        return v;
    }

    public void openDialog() {
        DialogAddSongsToPlaylist addSongs = new DialogAddSongsToPlaylist();
        addSongs.setListener(this);
        addSongs.show(getActivity().getSupportFragmentManager(), "Add Songs To Playlist");
    }

    @Override
    public void AddSong(final String songName) {
        dbListener2 = databaseReference.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            UserModel user = postSnapshot.getValue(UserModel.class);
                            ArrayList<PlaylistModel> playlists = user.getPlaylists();
                            PlaylistModel playlist = playlists.get(position);
                            ArrayList<String> songs = playlist.getSongs();
                            if (songs == null){
                                songs = new ArrayList<>();
                            }
                            boolean exists = false;
                            for (String song: songs){
                                if (song.equals(songName)){
                                    exists = true;
                                }
                            }
                            if (!exists){
                                songs.add(songName);
                            }
                            playlist.setSongs(songs);

                            String uploadId = postSnapshot.getKey();
                            databaseReference.child(uploadId).setValue(user);
                            databaseReference.removeEventListener(dbListener2);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void removeSongFromPlaylist(final SongModel songModel) {
        dbListener2 = databaseReference.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        songList.clear();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            UserModel user = postSnapshot.getValue(UserModel.class);
                            ArrayList<PlaylistModel> playlists = user.getPlaylists();
                            PlaylistModel playlist = playlists.get(position);

                            ArrayList<String> songs = playlist.getSongs();
                            if (songs == null){
                                songs = new ArrayList<>();
                            }
                            int position = -1;
                            for (int i = 0; i < songs.size(); i++) {
                                if (songs.get(i).equals(songModel.getTitle())) {
                                    position = i;
                                    break;
                                }
                            }
                            songs.remove(position);
                            playlist.setSongs(songs);

                            String uploadId = postSnapshot.getKey();
                            databaseReference.child(uploadId).setValue(user);
                            databaseReference.removeEventListener(dbListener2);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onItemClick(int position) {
        ((MainActivity)getActivity()).playSong(songList, position);
    }

    @Override
    public void onItemLongClick(int position, SongModel song) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putSerializable("song", song);

        BottomSheetPlaylistSongs bottomSheet = new BottomSheetPlaylistSongs();
        bottomSheet.setBottomSheetListener(this);
        bottomSheet.setArguments(bundle);
        bottomSheet.show(getChildFragmentManager(), "BottomSheetPlaylistSongs");
    }

    @Override
    public void onOptionClicked(int option, int position, SongModel song) {
        switch (option) {
            case 0:
                removeSongFromPlaylist(song);
                break;
            case 1:
                viewAlbum(song.getAlbum());
                break;
            case 2:
                viewArtist(song.getArtist());
                break;
        }
    }

    private void viewArtist(final String artistName){
        final DatabaseReference albumReference = FirebaseDatabase.getInstance().getReference("Artists");
        dbListener1 = albumReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArtistModel artist = null;
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    ArtistModel currentArtist = postSnapshot.getValue(ArtistModel.class);
                    if (currentArtist.getName().equals(artistName)){
                        artist = currentArtist;
                        break;
                    }
                }
                Bundle bundle = new Bundle();
                bundle.putSerializable(ArtistFragment.CURRENT_ARTIST, artist);
                View view = getActivity().findViewById(R.id.recycler_view);
                view.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.artistFragment, bundle));
                view.callOnClick();
                albumReference.removeEventListener(dbListener1);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void viewAlbum(final String albumName){
        final DatabaseReference albumReference = FirebaseDatabase.getInstance().getReference("Albums");
        dbListener1 = albumReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                AlbumModel album = null;
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    AlbumModel currentAlbum = postSnapshot.getValue(AlbumModel.class);
                    if (currentAlbum.getName().equals(albumName)){
                        album = currentAlbum;
                        break;
                    }
                }
                Bundle bundle = new Bundle();
                bundle.putSerializable(AlbumFragment.CURRENT_ALBUM, album);
                View view = getActivity().findViewById(R.id.recycler_view);
                view.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.albumFragment, bundle));
                view.callOnClick();
                albumReference.removeEventListener(dbListener1);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        databaseReference.removeEventListener(dbListener1);
    }
}