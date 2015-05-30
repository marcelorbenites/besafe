package com.panicbutton.panicbutton;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class BeSafeNotificationBuilder {

    private Context context;

    public BeSafeNotificationBuilder(Context context) {
        this.context = context;
    }

    public Notification build(String message) {
        Intent mainActivityIntent = new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return createBuilder(message, true, PendingIntent.getActivity(context, 0, mainActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT)).build();
    }

    protected Context getContext() {
        return context;
    }

    protected void setIcons(NotificationCompat.Builder builder) {
        builder.setSmallIcon(R.mipmap.small_icon);
        builder.setColor(context.getResources().getColor(R.color.colorPrimaryDark));
    }

    protected void setAlertSound(NotificationCompat.Builder builder) {
        builder.setDefaults(Notification.DEFAULT_ALL);
    }

    private NotificationCompat.Builder createBuilder(String message, boolean alertEnabled, PendingIntent pendingIntent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(context.getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentText(message)
                .setContentIntent(pendingIntent);

        setIcons(builder);

        if (alertEnabled) {
            setAlertSound(builder);
        }

        return builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(message));
    }
}
