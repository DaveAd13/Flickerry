package com.david.flickerry;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.david.flickerry.activities.ImageActivity;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private List<String> urlList;
    private Context context;


    public RecyclerViewAdapter(Context context) {
        this.context = context;
        urlList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        String size = urlList.get(position);
        Glide.with(context)
                .load(size)
//                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(new RequestOptions()
                .override(Target.SIZE_ORIGINAL)
                .placeholder(R.drawable.placeholder))
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return urlList.size();
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(List<String> data) {
        this.urlList.clear();
        this.urlList.addAll(data);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.img);
            view.setOnClickListener(v -> {
                int position = getAdapterPosition();
                Intent intent = new Intent(context, ImageActivity.class);
                intent.putExtra("url", urlList.get(position));
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context,  imageView, "image");
                context.startActivity(intent, options.toBundle());
            });
        }
    }
}
