package com.nautilusapps.amazondealsnotifier;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.TaskParams;
import java.util.List;

/**
 * Runs in background to update the items table in the database. If there is any item on sale shows
 * a notification.<br>
 * Note: battery-saving mode on Samsung devices prevents the service to run in background.
 */
public class UpdateService extends GcmTaskService {

    private static final String TAG = UpdateService.class.getCanonicalName();
    /** Max number of update attempts. */
    private final int MAX_ATTEMPTS = 2;

    @Override
    public int onRunTask(TaskParams taskParams) {

        int attempts = 0, maxDelay = 500;

        // Try to update the items in the database:
        while (!MainActivity.updateItems(getApplicationContext(), maxDelay * attempts) &&
                attempts < MAX_ATTEMPTS) {
            attempts++;
        }

        // If has failed, reschedule the task with back-off.
        if (attempts == MAX_ATTEMPTS - 1) {
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }

        // If there is any item on sale, show the notification:
        List<AmazonItem> itemsOnSale = MainActivity.getItemsOnSale(getApplicationContext());
        if (itemsOnSale.size() > 0 &&
                MainActivity.getEnableNotifications(getApplicationContext())) {
            showItemsOnSaleNotification(getApplicationContext(), itemsOnSale);
        }

        return GcmNetworkManager.RESULT_SUCCESS;

    }

    /**
     * Called when the app or Google Play Services are updated. Reschedules the last task.
     */
    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        scheduleUpdate(
                getApplicationContext(),
                MainActivity.getUpdateFrequency(getApplicationContext())
        );
    }

    /**
     * Schedules the execution of the task with a given period.<br>
     * Note: if Google Play Services is not available fails silently doing nothing.
     * @param period    Minimum time to wait until the next execution.
     */
    public static void scheduleUpdate(Context context, int period) {

        int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);

        if (result != ConnectionResult.SUCCESS) {
            return;
        }

        PeriodicTask periodicTask = new PeriodicTask.Builder()
                .setService(UpdateService.class)
                .setPeriod(period)
                // The task is run only when the device is connected to the network:
                .setRequiredNetwork(PeriodicTask.NETWORK_STATE_CONNECTED)
                .setTag(TAG)
                // The scheduling persists across boots:
                .setPersisted(true)
                // If there is another task with the same TAG, replace it:
                .setUpdateCurrent(true)
                .setRequiresCharging(false)
                .build();

        GcmNetworkManager.getInstance(context).schedule(periodicTask);

    }

    /**
     * Checks if vibration is enabled, which notification sound to use, and shows a notification
     * which shows the number of items which are on sale. Fails silently if
     * {@link android.content.Context#getSystemService(java.lang.String)} can't provide the
     * Notification Service.
     * @param itemsOnSale   List of items on sale.<br>
     *                      Note: must contain at least one item.
     */
    public static void showItemsOnSaleNotification(Context context, List<AmazonItem> itemsOnSale) {

        Boolean enableVibration = MainActivity.getEnableVibration(context);
        String notificationSound = MainActivity.getNotificationSound(context);

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            return;
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_launcher_circle);

        String NOTIFICATION_CHANNEL_ID = "items_on_sale_channel";
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setAutoCancel(true)
                        .setTicker(context.getString(R.string.title_some_items_on_sale))
                        .setSmallIcon(R.drawable.ic_action_cylinder_hat)
                        .setLargeIcon(largeIcon)
                        .setContentTitle(context.getString(R.string.title_some_items_on_sale))
                        .setContentText(
                                String.format(
                                        context.getString(R.string.msg_items_are_on_sale),
                                        itemsOnSale.size()
                                )
                        );

        // For most recent Android versions a notification channel is needed:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(
                            NOTIFICATION_CHANNEL_ID,
                            context.getString(R.string.title_application),
                            NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setVibrationPattern(new long[]{500, 1000});
            notificationChannel.enableVibration(enableVibration);
            notificationManager.createNotificationChannel(notificationChannel);
        } else {
            builder.setVibrate(new long[]{500, 1000});
        }

        // Check if the notification sound is valid:
        if (!notificationSound.equals("invalid")) {
            Uri uri = Uri.parse(notificationSound);
            builder.setSound(uri);
        }

        // On click call MainActivity:
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        notificationManager.notify(1, builder.build());

    }

}
