package com.hello_khd_verification_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import es.dmoral.toasty.Toasty;

public class Registration extends AppCompatActivity {
    int PERMISSION_ALL = 1;
    ConnectionDetecter cd;
    Typeface face;
    public EditText username,password;
    ProgressDialog pd;
    Button register;
    TextView text;
    public String txtusername = "",txtpassword="";
    final UserDatabaseHandler udb = new UserDatabaseHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        text = (TextView) findViewById(R.id.text);
        username = (EditText) findViewById(R.id.username);
        password=findViewById(R.id.password);
        register = (Button) findViewById(R.id.register);
        cd = new ConnectionDetecter(this);
        pd = new ProgressDialog(this);
        face = Typeface.createFromAsset(getAssets(), "proxibold.otf");
        text.setText("Verification Admin");
        text.setTypeface(face);
        username.setTypeface(face);
        password.setTypeface(face);
        register.setTypeface(face);
        FirebaseApp.initializeApp(this);
        String[] PERMISSIONS = {android.Manifest.permission.INTERNET,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.CALL_PHONE,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION};

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!cd.isConnectingToInternet()) {
                    Toasty.info(getApplicationContext(), Temp.nointernet, 0).show();
                } else if (username.getText().toString().equalsIgnoreCase("")) {
                    Toasty.info(getApplicationContext(), "Please enter username ", 1).show();
                    username.requestFocus();
                }
                else if (password.getText().toString().equalsIgnoreCase("")) {
                    Toasty.info(getApplicationContext(), "Please enter password ", 1).show();
                    password.requestFocus();
                }else {
                    pd.setMessage("Please wait...");
                    pd.setCancelable(false);
                    pd.show();
                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                pd.dismiss();
                                Toasty.info(getApplicationContext(), "താല്‍ക്കാലിക പ്രശ്‌നം ! ദയവായി 10 മിനിട്ടിന് ശേഷം ശ്രമിക്കുക ", 1).show();
                                return;
                            }
                            udb.addfcmid(((InstanceIdResult) task.getResult()).getToken());
                            txtusername = username.getText().toString();
                            txtpassword=password.getText().toString();
                            new registration().execute(new String[0]);
                        }
                    });
                }
            }
        });
    }

    public class registration extends AsyncTask<String, Void, String> {
        public void onPreExecute() {
            register.setEnabled(false);
        }

        public String doInBackground(String... arg0) {
            try {
                String link = Temp.weblink + "registrationveeriadmin.php";
                String data = URLEncoder.encode("item", "UTF-8")
                        + "=" + URLEncoder.encode(txtusername + ":%" + txtpassword+":%"+udb.getfcmid(), "UTF-8");
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
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            } catch (Exception e) {
                return new String("Unable to connect server! Please check your internet connection");
            }
        }

        public void onPostExecute(String result) {
            try {
                register.setEnabled(true);
                pd.dismiss();
                if (result.trim().contains(",ok")) {
                    String[] k = result.trim().split(",");
                    udb.adduser(k[0]);
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                    return;
                }
                if (result.contains("error")) {
                    Toasty.info(getApplicationContext(), "ക്ഷമിക്കണം !!! താങ്കള്‍ എന്റര്‍ ചെയ്ത വിവരങ്ങള്‍ തെറ്റാണ്‌ ", 0).show();
                } else {
                    Toasty.info(getApplicationContext(), Temp.tempproblem, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
            }
        }
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