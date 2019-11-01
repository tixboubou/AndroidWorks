package adapter;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dailybill_admin.Add_Town_List;
import com.dailybill_admin.ConnectionDetecter;
import com.dailybill_admin.R;
import com.dailybill_admin.ShopManagement;
import com.dailybill_admin.Temp;
import com.dailybill_admin.Town_List;

import data.Towns_FeedItem;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

public class Towns_list_ListAdapter extends BaseAdapter {
    public Activity activity;
    public ConnectionDetecter cd;
    public Context context;
    Typeface face;
    public List<Towns_FeedItem> feedItems;
    private LayoutInflater inflater;
    public String pcatid = "";
    ProgressDialog pd;
    int pos = 0;
    public Towns_list_ListAdapter(Activity activity2, List<Towns_FeedItem> feedItems2) {
        activity = activity2;
        feedItems = feedItems2;
        context = activity2.getApplicationContext();
        cd = new ConnectionDetecter(context);
        pd = new ProgressDialog(activity2);
        face = Typeface.createFromAsset(context.getAssets(), "proximanormal.ttf");
    }

    public int getCount() {
        return feedItems.size();
    }

    public Object getItem(int location) {
        return feedItems.get(location);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        if (inflater == null) {
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_towns, null);
        }
        Button shops = (Button) convertView.findViewById(R.id.shops);

        ImageView delete = (ImageView) convertView.findViewById(R.id.delete);
        ImageView edit = (ImageView) convertView.findViewById(R.id.edit);
        ((TextView) convertView.findViewById(R.id.townname)).setText(((Towns_FeedItem) feedItems.get(position)).getareaname());
        shops.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Towns_FeedItem towns_FeedItem = (Towns_FeedItem) feedItems.get(position);
                Temp.townid = ((Towns_FeedItem) feedItems.get(position)).gettownid();
                Intent i = new Intent(context, ShopManagement.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });

        delete.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                try {
                    Towns_FeedItem towns_FeedItem = (Towns_FeedItem) feedItems.get(position);
                    Towns_FeedItem item = (Towns_FeedItem) feedItems.get(position);
                    pcatid = item.getsn();
                    pos = position;
                    showalert_delete("Are you sure want to delete this product ?");
                } catch (Exception e) {
                }
            }
        });
        edit.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Towns_FeedItem towns_FeedItem = (Towns_FeedItem) feedItems.get(position);
                Towns_FeedItem item = (Towns_FeedItem) feedItems.get(position);
                Temp.townid = item.gettownid();
                Temp.townsn = item.getsn();
                Temp.townname = item.getareaname();
                Temp.townedit = 1;
                Intent i = new Intent(context, Add_Town_List.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });
        return convertView;
    }

    public void timerDelayRemoveDialog(long time, final Dialog d) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                d.dismiss();
            }
        }, time);
    }

    public void showalert_delete(String message) {
        Builder builder = new Builder(activity);
        builder.setMessage(message).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (cd.isConnectingToInternet()) {
                    new delete_product().execute(new String[0]);
                } else {
                    Toast.makeText(context, Temp.nointernet, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    public class delete_product extends AsyncTask<String, Void, String> {
        public void onPreExecute() {
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.show();
            timerDelayRemoveDialog(50000, pd);
        }
        public String doInBackground(String... arg0) {
            try {
                String link= Temp.weblink+"delete_town.php";
                String data  = URLEncoder.encode("item", "UTF-8")
                        + "=" + URLEncoder.encode(pcatid, "UTF-8");
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
                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                    ((Town_List) activity).removeitem(pos);
                    return;
                }
                Toast.makeText(context, Temp.tempproblem, Toast.LENGTH_SHORT).show();
            }
        }
    }
}