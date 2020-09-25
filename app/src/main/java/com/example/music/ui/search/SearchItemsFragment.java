package com.example.music.ui.search;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Adapter.SearchAdapter;
import com.example.music.MainActivity;
import com.example.music.Model.AlbumModel;
import com.example.music.Model.ArtistModel;
import com.example.music.Model.SongModel;
import com.example.music.R;
import com.example.music.ui.library.fragmentTabs.Album.AlbumFragment;
import com.example.music.ui.library.fragmentTabs.Artist.ArtistFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchItemsFragment extends Fragment implements SearchAdapter.OnSongClickListener,
        SearchAdapter.OnArtistClickListener, SearchAdapter.OnAlbumClickListener {

    private RecyclerView recyclerView;
    private SearchAdapter adapter;

    private ArrayList<SongModel> songList;
    private ArrayList<ArtistModel> artistList;
    private ArrayList<AlbumModel> albumList;

    private DatabaseReference songReference;
    private DatabaseReference artistReference;
    private DatabaseReference albumReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_items, container, false);

        Toolbar toolbar = v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        recyclerView = v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        songList = new ArrayList<>();
        artistList = new ArrayList<>();
        albumList = new ArrayList<>();

        adapter = new SearchAdapter(getActivity(), songList, artistList, albumList);
        adapter.setOnSongClickListener(this);
        adapter.setOnArtistClickListener(this);
        adapter.setOnAlbumClickListener(this);
        recyclerView.setAdapter(adapter);

        songReference = FirebaseDatabase.getInstance().getReference("Song");
        artistReference = FirebaseDatabase.getInstance().getReference("Artists");
        albumReference = FirebaseDatabase.getInstance().getReference("Albums");

        songReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                songList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    SongModel song = postSnapshot.getValue(SongModel.class);
                    song.setKey(postSnapshot.getKey());
                    songList.add(song);
                }
                adapter.notifyDataSetChanged();
                songReference.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        artistReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                artistList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    ArtistModel artist = postSnapshot.getValue(ArtistModel.class);
                    artist.setKey(postSnapshot.getKey());
                    artistList.add(artist);
                }
                adapter.notifyDataSetChanged();
                artistReference.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        albumReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                albumList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    AlbumModel album = postSnapshot.getValue(AlbumModel.class);
                    album.setKey(postSnapshot.getKey());
                    albumList.add(album);
                }
                adapter.notifyDataSetChanged();
                albumReference.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    @Override
    public void onItemClick(SongModel song) {
        ArrayList<SongModel> songs = new ArrayList<>();
        songs.add(song);
        ((MainActivity) getActivity()).playSong(songs, 0);
    }

    @Override
    public void onItemLongClick(int position, SongModel song) {

    }

    @Override
    public void onArtistItemClick(View view, ArtistModel artist) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ArtistFragment.CURRENT_ARTIST, artist);
        view.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.artistFragment, bundle));
        view.callOnClick();
    }

    @Override
    public void onArtistItemLongClick(int position, ArtistModel artistModel) {

    }

    @Override
    public void onAlbumItemClick(View view, AlbumModel album) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(AlbumFragment.CURRENT_ALBUM, album);
        view.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.albumFragment, bundle));
        view.callOnClick();
    }

    @Override
    public void onAlbumItemLongClick(int position, AlbumModel albumModel) {

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setIconified(false);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }
}
