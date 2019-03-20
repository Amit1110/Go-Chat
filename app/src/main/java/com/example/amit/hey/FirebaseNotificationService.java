package com.example.amit.hey;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseNotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //Log.i("Amit", "new noti");

        //FOREGROUND NOTIFICATION

      //  private final String CHANNEL_ID = "personal_notifications";

        String title = remoteMessage.getNotification().getTitle();
        String message = remoteMessage.getNotification().getBody();
        String click_Action = remoteMessage.getNotification().getClickAction();
        String from_user_id = remoteMessage.getData().get("from_user_id");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message);
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)




        Intent resultIntent = new Intent(click_Action);
        resultIntent.putExtra("user_id", from_user_id);


        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);



        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        int notification_id = (int) System.currentTimeMillis();
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notification_id,builder.build());
    }


}


