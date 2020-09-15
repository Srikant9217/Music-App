package com.example.music.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.music.R;
import com.example.music.ui.library.fragmentTabs.Albums;
import com.example.music.ui.library.fragmentTabs.Artists;
import com.example.music.ui.library.fragmentTabs.PlaylistsFragment;
import com.google.android.material.tabs.TabLayout;

public class LibraryFragment extends Fragment {
    private ViewPager viewPager;
    private TabLayout tabLayout;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.tabs);
        viewPager = view.findViewById(R.id.view_pager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new PlaylistsFragment(), "Playlists");
        adapter.addFragment(new Artists(), "Artists");
        adapter.addFragment(new Albums(), "Albums");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }
}