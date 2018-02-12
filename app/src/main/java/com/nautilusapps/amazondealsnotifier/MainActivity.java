package com.nautilusapps.amazondealsnotifier;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shows the list of Amazon items and provides a button to add an item and one to access to the app
 * settings.
 */
public class MainActivity extends AppCompatActivity {

    private SwipeRefreshLayout mItemsListSwipeRefreshLayout;
    private RecyclerView mItemsOnSaleList, mItemsNotDiscountedList;
    private TextView mItemsOnSaleSubheader, mItemsNotDiscountedSubheader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_main_activity));

        // Define the behavior for the FAB:
        FloatingActionButton addItemFloatingActionButton = findViewById(R.id.fab_add_item);
        addItemFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog("");
            }
        });

        // References to the widgets:
        mItemsOnSaleList = findViewById(R.id.recycler_items_on_sale_list);
        mItemsNotDiscountedList = findViewById(R.id.recycler_items_not_discounted_list);
        mItemsOnSaleSubheader = findViewById(R.id.text_subheader_items_on_sale);
        mItemsNotDiscountedSubheader = findViewById(R.id.text_subheader_items_not_discounted);
        mItemsListSwipeRefreshLayout = findViewById(R.id.swipe_refresh_items_list);

        // Define the swipe-down behavior:
        mItemsListSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {

                    /**
                     * Tries to update the items list. If fails, shows an error message. If
                     * successful, shows a success message, refreshes the items list, and
                     * schedules the next update.
                     */
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onRefresh() {

                        new AsyncTask<Void, Void, Void>() {

                            boolean successful = false;

                            @Override
                            protected Void doInBackground(Void... voids) {

                                // Try to update the items in the database:
                                if(updateItems(getApplicationContext(), 0)) {
                                    UpdateService.scheduleUpdate(getApplicationContext(),
                                            getUpdateFrequency(getApplicationContext()));
                                    successful = true;
                                }

                                return null;

                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {

                                super.onPostExecute(aVoid);
                                mItemsListSwipeRefreshLayout.setRefreshing(false);

                                if (successful) {
                                    refreshList();
                                    Snackbar.make(findViewById(android.R.id.content),
                                            getString(R.string.msg_update_successful),
                                            Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(findViewById(android.R.id.content),
                                            getString(R.string.error_update_unsuccessful),
                                            Snackbar.LENGTH_SHORT).show();
                                }

                            }

                        }.execute();

                    }

                });

        refreshList();

        // Compute and show how much time passed since the last update:
        String timeSinceLastUpdate = getTimeSinceLastUpdate();
        if (timeSinceLastUpdate != null) {
            Snackbar.make(findViewById(android.R.id.content),
                    String.format(getString(R.string.msg_updated_ago), timeSinceLastUpdate),
                    Snackbar.LENGTH_LONG).show();
        }

        checkGooglePlayServices();

    }

    /**
     * Checks if another app has shared with this one some text. If it has, tries to extract an url
     * from the text and calls {@link AddItemActivity} specifying the initial value for the URL
     * field.<br>
     * Note: the URL must be a valid Amazon URL.
     */
    @Override
    public void onResume() {

        super.onResume();

        final Pattern amazonUrlPattern = Pattern.compile(AddItemActivity.AMAZON_URL_REGEX);

        Intent intent = getIntent();

        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        String type = intent.getType();

        if (action == null || type == null) {
            return;
        }

        if (action.equals(Intent.ACTION_SEND) && type.equals("text/plain")) {

            String text = intent.getStringExtra(Intent.EXTRA_TEXT);
            Matcher matcher = amazonUrlPattern.matcher(text);
            String url = null;

            // Get the last occurrence of an url in the text.
            while (matcher.find()) {
                url = matcher.group();
            }

            showDialog(url);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item_settings:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * Shows a dialog to confirm if the user wants to quit.
     */
    @Override
    public void onBackPressed() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.title_confirmation_quit));

        dialog.setPositiveButton(
                getString(R.string.action_yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                });

        dialog.setNegativeButton(
                getString(R.string.action_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        dialog.show();

    }

    /**
     * Checks if Google Play Services is available and if it isn't informs the user that it is
     * needed for the background service.
     */
    private void checkGooglePlayServices() {

        int result = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this);

        if (result != ConnectionResult.SUCCESS) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(getString(R.string.title_play_services_needed));
            dialog.setMessage(R.string.msg_play_services_needed);

            dialog.setNeutralButton(
                    getString(R.string.action_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            dialog.show();

        }

    }

    /**
     * Checks the timestamp of the last update of the items list and returns a string in the format
     * "Gd, Hh, Mm, Ss", where G, H, M, S are the days, hours, minutes, seconds since the last
     * update. Returns {@code null} if there is no timestamp for the last update.
     */
    private String getTimeSinceLastUpdate() {

        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.sharedpref_file_name),
                MODE_PRIVATE);
        Long lastUpdateLong = sharedPreferences.getLong(
                getString(R.string.sharedpref_key_last_update),
                -1);

        if (lastUpdateLong == -1) {
            return null;
        }

        long elapsedMillis = new Date().getTime() - lastUpdateLong;
        long elapsedDays = elapsedMillis / (24 * 60 * 60 * 1000);
        elapsedMillis %= (24 * 60 * 60 * 1000);
        long elapsedHours = elapsedMillis / (60 * 60 * 1000);
        elapsedMillis %= (60 * 60 * 1000);
        long elapsedMinutes = elapsedMillis / (60 * 1000);
        elapsedMillis %= (60 * 1000);
        long elapsedSeconds = elapsedMillis / 1000;

        String elapsedTime = "";

        if (elapsedDays != 0) {
            elapsedTime += " " + String.valueOf(elapsedDays) + getString(R.string.d);
        }

        if (elapsedHours != 0) {
            elapsedTime += " " + String.valueOf(elapsedHours) + getString(R.string.h);
        }

        if (elapsedMinutes != 0) {
            elapsedTime += " " + String.valueOf(elapsedMinutes) + getString(R.string.m);
        }

        if (elapsedSeconds != 0) {
            elapsedTime += " " + String.valueOf(elapsedSeconds) + getString(R.string.s);
        }

        return elapsedTime;

    }

    /**
     * Calls {@link AddItemActivity} with an initial value for the URL field.
     * @param url   Initial value for the URL field.
     */
    private void showDialog(String url) {
        Intent intent = new Intent(getApplicationContext(), AddItemActivity.class);
        intent.putExtra("EXTRA_URL", url);
        startActivity(intent);
    }

    public static int getUpdateFrequency(Context context) {

        Resources resources = context.getResources();

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sharedpref_file_name),
                MODE_PRIVATE
        );

        String updateFrequencyIndex = sharedPreferences.getString(
                context.getString(R.string.pref_key_update_frequency),
                context.getString(R.string.pref_default_update_frequency));

        int index = Integer.valueOf(updateFrequencyIndex);

        return resources.getIntArray(R.array.pref_real_values_update_frequency)[index];

    }

    public static Boolean getUpdateOnBoot(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sharedpref_file_name),
                MODE_PRIVATE
        );

        return sharedPreferences.getBoolean(
                        context.getString(R.string.pref_key_update_on_boot),
                        context.getResources().getBoolean(R.bool.pref_default_update_on_boot));

    }

    public static Boolean getEnableNotifications(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sharedpref_file_name),
                MODE_PRIVATE
        );

        return sharedPreferences.getBoolean(
                context.getString(R.string.pref_key_enable_notifications),
                context.getResources().getBoolean(R.bool.pref_default_enable_notifications)
        );

    }

    public static Boolean getEnableVibration(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sharedpref_file_name),
                MODE_PRIVATE
        );

        return sharedPreferences.getBoolean(
                context.getString(R.string.pref_key_enable_vibration),
                context.getResources().getBoolean(R.bool.pref_default_enable_vibration)
        );

    }

    public static String getNotificationSound(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sharedpref_file_name),
                MODE_PRIVATE
        );

        return sharedPreferences.getString(
                context.getString(R.string.pref_key_notification_sound),
                context.getResources().getString(R.string.pref_default_notification_sound)
        );

    }

    /**
     * Tries to update the items in the database. If any item could not be updated because of a
     * connection error, returns {@code false}. If the update has been done completely, returns
     * {@code true}.
     * @param maxDelay  Max time to wait between each connection.
     * @return  {@code true} if every item has been updated correctly, {@code false} in case of a
     *          connection error.
     */
    public static boolean updateItems(Context context, int maxDelay) {

        DBHandler dbHandler = new DBHandler(context);
        // Get the items from the database:
        List<AmazonItem> oldAmazonItemsList = dbHandler.getItems();
        AmazonItem[] oldAmazonItems = oldAmazonItemsList
                .toArray(new AmazonItem[oldAmazonItemsList.size()]);

        AmazonItemLookUp amazonItemLookUp = new AmazonItemLookUp(oldAmazonItems, maxDelay);
        AmazonItem[] updatedAmazonItems = amazonItemLookUp.updateAmazonItems();

        // Check if any lookup has failed:
        for (AmazonItem item : updatedAmazonItems) {
            if (item == null) {
                return false;
            }
        }

        // Update the items in the database:
        dbHandler.updateItems(updatedAmazonItems);

        return true;

    }

    /**
     * Builds the lists of items on sale and items not discounted. Decides if showing the subheaders
     * and the divider between the two lists.
     */
    private void refreshList() {

        // Get the items from the database:
        DBHandler dbHandler = new DBHandler(getApplicationContext());
        List<AmazonItem> items = dbHandler.getItems();

        List<AmazonItem> itemsOnSale = new ArrayList<>();
        List<AmazonItem> itemsNotDiscounted = new ArrayList<>();

        for (AmazonItem item : items) {
            if (isItemOnSale(item)) {
                itemsOnSale.add(item);
            } else {
                itemsNotDiscounted.add(item);
            }
        }

        // Hide the lists and subheaders, if needed:
        if (itemsOnSale.size() == 0) {
            mItemsOnSaleSubheader.setVisibility(View.GONE);
            mItemsOnSaleList.setVisibility(View.GONE);
        }
        if (itemsNotDiscounted.size() == 0) {
            mItemsNotDiscountedSubheader.setVisibility(View.GONE);
            mItemsNotDiscountedList.setVisibility(View.GONE);
        }

        // Show the divider, if needed:
        if (itemsOnSale.size() != 0 && itemsNotDiscounted.size() != 0) {
            View divider = findViewById(R.id.view_divider);
            divider.setVisibility(View.VISIBLE);
        }

        // Instantiate the adapters:
        ItemsListAdapter itemsOnSaleAdapter =
                new ItemsListAdapter(itemsOnSale, mItemsOnSaleList);
        ItemsListAdapter itemsNotDiscountedAdapter =
                new ItemsListAdapter(itemsNotDiscounted, mItemsNotDiscountedList);

        mItemsOnSaleList.setLayoutManager(new LinearLayoutManager(this));
        mItemsNotDiscountedList.setLayoutManager(new LinearLayoutManager(this));
        mItemsOnSaleList.setItemAnimator(new DefaultItemAnimator());
        mItemsNotDiscountedList.setItemAnimator(new DefaultItemAnimator());

        // Define the behavior of the items on a left-swipe:
        ItemTouchHelper itemsOnSaleSwipeToDelete = new ItemTouchHelper(
                new SwipeToDeleteCallback(this, itemsOnSaleAdapter));
        itemsOnSaleSwipeToDelete.attachToRecyclerView(mItemsOnSaleList);
        ItemTouchHelper itemsNotDiscountedSwipeToDelete =
                new ItemTouchHelper(
                        new SwipeToDeleteCallback(this, itemsNotDiscountedAdapter));
        itemsNotDiscountedSwipeToDelete.attachToRecyclerView(mItemsNotDiscountedList);

        // Connect the adapters to their lists:
        mItemsOnSaleList.setAdapter(itemsOnSaleAdapter);
        mItemsNotDiscountedList.setAdapter(itemsNotDiscountedAdapter);

        itemsOnSaleAdapter.notifyDataSetChanged();
        itemsNotDiscountedAdapter.notifyDataSetChanged();

        // Scroll the list all the way up:
        final ScrollView scrollView = findViewById(R.id.scroll_items_list);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });

    }

    /**
     * Checks if the item is on sale.
     * @param item  Item to check.
     * @return      {@code true} if the item is on sale, {@code false} otherwise.
     */
    private static boolean isItemOnSale(AmazonItem item) {
        return item != null && item.currentPrice != null && item.previousPrice != null &&
                item.currentPrice < item.previousPrice;
    }

    /**
     * Gets the items from the database and returns the list of items whose current price is lower
     * than {@link AmazonItem#previousPrice}.
     * @return  List of items on sale.
     */
    public static List<AmazonItem> getItemsOnSale(Context context) {

        DBHandler dbHandler = new DBHandler(context);
        List<AmazonItem> items = dbHandler.getItems();
        List<AmazonItem> itemsOnSale = new LinkedList<>();

        for (AmazonItem item : items) {
            if (isItemOnSale(item)) {
                itemsOnSale.add(item);
            }
        }

        return itemsOnSale;

    }

}
