package com.zemose.admin;

import adapter.Product_ListAdapter;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.payumoney.core.PayUmoneyConstants;
import data.Productlist_FeedItem;
import es.dmoral.toasty.Toasty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Product_List extends AppCompatActivity {
    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    ImageView back;
    ConnectionDetecter cd;

    public List<Productlist_FeedItem> feedItems;
    public int limit = 0;

    public Product_ListAdapter listAdapter;
    ImageView loading;
    ImageView nodata;
    ProgressDialog pd;
    int pos = 0;
    RecyclerView recylerview;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_product__list);
        StrictMode.setThreadPolicy(new Builder().permitAll().build());
        this.pd = new ProgressDialog(this);
        this.cd = new ConnectionDetecter(this);
        this.back = (ImageView) findViewById(R.id.back);
        this.loading = (ImageView) findViewById(R.id.loading);
        this.cd = new ConnectionDetecter(this);
        this.recylerview = (RecyclerView) findViewById(R.id.recylerview);
        this.nodata = (ImageView) findViewById(R.id.nodata);
        this.feedItems = new ArrayList();
        this.listAdapter = new Product_ListAdapter(this, this.feedItems);
        this.recylerview.setLayoutManager(new GridLayoutManager(this, 1));
        this.recylerview.setAdapter(this.listAdapter);
        Glide.with(this).load(Integer.valueOf(R.drawable.loading)).into(this.loading);
        this.back.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Product_List.this.onBackPressed();
            }
        });
        if (this.cd.isConnectingToInternet()) {
            this.limit = 0;
            get_products();
            return;
        }
        Toasty.info(getApplicationContext(), Temp_Variable.nointernet, Toast.LENGTH_SHORT).show();
    }

    public void get_products() {
        try {
            this.nodata.setVisibility(View.GONE);
            this.recylerview.setVisibility(View.GONE);
            this.loading.setVisibility(View.VISIBLE);
            OkHttpClient client = new OkHttpClient();
            JSONObject jo = new JSONObject();
            try {
                jo.put("suppid", Temp_Variable.supplierid);
            } catch (JSONException e) {
            }
            RequestBody body = RequestBody.create(this.JSON, jo.toString());
            Request.Builder builder = new Request.Builder();
            StringBuilder sb = new StringBuilder();
            sb.append(Temp_Variable.baseurl);
            sb.append("appadmin/getsupplier_products");
            client.newCall(builder.url(sb.toString()).post(body).build()).enqueue(new Callback() {
                public void onFailure(Call call, IOException e) {
                    Product_List.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Product_List.this.nodata.setVisibility(View.VISIBLE);
                            Product_List.this.loading.setVisibility(View.GONE);
                            Product_List.this.recylerview.setVisibility(View.GONE);
                        }
                    });
                }

                public void onResponse(Call call, final Response response) throws IOException {
                    Product_List.this.runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                Product_List.this.pd.dismiss();
                                JSONObject jo = new JSONObject(response.body().string());
                                if (jo.getString(PayUmoneyConstants.MESSAGE).equalsIgnoreCase("No")) {
                                    Product_List.this.nodata.setVisibility(View.VISIBLE);
                                    Product_List.this.loading.setVisibility(View.GONE);
                                    Product_List.this.recylerview.setVisibility(View.GONE);
                                    Toasty.info(Product_List.this.getApplicationContext(), "Please contact admin", Toast.LENGTH_SHORT).show();
                                } else if (jo.getString(PayUmoneyConstants.MESSAGE).equalsIgnoreCase("got")) {
                                    Product_List.this.feedItems.clear();
                                    JSONArray jsonarray = jo.getJSONArray("data");
                                    for (int i = 0; i < jsonarray.length(); i++) {
                                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                                        Productlist_FeedItem h = new Productlist_FeedItem();
                                        h.setsn(jsonobject.getString("supplierProductId"));
                                        h.setproductqty(jsonobject.getString("quantity"));
                                        h.setstatus("1");
                                        JSONObject job = jsonobject.getJSONObject("product");
                                        h.setproductid(job.getString("_id"));
                                        h.setname(job.getString("name"));
                                        h.setimgpath(job.getString("imagePath"));
                                        Product_List.this.feedItems.add(h);
                                    }
                                    Product_List.this.loading.setVisibility(View.GONE);
                                    Product_List.this.recylerview.setVisibility(View.VISIBLE);
                                    Product_List.this.listAdapter.notifyDataSetChanged();
                                } else {
                                    Product_List.this.nodata.setVisibility(View.VISIBLE);
                                    Product_List.this.loading.setVisibility(View.GONE);
                                    Product_List.this.recylerview.setVisibility(View.GONE);
                                    Toasty.info(Product_List.this.getApplicationContext(), "Please try later", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception a) {
                                Log.w("EEEE", Log.getStackTraceString(a));
                                Product_List.this.nodata.setVisibility(View.VISIBLE);
                                Product_List.this.loading.setVisibility(View.GONE);
                                Product_List.this.recylerview.setVisibility(View.GONE);
                                Toasty.info(Product_List.this.getApplicationContext(), "Please try later", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        } catch (Exception e2) {
        }
    }
}