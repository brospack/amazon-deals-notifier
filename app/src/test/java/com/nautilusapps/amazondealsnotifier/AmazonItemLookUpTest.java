package com.nautilusapps.amazondealsnotifier;

import org.junit.Test;

public class AmazonItemLookUpTest {

    final String[] itemsExamples = {
            "https://www.amazon.it/Samsung-UE55NU7370UXZT-Smart-Tecnologia-Nero/dp/B07D95FNRK/ref=sr_1_1?s=electronics&ie=UTF8&qid=1529840501&sr=1-1",
            "https://www.amazon.it/Argoclima-Climatizzatore-Portatile-Pannello-Digitale/dp/B00WCUEXGS/ref=br_msw_pdt-2?_encoding=UTF8&smid=A11IL2PNWYJU7H&pf_rd_m=A11IL2PNWYJU7H&pf_rd_s=&pf_rd_r=9T3SAP35BY1X2AB1FVZ1&pf_rd_t=36701&pf_rd_p=6a93360c-f4c9-41ac-b0f5-17065ecd1083&pf_rd_i=desktop",
            "https://www.amazon.it/HP-15-bs536nl-Notebook-Processore-i7-7500U/dp/B07BPBPTVV/ref=br_msw_pdt-3?_encoding=UTF8&smid=A11IL2PNWYJU7H&pf_rd_m=A11IL2PNWYJU7H&pf_rd_s=&pf_rd_r=9T3SAP35BY1X2AB1FVZ1&pf_rd_t=36701&pf_rd_p=6a93360c-f4c9-41ac-b0f5-17065ecd1083&pf_rd_i=desktop",
            "https://www.amazon.it/gp/product/B00BAJ6GL2/ref=s9u_wish_gw_i1?ie=UTF8&colid=WD2VKYS8SBWE&coliid=I3D26UNH6CTZB4&pd_rd_i=B00BAJ6GL2&pd_rd_r=8c8ac823-77a3-11e8-8019-5bcbb218b215&pd_rd_w=QnroE&pd_rd_wg=YaSzo&pf_rd_m=A11IL2PNWYJU7H&pf_rd_s=&pf_rd_r=9T3SAP35BY1X2AB1FVZ1&pf_rd_t=36701&pf_rd_p=65dab04e-e167-43b2-8a87-d61d89060235&pf_rd_i=desktop",
            "https://www.amazon.it/gp/product/B000OCXIRG/ref=s9u_wish_gw_i2?ie=UTF8&colid=WD2VKYS8SBWE&coliid=I2S7OCYQ8H8KDI&pd_rd_i=B000OCXIRG&pd_rd_r=8c8ac823-77a3-11e8-8019-5bcbb218b215&pd_rd_w=QnroE&pd_rd_wg=YaSzo&pf_rd_m=A11IL2PNWYJU7H&pf_rd_s=&pf_rd_r=9T3SAP35BY1X2AB1FVZ1&pf_rd_t=36701&pf_rd_p=65dab04e-e167-43b2-8a87-d61d89060235&pf_rd_i=desktop",
            "https://www.amazon.it/gp/product/B001BANK32/ref=s9u_wish_gw_i3?ie=UTF8&colid=WD2VKYS8SBWE&coliid=I2HSKY2QF0KALW&pd_rd_i=B001BANK32&pd_rd_r=8c8ac823-77a3-11e8-8019-5bcbb218b215&pd_rd_w=QnroE&pd_rd_wg=YaSzo&pf_rd_m=A11IL2PNWYJU7H&pf_rd_s=&pf_rd_r=9T3SAP35BY1X2AB1FVZ1&pf_rd_t=36701&pf_rd_p=65dab04e-e167-43b2-8a87-d61d89060235&pf_rd_i=desktop",
            "https://www.amazon.it/gp/product/B00JAKBZGY/ref=s9_acsd_al_bw_c_x_1_w?pf_rd_m=A11IL2PNWYJU7H&pf_rd_s=merchandised-search-5&pf_rd_r=67FS9KPE1KBGSFYRAQ0A&pf_rd_r=67FS9KPE1KBGSFYRAQ0A&pf_rd_t=101&pf_rd_p=b1436fc9-5692-427d-88bd-7049cba856af&pf_rd_p=b1436fc9-5692-427d-88bd-7049cba856af&pf_rd_i=10675215031",
            "https://www.amazon.it/gp/product/B00QJEL42Y/ref=s9_acsd_al_bw_c_x_2_w?pf_rd_m=A11IL2PNWYJU7H&pf_rd_s=merchandised-search-5&pf_rd_r=67FS9KPE1KBGSFYRAQ0A&pf_rd_r=67FS9KPE1KBGSFYRAQ0A&pf_rd_t=101&pf_rd_p=b1436fc9-5692-427d-88bd-7049cba856af&pf_rd_p=b1436fc9-5692-427d-88bd-7049cba856af&pf_rd_i=10675215031",
            "https://www.amazon.it/gp/product/B06XWVP8N6/ref=s9_acsd_al_bw_c_x_3_w?pf_rd_m=A11IL2PNWYJU7H&pf_rd_s=merchandised-search-5&pf_rd_r=67FS9KPE1KBGSFYRAQ0A&pf_rd_r=67FS9KPE1KBGSFYRAQ0A&pf_rd_t=101&pf_rd_p=b1436fc9-5692-427d-88bd-7049cba856af&pf_rd_p=b1436fc9-5692-427d-88bd-7049cba856af&pf_rd_i=10675215031",
            "https://www.amazon.it/Camicia-Elastica-Manica-Casual-Formale/dp/B0747HC5TT/ref=lp_2892940031_1_4?s=apparel&ie=UTF8&qid=1529840540&sr=1-4&th=1&psc=1",
            "https://www.amazon.it/Puro-Lino-Camicia-Taschino-Primavera/dp/B07CGS71WP/ref=lp_2892940031_1_5?s=apparel&ie=UTF8&qid=1529840540&sr=1-5",
            "https://www.amazon.it/Puro-Lino-Camicia-Taschino-Primavera/dp/B07CJSYDW1/ref=lp_2892940031_1_5?s=apparel&ie=UTF8&qid=1529840540&sr=1-5&th=1&psc=1",
            "https://www.amazon.it/gp/product/B00P9DU6K2/ref=s9_acsd_top_hd_bw_blPnxP_c_x_1_w?pf_rd_m=A11IL2PNWYJU7H&pf_rd_s=merchandised-search-4&pf_rd_r=CBJQWCK0QF6E5G6XEC11&pf_rd_t=101&pf_rd_p=aa4f6e7d-c4f8-5bd7-8f7d-94242f7d0587&pf_rd_i=700638031&th=1&psc=1",
            "https://www.amazon.it/Death-Song-Paul-Cain/dp/0940941066/ref=tmm_hrd_swatch_0?_encoding=UTF8&coliid=I3D26UNH6CTZB4&colid=WD2VKYS8SBWE&qid=&sr="
    };

