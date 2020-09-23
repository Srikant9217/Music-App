package com.example.music.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Adapter.AlbumAdapter;
import com.example.music.Adapter.AlbumAdapterHorizontal;
import com.example.music.Adapter.ArtistAdapter;
import com.example.music.Adapter.ArtistAdapterHorizontal;
import com.example.music.Adapter.SongAdapter;
import com.example.music.Adapter.SongAdapterHorizontal;
import com.example.music.MainActivity;
import com.example.music.Model.AlbumModel;
import com.example.music.Model.ArtistModel;
import com.example.music.Model.SongModel;
import com.example.music.R;
import com.example.music.ui.library.fragmentTabs.Album.AlbumFragment;
import com.example.music.ui.library.fragmentTabs.Artist.ArtistFragment;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements SongAdapterHorizontal.OnItemClickListener,
        ArtistAdapterHorizontal.OnItemClickListener,
        AlbumAdapterHorizontal.OnItemClickListener {
    private static ImageView imageViewUserProfile;
    private TextView titleSong;
    private TextView titleArtist;
    private TextView titleAlbum;
    private ProgressBar progressBarRecycler;

    private RecyclerView recyclerViewSong;
    private SongAdapterHorizontal songAdapter;
    private ArrayList<SongModel> songList;

    private RecyclerView recyclerViewArtist;
    private ArtistAdapterHorizontal artistAdapter;
    private ArrayList<ArtistModel> artistList;

    private RecyclerView recyclerViewAlbum;
    private AlbumAdapterHorizontal albumAdapter;
    private ArrayList<AlbumModel> albumList;

    private DatabaseReference songReference;
    private ValueEventListener dbListener1;

    private DatabaseReference artistReference;
    private ValueEventListener dbListener2;

    private DatabaseReference albumReference;
    private ValueEventListener dbListener3;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        imageViewUserProfile = v.findViewById(R.id.image_view_user_profile);
        titleSong = v.findViewById(R.id.title_song);
        titleArtist = v.findViewById(R.id.title_artist);
        titleAlbum = v.findViewById(R.id.title_album);
        progressBarRecycler = v.findViewById(R.id.recycler_view_progress_bar);

        imageViewUserProfile.setVisibility(View.INVISIBLE);
        titleSong.setVisibility(View.INVISIBLE);
        titleArtist.setVisibility(View.INVISIBLE);
        titleAlbum.setVisibility(View.INVISIBLE);

        recyclerViewSong = v.findViewById(R.id.recycler_view_song);
        recyclerViewArtist = v.findViewById(R.id.recycler_view_artist);
        recyclerViewAlbum = v.findViewById(R.id.recycler_view_album);

        recyclerViewSong.setHasFixedSize(true);
        recyclerViewSong.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewArtist.setHasFixedSize(true);
        recyclerViewArtist.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewAlbum.setHasFixedSize(true);
        recyclerViewAlbum.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        songList = new ArrayList<>();
        artistList = new ArrayList<>();
        albumList = new ArrayList<>();

        songAdapter = new SongAdapterHorizontal(getActivity(), songList);
        songAdapter.setOnItemClickListener(this);
        recyclerViewSong.setAdapter(songAdapter);

        artistAdapter = new ArtistAdapterHorizontal(getActivity(), artistList);
        artistAdapter.setOnItemClickListener(this);
        recyclerViewArtist.setAdapter(artistAdapter);

        albumAdapter = new AlbumAdapterHorizontal(getActivity(), albumList);
        albumAdapter.setOnItemClickListener(this);
        recyclerViewAlbum.setAdapter(albumAdapter);

        songReference = FirebaseDatabase.getInstance().getReference("Song");
        artistReference = FirebaseDatabase.getInstance().getReference("Artists");
        albumReference = FirebaseDatabase.getInstance().getReference("Albums");

        dbListener1 = songReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                songList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    SongModel song = postSnapshot.getValue(SongModel.class);
                    song.setKey(postSnapshot.getKey());
                    songList.add(song);
                }
                songAdapter.notifyDataSetChanged();
                uiVisible();            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                uiVisible();            }
        });

        dbListener2 = artistReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                artistList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    ArtistModel artist = postSnapshot.getValue(ArtistModel.class);
                    artist.setKey(postSnapshot.getKey());
                    artistList.add(artist);
                }
                artistAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                uiVisible();            }
        });


        dbListener3 = albumReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                albumList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    AlbumModel album = postSnapshot.getValue(AlbumModel.class);
                    album.setKey(postSnapshot.getKey());
                    albumList.add(album);
                }
                albumAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                uiVisible();
            }
        });

        updateUserUI();
        return v;
    }

    @Override
    public void onItemClick(int position) {
        ((MainActivity) getActivity()).playSong(songList, position);
    }

    @Override
    public void onItemLongClick(int position, SongModel songModel) {

    }

    @Override
    public void onArtistItemClick(View view, ArtistModel artist) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ArtistFragment.CURRENT_ARTIST, artist);
        view.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.artistFragment, bundle));
        view.callOnClick();
    }

    @Override
    public void onArtistItemLongClick(int position) {

    }

    @Override
    public void onAlbumItemClick(View view, AlbumModel album) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(AlbumFragment.CURRENT_ALBUM, album);
        view.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.albumFragment, bundle));
        view.callOnClick();
    }

    @Override
    public void onAlbumItemLongClick(int position) {

    }

    public static void updateUserUI() {
        if (MainActivity.currentUser == null) {
            imageViewUserProfile.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.loginFragment));
        } else {
            imageViewUserProfile.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.userProfileFragment));
        }
    }

    private void uiVisible() {
        imageViewUserProfile.setVisibility(View.VISIBLE);
        titleSong.setVisibility(View.VISIBLE);
        titleArtist.setVisibility(View.VISIBLE);
        titleAlbum.setVisibility(View.VISIBLE);
        progressBarRecycler.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStop() {
        super.onStop();
        songReference.removeEventListener(dbListener1);
        artistReference.removeEventListener(dbListener2);
        albumReference.removeEventListener(dbListener3);
    }
}
