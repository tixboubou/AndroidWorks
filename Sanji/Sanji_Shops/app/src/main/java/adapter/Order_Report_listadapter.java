package adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.sanji_shop.DatabaseHandler;
import com.sanji_shop.Delivery_Reports;
import com.sanji_shop.R;
import com.sanji_shop.Temp;
import data.OrderProductList_Report_Feed;
import data.Order_Report_Feed;
import java.util.ArrayList;
import java.util.List;

public class Order_Report_listadapter extends Adapter<ViewHolder> {
    private static final int TYPE_FOOTER = 1;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_NULL = 2;
    private Activity activity;
    private Context context;
    public DatabaseHandler db = new DatabaseHandler(context);
    Typeface face = Typeface.createFromAsset(context.getAssets(), "font/proxibold.otf");
    Typeface face1 = Typeface.createFromAsset(context.getAssets(), "font/proximanormal.ttf");
    Typeface face2 = Typeface.createFromAsset(context.getAssets(), "font/Rachana.ttf");
    private List<Order_Report_Feed> feedItems;
    private LayoutInflater inflater;

    public class viewHolder extends ViewHolder {
        TextView deliverycharge;
        TextView groupid;
        RelativeLayout layout;
        ListView list;
        TextView orderdate;
        TextView status;
        TextView total;
        TextView txtdeliverycharge;
        TextView txtgroupid;
        TextView txttotal;

        public viewHolder(View itemView) {
            super(itemView);
            list = (ListView) itemView.findViewById(R.id.list);
            txtgroupid = (TextView) itemView.findViewById(R.id.txtgroupid);
            groupid = (TextView) itemView.findViewById(R.id.groupid);
            orderdate = (TextView) itemView.findViewById(R.id.orderdate);
            status = (TextView) itemView.findViewById(R.id.status);
            layout = (RelativeLayout) itemView.findViewById(R.id.layout);
            txttotal = (TextView) itemView.findViewById(R.id.txttoal);
            total = (TextView) itemView.findViewById(R.id.total);
            txtdeliverycharge = (TextView) itemView.findViewById(R.id.txtdeliverycharge);
            deliverycharge = (TextView) itemView.findViewById(R.id.deliverycharge);
        }
    }

    public class viewHolderFooter extends ViewHolder {
        RelativeLayout layout1;

        public viewHolderFooter(View itemView) {
            super(itemView);
            layout1 = (RelativeLayout) itemView.findViewById(R.id.layout1);
        }
    }

    public Order_Report_listadapter(Activity activity2, List<Order_Report_Feed> feedItems2) {
        activity = activity2;
        feedItems = feedItems2;
        context = activity2.getApplicationContext();
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new viewHolder(LayoutInflater.from(context).inflate(R.layout.custom_orderhistory, parent, false));
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

    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder instanceof viewHolder) {
            try {
                if (position == feedItems.size() - 1 && feedItems.size() > 4) {
                    ((Delivery_Reports) activity).loadmore();
                }
                Order_Report_Feed item = (Order_Report_Feed) feedItems.get(position);
                viewHolder viewHolder2 = (viewHolder) holder;
                List<OrderProductList_Report_Feed> feedItems1 = new ArrayList<>();
                OrderProductList_Report_ListAdapter listAdapter = new OrderProductList_Report_ListAdapter(activity, feedItems1);
                viewHolder2.list.setAdapter(listAdapter);
                feedItems1.clear();
                String[] got = item.getitemdetails().split("::");
                int p = 0;
                while (p < got.length) {
                    OrderProductList_Report_Feed item2 = new OrderProductList_Report_Feed();
                    item2.setitemname(got[p]);
                    int p2 = p + 1;
                    item2.setqty(got[p2]);
                    int p3 = p2 + 1;
                    item2.settotalamount(got[p3]);
                    feedItems1.add(item2);
                    p = p3 + 1;
                }
                viewHolder2.txttotal.setTypeface(face);
                viewHolder2.total.setTypeface(face);
                viewHolder2.txtgroupid.setTypeface(face);
                viewHolder2.groupid.setTypeface(face);
                viewHolder2.status.setTypeface(face2);
                viewHolder2.orderdate.setTypeface(face);
                viewHolder2.txtdeliverycharge.setTypeface(face);
                viewHolder2.deliverycharge.setTypeface(face);
                viewHolder2.deliverycharge.setText(String.format("%.2f", new Object[]{Float.valueOf(Float.parseFloat(item.getdlcharge()))}));
                viewHolder2.total.setText(String.format("%.2f", new Object[]{Float.valueOf(Float.parseFloat(item.gettotalamount()) + Float.parseFloat(item.getdlcharge()))}));
                viewHolder2.list.setVisibility(View.VISIBLE);
                listAdapter.notifyDataSetChanged();
                viewHolder2.orderdate.setText(item.getorderdate());
                TextView textView = viewHolder2.groupid;
                StringBuilder sb = new StringBuilder();
                sb.append(Temp.grpid);
                sb.append("-");
                sb.append(item.getgroupid());
                textView.setText(sb.toString());
                if (item.getstatus().equalsIgnoreCase("0")) {
                    viewHolder2.status.setTextColor(Color.parseColor("#df4343"));
                    viewHolder2.status.setText("Not Confirmed");
                } else if (item.getstatus().equalsIgnoreCase("1")) {
                    viewHolder2.status.setTextColor(Color.parseColor("#58a942"));
                    viewHolder2.status.setText("Ready for Delivery");
                } else if (item.getstatus().equalsIgnoreCase("2")) {
                    viewHolder2.status.setTextColor(Color.parseColor("#ff9275"));
                    viewHolder2.status.setText("Out to Delivery");
                } else if (item.getstatus().equalsIgnoreCase("4")) {
                    viewHolder2.status.setTextColor(Color.parseColor("#5d84a2"));
                    viewHolder2.status.setText("Delivered");
                } else if (item.getstatus().equalsIgnoreCase("5")) {
                    viewHolder2.status.setTextColor(Color.parseColor("#df4343"));
                    viewHolder2.status.setText("Delivery Cancelled");
                } else if (item.getstatus().equalsIgnoreCase("6")) {
                    viewHolder2.status.setTextColor(Color.parseColor("#df4343"));
                    viewHolder2.status.setText("Delivery Returned");
                } else {
                    viewHolder2.status.setText("");
                }
            } catch (Exception e) {
            }
        }
    }
}