    final String[] booksExamples = {
            "https://www.amazon.it/gp/product/B00BAJ6GL2/ref=s9u_wish_gw_i1?ie=UTF8&colid=WD2VKYS8SBWE&coliid=I3D26UNH6CTZB4&pd_rd_i=B00BAJ6GL2&pd_rd_r=8c8ac823-77a3-11e8-8019-5bcbb218b215&pd_rd_w=QnroE&pd_rd_wg=YaSzo&pf_rd_m=A11IL2PNWYJU7H&pf_rd_s=&pf_rd_r=9T3SAP35BY1X2AB1FVZ1&pf_rd_t=36701&pf_rd_p=65dab04e-e167-43b2-8a87-d61d89060235&pf_rd_i=desktop",
            "https://www.amazon.it/gp/product/B000OCXIRG/ref=s9u_wish_gw_i2?ie=UTF8&colid=WD2VKYS8SBWE&coliid=I2S7OCYQ8H8KDI&pd_rd_i=B000OCXIRG&pd_rd_r=8c8ac823-77a3-11e8-8019-5bcbb218b215&pd_rd_w=QnroE&pd_rd_wg=YaSzo&pf_rd_m=A11IL2PNWYJU7H&pf_rd_s=&pf_rd_r=9T3SAP35BY1X2AB1FVZ1&pf_rd_t=36701&pf_rd_p=65dab04e-e167-43b2-8a87-d61d89060235&pf_rd_i=desktop",
            "https://www.amazon.it/gp/product/B001BANK32/ref=s9u_wish_gw_i3?ie=UTF8&colid=WD2VKYS8SBWE&coliid=I2HSKY2QF0KALW&pd_rd_i=B001BANK32&pd_rd_r=8c8ac823-77a3-11e8-8019-5bcbb218b215&pd_rd_w=QnroE&pd_rd_wg=YaSzo&pf_rd_m=A11IL2PNWYJU7H&pf_rd_s=&pf_rd_r=9T3SAP35BY1X2AB1FVZ1&pf_rd_t=36701&pf_rd_p=65dab04e-e167-43b2-8a87-d61d89060235&pf_rd_i=desktop",
            "https://www.amazon.it/Death-Song-Paul-Cain/dp/0940941066/ref=tmm_hrd_swatch_0?_encoding=UTF8&coliid=I3D26UNH6CTZB4&colid=WD2VKYS8SBWE&qid=&sr="
    };

