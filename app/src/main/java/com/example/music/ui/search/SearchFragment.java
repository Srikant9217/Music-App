package com.example.music.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.music.R;

public class SearchFragment extends Fragment {
    private Button buttonSearch;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        buttonSearch = v.findViewById(R.id.button_search);
        buttonSearch.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.searchItemsFragment));
        return v;
    }
}