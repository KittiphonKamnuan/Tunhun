package com.example.project.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project.R;
import com.example.project.model.MarketNews;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private List<MarketNews> newsList;

    public NewsAdapter(Context context, List<MarketNews> newsList) {
        this.context = context;
        this.newsList = newsList;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        MarketNews news = newsList.get(position);

        holder.headline.setText(news.getHeadline());
        holder.summary.setText(news.getSummary());
        holder.source.setText(news.getSource());

        // Load Image
        if (news.getImage() != null && !news.getImage().isEmpty()) {
            Glide.with(context)
                    .load(news.getImage())
                    .centerCrop()
                    .placeholder(android.R.color.darker_gray)
                    .into(holder.image);
        } else {
            // กรณีไม่มีรูป
            holder.image.setImageResource(android.R.color.darker_gray);
        }

        // Click to open URL
        holder.itemView.setOnClickListener(v -> {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getUrl()));
                context.startActivity(browserIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView headline, summary, source;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.img_news);
            headline = itemView.findViewById(R.id.tv_news_headline);
            summary = itemView.findViewById(R.id.tv_news_summary);
            source = itemView.findViewById(R.id.tv_news_source);
        }
    }
}