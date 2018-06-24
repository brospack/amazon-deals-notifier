package com.nautilusapps.amazondealsnotifier;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides methods to look up Amazon products.
 */
public class AmazonItemLookUp {

    /**
     * Instantiates a {@link org.jsoup.nodes.Document} from an URL. In case of an
     * {@code IOException}, the {@code Document} instance is set to {@code null}.
     */
    private class GetDocumentThread extends Thread {

        public Document result;
        private final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36";
        private final String REFERRER = "https://www.google.com";
        private String mUrl;

        public GetDocumentThread(String url) { this.mUrl = url; }

        public void run() {
            try {
                this.result = Jsoup.connect(this.mUrl)
                        .userAgent(this.USER_AGENT)
                        .referrer(this.REFERRER)
                        .get();
            } catch (IOException e) {
                this.result = null;
            }
        }

    }

    /**
     * Scrapes the price of an Amazon product from an instance of {@link org.jsoup.nodes.Document}.
     * If found, the result is a {@code Float} value, otherwise it's set to {@code null}.
     * If the {@code Document} instance is {@code null}, the result is set to {@code null}.
     */
    private class GetPriceThread extends Thread {

        public Float result;
        // Jsoup CSS queries:
        /** Selects the elements which contain the price on sale. */
        private final String DEAL_PRICE_CSS_QUERY = "span[id*=dealprice],span[id=priceblock_saleprice].a-size-medium.a-color-price,td.a-color-price.a-size-medium.a-align-bottom";
        /** Selects the elements which contain the current price. */
        private final String PRICE_CSS_QUERY = "span[id*=ourprice],span[id*=saleprice],span.a-size-large.a-color-result.guild_priceblock_ourprice,span.a-size-medium.a-color-price.offer-price.a-text-normal";
        private Document mDocument;

        public GetPriceThread(Document document) { this.mDocument = document; }

        public void run() {

            if (this.mDocument == null) {
                this.result = null;
                return;
            }

            String price;

            Elements elements = mDocument.select(DEAL_PRICE_CSS_QUERY);
            // Get the deal price, if there is one:
            if (elements != null && elements.size() > 0) {
                price = elements.get(0).text();
            } else {
                // If there isn't a deal price, get the current price:
                elements = mDocument.select(PRICE_CSS_QUERY);
                if (elements != null && elements.size() > 0) {
                    price = elements.get(0).text();
                } else {
                    price = null;
                }
            }

            // Get the price as a Float:
            this.result = parsePrice(price);

        }

        /**
         * Parses the price value from a {@code String}. The {@code String} must be in one of the
         * following formats:
         * <ul>
         *     <li>XY Z</li>
         *     <li>XY.Z</li>
         *     <li>XY,Z</li>
         * </ul>
         * Where:
         * <ul>
         *     <li>X is a currency symbol or string such as '$', '€', 'EUR'</li>
         *     <li>Y is the integer part of the price (must be less than a million if uses a point
         *     or a comma to separate each thousand)</li>
         *     <li>Z is the fractional part of the price</li>
         * </ul>
         * @param price The price to parse.
         * @return  The price value, as an instance of {@code Float}.<br>
         *          If the string is {@code null} or not in a valid format, returns {@code null}.
         */
        private Float parsePrice(String price) {

            if (price == null) {
                return null;
            }

            Pattern pattern = Pattern.compile("(\\d+[.,\\s]\\d+)");
            Matcher matcher = pattern.matcher(price);

            if (matcher.find()) {
                try {
                    price = matcher.group(1);
                    // Remove points, commas, spaces:
                    price = price.replaceAll("[.,\\s]", "");
                    // The price is the value divided by 100:
                    return Float.valueOf(price) / 100;
                } catch (IndexOutOfBoundsException | NumberFormatException e) {
                    return null;
                }
            } else {
                return null;
            }

        }

    }

    /**
     * Scrapes the title of an Amazon product from an instance of {@link org.jsoup.nodes.Document}.
     * If found, the result is a {@code String}, otherwise it's set to {@code null}. If the
     * {@code Document} instance is {@code null}, the result is set to {@code null}.
     */
    private class GetTitleThread extends Thread {

        public String result;
        // Jsoup CSS query:
        /** Selects the elements which contain the title. */
        private final String TITLE_CSS_QUERY = "span[id=ebooksProductTitle],span[id=productTitle]";
        private Document mDocument;

        public GetTitleThread(Document document) { this.mDocument = document; }

        public void run() {

            if (this.mDocument == null) {
                this.result = null;
                return;
            }

            Elements elements = mDocument.select(TITLE_CSS_QUERY);
            if (elements != null && elements.size() > 0) {
                result = elements.get(0).text();
            } else {
                result = null;
            }

        }

    }

    /** Max number of concurrent threads. */
    private static final int MAX_N_THREADS = 8;
    /** The products URLs. */
    private String[] mUrls;
    /** Array of {@link org.jsoup.nodes.Document} instantiated with {@link GetDocumentThread}. */
    private Document[] mDocuments;
    /** The result of the look up or the update. */
    private AmazonItem[] mAmazonItems;

    /**
     * Instantiates the class from an array of URLs.
     * @see #getAmazonItems()
     * @param urls  Array of URLs of the products to lookup.
     */
    public AmazonItemLookUp(String[] urls) {
        this.mUrls = urls;
        this.mDocuments = getDocuments(urls);
        this.mAmazonItems = null;
    }

