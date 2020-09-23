package com.example.music.ui.library.fragmentTabs.Album;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.music.Adapter.AlbumAdapter;
import com.example.music.Adapter.ArtistAdapter;
import com.example.music.BottomSheets.BottomSheetAlbums;
import com.example.music.BottomSheets.BottomSheetArtists;
import com.example.music.MainActivity;
import com.example.music.Model.AlbumModel;
import com.example.music.Model.ArtistModel;
import com.example.music.Model.SongModel;
import com.example.music.Model.UserModel;
import com.example.music.R;
import com.example.music.ui.library.fragmentTabs.Artist.ArtistFragment;
import com.example.music.ui.library.fragmentTabs.Artist.FavouriteArtists;
import com.example.music.ui.library.fragmentTabs.Playlist.FavouriteSongs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class AlbumsFragment extends Fragment implements AlbumAdapter.OnItemClickListener,
        BottomSheetAlbums.BottomSheetListener {
    private RecyclerView recyclerView;
    private AlbumAdapter adapter;
    private ProgressBar progressBarRecycler;

    private ArrayList<AlbumModel> albumList;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private FirebaseStorage firebaseStorage;
    private DatabaseReference databaseReference;
    private ValueEventListener dbListener;
    private ValueEventListener dbListener1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_albums, container, false);
        progressBarRecycler = v.findViewById(R.id.recycler_view_progress_bar);
        recyclerView = v.findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        albumList = new ArrayList<>();

        adapter = new AlbumAdapter(getActivity(), albumList);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        firebaseStorage = FirebaseStorage.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            dbListener = databaseReference.orderByChild("userId").equalTo(currentUser.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            albumList.clear();
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                UserModel user = postSnapshot.getValue(UserModel.class);
                                ArrayList<AlbumModel> albums = user.getFavouriteAlbums();

                                if (albums != null && !albums.isEmpty()) {
                                    for (AlbumModel album : albums) {
                                        album.setKey(postSnapshot.getKey());
                                        albumList.add(album);
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
        return v;
    }

    @Override
    public void onAlbumItemClick(View view, AlbumModel album) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(AlbumFragment.CURRENT_ALBUM, album);
        view.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.albumFragment, bundle));
        view.callOnClick();
    }

    @Override
    public void onAlbumItemLongClick(int position, AlbumModel album) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putSerializable("album", album);

        BottomSheetAlbums bottomSheetAlbums = new BottomSheetAlbums();
        bottomSheetAlbums.setBottomSheetListener(this);
        bottomSheetAlbums.setArguments(bundle);
        bottomSheetAlbums.show(getChildFragmentManager(), "BottomSheetAlbums");
    }

    @Override
    public void onOptionClicked(int option, int position, final AlbumModel album) {
        switch (option) {
            case 0:
                FavouriteAlbums favouriteAlbums = FavouriteAlbums.getInstance(getActivity());
                favouriteAlbums.favourite(album, getActivity());
                break;
            case 1:
                final DatabaseReference albumReference = FirebaseDatabase.getInstance().getReference("Song");
                dbListener1 = albumReference.orderByChild("album").equalTo(album.getName())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ArrayList<SongModel> songList = new ArrayList<>();
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    SongModel song = postSnapshot.getValue(SongModel.class);
                                    songList.add(song);
                                }
                                FavouriteSongs favouriteSongs = FavouriteSongs.getInstance(getActivity());
                                if (favouriteSongs.isFavouriteAll(songList)){
                                    unFavouriteAllSongs(album.getName());
                                }else {
                                    favouriteAllSongs(album.getName());
                                }
                                albumReference.removeEventListener(dbListener1);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                break;
            case 2:
                Bundle bundle = new Bundle();
                bundle.putSerializable(AlbumFragment.CURRENT_ALBUM, album);
                View view = getActivity().findViewById(R.id.recycler_view);
                view.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.albumFragment, bundle));
                view.callOnClick();
                break;
        }
    }

    private void favouriteAllSongs(final String albumName){
        final DatabaseReference albumReference = FirebaseDatabase.getInstance().getReference("Song");
        albumReference.orderByChild("album").equalTo(albumName)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<SongModel> songList = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    SongModel song = postSnapshot.getValue(SongModel.class);
                    songList.add(song);
                }
                FavouriteSongs favouriteSongs = FavouriteSongs.getInstance(getActivity());
                favouriteSongs.favouriteAll(songList);
                albumReference.removeEventListener(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void unFavouriteAllSongs(final String albumName){
        final DatabaseReference albumReference = FirebaseDatabase.getInstance().getReference("Song");
        albumReference.orderByChild("album").equalTo(albumName)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<SongModel> songList = new ArrayList<>();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            SongModel song = postSnapshot.getValue(SongModel.class);
                            songList.add(song);
                        }
                        FavouriteSongs favouriteSongs = FavouriteSongs.getInstance(getActivity());
                        favouriteSongs.unFavouriteAll(songList);
                        albumReference.removeEventListener(this);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (dbListener != null) {
            databaseReference.removeEventListener(dbListener);
        }
    }
}