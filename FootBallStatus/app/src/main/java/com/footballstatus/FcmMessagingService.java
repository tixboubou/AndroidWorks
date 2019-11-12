package com.footballstatus;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build.VERSION;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat.Builder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class FcmMessagingService extends FirebaseMessagingService {
    public UserDatabaseHandler udb;

    public void onMessageReceived(RemoteMessage remoteMessage) {
        udb = new UserDatabaseHandler(this);
        if (remoteMessage.getData().size() > 0) {
            try {
                String message = new JSONObject(remoteMessage.getData().get("football")).getString("salman");
                JSONObject json1 = new JSONObject();

                String[] s = message.split(":%");
                int i = 0;
                while (i < s.length) {
                    int a = i;
                    int i2 = i + 1;
                    json1.put(s[a], s[i2]);
                    i = i2 + 1;
                }

            try {
                JSONObject json = new JSONObject(json1.toString());
                if (json.getString("notitype").equalsIgnoreCase("newstatus")) {
                    try {
                        generate_newstatus();
                    } catch (Exception e5) {
                    }
                }
            } catch (Exception e10) {
            }
            } catch (JSONException e) {
            }
        }
    }

    public void generate_newstatus(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        RemoteViews contentView = new RemoteViews(Temp.packagename, R.layout.customnotification);
        contentView.setTextViewText(R.id.title, "New FootBall Status !!!");


        if (VERSION.SDK_INT < 26) {
            Notification notification = new Builder(this).setSmallIcon(R.drawable.smalllogo).setContentIntent(pendingIntent).setContent(contentView).build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(NotificationID.getID(), notification);

        }
        else
        {
            String str = "FBSALMANChannels";
            NotificationChannel channel = new NotificationChannel(str, "FBSALMAN_Noti", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("FBSALMANNoti");
            NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            Notification notification2 = new Builder(this, str).setSmallIcon(R.drawable.smalllogo).setContentIntent(pendingIntent).setContent(contentView).setPriority(0).build();
            notification2.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(NotificationID.getID(), notification2);
        }

    }
}
