package adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.fishappadmin.ConnectionDetecter;
import com.fishappadmin.DatabaseHandler;
import com.fishappadmin.OrderGroup;
import com.fishappadmin.Order_List;
import com.fishappadmin.R;
import com.fishappadmin.Temp;
import com.fishappadmin.UserDatabaseHandler;
import data.OrderGroup_Feeditem;
import java.util.List;

public class OrderGroup__ListAdapter extends Adapter<ViewHolder> {
    private static final int TYPE_FOOTER = 1;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_NULL = 2;
    private Activity activity;
    ConnectionDetecter cd;
    public Context context;
    public DatabaseHandler db;
    Typeface face;
    public List<OrderGroup_Feeditem> feedItems;
    private LayoutInflater inflater;
    ProgressDialog pd;
    int pos = 0;
    UserDatabaseHandler udb;
    public class viewHolder extends ViewHolder {
        TextView deliverycharge;
        TextView grandtotal;
        RelativeLayout layout;
        TextView oid;
        TextView orderdate;
        TextView total;
        TextView txtdeliverycharge;
        TextView txtgrandtotal;
        TextView txtoid;
        TextView txtorderdate;
        TextView txttotal;

        public viewHolder(View itemView) {
            super(itemView);
            layout = (RelativeLayout) itemView.findViewById(R.id.layout);
            txtoid = (TextView) itemView.findViewById(R.id.txtoid);
            oid = (TextView) itemView.findViewById(R.id.oid);
            txtorderdate = (TextView) itemView.findViewById(R.id.txtorderdate);
            orderdate = (TextView) itemView.findViewById(R.id.orderdate);
            txttotal = (TextView) itemView.findViewById(R.id.txttotal);
            total = (TextView) itemView.findViewById(R.id.total);
            txtdeliverycharge = (TextView) itemView.findViewById(R.id.txtdeliverycharge);
            deliverycharge = (TextView) itemView.findViewById(R.id.deliverycharge);
            txtgrandtotal = (TextView) itemView.findViewById(R.id.txtgrandtotal);
            grandtotal = (TextView) itemView.findViewById(R.id.grandtotal);
        }
    }

    public class viewHolderFooter extends ViewHolder {
        RelativeLayout layout1;

        public viewHolderFooter(View itemView) {
            super(itemView);
            layout1 = (RelativeLayout) itemView.findViewById(R.id.layout1);
        }
    }

    public OrderGroup__ListAdapter(Activity activity2, List<OrderGroup_Feeditem> feedItems2) {
        activity = activity2;
        feedItems = feedItems2;
        context = activity2.getApplicationContext();
        db = new DatabaseHandler(context);
        udb = new UserDatabaseHandler(context);
        pd = new ProgressDialog(activity2);
        cd = new ConnectionDetecter(context);
        face = Typeface.createFromAsset(context.getAssets(), "proxibold.otf");
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new viewHolder(LayoutInflater.from(context).inflate(R.layout.custom_ordergroup, parent, false));
        }
        if (viewType == 1) {
            return new viewHolderFooter(LayoutInflater.from(context).inflate(R.layout.footerview, parent, false));
        }
        if (viewType == 2) {
            return new viewHolderFooter(LayoutInflater.from(context).inflate(R.layout.fullloaded, parent, false));
        }
        return null;
    }

    public int getItemViewType(int position) {
        if (position < feedItems.size()) {
            return 0;
        }
        return 2;
    }

    public int getItemCount() {
        return feedItems.size() + 1;
    }

    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (holder instanceof viewHolder) {
            try {
                if (position == feedItems.size() - 1 && feedItems.size() > 10) {
                    ((OrderGroup) activity).loadmore();
                }
                OrderGroup_Feeditem item = (OrderGroup_Feeditem) feedItems.get(position);
                viewHolder viewHolder2 = (viewHolder) holder;
                viewHolder2.txtoid.setTypeface(face);
                viewHolder2.oid.setTypeface(face);
                viewHolder2.txtorderdate.setTypeface(face);
                viewHolder2.orderdate.setTypeface(face);
                viewHolder2.txtdeliverycharge.setTypeface(face);
                viewHolder2.deliverycharge.setTypeface(face);
                viewHolder2.txttotal.setTypeface(face);
                viewHolder2.total.setTypeface(face);
                viewHolder2.txtgrandtotal.setTypeface(face);
                viewHolder2.grandtotal.setTypeface(face);
                String rupee = context.getResources().getString(R.string.Rs);
                viewHolder2.oid.setText(Temp.grpid+"-"+item.getgroupid());
                viewHolder2.orderdate.setText(item.getorderdate());

                viewHolder2.total.setText(rupee+String.format("%.2f", Float.parseFloat(item.gettotal())));
                viewHolder2.deliverycharge.setText(rupee+String.format("%.2f", Float.parseFloat(item.getDeliverycharge())));

                viewHolder2.grandtotal.setText(rupee+String.format("%.2f", Float.parseFloat(item.gettotal()) + Float.parseFloat(item.getDeliverycharge())));

                viewHolder2.layout.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        OrderGroup_Feeditem orderGroup_Feeditem = (OrderGroup_Feeditem) feedItems.get(position);
                        OrderGroup_Feeditem item = (OrderGroup_Feeditem) feedItems.get(position);
                        Temp.ordergroupid = item.getgroupid();
                        Temp.totalcharge = Float.parseFloat(item.gettotal()) + Float.parseFloat(item.getDeliverycharge())+"";
                        Intent i = new Intent(context, Order_List.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
                    }
                });
            } catch (Exception e) {
            }
        }
    }
}
