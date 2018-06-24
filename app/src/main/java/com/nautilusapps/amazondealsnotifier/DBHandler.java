package com.nautilusapps.amazondealsnotifier;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provides methods to manage the database.
 */
public class DBHandler extends SQLiteOpenHelper {

    public static final String DB_NAME = "AmazonItems";
    private static final int DB_VERSION = 1;
    private Context mContext;

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                DBEntry.TABLE + " (" +
                DBEntry._ID + " INTEGER PRIMARY KEY, " +
                DBEntry.TITLE + " TEXT, " +
                DBEntry.CURRENT_PRICE + " REAL, " +
                DBEntry.PREVIOUS_PRICE + " REAL, " +
                DBEntry.URL + " TEXT)";

        db.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String DROP_TABLE = "DROP TABLE IF EXISTS " + DBEntry.TABLE;
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Returns a list of {@link AmazonItem} containing the items in the database.
     */
    public List<AmazonItem> getItems() {

        List<AmazonItem> items = new ArrayList<>();

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        String[] select = {
                DBEntry._ID,
                DBEntry.TITLE,
                DBEntry.CURRENT_PRICE,
                DBEntry.PREVIOUS_PRICE,
                DBEntry.URL
        };

        // Place on top the most discounted items:
        String sortOrder = DBEntry.CURRENT_PRICE + " - " + DBEntry.PREVIOUS_PRICE + " ASC";

        Cursor cursor = sqLiteDatabase.query(
                DBEntry.TABLE,
                select,
                null,
                null,
                null,
                null,
                sortOrder);

        while (cursor.moveToNext()) {
            String title = null;
            Float currentPrice = null;
            Float previousPrice = null;
            String url = null;
            // If a field is null, leave the variable to null:
            if (!cursor.isNull(1)) {
                title = cursor.getString(1);
            }
            if (!cursor.isNull(2)) {
                currentPrice = cursor.getFloat(2);
            }
            if (!cursor.isNull(3)) {
                previousPrice = cursor.getFloat(3);
            }
            if (!cursor.isNull(4)) {
                url = cursor.getString(4);
            }
            AmazonItem item = new AmazonItem(
                    title,
                    currentPrice,
                    previousPrice,
                    url);
            items.add(item);
        }

        cursor.close();

        return items;

    }

    /**
     * Adds an item to the database and returns the item ID in the table.
     * @param item  Item to add.
     * @return      The item ID if successful, -1 otherwise.
     */
    public long addItem(@NonNull AmazonItem item) {

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(DBEntry.TITLE, item.title);
        contentValues.put(DBEntry.CURRENT_PRICE, item.currentPrice);
        contentValues.put(DBEntry.PREVIOUS_PRICE, item.previousPrice);
        contentValues.put(DBEntry.URL, item.url);

        long id = sqLiteDatabase.insert(DBEntry.TABLE, null, contentValues);
        sqLiteDatabase.close();
        return id;

    }

    /**
     * Checks whether an item is already in the database.
     * @param item  Item to search.
     * @return      {@code true} if the item is already in the database, {@code false} otherwise.
     */
    public boolean hasItem(@NonNull AmazonItem item) {

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        String[] select = {
                DBEntry._ID
        };

        Cursor cursor = sqLiteDatabase.query(
                DBEntry.TABLE,
                select,
                DBEntry.URL + "=?",
                new String[]{item.url},
                null,
                null,
                null);

        if (cursor.moveToNext()) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Removes an item from the database.
     * @param item  Item to remove.
     */
    public void removeItem(AmazonItem item) {

        String url = item.url;

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        sqLiteDatabase.delete(
                DBEntry.TABLE,
                DBEntry.URL + "=?",
                new String[]{url});
        sqLiteDatabase.close();

    }

    /**
     * Updates the items in the database using an array containing the updated items. Finally,
     * updates the timestamp of the last update.
     * @param items Array containing the updated items.
     */
    public void updateItems(AmazonItem[] items) {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        for (AmazonItem item : items) {

            ContentValues contentValues = new ContentValues();
            contentValues.put(DBEntry.TITLE, item.title);
            contentValues.put(DBEntry.CURRENT_PRICE, item.currentPrice);
            contentValues.put(DBEntry.PREVIOUS_PRICE, item.previousPrice);
            contentValues.put(DBEntry.URL, item.url);

            sqLiteDatabase.update(
                    DBEntry.TABLE,
                    contentValues,
                    DBEntry.URL + "=?",
                    new String[]{item.url}
            );

        }

        sqLiteDatabase.close();

        updateTimestamp();

    }

    /**
     * Removes all the items from the items table and sets the value of the timestamp of the last
     * update to -1.
     */
    public void emptyTable() {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.delete(DBEntry.TABLE, null, null);
        sqLiteDatabase.close();

        updateTimestamp();

    }

    /**
     * Sets the timestamp of the last update to the current time.
     */
    private void updateTimestamp() {

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(
                mContext.getString(R.string.sharedpref_file_name),
                Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(
                mContext.getString(R.string.sharedpref_key_last_update),
                new Date().getTime()
        );
        editor.apply();

    }

}
