package com.ahmed.newssearch;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListAdapter.ViewHolder> {
    private ArrayList<Article> articles;
    private SpeakerListener speakerListener;
    private ArticleListener articleListener;

    private static final String tag = "ArticleListAdapter";

    public ArticleListAdapter(ArrayList<Article> articles, SpeakerListener speakerListener,
                              ArticleListener articleListener) {
        this.articles = articles;
        this.speakerListener = speakerListener;
        this.articleListener = articleListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout articlePanel;
        private TextView titleText;
        private TextView descriptionText;
        private ImageButton speaker;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            articlePanel = itemView.findViewById(R.id.articlePanel);
            titleText = itemView.findViewById(R.id.titleText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            speaker = itemView.findViewById(R.id.speaker_button);
        }
    }

    @NonNull
    @Override
    public ArticleListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_card, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleListAdapter.ViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.titleText.setText(article.getTitle());
        holder.descriptionText.setText(article.getDescription());
        holder.speaker.setOnClickListener((View view) -> speakerListener.onSpeakerPressed(article.getDescription()));
        holder.articlePanel.setOnClickListener((View view) -> articleListener.onArticlePressed(article.getUrl()));
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }
}
