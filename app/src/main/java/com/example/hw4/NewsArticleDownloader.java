package com.example.hw4;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NewsArticleDownloader implements Runnable{

    NewsService newsService;
    String source;

    NewsArticleDownloader(NewsService newsService, String source) {
        this.newsService = newsService;
        this.source = source;
    }
    private void parseJson(String s) {

        try {
            JSONObject jObjMain = new JSONObject(s);
            JSONArray jArrSources = jObjMain.getJSONArray("articles");
            for (int i =0; i<jArrSources.length(); i++){
                JSONObject jObjSource = jArrSources.getJSONObject(i);
                String author = jObjSource.getString("author");
                String title = jObjSource.getString("title");
                String description = jObjSource.getString("description");
                String url = jObjSource.getString("url");
                String urlToImage = jObjSource.getString("urlToImage");
                String publishedAt = jObjSource.getString("publishedAt");
                newsService.addArticle(new Article(author, title, description, url, urlToImage, publishedAt, jArrSources.length(),i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleResults(String s){
        parseJson(s);
    }

    @Override
    public void run() {

        StringBuilder sb = new StringBuilder();

        try {
            final String prefix = "https://newsapi.org/v2/top-headlines?";
            final String apikey = "63c267b58435414d84bbe4adf5f594ea";
            Uri.Builder buildURL = Uri.parse(prefix).buildUpon();
            buildURL.appendQueryParameter("sources", source);
            buildURL.appendQueryParameter("apiKey", apikey);
            String urlToUse = buildURL.build().toString();
            URL url = new URL(urlToUse);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("User-Agent","");
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(inputStream)));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
                Log.d("article: ", line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        handleResults(sb.toString());

    }
}
