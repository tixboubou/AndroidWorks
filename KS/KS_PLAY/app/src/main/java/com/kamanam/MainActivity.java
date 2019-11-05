package com.kamanam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    ImageView positions,web,whatsapp,loan;
    TextView text,help1,txtweb,txtwhatsapp;
    Typeface face,face1;
    int PERMISSION_ALL = 1;
    final DataBase db=new DataBase(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        positions=findViewById(R.id.positions);
        help1=findViewById(R.id.help1);
        text=findViewById(R.id.text);
        web=findViewById(R.id.web);
        loan=findViewById(R.id.loan);
        whatsapp=findViewById(R.id.whatsapp);
        txtweb=findViewById(R.id.txtweb);
        txtwhatsapp=findViewById(R.id.txtwhatsapp);
        face = Typeface.createFromAsset(getAssets(), "fonts/proxibold.otf");
        face1 = Typeface.createFromAsset(getAssets(), "fonts/proximanormal.ttf");
        text.setTypeface(face);
        text.setText("മലയാളം കാമസൂത്ര");
        txtweb.setText("വെബ്\u200Cസൈറ്റ് ");
        txtwhatsapp.setText("വാട്\u200Cസ്ആപ്പ്\u200C");
        txtwhatsapp.setTypeface(face);
        txtweb.setTypeface(face);
        if (db.get_scrwidth().equalsIgnoreCase("")) {
            int width = getResources().getDisplayMetrics().widthPixels;
            db.add_widthscreen(width+"");
        }
        help1.setText("ക്ഷമിക്കണം ! പ്ലേസ്റ്റോര്\u200D അനുവദിക്കാത്തത് കാരണം ഈ ആപ്പില്\u200D ചിത്രങ്ങള്\u200D ഉള്\u200Dപ്പെടുത്തിയിട്ടില്ല.ചിത്രങ്ങള്\u200D ഉള്\u200Dപ്പെടുത്തിയ ഒറിജിനല്\u200D ആപ്പ് ഞങ്ങളുടെ വെബ്\u200Cസൈറ്റില്\u200D നിന്നും ഡൗണ്\u200Dലോഡ് ചെയ്\u200Cതെടുക്കാവുന്നതാണ്. താഴെയുള്ള വെബ് ഐക്കണില്\u200D പ്രസ്സ് ചെയ്ത് വെബ്\u200Cസൈറ്റ് തുറക്കാം ");
        help1.setTypeface(face1);
        positions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i=new Intent(getApplicationContext(),Position_List.class);
                startActivity(i);
            }
        });

        web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://malayalamkamasuthra.xyz"));
                startActivity(browserIntent);

            }
        });
        txtweb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://malayalamkamasuthra.xyz"));
                startActivity(browserIntent);

            }
        });
        whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                openWhatsApp("+91 9048801231");
            }
        });

        txtwhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWhatsApp("+91 9048801231");
            }
        });

        loan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] PERMISSIONS = {android.Manifest.permission.INTERNET, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_NETWORK_STATE};

                if (!check_permission(MainActivity.this, PERMISSIONS)) {
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);
                }
                else
                {
                    startActivity(new Intent(getApplicationContext(),App_Deatils.class));
                }

            }
        });

    }

    private void openWhatsApp(String number) {
        try {
            number = number.replace(" ", "").replace("+", "");
            Intent sendIntent = new Intent("android.intent.action.MAIN");
            sendIntent.setComponent(new ComponentName("com.whatsapp","com.whatsapp.Conversation"));
            sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators(number)+"@s.whatsapp.net");
            startActivity(sendIntent);

        } catch(Exception e) {

            Toast.makeText(getApplicationContext(),number+" എന്ന നമ്പര്\u200D സേവ് ചെയ്ത് വാട്\u200Cസ്ആപ്പ് അയക്കുക ",Toast.LENGTH_LONG).show();
        }
    }


    public static boolean check_permission(Context context, String... permissions) {
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
