package com.zemose.admin;

import adapter.Sna_ListAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.payumoney.core.PayUmoneyConstants;
import data.Sna_FeedItem;
import es.dmoral.toasty.Toasty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class sna_fragment extends Fragment {
    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    ConnectionDetecter cd;

    public Context context;

    public List<Sna_FeedItem> feedItems;
    public int limit = 0;

    public Sna_ListAdapter listAdapter;
    ImageView loading;
    String mobile = "";
    ImageView nodata;
    ProgressDialog pd;
    RecyclerView recylerview;
    private AppCompatActivity activity;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.sna_fragment, container, false);
        try {
            this.context = getActivity();
            this.pd = new ProgressDialog(this.context);
            this.cd = new ConnectionDetecter(this.context);
            this.loading = (ImageView) rootView.findViewById(R.id.loading);
            this.cd = new ConnectionDetecter(this.context);
            this.recylerview = (RecyclerView) rootView.findViewById(R.id.recylerview);
            this.nodata = (ImageView) rootView.findViewById(R.id.nodata);
            this.feedItems = new ArrayList();
            this.listAdapter = new Sna_ListAdapter(activity, this.feedItems);
            this.recylerview.setLayoutManager(new GridLayoutManager(getContext(), 1));
            this.recylerview.setAdapter(this.listAdapter);
            Glide.with(context).load(Integer.valueOf(R.drawable.loading)).into(this.loading);
            if (this.cd.isConnectingToInternet()) {
                this.limit = 0;
                getpending_Payments();
            } else {
                Toasty.info(this.context, Temp_Variable.nointernet, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
        }
        return rootView;
    }

    public void refresh() {
        if (this.cd.isConnectingToInternet()) {
            this.limit = 0;
            getpending_Payments();
            return;
        }
        Toasty.info(this.context, Temp_Variable.nointernet, Toast.LENGTH_SHORT).show();
    }

    public void call(String mobile1) {
        this.mobile = mobile1;
        try {
            if (ContextCompat.checkSelfPermission(this.context, "android.permission.CALL_PHONE") != 0) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{"android.permission.CALL_PHONE"}, 1);
                return;
            }
            Intent callIntent = new Intent("android.intent.action.CALL");
            StringBuilder sb = new StringBuilder();
            sb.append("tel:");
            sb.append(this.mobile);
            callIntent.setData(Uri.parse(sb.toString()));
            startActivity(callIntent);
        } catch (Exception e) {
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == 0) {
            call(this.mobile);
        }
    }

    public void removeitem(int position) {
        try {
            this.feedItems.remove(position);
            this.listAdapter.notifyDataSetChanged();
        } catch (Exception e) {
        }
    }

    public void getpending_Payments() {
        try {
            this.nodata.setVisibility(View.GONE);
            this.recylerview.setVisibility(View.GONE);
            this.loading.setVisibility(View.VISIBLE);
            OkHttpClient client = new OkHttpClient();
            JSONObject jo = new JSONObject();
            try {
                jo.put("adminid", "");
            } catch (JSONException e) {
            }
            RequestBody body = RequestBody.create(this.JSON, jo.toString());
            Builder builder = new Builder();
            StringBuilder sb = new StringBuilder();
            sb.append(Temp_Variable.baseurl);
            sb.append("appadmin/getsna_report");
            client.newCall(builder.url(sb.toString()).post(body).build()).enqueue(new Callback() {
                public void onFailure(Call call, IOException e) {
                    sna_fragment.this.getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            sna_fragment.this.nodata.setVisibility(View.VISIBLE);
                            sna_fragment.this.loading.setVisibility(View.GONE);
                            sna_fragment.this.recylerview.setVisibility(View.GONE);
                        }
                    });
                }

                public void onResponse(Call call, final Response response) throws IOException {
                    sna_fragment.this.getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                sna_fragment.this.pd.dismiss();
                                String result = response.body().string();
                                Log.w("RR", result);
                                JSONObject jo = new JSONObject(result);
                                if (jo.getString(PayUmoneyConstants.MESSAGE).equalsIgnoreCase("No")) {
                                    sna_fragment.this.nodata.setVisibility(View.VISIBLE);
                                    sna_fragment.this.loading.setVisibility(View.GONE);
                                    sna_fragment.this.recylerview.setVisibility(View.GONE);
                                    Toasty.info(sna_fragment.this.context, "Please contact admin", Toast.LENGTH_SHORT).show();
                                } else if (jo.getString(PayUmoneyConstants.MESSAGE).equalsIgnoreCase("got")) {
                                    sna_fragment.this.feedItems.clear();
                                    JSONArray jsonarray = jo.getJSONArray("data");
                                    if (jsonarray.length() > 0) {
                                        for (int i = 0; i < jsonarray.length(); i++) {
                                            JSONObject jsonobject = jsonarray.getJSONObject(i);
                                            Sna_FeedItem h = new Sna_FeedItem();
                                            h.setproductname(jsonobject.getString("productName"));
                                            h.setorderdate(jsonobject.getString("timeStamp"));
                                            h.setproductimage(jsonobject.getString("productImage"));
                                            h.setimgsig(jsonobject.getString("productImageSign"));
                                            JSONArray jarray = jsonobject.getJSONArray("fromItems");
                                            h.setcustomername(jarray.getJSONObject(0).getString("customerName"));
                                            h.setcustomercontact(jarray.getJSONObject(0).getString("contactOne"));
                                            sna_fragment.this.feedItems.add(h);
                                        }
                                        sna_fragment.this.loading.setVisibility(View.GONE);
                                        sna_fragment.this.recylerview.setVisibility(View.VISIBLE);
                                        sna_fragment.this.listAdapter.notifyDataSetChanged();
                                    } else {
                                        sna_fragment.this.nodata.setVisibility(View.VISIBLE);
                                        sna_fragment.this.loading.setVisibility(View.GONE);
                                        sna_fragment.this.recylerview.setVisibility(View.GONE);
                                    }
                                } else {
                                    sna_fragment.this.nodata.setVisibility(View.VISIBLE);
                                    sna_fragment.this.loading.setVisibility(View.GONE);
                                    sna_fragment.this.recylerview.setVisibility(View.GONE);
                                    Toasty.info(sna_fragment.this.context, "Please try later", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                sna_fragment.this.nodata.setVisibility(View.VISIBLE);
                                sna_fragment.this.loading.setVisibility(View.GONE);
                                sna_fragment.this.recylerview.setVisibility(View.GONE);
                                Toasty.info(sna_fragment.this.context, "Please try later", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        } catch (Exception e2) {
        }
    }
}
