package com.nautilusapps.amazondealsnotifier;

import android.provider.BaseColumns;

/**
 * Represents a row of the items table.
 */
public class DBEntry implements BaseColumns {
    public static final String TABLE = "item";
    public static final String TITLE = "title";
    public static final String CURRENT_PRICE = "current_price";
    public static final String PREVIOUS_PRICE = "previous_price";
    public static final String URL = "url";
}