    final String[] clothingExamples = {
            "https://www.amazon.it/Camicia-Elastica-Manica-Casual-Formale/dp/B0747HC5TT/ref=lp_2892940031_1_4?s=apparel&ie=UTF8&qid=1529840540&sr=1-4&th=1&psc=1",
            "https://www.amazon.it/Puro-Lino-Camicia-Taschino-Primavera/dp/B07CGS71WP/ref=lp_2892940031_1_5?s=apparel&ie=UTF8&qid=1529840540&sr=1-5",
            "https://www.amazon.it/Puro-Lino-Camicia-Taschino-Primavera/dp/B07CJSYDW1/ref=lp_2892940031_1_5?s=apparel&ie=UTF8&qid=1529840540&sr=1-5&th=1&psc=1",
            "https://www.amazon.it/gp/product/B00P9DU6K2/ref=s9_acsd_top_hd_bw_blPnxP_c_x_1_w?pf_rd_m=A11IL2PNWYJU7H&pf_rd_s=merchandised-search-4&pf_rd_r=CBJQWCK0QF6E5G6XEC11&pf_rd_t=101&pf_rd_p=aa4f6e7d-c4f8-5bd7-8f7d-94242f7d0587&pf_rd_i=700638031&th=1&psc=1"
    };

    public void getAmazonItems(String[] urls) {

        AmazonItemLookUp lookUp = new AmazonItemLookUp(urls);

        AmazonItem[] items = lookUp.getAmazonItems();

        for (int i = 0; i < items.length; i++) {
            System.out.printf("Item %d\n", i + 1);
            System.out.printf("Title: %s\n", items[i].title);
            System.out.printf("URL: %s\n", items[i].url);
            System.out.printf("Previous price: %f\n", items[i].previousPrice);
            System.out.printf("Current price: %f\n\n", items[i].currentPrice);
        }

        System.out.printf("Number of items: %d", urls.length);

    }

    @Test
    public void getAmazonItemsTest() {

        getAmazonItems(itemsExamples);

    }

    @Test
    public void updateAmazonItemsTest() {

        AmazonItem[] items = new AmazonItemLookUp(itemsExamples).getAmazonItems();
        AmazonItemLookUp itemLookUp = new AmazonItemLookUp(items, 500);
        AmazonItem[] updatedItems = itemLookUp.updateAmazonItems();

    }

}