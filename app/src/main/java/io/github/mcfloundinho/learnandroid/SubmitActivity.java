package io.github.mcfloundinho.learnandroid;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

public class SubmitActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        Bundle extras = getIntent().getExtras();
        new FetchTask().execute(extras);

    }


    public class FetchTask extends AsyncTask<Bundle, Void, String[]> {
        @Override
        protected String[] doInBackground(Bundle... params) {
            final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";
            final String QUERY_PARAM = "q";
            final String UNIT_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APPID_PARAM = "appid";
            final String unit = "metric";
            final String appid = "da97c207308d79c59486efe3b3a401fb";
            final String city = params[0].getString("CITY");
            final int numDays = params[0].getInt("NUMBER");

            try {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String jsonStr = null;

                Uri uri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, city)
                        .appendQueryParameter(UNIT_PARAM, unit)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, appid)
                        .build();

                URL url = new URL(uri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                jsonStr = buffer.toString();
                urlConnection.disconnect();
                reader.close();
                return parseJson(jsonStr);
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error occurred during fetching", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] newData) {
            if (newData != null) {
                mAdapter = new MyAdapter(newData);
                mRecyclerView.setAdapter(mAdapter);
            }
        }

        private String dtToStr(long dt) {
            return new SimpleDateFormat("EEE, MM/dd").format(dt * 1000);
        }

        private String[] parseJson(String jsonStr) throws JSONException {
            JSONObject jsonObj = new JSONObject(jsonStr);
            JSONArray dataArray = jsonObj.getJSONArray("list");
            String[] results = new String[dataArray.length()];
            for (int i = 0; i < dataArray.length(); ++i) {
                JSONObject dataEntry = dataArray.getJSONObject(i);
                long dt = dataEntry.getLong("dt");
                String weather = dataEntry.getJSONArray("weather").getJSONObject(0).getString("main");
                double tempLow = dataEntry.getJSONObject("temp").getDouble("min");
                double tempHigh = dataEntry.getJSONObject("temp").getDouble("max");
                results[i] = dtToStr(dt) + ": " + weather + ", " +
                        Long.toString(Math.round(tempLow)).toString() + "~" +
                        Long.toString(Math.round(tempHigh)).toString() + "°C";
            }
            return results;
        }
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private String[] mDataset;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;
            public ViewHolder(TextView v) {
                super(v);
                mTextView = v;
            }
        }

        public MyAdapter(String[] myDataset) {
            mDataset = myDataset;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_textview, parent, false);
            ViewHolder vh = new ViewHolder((TextView)v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mTextView.setText(mDataset[position]);
        }

        @Override
        public int getItemCount() {
            return mDataset.length;
        }
    }

}
