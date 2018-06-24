package com.nautilusapps.amazondealsnotifier;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DBHandlerTest {

    private DBHandler mDbHandler;

    @Before
    public void setUp() {
        InstrumentationRegistry.getTargetContext().deleteDatabase(DBHandler.DB_NAME);
        mDbHandler = new DBHandler(InstrumentationRegistry.getTargetContext());
    }

    @After
    public void tearDown() throws Exception {
        mDbHandler.close();
    }

    @Test
    public void getItems() {

        AmazonItem item1 = new AmazonItem(
                "title1",
                42.0f,
                "https://example.com/item1");
        AmazonItem item2 = new AmazonItem(
                "title2",
                43.0f,
                "https://example.com/item2");
        AmazonItem item3 = new AmazonItem(
                "title3",
                44.0f,
                "https://example.com/item3");

        mDbHandler.addItem(item1);
        mDbHandler.addItem(item2);
        mDbHandler.addItem(item3);

        assertTrue(mDbHandler.hasItem(item1));
        assertTrue(mDbHandler.hasItem(item2));
        assertTrue(mDbHandler.hasItem(item3));

        List<AmazonItem> items = mDbHandler.getItems();

        assertTrue(items.size() == 3);
        assertTrue(items.get(0).title.equals(item1.title));
        assertTrue(items.get(1).title.equals(item2.title));
        assertTrue(items.get(2).title.equals(item3.title));

    }

    @Test
    public void addItem() {

        AmazonItem example = new AmazonItem(
                "title",
                42.0f,
                "https://example.com/item");
        mDbHandler.addItem(example);

        List<AmazonItem> items = mDbHandler.getItems();

        assertTrue(items.size() == 1);
        assertTrue(items.get(0).title.equals(example.title));
        assertTrue(items.get(0).currentPrice.equals(example.currentPrice));
        assertTrue(items.get(0).url.equals(example.url));

    }

    @Test
    public void hasItem() {

        AmazonItem item1 = new AmazonItem(
                "title",
                42.0f,
                "https://example.com/item1");
        AmazonItem item2 = new AmazonItem(
                "title",
                42.0f,
                "https://example.com/item2");

        mDbHandler.addItem(item1);

        assertTrue(mDbHandler.hasItem(item1));
        assertFalse(mDbHandler.hasItem(item2));

    }

    @Test
    public void removeItem() {

        AmazonItem item = new AmazonItem(
                "title",
                42.0f,
                "https://example.com/item");
        mDbHandler.addItem(item);

        assertTrue(mDbHandler.hasItem(item));

        mDbHandler.removeItem(item);

        assertFalse(mDbHandler.hasItem(item));

    }

    @Test
    public void updateItems() {

        AmazonItem item1 = new AmazonItem(
                "title1",
                42.0f,
                "https://example.com/item1");
        AmazonItem item2 = new AmazonItem(
                "title2",
                42.0f,
                "https://example.com/item2");
        AmazonItem item3 = new AmazonItem(
                "title3",
                42.0f,
                "https://example.com/item3");

        mDbHandler.addItem(item1);
        mDbHandler.addItem(item2);
        mDbHandler.addItem(item3);
        assertTrue(mDbHandler.hasItem(item1));
        assertTrue(mDbHandler.hasItem(item2));
        assertTrue(mDbHandler.hasItem(item3));

        item1.currentPrice = 12.0f;
        item2.currentPrice = 23.0f;
        item2.previousPrice = 11.0f;
        AmazonItem[] updatedItems = { item1, item2 };
        mDbHandler.updateItems(updatedItems);

        List<AmazonItem> items = mDbHandler.getItems();
        for (AmazonItem item : items) {
            if (item.title.equals("title1")) {
                assertTrue(item.currentPrice.equals(12.0f));
            } else if (item.title.equals("title2")) {
                assertTrue(item.currentPrice.equals(23.0f));
                assertTrue(item.previousPrice != null);
            } else if (item.title.equals("title3")) {
                assertTrue(item.currentPrice.equals(42.0f));
            } else {
                assertFalse(true);
            }
        }

    }

    @Test
    public void emptyTable() {

        AmazonItem item1 = new AmazonItem(
                "title1",
                42.0f,
                "https://example.com/item1");
        AmazonItem item2 = new AmazonItem(
                "title2",
                42.0f,
                "https://example.com/item2");
        mDbHandler.addItem(item1);
        mDbHandler.addItem(item2);

        assertTrue(mDbHandler.hasItem(item1));
        assertTrue(mDbHandler.hasItem(item2));

        mDbHandler.emptyTable();

        assertFalse(mDbHandler.hasItem(item1));
        assertFalse(mDbHandler.hasItem(item2));
        assertTrue(mDbHandler.getItems().size() == 0);

    }

}