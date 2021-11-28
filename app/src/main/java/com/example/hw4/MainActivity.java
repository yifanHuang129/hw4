package com.example.hw4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final String MSG_TO_SERVICE = "MSG_TO_SERVICE";
    static final String NEWS_STORY = "NEWS_STORY";
    private NewsReceiver newsReceiver;
    private final ArrayList<Source> SList = new ArrayList<>();
    private final HashMap<String, ArrayList<Source>> sources = new HashMap<>();
    private Menu menu_main;
    private DrawerLayout DrawerLayout;
    private ActionBarDrawerToggle DrawerToggle;
    private ListView DrawerList;
    private List<Fragment> fragments;
    private PagerAdapter PagerAdapter;
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(MainActivity.this, NewsService.class);
        startService(intent);
        newsReceiver = new NewsReceiver();
        registerReceiver(newsReceiver, new IntentFilter(NEWS_STORY));
        registerReceiver(newsReceiver, new IntentFilter(MSG_TO_SERVICE));
        DrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        DrawerList = (ListView) findViewById(R.id.drawer);
        DrawerToggle = new ActionBarDrawerToggle(this,DrawerLayout,R.string.drawer_open, R.string.drawer_close);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        DrawerList.setOnItemClickListener((parent, view, position, id) -> SelectNewsSource(position));
        fragments = new ArrayList<>();
        PagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(PagerAdapter);
        if (savedInstanceState != null){
            fragments = (List<Fragment>) savedInstanceState.getSerializable("fragments");
            setTitle(savedInstanceState.getString("title"));
            PagerAdapter.notifyDataSetChanged();
            for (int i = 0; i< PagerAdapter.getCount(); i++)
                PagerAdapter.notifyChangeInPosition(i);
        }

        NewsSourceDownloader newsSourceDownloader = new NewsSourceDownloader(MainActivity.this);
        new Thread(newsSourceDownloader).start();

    }

    @Override
    protected void onResume() {
        registerReceiver(newsReceiver, new IntentFilter(NEWS_STORY));
        super.onResume();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(newsReceiver);
        super.onStop();
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        private long baseId = 0;
        public PagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public long getItemId(int position) {
            return baseId+position;
        }

        public void notifyChangeInPosition(int n) {
            baseId += getCount() + n;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("fragments", (Serializable) fragments);
        outState.putString("title",getTitle().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        DrawerToggle.syncState();
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu_main = menu;
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu_main = menu;
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (DrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        String str = item.getTitle().toString();
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString redSpannable = new SpannableString(item.getTitle().toString());
        redSpannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, str.length(), 0);
        builder.append(redSpannable);
        setTitle(builder);
        SList.clear();
        ArrayList<Source> list = sources.get(item.getTitle().toString());
        if (list != null) {
            SList.addAll(list);
        }

        ((ArrayAdapter) DrawerList.getAdapter()).notifyDataSetChanged();

        return super.onOptionsItemSelected(item);
    }
    public void getData(ArrayList<Source> list) {

        for (Source s : list) {
            if (s.getCategory().isEmpty()) {
                s.setCategory("Unspecified");
            }
            if (!sources.containsKey(s.getCategory())) {
                sources.put(s.getCategory(), new ArrayList<>());
            }
            ArrayList<Source> arr = sources.get(s.getCategory());
            if (arr != null) {
                arr.add(s);
            }
        }
        sources.put("all", list);


        ArrayList<String> sourceslist = new ArrayList<>(sources.keySet());
        for (String s  : sourceslist) {
            if (s.equalsIgnoreCase("general")) {
                SpannableStringBuilder builder = new SpannableStringBuilder();
                SpannableString Spannable = new SpannableString(s);
                Spannable.setSpan(new AbsoluteSizeSpan(50), 0, s.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                Spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#f1b541")), 0, s.length(), 0);
                builder.append(Spannable);
                menu_main.add(builder);
            }
            else if (s.equalsIgnoreCase("sports")) {
                SpannableStringBuilder builder = new SpannableStringBuilder();
                SpannableString Spannable = new SpannableString(s);
                Spannable.setSpan(new AbsoluteSizeSpan(50), 0, s.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                Spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#a9a6e0")), 0, s.length(), 0);
                builder.append(Spannable);
                menu_main.add(builder);
            }
            else if (s.equalsIgnoreCase("science")){
                SpannableStringBuilder builder = new SpannableStringBuilder();
                SpannableString Spannable = new SpannableString(s);
                Spannable.setSpan(new AbsoluteSizeSpan(50), 0, s.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                Spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#0CB1BB")), 0, s.length(), 0);
                builder.append(Spannable);
                menu_main.add(builder);

            }
            else if (s.equalsIgnoreCase("health")) {
                SpannableStringBuilder builder = new SpannableStringBuilder();
                SpannableString Spannable = new SpannableString(s);
                Spannable.setSpan(new AbsoluteSizeSpan(50), 0, s.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                Spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#8B008B")), 0, s.length(), 0);
                builder.append(Spannable);
                menu_main.add(builder);
            }
            else if (s.equalsIgnoreCase("business")){
                SpannableStringBuilder builder = new SpannableStringBuilder();
                SpannableString Spannable = new SpannableString(s);
                Spannable.setSpan(new AbsoluteSizeSpan(50), 0, s.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                Spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#008000")), 0, s.length(), 0);
                builder.append(Spannable);
                menu_main.add(builder);
            }
            else if (s.equalsIgnoreCase("entertainment")){
                SpannableStringBuilder builder = new SpannableStringBuilder();
                SpannableString Spannable = new SpannableString(s);
                Spannable.setSpan(new AbsoluteSizeSpan(50), 0, s.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                Spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF0000")), 0, s.length(), 0);
                builder.append(Spannable);
                menu_main.add(builder);
            }

            else if (s.equalsIgnoreCase("technology")){
                SpannableStringBuilder builder = new SpannableStringBuilder();
                SpannableString Spannable = new SpannableString(s);
                Spannable.setSpan(new AbsoluteSizeSpan(50), 0, s.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                Spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF1493")), 0, s.length(), 0);
                builder.append(Spannable);
                menu_main.add(builder);
            }
            else {
                SpannableStringBuilder builder = new SpannableStringBuilder();
                SpannableString Spannable = new SpannableString(s);
                Spannable.setSpan(new AbsoluteSizeSpan(50), 0, s.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                Spannable.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s.length(), 0);
                builder.append(Spannable);
                menu_main.add(builder);
            }
        }
        SList.addAll(list);
        DrawerList.setAdapter(new SourceAdapter(this, R.layout.drawer_list, SList));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    public class NewsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (NEWS_STORY.equals(intent.getAction())) {
                ArrayList<Article> articles = (ArrayList<Article>) intent.getSerializableExtra("articles");
                if(fragments == null){
                }
                fragments.clear();

                assert articles != null;
                int x = articles.size() - 90;
                for (int i = 0; i < x; i++) {
                    fragments.add(ArticleFragment.newInstance(articles.get(i)));
                    PagerAdapter.notifyChangeInPosition(i);
                }
                PagerAdapter.notifyDataSetChanged();
                pager.setCurrentItem(0);
            }
        }
    }

    private void SelectNewsSource(int position) {
        setTitle(SList.get(position).getName());
        Intent requestIntent = new Intent();
        requestIntent.setAction(MainActivity.MSG_TO_SERVICE);
        requestIntent.putExtra("sources", SList.get(position).getId());
        sendBroadcast(requestIntent);
        DrawerLayout.closeDrawer(DrawerList);
    }


}