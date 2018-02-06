package com.nautilusapps.amazondealsnotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.List;

/**
 * Receives the device boot or an app update.<br>
 * If enabled, tries to update the items in the database and checks if there are any items on sale.
 * In that case shows a notification. If the update fails for more than {@link #MAX_ATTEMPTS} times,
 * the items are not updated.<br>
 * If the app is replaced (e.g. updated) reschedules the next update of the items table.
 */
public class UpdateReceiver extends BroadcastReceiver {

    /** Max number of update attempts. */
    private final int MAX_ATTEMPTS = 3;

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (action != null && action.equals(Intent.ACTION_BOOT_COMPLETED)) {

            // Check if update-on-boot is enabled:
            if (MainActivity.getUpdateOnBoot(context)) {

                int attempts = 0, maxDelay = 500;

                // Try to update the items in the database, increasing the delay if it fails:
                while (!MainActivity.updateItems(context, maxDelay * attempts) &&
                        attempts < MAX_ATTEMPTS) {
                    attempts++;
                }

                // If there is any item on sale, show the notification:
                List<AmazonItem> itemsOnSale = MainActivity.getItemsOnSale(context);
                if (itemsOnSale.size() > 0) {
                    UpdateService.showItemsOnSaleNotification(context, itemsOnSale);
                }

            }

        } else if (action != null && action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            // If the app is replaced (e.g. updated), reschedule the next update of the items table:
            UpdateService.scheduleUpdate(
                    context,
                    MainActivity.getUpdateFrequency(context)
            );
        }

    }

}
