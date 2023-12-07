package com.ahmed.newssearch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String tag = "MainActivity";
    private ArrayList<Article> articles = new ArrayList<Article>();
    private RecyclerView articleList;
    private final Calendar calendar = Calendar.getInstance();
    private EditText searchBar;
    private EditText datePicker;
    private TextView noResults;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String endpointBase =
            "https://newsapi.org/v2/everything?language=en&searchIn=title&sortBy=relevancy";
    private static final String API_key = BuildConfig.NEWS_API_KEY;
    private final OkHttpClient client = new OkHttpClient();
    private TextToSpeech tts;
    private final SimpleDateFormat textFieldFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
    private final SimpleDateFormat apiFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        articleList = findViewById(R.id.articleList);
        datePicker = findViewById(R.id.datePicker);
        searchBar = findViewById(R.id.searchView);
        noResults = findViewById(R.id.no_results);
        tts = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            tts.setLanguage(Locale.UK);
                        }
                    }
                });
        setDatePicking();
        setFindButton();
        setArticleAdapter();
    }

    private void setDatePicking() {
        DatePickerDialog.OnDateSetListener date = (DatePicker view, int year, int month, int day) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                datePicker.setText(textFieldFormatter.format(calendar.getTime()));
        };

        datePicker.setOnClickListener((View view) -> {
            new DatePickerDialog(MainActivity.this,
                    date,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setArticleAdapter() {
        SpeakerListener speakerListener = (String text) -> {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        };

        ArticleListener articleListener = (String url) -> {
            Intent i = new Intent(MainActivity.this, WebActivity.class);
            i.putExtra("url", url);
            startActivity(i);
        };

        ArticleListAdapter adapter = new ArticleListAdapter(articles, speakerListener, articleListener);
        articleList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        articleList.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividers = new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL);
        articleList.addItemDecoration(dividers);
        articleList.setAdapter(adapter);
    }

    private void setFindButton() {
        Button button = findViewById(R.id.find_button);
        button.setOnClickListener((View view) -> makeRequest());
    }


    private void makeRequest() {
        articles.clear();
        String keyword = searchBar.getText().toString();
        String dateString = datePicker.getText().toString();
        String date = null;
        try {
            date = apiFormatter.format(textFieldFormatter.parse(dateString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String url = endpointBase + "&q=" + keyword + "&from=" + date + "&to=" + date;

        executorService.execute(() -> {
            Request request = new Request.Builder().url(url)
                    .addHeader("X-Api-Key", API_key).build();

            try (Response response = client.newCall(request).execute()) {
                JSONObject jsonObject = new JSONObject(response.body().string());

                if (jsonObject.get("status").equals("ok")) {
                    JSONArray articles = jsonObject.getJSONArray("articles");

                    for (int i = 0; i < articles.length(); i++) {
                        JSONObject articleObject = articles.getJSONObject(i);
                        String title = articleObject.get("title").toString();
                        String description = articleObject.get("author").toString();

                        if (description.equals("null")) {
                            description = "";
                        }

                        String article_url = articleObject.get("url").toString();
                        this.articles.add(new Article(title, description, article_url));
                    }
                }

                MainActivity.this.runOnUiThread(this::updateUIOnResults);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateUIOnResults() {
        articleList.getAdapter().notifyDataSetChanged();
        if (articles.size() == 0) {
            articleList.setVisibility(View.INVISIBLE);
            noResults.setVisibility(View.VISIBLE);
        } else {
            articleList.setVisibility(View.VISIBLE);
            noResults.setVisibility(View.INVISIBLE);
        }
    }
}