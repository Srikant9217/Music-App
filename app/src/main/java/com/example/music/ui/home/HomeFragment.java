package com.example.music.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.SongAdapter;
import com.example.music.MainActivity;
import com.example.music.SongModel;
import com.example.music.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;


public class HomeFragment extends Fragment implements SongAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private ProgressBar progressBarRecycler;

    private ArrayList<SongModel> songList;

    private FirebaseStorage firebaseStorage;
    private DatabaseReference databaseReference;
    private ValueEventListener dbListener;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        progressBarRecycler = v.findViewById(R.id.recycler_view_progress_bar);
        recyclerView = v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        songList = new ArrayList<>();

        adapter = new SongAdapter(getActivity(), songList);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        firebaseStorage = FirebaseStorage.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Song");

        dbListener = databaseReference.addValueEventListener(new ValueEventListener() {
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
        return v;
    }

    @Override
    public void onItemClick(int position) {
        ((MainActivity)getActivity()).playSong(songList, position);
    }
}
