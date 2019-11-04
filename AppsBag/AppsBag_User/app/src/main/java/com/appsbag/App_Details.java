package com.appsbag;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.takwolf.android.hfrecyclerview.HeaderAndFooterRecyclerView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import adapter.AppHelpList_Adapter;
import adapter.AppList_Adapter;
import data.AppHelplist_Feed;
import data.Applist_Feed;

public class App_Details extends AppCompatActivity {
    ImageView back, heart;
    TextView text, headertext, footertext;
    Button applink;
    Typeface face;
    ConnectionDetecter cd;
    public List<AppHelplist_Feed> feeditem;
    public AppHelpList_Adapter adapter;
    HeaderAndFooterRecyclerView recyclerview;
    public int limit = 0;
    ImageView nointernet;
    View headerview;
    View footerview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app__details);
        face = Typeface.createFromAsset(getAssets(), "font/proxibold.otf");
        back = findViewById(R.id.back);
        heart = findViewById(R.id.heart);
        text = findViewById(R.id.text);
        applink = findViewById(R.id.applink);
        nointernet=findViewById(R.id.nointernet);
        cd = new ConnectionDetecter(this);
        recyclerview = findViewById(R.id.recyclerview);
        text.setTypeface(face);
        text.setText(Temp.appname);
        applink.setTypeface(face);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });

        applink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Temp.appurl));
                startActivity(browserIntent);
            }
        });

        Glide.with(this).load(R.drawable.loading).into(heart);

        feeditem = new ArrayList();
        adapter = new AppHelpList_Adapter(this, feeditem);

        recyclerview.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerview.setAdapter(adapter);


        footerview = LayoutInflater.from(this).inflate(R.layout.detailsfooter, recyclerview.getFooterContainer(), false);
        recyclerview.addFooterView(footerview);

        headerview = LayoutInflater.from(this).inflate(R.layout.detailsheader, recyclerview.getHeaderContainer(), false);
        recyclerview.addHeaderView(headerview);

        headertext = headerview.findViewById(R.id.headertext);
        footertext = footerview.findViewById(R.id.footertext);
        headertext.setTypeface(face);
        footertext.setTypeface(face);
        headertext.setText(Temp.appheader);
        footertext.setText(Temp.appfooter);

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
        if (cd.isConnectingToInternet()) {
            nointernet.setVisibility(View.GONE);
            limit = 0;
            new loadstatus().execute(new String[0]);
        }
        else
        {
            nointernet.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), Temp.nointernet, Toast.LENGTH_SHORT).show();
        }

    }

    public class loadstatus extends AsyncTask<String, Void, String> {
        public void onPreExecute() {
            recyclerview.setVisibility(View.INVISIBLE);
            heart.setVisibility(View.VISIBLE);
            limit = 0;
        }
        public String doInBackground(String... arg0) {

            try {

                String link= Temp.weblink +"getapphelpdetails.php";
                String data  = URLEncoder.encode("item", "UTF-8")
                        + "=" + URLEncoder.encode(Temp.appid, "UTF-8");
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
                if (result.contains("%%ok")) {
                    String[] got = result.trim().split("%%");
                    int k = (got.length - 1) / 4;
                    int m = -1;
                    for (int i = 1; i <= k; i++) {
                        AppHelplist_Feed item = new AppHelplist_Feed();
                        m=m+1;
                        item.setSn(got[m]);
                        m=m+1;
                        item.setMalayalam(got[m]);
                        m=m+1;
                        item.setFbdim(got[m]);
                        m=m+1;
                        item.setImageid(got[m]);
                        feeditem.add(item);
                    }
                    recyclerview.setVisibility(View.VISIBLE);
                    heart.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    return;
                }
                heart.setVisibility(View.GONE);
            } catch (Exception e) {
            }
        }
    }


}