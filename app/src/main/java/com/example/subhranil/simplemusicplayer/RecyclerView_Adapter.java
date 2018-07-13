package com.example.subhranil.simplemusicplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerView_Adapter extends RecyclerView.Adapter<ViewHolder> {
    List<SongFile> list = Collections.emptyList();
    Context context;
    onItemClickListener clickListener;
    public RecyclerView_Adapter(List<SongFile> list, Context context) {
        this.list = list;
        this.context = context;
    }
    public void setClickListener(onItemClickListener clickListener){
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.nameTxtView.setText(list.get(position).getTitle());
       // holder.albumTxtView.setText(list.get(position).getAlbum());
        holder.artistTxtView.setText(list.get(position).getArtist());
        Glide.with(holder.circleImageView)
                .load(holder.circleImageView
                        .getContext()
                        .getResources()
                        .getDrawable(R.drawable.iconlogo))
                .into(holder.circleImageView);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}

class ViewHolder extends RecyclerView.ViewHolder {
    public TextView nameTxtView;
    public TextView albumTxtView;
    public TextView artistTxtView;
    public LinearLayout container;
    CircleImageView circleImageView;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        container = (LinearLayout) itemView.findViewById(R.id.container);
        nameTxtView = (TextView) itemView.findViewById(R.id.nametxt);
        albumTxtView = (TextView) itemView.findViewById(R.id.albumtxt);
        artistTxtView = (TextView) itemView.findViewById(R.id.artisttxt);
        circleImageView = (CircleImageView) itemView.findViewById(R.id.image);
    }
}