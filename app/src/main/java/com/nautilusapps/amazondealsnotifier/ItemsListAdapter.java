package com.nautilusapps.amazondealsnotifier;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

/**
 * Adapter for the items list.
 */
public class ItemsListAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<AmazonItem> mAmazonItems;
    private RecyclerView mRecyclerView;

    /**
     * Represents a row of the list.
     */
    private class ItemHolder extends RecyclerView.ViewHolder {

        public TextView titleTextView, currentPriceTextView, priceVariationTextView;

        public ItemHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.item_text_title);
            currentPriceTextView = itemView.findViewById(R.id.item_text_current_price);
            priceVariationTextView = itemView.findViewById(R.id.item_text_price_variation);
        }

    }

    /**
     * Defines the on-click behavior for the items in the list.<br>
     * When an mItemToRemove is clicked launches the browser with the mItemToRemove url.
     */
    private class OnItemClickListener implements View.OnClickListener {

        @Override
        public void onClick(View itemView) {

            int itemPosition = mRecyclerView.getChildAdapterPosition(itemView);
            String url = mAmazonItems.get(itemPosition).url;

            if (url != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                itemView.getContext().startActivity(intent);
            }

        }

    }

    /**
     * Creates an {@code Adapter} for a list of items.
     * @param amazonItems   Items to show in the list.
     * @param recyclerView  Widget where to show the items.
     */
    public ItemsListAdapter(List<AmazonItem> amazonItems, RecyclerView recyclerView) {
        this.mAmazonItems = amazonItems;
        this.mRecyclerView = recyclerView;
        this.mContext = recyclerView.getContext();
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_row, parent, false);
        // Set the on-click behavior:
        itemView.setOnClickListener(new OnItemClickListener());

        return new ItemHolder(itemView);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        AmazonItem item = this.mAmazonItems.get(position);
        Resources resources = holder.itemView.getResources();
        String title = item.title;
        Float currentPrice = item.currentPrice;
        Float previousPrice = item.previousPrice;

        if (title != null) {
            ((ItemHolder) holder).titleTextView.setText(title);
        }

        if (currentPrice != null) {
            ((ItemHolder) holder).currentPriceTextView.setText(
                    String.format(
                            resources.getString(R.string.title_current_price),
                            String.format("%.2f", currentPrice)));
        }

        // If there is a previous price:
        if (currentPrice != null && previousPrice != null) {

            Float difference = currentPrice - previousPrice;
            Float percent =  difference / previousPrice * 100;
            String priceVariation = String.format("%.2f", difference);
            String priceVariationPercent = String.format("%.1f", percent);

            if (difference > 0) {
                priceVariation = "+" + priceVariation;
                priceVariationPercent = "+" + priceVariationPercent;
                ((ItemHolder) holder).priceVariationTextView
                        .setTextColor(resources.getColor(R.color.colorRed));
            } else if (difference < 0) {
                ((ItemHolder) holder).priceVariationTextView
                        .setTextColor(resources.getColor(R.color.colorGreen));
            } else {
                ((ItemHolder) holder).priceVariationTextView.setVisibility(View.GONE);
            }

            // Set the text:
            ((ItemHolder) holder).priceVariationTextView.setText(
                    String.format(
                            resources.getString(R.string.title_price_variation),
                            priceVariation,
                            priceVariationPercent));

        }

    }

    @Override
    public int getItemCount() { return mAmazonItems.size(); }

    /**
     * Removes an item from the list but not from the database.
     * @param position  Position of the mItemToRemove to remove.
     */
    private void removeItem(int position) {
        mAmazonItems.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    /**
     * Adds an item to the list, but not to the database.
     * @param position  Position of the item before it was removed.
     * @param item      Item to restore.
     */
    private void restoreItem(int position, AmazonItem item) {
        mAmazonItems.add(position, item);
        notifyItemInserted(position);
        notifyDataSetChanged();
    }

    /**
     * Removes temporarily an item from the list and shows a {@code Snackbar} with a button to
     * restore it. If the user clicks the button the item is added to the list again.
     * If the {@code Snackbar} is dismissed, the item is removed also from the database.
     * @param position  Position of the item to remove.
     * @param rootView  Root view.
     */
    public void pendingRemove(int position, View rootView) {

        class SnackbarUndoCallback extends Snackbar.Callback implements View.OnClickListener {

            private AmazonItem mItemToRemove;
            private int mItemPosition;
            private boolean remove;

            public SnackbarUndoCallback(AmazonItem itemToRemove, int itemPosition) {
                this.mItemToRemove = itemToRemove;
                this.mItemPosition = itemPosition;
                this.remove = true;
            }

            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {

                super.onDismissed(transientBottomBar, event);

                if (remove) {
                    DBHandler dbHandler = new DBHandler(mContext);
                    dbHandler.removeItem(mItemToRemove);
                }

            }

            @Override
            public void onClick(View v) {
                this.remove = false;
                restoreItem(mItemPosition, mItemToRemove);
            }

        }

        // Item to remove:
        AmazonItem item = mAmazonItems.get(position);
        // Remove the item from the list but not from the database:
        removeItem(position);

        Snackbar snackbar = Snackbar.make(
                rootView,
                mContext.getString(R.string.msg_item_removed),
                Snackbar.LENGTH_LONG
        );
        SnackbarUndoCallback snackbarUndoCallback = new SnackbarUndoCallback(item, position);
        snackbar.addCallback(snackbarUndoCallback);
        snackbar.setAction(
                mContext.getString(R.string.action_undo),
                snackbarUndoCallback
        );
        snackbar.show();

    }

}
