package com.example.hw4;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class NewsSourceDownloader implements Runnable {

    private MainActivity mainActivity;

    public NewsSourceDownloader(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {

        StringBuilder sb = new StringBuilder();

        try {
//          String prefix = "https://newsapi.org/v2/sources?language=en&country=us&category=";
            String prefix  = "https://newsapi.org/v2/sources?";
            String apikey = "&apiKey=6bc7a6c09b3b4d11bcddcfb9b1eff8e8";
            URL url = new URL(prefix + apikey);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("User-Agent","");
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(inputStream)));
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
        handleResults(sb.toString());

    }
    private void handleResults(String s){
        final ArrayList<Source> allSources = parseJSON(s);
        mainActivity.runOnUiThread(() -> {
            if(allSources != null){
                mainActivity.getData(allSources);
            }
        });
    }

    private ArrayList<Source> parseJSON(String s) {
        ArrayList<Source> arr = new ArrayList<>();
        try {
            JSONObject jObjMain = new JSONObject(s);
            JSONArray jArrSources = jObjMain.getJSONArray("sources");
            for (int i =0; i<jArrSources.length(); i++){
                JSONObject jObjSource = jArrSources.getJSONObject(i);
                String id = jObjSource.getString("id");
                String name = jObjSource.getString("name");
                String url = jObjSource.getString("url");
                String category = jObjSource.getString("category");
                String country = jObjSource.getString("country");
                String language = jObjSource.getString("language");
                arr.add(new Source(id,name,url,category, country, language));
            }
            return arr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}