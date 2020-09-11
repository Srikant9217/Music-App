package com.example.music.Adapter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Model.AlbumModel;
import com.example.music.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {
    private Context context;
    private List<AlbumModel> albums;
    private OnItemClickListener listener;

    public AlbumAdapter(Context context, List<AlbumModel> albums) {
        this.context = context;
        this.albums = albums;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        AlbumModel currentAlbum = albums.get(position);
        Picasso.with(context)
                .load(currentAlbum.getImageUrl())
                .fit()
                .centerCrop()
                .into(holder.imageViewAlbumProfile);
        holder.textViewAlbumName.setText(currentAlbum.getName());
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        private ImageView imageViewAlbumProfile;
        private TextView textViewAlbumName;
        private ImageView imageViewAlbumMenu;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAlbumProfile = itemView.findViewById(R.id.image_view_album_profile);
            textViewAlbumName = itemView.findViewById(R.id.text_view_album_name);
            imageViewAlbumMenu = itemView.findViewById(R.id.image_view_album_menu);

            itemView.setOnClickListener(this);
            imageViewAlbumMenu.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                default:
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            MenuItem delete = contextMenu.add(Menu.NONE, 1, 1, "Delete");
            delete.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (listener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    switch (menuItem.getItemId()) {
                        case 1:
                            listener.onDeleteClick(position);
                            return true;
                    }
                }
            }
            return false;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onDeleteClick(int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
