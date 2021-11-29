package com.example.hw4;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
            String prefix = "https://newsapi.org/v2/top-headlines?sources=";
            String apikey = "&pageSize=100&apiKey=33d5565ffe264727a70ac001cb03a0b4";
            URL url = new URL(prefix +source+ apikey);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("User-Agent","");
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(inputStream)));
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        handleResults(sb.toString());

    }
}
