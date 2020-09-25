package com.example.music.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.MainActivity;
import com.example.music.Model.AlbumModel;
import com.example.music.Model.ArtistModel;
import com.example.music.Model.SongModel;
import com.example.music.R;
import com.example.music.Storage.StorageUtil;
import com.example.music.ui.library.fragmentTabs.Playlist.FavouriteSongs;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    public static final int SONG = 0;
    public static final int ARTIST = 1;
    public static final int ALBUM = 2;

    private List<SongModel> currentSongs;
    private List<ArtistModel> currentArtists;
    private List<AlbumModel> currentAlbums;

    private List<SongModel> songsAll;
    private List<ArtistModel> artistsAll;
    private List<AlbumModel> albumsAll;

    private OnSongClickListener songListener;
    private OnArtistClickListener artistListener;
    private OnAlbumClickListener albumListener;

    private Context context;
    private StorageUtil storage;
    private ArrayList<SongModel> favSongs;
    private FavouriteSongs favouriteSongs;

    private boolean updated = false;

    public SearchAdapter(Context context, List<SongModel> songs, List<ArtistModel> artists, List<AlbumModel> albums) {
        currentSongs = songs;
        currentArtists = artists;
        currentAlbums = albums;

        this.context = context;
        storage = new StorageUtil(context);
        favouriteSongs = FavouriteSongs.getInstance(context);
    }

    private void updateAllLists() {
        if (!updated) {
            songsAll = new ArrayList<>(currentSongs);
            artistsAll = new ArrayList<>(currentArtists);
            albumsAll = new ArrayList<>(currentAlbums);
            updated = true;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < currentSongs.size()) {
            return SONG;
        } else if (currentSongs.size() <= position && position < currentSongs.size() + currentArtists.size()) {
            return ARTIST;
        } else if (currentSongs.size() + currentArtists.size() <= position && position < currentSongs.size() + currentArtists.size() + currentAlbums.size()) {
            return ALBUM;
        }
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case SONG:
                View v0 = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
                favSongs = storage.loadFavouriteSongs();
                return new SongViewHolder(v0);
            case ARTIST:
                View v1 = LayoutInflater.from(context).inflate(R.layout.item_artist, parent, false);
                return new ArtistViewHolder(v1);
            case ALBUM:
                View v2 = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false);
                return new AlbumViewHolder(v2);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case SONG:
                SongViewHolder songViewHolder = (SongViewHolder) holder;
                SongModel currentSong = currentSongs.get(position);
                Picasso.with(context)
                        .load(currentSong.getImageUrl())
                        .fit()
                        .centerCrop()
                        .into(songViewHolder.imageViewSongImage);
                songViewHolder.textViewTitle.setText(currentSong.getTitle());
                songViewHolder.textViewArtist.setText(currentSong.getArtist());

                if (favSongs != null) {
                    if (favouriteSongs.isFavourite(currentSong)) {
                        songViewHolder.imageViewFavourite.setImageResource(R.drawable.ic_baseline_favorite_24);
                    } else {
                        songViewHolder.imageViewFavourite.setImageResource(R.drawable.ic_baseline_not_favorite);
                    }
                }
                break;
            case ARTIST:
                ArtistViewHolder artistViewHolder = (ArtistViewHolder) holder;
                ArtistModel currentArtist = currentArtists.get(position - currentSongs.size());
                Picasso.with(context)
                        .load(currentArtist.getImageUrl())
                        .fit()
                        .centerCrop()
                        .into(artistViewHolder.imageViewArtistProfile);
                artistViewHolder.textViewArtistName.setText(currentArtist.getName());
                break;
            case ALBUM:
                AlbumViewHolder albumViewHolder = (AlbumViewHolder) holder;
                AlbumModel currentAlbum = currentAlbums.get(position - currentSongs.size() - currentArtists.size());
                Picasso.with(context)
                        .load(currentAlbum.getImageUrl())
                        .fit()
                        .centerCrop()
                        .into(albumViewHolder.imageViewAlbumProfile);
                albumViewHolder.textViewAlbumName.setText(currentAlbum.getName());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return currentSongs.size() + currentArtists.size() + currentAlbums.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            return null;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            if ((!currentSongs.isEmpty() && !currentArtists.isEmpty() && !currentAlbums.isEmpty()) || updated) {
                updateAllLists();

                currentSongs.clear();
                currentArtists.clear();
                currentAlbums.clear();

                String filterPattern = charSequence.toString().toLowerCase().trim();

                for (SongModel item : songsAll) {
                    if (item.getTitle().toLowerCase().contains(filterPattern)) {
                        currentSongs.add(item);
                    }
                }

                for (ArtistModel item : artistsAll) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        currentArtists.add(item);
                    }
                }

                for (AlbumModel item : albumsAll) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        currentAlbums.add(item);
                    }
                }
                notifyDataSetChanged();
            }
        }
    };

    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView textViewTitle;
        private TextView textViewArtist;
        private ImageView imageViewSongImage;
        private ImageView imageViewFavourite;
        private ImageView imageViewMenu;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewArtist = itemView.findViewById(R.id.text_view_artist);
            imageViewSongImage = itemView.findViewById(R.id.image_view_song);
            imageViewFavourite = itemView.findViewById(R.id.image_view_favourite);
            imageViewMenu = itemView.findViewById(R.id.image_view_menu);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            imageViewFavourite.setOnClickListener(this);
            imageViewMenu.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            SongModel song = currentSongs.get(position);
            switch (view.getId()) {
                case R.id.image_view_favourite:
                    boolean isFavourite = favouriteSongs.favouriteOrUnFavourite(currentSongs.get(position), context);
                    if (isFavourite) {
                        imageViewFavourite.setImageResource(R.drawable.ic_baseline_favorite_24);
                    } else {
                        imageViewFavourite.setImageResource(R.drawable.ic_baseline_not_favorite);
                    }
                    break;
                case R.id.image_view_menu:
                    if (songListener != null) {
                        if (position != RecyclerView.NO_POSITION) {
                            songListener.onItemLongClick(position, song);
                        }
                    }
                    break;
                default:
                    if (songListener != null) {
                        if (position != RecyclerView.NO_POSITION) {
                            songListener.onItemClick(song);
                        }
                    }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (songListener != null) {
                int position = getAdapterPosition();
                SongModel song = currentSongs.get(position);
                if (position != RecyclerView.NO_POSITION) {
                    songListener.onItemLongClick(position, song);
                }
            }
            return true;
        }
    }

    public interface OnSongClickListener {
        void onItemClick(SongModel song);

        void onItemLongClick(int position, SongModel song);
    }

    public void setOnSongClickListener(OnSongClickListener listener) {
        this.songListener = listener;
    }


    public class ArtistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private ImageView imageViewArtistProfile;
        private TextView textViewArtistName;
        private ImageView imageViewArtistMenu;

        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewArtistProfile = itemView.findViewById(R.id.image_view_artist_profile);
            textViewArtistName = itemView.findViewById(R.id.text_view_artist_name);
            imageViewArtistMenu = itemView.findViewById(R.id.image_view_artist_menu);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (artistListener != null) {
                int position = getAdapterPosition() - currentSongs.size();
                ArtistModel artist = currentArtists.get(position);
                if (position != RecyclerView.NO_POSITION) {
                    artistListener.onArtistItemClick(view, artist);
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (artistListener != null) {
                int position = getAdapterPosition() - currentSongs.size();
                if (position != RecyclerView.NO_POSITION) {
                    artistListener.onArtistItemLongClick(position, currentArtists.get(position));
                }
            }
            return true;
        }
    }

    public interface OnArtistClickListener {
        void onArtistItemClick(View view, ArtistModel artist);

        void onArtistItemLongClick(int position, ArtistModel artistModel);
    }

    public void setOnArtistClickListener(OnArtistClickListener listener) {
        this.artistListener = listener;
    }


    public class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {
        private ImageView imageViewAlbumProfile;
        private TextView textViewAlbumName;
        private ImageView imageViewAlbumMenu;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAlbumProfile = itemView.findViewById(R.id.image_view_album_profile);
            textViewAlbumName = itemView.findViewById(R.id.text_view_album_name);
            imageViewAlbumMenu = itemView.findViewById(R.id.image_view_album_menu);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (albumListener != null) {
                int position = getAdapterPosition() - currentSongs.size() - currentArtists.size();
                AlbumModel album = currentAlbums.get(position);
                if (position != RecyclerView.NO_POSITION) {
                    albumListener.onAlbumItemClick(view, album);
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (albumListener != null) {
                int position = getAdapterPosition() - currentSongs.size() - currentArtists.size();
                if (position != RecyclerView.NO_POSITION) {
                    albumListener.onAlbumItemLongClick(position, currentAlbums.get(position));
                }
            }
            return true;
        }
    }

    public interface OnAlbumClickListener {
        void onAlbumItemClick(View view, AlbumModel album);

        void onAlbumItemLongClick(int position, AlbumModel albumModel);
    }

    public void setOnAlbumClickListener(OnAlbumClickListener listener) {
        this.albumListener = listener;
    }

}
