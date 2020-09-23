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
            if (listener != null) {
                int position = getAdapterPosition();
                AlbumModel album = albums.get(position);
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAlbumItemClick(view, album);
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (listener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAlbumItemLongClick(position, albums.get(position));
                }
            }
            return true;
        }
    }

    public interface OnItemClickListener {
        void onAlbumItemClick(View view, AlbumModel album);
        void onAlbumItemLongClick(int position, AlbumModel albumModel);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