    /**
     * Instantiates the class from an array of {@link AmazonItem}.
     * @see #updateAmazonItems()
     * @param amazonItems   Array of items to update.
     * @param maxDelay      Max time to wait between each connection, in millis.
     */
    public AmazonItemLookUp(AmazonItem[] amazonItems, Integer maxDelay) {

        // Get the items URLs:
        this.mUrls = new String[amazonItems.length];
        for (int i = 0; i < this.mUrls.length; i++) {
            this.mUrls[i] = amazonItems[i].url;
        }
        this.mDocuments = getDocuments(mUrls, maxDelay);
        this.mAmazonItems = amazonItems;

    }

    /**
     * Instantiates an array of {@link org.jsoup.nodes.Document} from an array of URLs by using a
     * {@code ThreadPool} of {@link GetDocumentThread}.<br>
     * Note: a {@code Document} is {@code null} in case of an {@code IOException}.
     * @param urls  Array of URLs.
     */
    private Document[] getDocuments(String[] urls) { return getDocuments(urls, null); }

    /**
     * Instantiates an array of {@link org.jsoup.nodes.Document} from an array of URLs by using a
     * {@code ThreadPool} of {@link GetDocumentThread}.<br>
     * Note: a {@code Document} is {@code null} in case of an {@code IOException}.
     * @param urls      Array of URLs.
     * @param maxDelay  Max time to wait between each connection, in millis.
     */
    private Document[] getDocuments(String[] urls, Integer maxDelay) {

        Document[] documents = new Document[urls.length];
        // Vector of references to the Threads:
        Thread[] getDocumentThreads = new Thread[urls.length];

        ExecutorService executorService = Executors.newFixedThreadPool(MAX_N_THREADS);

        for (int i = 0; i < getDocumentThreads.length; i++) {
            getDocumentThreads[i] = new GetDocumentThread(urls[i]);
            // Run the thread:
            executorService.execute(getDocumentThreads[i]);

            // Delay the execution of the next thread:
            if (maxDelay != null) {
                try {
                    // Randomize the delay:
                    Integer minDelay = maxDelay / 2;
                    Integer delay =
                            new Random().nextInt((maxDelay - minDelay) + 1) + minDelay;
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    // Ignore the sleep, if interrupted.
                }
            }

        }

        executorService.shutdown();

        // Wait for the threads to finish:
        while (!executorService.isTerminated()) {}

        // Get the threads results:
        for (int i = 0; i < getDocumentThreads.length; i++) {
            documents[i] = (((GetDocumentThread) getDocumentThreads[i]).result);
        }

        return documents;

    }

    /**
     * Returns an array containing the prices scraped from the array of
     * {@link org.jsoup.nodes.Document}.<br>
     * Note: a value is {@code null} if not found.
     * @param documents Array of {@code Document} from which to scrape the data.
     */
    private Float[] getPrices(Document[] documents) {

        Float[] prices = new Float[documents.length];
        Thread[] getPriceThreads = new Thread[documents.length];

        ExecutorService executorService = Executors.newFixedThreadPool(MAX_N_THREADS);

        for (int i = 0; i < documents.length; i++) {
            getPriceThreads[i] = new GetPriceThread(documents[i]);
            executorService.execute(getPriceThreads[i]);
        }

        executorService.shutdown();

        while (!executorService.isTerminated()) {}

        for (int i = 0; i < getPriceThreads.length; i++) {
            prices[i] = ((GetPriceThread) getPriceThreads[i]).result;
        }

        return prices;

    }

    /**
     * Returns an array containing the titles scraped from the array of
     * {@link org.jsoup.nodes.Document}.<br>
     * Note: a value is {@code null} if not found.
     */
    private String[] getTitles(Document[] documents) {

        String[] titles = new String[documents.length];
        Thread[] getTitleThreads = new Thread[documents.length];

        ExecutorService executorService = Executors.newFixedThreadPool(MAX_N_THREADS);

        for (int i = 0; i < documents.length; i++) {
            getTitleThreads[i] = new GetTitleThread(documents[i]);
            executorService.execute(getTitleThreads[i]);
        }

        executorService.shutdown();

        while (!executorService.isTerminated()) {}

        for (int i = 0; i < getTitleThreads.length; i++) {
            titles[i] = ((GetTitleThread) getTitleThreads[i]).result;
        }

        return titles;

    }

    /**
     * Scrapes all the data for the products requested and returns an array of {@link AmazonItem}.
     * If a {@link Document} is {@code null}, the corresponding item is set to {@code null}.
     * @see #AmazonItemLookUp(String[])
     * @see #getDocuments(String[])
     */
    public AmazonItem[] getAmazonItems() {

        this.mAmazonItems = new AmazonItem[this.mDocuments.length];
        Float[] prices = getPrices(this.mDocuments);
        String[] titles = getTitles(this.mDocuments);

        for (int i = 0; i < this.mAmazonItems.length; i++) {

            if (this.mDocuments[i] == null) {
                this.mAmazonItems[i] = null;
            } else {
                this.mAmazonItems[i] = new AmazonItem(titles[i], prices[i], this.mUrls[i]);
            }

        }

        return this.mAmazonItems;

    }

    /**
     * Scrapes only the data to update for the products requested and returns an array of
     * {@link AmazonItem} containing the updated items. If a {@link Document} is {@code null}, the
     * corresponding item is set to {@code null}.
     * @see #AmazonItemLookUp(AmazonItem[], Integer)
     * @see #getDocuments(String[], Integer)
     */
    public AmazonItem[] updateAmazonItems() {

        // If it was used the first constructor:
        if (this.mAmazonItems == null) {
            return getAmazonItems();
        }

        Float[] prices = getPrices(this.mDocuments);

        for (int i = 0; i < this.mAmazonItems.length; i++) {
            if (this.mDocuments[i] == null) {
                this.mAmazonItems[i] = null;
            } else {
                this.mAmazonItems[i] = this.mAmazonItems[i].updateItem(prices[i]);
            }
        }

        return this.mAmazonItems;

    }

}
