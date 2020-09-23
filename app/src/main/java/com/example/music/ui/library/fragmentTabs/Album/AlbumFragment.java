package com.example.music.ui.library.fragmentTabs.Album;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Adapter.SongAdapter;
import com.example.music.BottomSheets.BottomSheetAlbumSongs;
import com.example.music.MainActivity;
import com.example.music.Model.AlbumModel;
import com.example.music.Model.ArtistModel;
import com.example.music.Model.SongModel;
import com.example.music.R;
import com.example.music.Storage.StorageUtil;
import com.example.music.ui.library.fragmentTabs.Artist.ArtistFragment;
import com.example.music.ui.library.fragmentTabs.Playlist.FavouriteSongs;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AlbumFragment extends Fragment implements SongAdapter.OnItemClickListener,
        BottomSheetAlbumSongs.BottomSheetListener {
    public static final String CURRENT_ALBUM = "CURRENT_ALBUM";

    private ImageView buttonFavourite;
    private ImageView artistImage;

    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private ProgressBar progressBarRecycler;

    private StorageUtil storage;
    private FavouriteAlbums favouriteAlbums;

    private ArrayList<SongModel> songList;
    private AlbumModel currentAlbum;

    private DatabaseReference songDatabaseRef;
    private ValueEventListener dbListener1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_album, container, false);

        AppBarLayout appBarLayout = v.findViewById(R.id.appbar);
        CollapsingToolbarLayout collapsingToolbarLayout = v.findViewById(R.id.collapsing_toolbar);
        artistImage = v.findViewById(R.id.toolbar_image);
        buttonFavourite = v.findViewById(R.id.button_favourite);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float range = (float) -appBarLayout.getTotalScrollRange();
                float value = (255 * (1.0f - (float) verticalOffset/range));
                artistImage.setImageAlpha((int) value);
                if (value <= 45){
                    buttonFavourite.setVisibility(View.GONE);
                }else {
                    buttonFavourite.setVisibility(View.VISIBLE);
                }
            }
        });


        currentAlbum = (AlbumModel) getArguments().getSerializable(CURRENT_ALBUM);
        storage = new StorageUtil(getActivity());
        favouriteAlbums = FavouriteAlbums.getInstance(getActivity());
        if (storage.loadFavouriteAlbums() != null) {
            if (favouriteAlbums.isFavourite(currentAlbum)) {
                buttonFavourite.setImageResource(R.drawable.ic_baseline_favorite_24);
            }else {
                buttonFavourite.setImageResource(R.drawable.ic_baseline_not_favorite);
            }
        }

        Picasso.with(getActivity())
                .load(currentAlbum.getImageUrl())
                .fit()
                .centerCrop()
                .into(artistImage);
        collapsingToolbarLayout.setTitle(currentAlbum.getName());

        progressBarRecycler = v.findViewById(R.id.recycler_view_progress_bar);
        recyclerView = v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        songList = new ArrayList<>();

        adapter = new SongAdapter(getActivity(), songList);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        songDatabaseRef = FirebaseDatabase.getInstance().getReference("Song");

        dbListener1 = songDatabaseRef.orderByChild("album").equalTo(currentAlbum.getName())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        songList.clear();
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

        buttonFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favouriteAlbum();
            }
        });

        return v;
    }

    private void favouriteAlbum(){
        boolean isFavourite = favouriteAlbums.favourite(currentAlbum, getActivity());
        if (isFavourite) {
            buttonFavourite.setImageResource(R.drawable.ic_baseline_favorite_24);
        } else {
            buttonFavourite.setImageResource(R.drawable.ic_baseline_not_favorite);
        }
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

        BottomSheetAlbumSongs bottomSheet = new BottomSheetAlbumSongs();
        bottomSheet.setBottomSheetListener(this);
        bottomSheet.setArguments(bundle);
        bottomSheet.show(getChildFragmentManager(), "BottomSheetAlbumSongs");
    }

    @Override
    public void onOptionClicked(int option, int position, SongModel song) {
        switch (option) {
            case 0:
                FavouriteSongs favouriteSongs = FavouriteSongs.getInstance(getActivity());
                favouriteSongs.favouriteOrUnFavourite(song, getActivity());
                break;
            case 1:
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

    @Override
    public void onStop() {
        super.onStop();
        songDatabaseRef.removeEventListener(dbListener1);
    }
}
