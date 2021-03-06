package com.hello_khd_verification_admin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import adapter.Verificationlist_ListAdapter;
import data.Verification_FeedItem;

public class MainActivity extends AppCompatActivity {
    final UserDatabaseHandler udb=new UserDatabaseHandler(this);
    ConnectionDetecter cd;
    Typeface face;
    public List<Verification_FeedItem> feedItems;
    RelativeLayout footerview;
    ImageView heart;
    public int limit = 0;
    ListView list;
    public Verificationlist_ListAdapter listAdapter;
    ImageView nodata;
    ImageView nointernet;
    TextView text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        if (udb.get_userid().equalsIgnoreCase("")) {
            startActivity(new Intent(getApplicationContext(), Registration.class));
            finish();
            return;
        }
        face = Typeface.createFromAsset(getAssets(), "proxibold.otf");
        text = (TextView) findViewById(R.id.text);
        text.setText("Verification Admin");
        text.setTypeface(face);
        heart = (ImageView) findViewById(R.id.heart);
        cd = new ConnectionDetecter(this);
        list = (ListView) findViewById(R.id.list);
        nodata = (ImageView) findViewById(R.id.nodata);
        nointernet = (ImageView) findViewById(R.id.nointernet);
        footerview = (RelativeLayout) getLayoutInflater().inflate(R.layout.footerview, null);
        list.addFooterView(footerview);
        footerview.setVisibility(View.GONE);
        feedItems = new ArrayList();
        listAdapter = new Verificationlist_ListAdapter(this, feedItems);
        list.setAdapter(listAdapter);

        Glide.with(this).load(R.drawable.loading).into(heart);
        nointernet.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (cd.isConnectingToInternet()) {
                    nointernet.setVisibility(View.GONE);
                    limit = 0;
                    new loadstatus().execute(new String[0]);
                    return;
                }
                nointernet.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), Temp.nointernet, Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        try {
            if (cd.isConnectingToInternet()) {
                nointernet.setVisibility(View.GONE);
                limit = 0;
                new loadstatus().execute(new String[0]);
                return;
            }
            nointernet.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), Temp.nointernet, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
        }
    }

    public void removeitem(int position) {
        try {
            feedItems.remove(position);
            listAdapter.notifyDataSetChanged();
        } catch (Exception e) {
        }
    }

    public class loadstatus extends AsyncTask<String, Void, String> {


        public void onPreExecute() {
            nointernet.setVisibility(View.GONE);
            nodata.setVisibility(View.GONE);
            list.setVisibility(View.GONE);
            heart.setVisibility(View.VISIBLE);
            limit = 0;
        }

        public String doInBackground(String... arg0) {

            try {
                String link=Temp.weblink +"getstudentdetails_formarkveri.php";
                String data  = URLEncoder.encode("item", "UTF-8")
                        + "=" + URLEncoder.encode(limit+"", "UTF-8");
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter
                        (conn.getOutputStream());
                wr.write(data);
                wr.flush();
                BufferedReader reader = new BufferedReader
                        (new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while((line = reader.readLine()) != null)
                {
                    sb.append(line);
                }
                return sb.toString();
            } catch (Exception e) {
                return new String("Unable to connect server! Please check your internet connection");
            }
        }


        public void onPostExecute(String result) {
            try {
                Log.w("Reslsdsd",result);
                if (result.trim().contains(":%ok")) {
                    feedItems.clear();
                    String[] got = result.trim().split(":%");
                    int k = (got.length - 1) / 5;
                    int m = -1;
                    for (int i = 1; i <= k; i++) {
                        Verification_FeedItem item = new Verification_FeedItem();
                        m=m+1;
                        item.setSn(got[m]);
                        m=m+1;
                        item.setStudentname(got[m]);
                        m=m+1;
                        item.setMark(got[m]);
                        m=m+1;
                        item.setItemname(got[m]);
                        m=m+1;
                        item.setSchoolname(got[m]);
                        feedItems.add(item);
                    }
                    heart.setVisibility(View.GONE);
                    list.setVisibility(View.VISIBLE);
                    listAdapter.notifyDataSetChanged();
                    return;
                }
                nodata.setVisibility(View.VISIBLE);
                heart.setVisibility(View.GONE);
                footerview.setVisibility(View.GONE);
            } catch (Exception e) {
            }
        }
    }

}

