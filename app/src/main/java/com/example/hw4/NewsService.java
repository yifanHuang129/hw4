package com.example.hw4;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class NewsService extends Service {

    private ArrayList<Article> articles;
    private ServiceReceiver serviceReceiver;
    private boolean running = true;
    private static NewsService newsService;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public NewsService() {
        newsService = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        articles = new ArrayList<>();
        new Thread(() -> {
            serviceReceiver = new ServiceReceiver();
            registerReceiver(serviceReceiver, new IntentFilter(MainActivity.MSG_TO_SERVICE));
            while (running){
                if (articles.size() == 0 || articles.size() != articles.get(0).getTotal()){
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Intent responseIntent = new Intent();
                    responseIntent.setAction(MainActivity.NEWS_STORY);
                    responseIntent.putExtra("articles", articles);
                    sendBroadcast(responseIntent);
                    articles.clear();
                }
            }
        }).start();
        return START_STICKY;
    }

    public void addArticle(Article article){
        articles.add(article);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            NewsArticleDownloader newsArticleDownloader = new NewsArticleDownloader(newsService, intent.getStringExtra("sources"));
            new Thread(newsArticleDownloader).start();
        }
    }
}
