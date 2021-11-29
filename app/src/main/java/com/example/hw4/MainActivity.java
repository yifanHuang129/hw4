package com.example.hw4;

import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    static final String MSG_TO_SERVICE = "MSG_TO_SERVICE";
    static final String NEWS_STORY = "NEWS_STORY";
    private NewsReceiver newsReceiver;
    private final ArrayList<Source> SList = new ArrayList<>();
    private final HashMap<String, ArrayList<Source>> Topic = new HashMap<>();
    private final HashMap<String, ArrayList<Source>> Country = new HashMap<>();
    private final HashMap<String, ArrayList<Source>> Language = new HashMap<>();
    private Menu menu_main;
    private DrawerLayout DrawerLayout;
    private ActionBarDrawerToggle DrawerToggle;
    private ListView DrawerList;
    private List<Fragment> fragments;
    private PagerAdapter PagerAdapter;
    private ArrayAdapter<String> arrayAdapter;
    private ViewPager pager;

    private String currentTopic = null;
    private String currentCountry = null;
    private String currentLanguage = null;



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
        DrawerList.setOnItemClickListener(
                (parent, view, position, id) -> {
                    SelectNewsSource(position);
                }
        );
        DrawerToggle = new ActionBarDrawerToggle(this, DrawerLayout, R.string.drawer_open, R.string.drawer_close);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        PagerAdapter = new PagerAdapter(getSupportFragmentManager());
        fragments = new ArrayList<>();
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

        SubMenu subMenu = item.getSubMenu();

        if (subMenu == null) {

            int id = item.getItemId();
            if (id == 0) {
                currentTopic = item.getTitle().toString();
                ArrayList<Source> topics = Topic.get(currentTopic);
                setTitle(String.format("%s (%s)", getString(R.string.app_name), topics.size()));
                SList.clear();
                arrayAdapter.notifyDataSetChanged();
                for (Source s : topics) {
                    SList.add(s);
                }
            }
            else if (id == 1) {
                currentLanguage = item.getTitle().toString();
                ArrayList<Source> languages = Language.get(currentLanguage);
                SList.clear();
                arrayAdapter.notifyDataSetChanged();

                List<Source> filteredLanguages = new ArrayList<>();
                if (currentTopic != null) {
                    for(Source s: languages){
                        if(s.getCategory().equalsIgnoreCase(currentTopic))
                            filteredLanguages.add(s);
                    }
                } else {
                    filteredLanguages = new ArrayList<>(languages);
                }
                setTitle(String.format("%s (%s)", getString(R.string.app_name), filteredLanguages.size()));
                for (Source s : filteredLanguages) {
                    SList.add(s);
                }
            }
            else if (id == 2) {
                currentCountry = item.getTitle().toString();
                ArrayList<Source> countries = Country.get(currentCountry);
                SList.clear();
                arrayAdapter.notifyDataSetChanged();

                List<Source> filteredCountries = new ArrayList<>();
                if (currentLanguage != null && !currentLanguage.equalsIgnoreCase("All")) {
                    for(Source s: countries){
                        if(s.getLanguage().equalsIgnoreCase(currentLanguage))
                            filteredCountries.add(s);
                    }
                } else {
                    filteredCountries = new ArrayList<>(countries);
                }
                setTitle(String.format("%s (%s)", getString(R.string.app_name), filteredCountries.size()));
                for (Source s : filteredCountries) {
                    SList.add(s);
                }
            }
        }

        arrayAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(item);
    }


    public void getData(ArrayList<Source> list) {
        for (Source s : list) {
            if (s.getCategory().isEmpty()) {
                s.setCategory("Unspecified");
            }
            if (s.getLanguage().isEmpty()) {
                s.setLanguage("Unspecified");
            }
            if (s.getCountry().isEmpty()) {
                s.setCountry("Unspecified");
            }
            if (!Topic.containsKey(s.getCategory())) {
                Topic.put(s.getCategory(), new ArrayList<>());
            }

            String jLanguage = ConvertJtoS(getResources(), R.raw.language_codes);

            try {
                JSONObject jsonObject = new JSONObject(jLanguage);
                JSONArray jsonArray = jsonObject.getJSONArray("languages");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jObj = jsonArray.getJSONObject(i);
                    if (jObj.getString("code").toLowerCase(Locale.ROOT).equalsIgnoreCase(s.getLanguage())) {
                        if (!Language.containsKey(jObj.getString("name"))) {
                            Language.put(jObj.getString("name"), new ArrayList<>());
                        }
                        Objects.requireNonNull(Language.get(jObj.getString("name"))).add(s);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String jCountries = ConvertJtoS(getResources(), R.raw.country_codes);
            try {
                JSONObject jsonObject = new JSONObject(jCountries);
                JSONArray jsonArray = jsonObject.getJSONArray("countries");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jObj = jsonArray.getJSONObject(i);
                    if (jObj.getString("code").toLowerCase(Locale.ROOT).equalsIgnoreCase(s.getCountry())) {
                        if (!Country.containsKey(jObj.getString("name"))) {
                            Country.put(jObj.getString("name"), new ArrayList<>());
                        }
                        Objects.requireNonNull(Country.get(jObj.getString("name"))).add(s);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ArrayList<Source> arr = Topic.get(s.getCategory());
            if (arr != null) {
                arr.add(s);
            }
        }
        Topic.put("all", new ArrayList<>(list));
        Country.put("all", new ArrayList<>(list));
        Language.put("all", new ArrayList<>(list));


        ArrayList<String> topicList = new ArrayList<>(Topic.keySet());
        ArrayList<String> countryList = new ArrayList<>(Country.keySet());
        ArrayList<String> languageList = new ArrayList<>(Language.keySet());


        SubMenu topicsSubMenu    = menu_main.addSubMenu(0,0,0,"Topics");
        SubMenu languagesSubMenu = menu_main.addSubMenu(0,1,0,"Languages");
        SubMenu countriesSubMenu = menu_main.addSubMenu(0,2,0,"Countries");


        for (String s  : topicList) {
            if (s.equalsIgnoreCase("general"))
                menu_main.getItem(0).getSubMenu().add(makeButton(s, "#f1b541"));
            else if (s.equalsIgnoreCase("sports"))
                menu_main.getItem(0).getSubMenu().add(makeButton(s,"#a9a6e0"));
            else if (s.equalsIgnoreCase("science"))
                menu_main.getItem(0).getSubMenu().add(makeButton(s,"#0CB1BB"));
            else if (s.equalsIgnoreCase("health"))
                menu_main.getItem(0).getSubMenu().add(makeButton(s,"#8B008B"));
            else if (s.equalsIgnoreCase("business"))
                menu_main.getItem(0).getSubMenu().add(makeButton(s,"#008000"));
            else if (s.equalsIgnoreCase("entertainment"))
                menu_main.getItem(0).getSubMenu().add(makeButton(s,"#FF0000"));
            else if (s.equalsIgnoreCase("technology"))
                menu_main.getItem(0).getSubMenu().add(makeButton(s, "#FF1493"));
            else
                menu_main.getItem(0).getSubMenu().add(makeButton(s, "default"));
        }

        for (int i = 0; i < languageList.size(); i++) {
            languagesSubMenu.add(Menu.NONE, 1, i, languageList.get(i));
        }

        for (int i = 0; i < countryList.size(); i++) {
            countriesSubMenu.add(Menu.NONE, 2, i, countryList.get(i));
        }

        SList.addAll(list);
        DrawerList.setAdapter(new SourceAdapter(this, R.layout.drawer_list, SList));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    public SpannableStringBuilder makeButton(String s, String color){

        if(color.equalsIgnoreCase("default"))
            color = "#303F9F";
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString Spannable = new SpannableString(s);
        Spannable.setSpan(new AbsoluteSizeSpan(50), 0, s.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        Spannable.setSpan(new ForegroundColorSpan(Color.parseColor(color)), 0, s.length(), 0);
        builder.append(Spannable);
        return builder;
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


    //The following method convert Json file to String
    public String ConvertJtoS(Resources resources, int id){
        InputStream Reader = resources.openRawResource(id);
        Writer writer = new StringWriter();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Reader, "UTF-8"));
            String line = reader.readLine();
            while (line != null) {
                writer.write(line);
                line = reader.readLine();
            }
        } catch (Exception e) {
            Log.e(TAG, "Unhandled exception while using JSONResourceReader", e);
        } finally {
            try {
                Reader.close();
            } catch (Exception e) {
                Log.e(TAG, "Unhandled exception while using JSONResourceReader", e);
            }
        }
        String jsonString = writer.toString();
        return jsonString;
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