package com.fortune_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Registration extends AppCompatActivity {
    int PERMISSION_ALL = 1;
    ConnectionDetecter cd;
    Typeface face;
    public EditText name;
    ProgressDialog pd;
    Button register;
    TextView text;
    public String txtname = "";
    final UserDatabaseHandler udb = new UserDatabaseHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);text = (TextView) findViewById(R.id.text);
        name = (EditText) findViewById(R.id.name);
        register = (Button) findViewById(R.id.register);
        cd = new ConnectionDetecter(this);
        pd = new ProgressDialog(this);
        face = Typeface.createFromAsset(getAssets(), "heading.otf");
        text.setText(Temp.appname);
        text.setTypeface(face);

        String[] PERMISSIONS = {android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.CALL_PHONE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA};

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!cd.isConnectingToInternet()) {
                    Toast.makeText(getApplicationContext(), Temp.nointernet, Toast.LENGTH_SHORT).show();
                } else if (name.getText().toString().equalsIgnoreCase("")) {
                    Toast.makeText(getApplicationContext(), "Please enter secret code", Toast.LENGTH_SHORT).show();
                    name.requestFocus();
                } else {
                    pd.setMessage("Please wait...");
                    pd.setCancelable(false);
                    pd.show();
                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                pd.dismiss();
                                Toast.makeText(getApplicationContext(), Temp.tempproblem, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            udb.addfcmid(((InstanceIdResult) task.getResult()).getToken());
                            registration();
                        }
                    });
                }
            }
        });
    }

    public void registration()
    {
        OkHttpClient httpClient = new OkHttpClient();
        String url =Temp.weblink+"registration_admin.php";
        httpClient.newCall(new Request.Builder().url(url).post(new FormBody.Builder()
                .add("secretkey", name.getText().toString())
                .add("fcmid",udb.getfcmid())
                .build()).build()).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                register.setEnabled(true);
                pd.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),Temp.tempproblem,Toast.LENGTH_SHORT).show();
                    }
                });
            }

            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                register.setEnabled(true);
                                pd.dismiss();
                                JSONObject job=new JSONObject(result);
                                JSONObject job1=job.getJSONObject("response");

                                if(job1.getString("status").equalsIgnoreCase("success"))
                                {
                                    udb.adduser("ok");
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();
                                    return;
                                }
                                else if(job1.getString("status").equalsIgnoreCase("error"))
                                {
                                    Toast.makeText(getApplicationContext(), "Please check your secret code", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(), Temp.tempproblem, Toast.LENGTH_SHORT).show();
                                }
                            }
                            catch (Exception a)
                            {

                            }

                        }
                    });
                }
            }
        });
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (!(context == null || permissions == null)) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != 0) {
                    return false;
                }
            }
        }
        return true;
    }
}