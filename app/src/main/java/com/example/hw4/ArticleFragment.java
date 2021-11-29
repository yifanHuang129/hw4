package com.example.hw4;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ArticleFragment extends Fragment implements Serializable {

    public ArticleFragment() {
    }

    public static ArticleFragment newInstance(Article article) {
        ArticleFragment fragment = new ArticleFragment();
        Bundle args = new Bundle();
        args.putSerializable("article", article);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        assert getArguments() != null;
        outState.putSerializable("article", getArguments().getSerializable("article"));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Article article;
        if (savedInstanceState == null) {
            assert getArguments() != null;
            article = (Article) getArguments().getSerializable("article");
        }
        else
            article = (Article) savedInstanceState.getSerializable("article");

        View v = inflater.inflate(R.layout.fragment, container, false);
        TextView title = (TextView) v.findViewById(R.id.titleView);
        TextView author = (TextView) v.findViewById(R.id.author);
        TextView dateTextView = (TextView) v.findViewById(R.id.date);
        TextView articletext = (TextView) v.findViewById(R.id.articleText);
        TextView index = (TextView) v.findViewById(R.id.index);
        final ImageButton imageButton = (ImageButton) v.findViewById(R.id.image);
        assert article != null;
        title.setText(article.getTitle());
        String aut=article.getAuthor();
        if(aut.equalsIgnoreCase("null")){
            author.setText("");
        }
        else{
            author.setText(article.getAuthor());
        }
        String input = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        String output = "MMM dd, yyyy HH:mm";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat inputFormat = new SimpleDateFormat(input);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat(output);

        Date date;
        String str = null;

        try {
            date = inputFormat.parse(article.getPublishedAt());
            assert date != null;
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert str != null;
        if(str.equalsIgnoreCase("null"))
            dateTextView.setText("");
        else
            dateTextView.setText(str);

        articletext.setText(article.getDescription());
        index.setText(""+article.getIndex()+" of "+(article.getTotal()-90));

        if (article.getUrlToImage() != null){
            Picasso picasso = new Picasso.Builder(v.getContext()).listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception e) {
                    final String changedUrl = article.getUrlToImage().replace("http:", "https:");
                    picasso.load(changedUrl) .error(R.drawable.brokenimage)
                            .placeholder(R.drawable.loading).into(imageButton);
                }
            }).build();
            picasso.load(article.getUrlToImage()) .error(R.drawable.brokenimage)
                    .placeholder(R.drawable.loading) .into(imageButton);
        }else {
            Picasso.get().load(article.getUrlToImage()).error(R.drawable.brokenimage).placeholder(R.drawable.noimage);
        }

        final Intent intent = new Intent((Intent.ACTION_VIEW));
        intent.setData(Uri.parse(article.getUrl()));
        title.setOnClickListener(v1 -> startActivity(intent));
        articletext.setOnClickListener(v2 -> startActivity(intent));
        imageButton.setOnClickListener(v3 -> startActivity(intent));

        return v;

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }
}