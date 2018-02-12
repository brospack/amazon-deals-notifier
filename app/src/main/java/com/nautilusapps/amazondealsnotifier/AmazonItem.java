package com.nautilusapps.amazondealsnotifier;

/**
 * Represents an Amazon item.
 */
public class AmazonItem {

    /** Title of the item. */
    public String title;
    /** Current price of the item. */
    public Float currentPrice;
    /** Price of the item since the last update. */
    public Float previousPrice;
    /** URL of the item. */
    public String url;

    /**
     * Instantiates an item.<br>
     * {@link #previousPrice} is set to {@code null}.
     * @param title         Title of the item.
     * @param currentPrice  Current price of the item.
     * @param url           URL of the item.
     */
    public AmazonItem(String title, Float currentPrice, String url) {
        this.title = title;
        this.currentPrice = currentPrice;
        this.previousPrice = null;
        this.url = url;
    }

    /**
     * Instantiates a new item from its data.
     * @param title         Title of the item.
     * @param currentPrice  Current price of the item.
     * @param previousPrice Price of the item since the last update.
     * @param url           URL of the item.
     */
    public AmazonItem(String title, Float currentPrice, Float previousPrice, String url) {
        this.title = title;
        this.currentPrice = currentPrice;
        this.previousPrice = previousPrice;
        this.url = url;
    }

    /**
     * Returns a new instance of {@code AmazonItem} with the updated current price.
     * @param newPrice  New price of the item.
     */
    public AmazonItem updateItem(Float newPrice) {
        this.previousPrice = this.currentPrice;
        this.currentPrice = newPrice;
        return this;
    }

}
