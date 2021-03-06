package com.footbale_admin;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    ConnectionDetecter cd;
    final DatabaseHandler db = new DatabaseHandler(this);
    Button instagramtofacebook;
    Button uplaodtochintha;
    ProgressDialog pd;
    ImageView notifootball;

    Button actress,uploadactress,vijay,uploadvijay,narendramodi,uploadnarendramodi,amith,uploadamith,alluarjun,uploadalluarjun,dulqar,uploaddulqar;
    ImageView actressnoti,vijaynoti,narendramodinoti,amothnoti,alluarjunnoti,dulqarnoti;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_main);
        uplaodtochintha = (Button) findViewById(R.id.uplaodtochintha);
        instagramtofacebook = (Button) findViewById(R.id.instagramtofacebook);
        notifootball=findViewById(R.id.notifootball);
        actress=findViewById(R.id.actress);
        uploadactress=findViewById(R.id.uploadactress);
        vijay=findViewById(R.id.vijay);
        uploadvijay=findViewById(R.id.uploadvijay);
        narendramodi=findViewById(R.id.narendramodi);
        uploadnarendramodi=findViewById(R.id.uploadnarendramodi);
        amith=findViewById(R.id.amith);
        uploadamith=findViewById(R.id.uploadamith);
        alluarjun=findViewById(R.id.alluarjun);
        uploadalluarjun=findViewById(R.id.uploadalluarjun);
        dulqar=findViewById(R.id.dulqar);
        uploaddulqar=findViewById(R.id.uploaddulqar);
        actressnoti=findViewById(R.id.actressnoti);
        vijaynoti=findViewById(R.id.vijaynoti);
        narendramodinoti=findViewById(R.id.narendramodinoti);
        amothnoti=findViewById(R.id.amothnoti);
        alluarjunnoti=findViewById(R.id.alluarjunnoti);
        dulqarnoti=findViewById(R.id.dulqarnoti);

        cd = new ConnectionDetecter(this);
        pd = new ProgressDialog(this);
        if (db.getscreenwidth().equalsIgnoreCase("")) {
            int width = getResources().getDisplayMetrics().widthPixels;
            db.addscreenwidth(width+"");
        }
        if (db.getuserid().equalsIgnoreCase("")) {
            startActivity(new Intent(getApplicationContext(), Registration.class));
            finish();
        }
        uplaodtochintha.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showalert1("Are you sure want to upload app ?");
            }
        });

        actressnoti.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Tempvariable.feedtype=1;
                Tempvariable.feedtopic= Tempvariable.actress_topic;
                showalertnoti_extra("Are you sure want to notify?");
            }
        });

        vijaynoti.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Tempvariable.feedtype=2;
                Tempvariable.feedtopic= Tempvariable.vijay_topic;
                showalertnoti_extra("Are you sure want to notify?");

            }
        });

        narendramodinoti.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Tempvariable.feedtype=3;
                Tempvariable.feedtopic= Tempvariable.narendramodi_topic;
                showalertnoti_extra("Are you sure want to notify?");

            }
        });
        amothnoti.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Tempvariable.feedtype=5;
                Tempvariable.feedtopic= Tempvariable.amith_topic;
                showalertnoti_extra("Are you sure want to notify?");
            }
        });

        alluarjunnoti.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Tempvariable.feedtype=4;
                Tempvariable.feedtopic= Tempvariable.alluarjun_topic;
                showalertnoti_extra("Are you sure want to notify?");
            }
        });
        dulqarnoti.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Tempvariable.feedtype=6;
                Tempvariable.feedtopic= Tempvariable.dulqar_topic;
                showalertnoti_extra("Are you sure want to notify?");

            }
        });

        instagramtofacebook.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), InstagramToFacebook.class));
            }
        });
        notifootball.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                showalertnoti("Are you sure want to notify?");
            }
        });

    }

    public void showalert1(String message) {
        Builder builder = new Builder(this);
        builder.setMessage(message).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (cd.isConnectingToInternet()) {
                    new uploadtochintha().execute(new String[0]);
                } else {
                    Toast.makeText(getApplicationContext(), "Please make sure your internet connection is active", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    public class uploadtochintha extends AsyncTask<String, Void, String> {
        public void onPreExecute() {
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.show();
        }
        public String doInBackground(String... arg0) {
            try {
                String link=Tempvariable.weblink+"uploadpagefeedtoapp.php";
                String data  = URLEncoder.encode("item", "UTF-8")
                        + "=" + URLEncoder.encode("", "UTF-8");
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
            if (pd != null || pd.isShowing()) {
                pd.dismiss();
                if (result.contains("ok")) {
                    Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }





    public void showalertnoti(String message) {
        Builder builder = new Builder(this);
        builder.setMessage(message).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (cd.isConnectingToInternet()) {
                    new noti().execute(new String[0]);
                } else {
                    Toast.makeText(getApplicationContext(), "Please make sure your internet connection is active", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    public class noti extends AsyncTask<String, Void, String> {
        public void onPreExecute() {
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.show();
        }
        public String doInBackground(String... arg0) {
            try {
                String link=Tempvariable.weblink+"sendnoti.php";
                String data  = URLEncoder.encode("item", "UTF-8")
                        + "=" + URLEncoder.encode("", "UTF-8");
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
            if (pd != null || pd.isShowing()) {
                pd.dismiss();
                if (result.contains("ok")) {
                    Toast.makeText(getApplicationContext(), "Notified", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    public void showalertnoti_extra(String message) {
        Builder builder = new Builder(this);
        builder.setMessage(message).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (cd.isConnectingToInternet()) {
                    new noti_extra().execute(new String[0]);
                } else {
                    Toast.makeText(getApplicationContext(), "Please make sure your internet connection is active", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    public class noti_extra extends AsyncTask<String, Void, String> {
        public void onPreExecute() {
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.show();
        }
        public String doInBackground(String... arg0) {
            try {
                String link=Tempvariable.weblink+"sendnoti_extra.php";
                String data  = URLEncoder.encode("item", "UTF-8")
                        + "=" + URLEncoder.encode("", "UTF-8");
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
            if (pd != null || pd.isShowing()) {
                pd.dismiss();
                if (result.contains("ok")) {
                    Toast.makeText(getApplicationContext(), "Notified", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
