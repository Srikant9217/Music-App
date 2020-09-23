package com.example.music.ui.library.fragmentTabs.Playlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Adapter.SongAdapter;
import com.example.music.BottomSheets.BottomSheetFavouriteSongs;
import com.example.music.MainActivity;
import com.example.music.Model.AlbumModel;
import com.example.music.Model.ArtistModel;
import com.example.music.Model.SongModel;
import com.example.music.Model.UserModel;
import com.example.music.R;
import com.example.music.Storage.StorageUtil;
import com.example.music.ui.library.fragmentTabs.Album.AlbumFragment;
import com.example.music.ui.library.fragmentTabs.Artist.ArtistFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavouriteFragment extends Fragment implements SongAdapter.OnItemClickListener,
        BottomSheetFavouriteSongs.BottomSheetListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    private ValueEventListener dbListener1;

    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private ProgressBar progressBarRecycler;

    private StorageUtil storage;
    private ArrayList<SongModel> favouriteSongList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_playlist_favourite, container, false);

        progressBarRecycler = v.findViewById(R.id.recycler_view_progress_bar);
        recyclerView = v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        storage = new StorageUtil(getActivity());
        favouriteSongList = storage.loadFavouriteSongs();

        if (favouriteSongList == null) {
            favouriteSongList = new ArrayList<>();
        }

        if (favouriteSongList.isEmpty()) {
            progressBarRecycler.setVisibility(View.INVISIBLE);
        }

        adapter = new SongAdapter(getActivity(), favouriteSongList);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

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
                                favouriteSongList.clear();
                                favouriteSongList.addAll(songs);
                            }
                            adapter.notifyDataSetChanged();
                            if (favouriteSongList.isEmpty()){
                                progressBarRecycler.setVisibility(View.INVISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

        return v;
    }

    @Override
    public void onItemClick(int position) {
        ((MainActivity) getActivity()).playSong(favouriteSongList, position);
    }

    @Override
    public void onItemLongClick(int position, SongModel song) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putSerializable("song", song);

        BottomSheetFavouriteSongs bottomSheet = new BottomSheetFavouriteSongs();
        bottomSheet.setBottomSheetListener(this);
        bottomSheet.setArguments(bundle);
        bottomSheet.show(getChildFragmentManager(), "BottomSheetFavouriteSongs");
    }

    @Override
    public void onOptionClicked(int option, int position, SongModel song) {
        switch (option) {
            case 0:
                FavouriteSongs favouriteSongs = FavouriteSongs.getInstance(getActivity());
                favouriteSongs.favouriteOrUnFavourite(song, getActivity());
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
        if (dbListener1 != null) {
            databaseReference.removeEventListener(dbListener1);
        }
    }
}
