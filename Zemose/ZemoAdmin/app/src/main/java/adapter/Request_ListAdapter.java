package adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.zemose.admin.ChatDB;
import com.zemose.admin.ConnectionDetecter;
import com.zemose.admin.R;
import com.zemose.admin.Requests;

import java.util.List;

import data.RequestPro_FeedItem;

public class Request_ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int TYPE_FOOTER = 1;
    private final int TYPE_ITEM = 0;
    private final int TYPE_NULL = 2;

    public AppCompatActivity activity;
    public ConnectionDetecter cd;
    private Context context;
    public ChatDB db;
    Typeface face;
    Typeface face1;

    public List<RequestPro_FeedItem> feedItems;
    private LayoutInflater inflater;
    ProgressDialog pd;
    int pos = 0;
    public String userid = "";

    public class viewHolder extends RecyclerView.ViewHolder {
        ImageView call;
        TextView date;
        TextView disc;
        TextView name;
        ImageView process;
        TextView suppliername;

        public viewHolder(View itemView) {
            super(itemView);
            this.date = (TextView) itemView.findViewById(R.id.date);
            this.name = (TextView) itemView.findViewById(R.id.name);
            this.disc = (TextView) itemView.findViewById(R.id.disc);
            this.suppliername = (TextView) itemView.findViewById(R.id.suppliername);
            this.process = (ImageView) itemView.findViewById(R.id.process);
            this.call = (ImageView) itemView.findViewById(R.id.call);
        }
    }

    public class viewHolderFooter extends RecyclerView.ViewHolder {
        RelativeLayout layout1;

        public viewHolderFooter(View itemView) {
            super(itemView);
            this.layout1 = (RelativeLayout) itemView.findViewById(R.id.layout1);
        }
    }

    public Request_ListAdapter(AppCompatActivity activity2, List<RequestPro_FeedItem> feedItems2) {
        this.activity = activity2;
        this.feedItems = feedItems2;
        this.context = activity2.getApplicationContext();
        this.pd = new ProgressDialog(activity2);
        this.cd = new ConnectionDetecter(this.context);
        this.db = new ChatDB(this.context);
        this.face = Typeface.createFromAsset(this.context.getAssets(), "font/proxibold.otf");
        this.face1 = Typeface.createFromAsset(this.context.getAssets(), "font/proximanormal.ttf");
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new viewHolder(LayoutInflater.from(this.context).inflate(R.layout.request_customlayout, parent, false));
        }
        if (viewType == 1) {
            return new viewHolderFooter(LayoutInflater.from(this.context).inflate(R.layout.footerview, parent, false));
        }
        if (viewType == 2) {
            return new viewHolderFooter(LayoutInflater.from(this.context).inflate(R.layout.fullloaded, parent, false));
        }
        return null;
    }

    public int getItemViewType(int position) {
        if ((position != this.feedItems.size() || this.feedItems.size() <= 10) && position >= this.feedItems.size()) {
            return 2;
        }
        return 0;
    }

    public int getItemCount() {
        return this.feedItems.size() + 1;
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof viewHolder) {
            try {
                RequestPro_FeedItem item = (RequestPro_FeedItem) this.feedItems.get(position);
                viewHolder viewHolder2 = (viewHolder) holder;
                viewHolder2.name.setText(item.getitemname());
                viewHolder2.disc.setText(item.getitemdisc());
                viewHolder2.suppliername.setText(item.getsupnam());
                viewHolder2.date.setText(item.getregdate());
                viewHolder2.call.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        Object obj = Request_ListAdapter.this.feedItems.get(position);
                        ((Requests) Request_ListAdapter.this.activity).call(((RequestPro_FeedItem) Request_ListAdapter.this.feedItems.get(position)).getsupcontact());
                    }
                });
                viewHolder2.process.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        Object obj = Request_ListAdapter.this.feedItems.get(position);
                        ((Requests) Request_ListAdapter.this.activity).show_requestconfirm(((RequestPro_FeedItem) Request_ListAdapter.this.feedItems.get(position)).getsn(), position);
                    }
                });
            } catch (Exception e) {
            }
        }
    }
}
