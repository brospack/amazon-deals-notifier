package com.nautilusapps.amazondealsnotifier;

import org.junit.Test;

import static org.junit.Assert.*;

public class AmazonItemTest {

    @Test
    public void updateItemTest() {

        AmazonItem item = new AmazonItem(
                "Item example title",
                100.0f,
                "https://example.com/itemexample");

        AmazonItem updatedItem = item.updateItem(50.0f);

        assertEquals(50.0f, updatedItem.currentPrice, 0);

    }

}