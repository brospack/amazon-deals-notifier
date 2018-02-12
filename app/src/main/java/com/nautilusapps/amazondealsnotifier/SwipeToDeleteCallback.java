package com.nautilusapps.amazondealsnotifier;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private Drawable mBackground;
    private ItemsListAdapter mAdapter;

    public SwipeToDeleteCallback(Context context, ItemsListAdapter adapter) {
        super(0, ItemTouchHelper.LEFT);
        Resources resources = context.getResources();
        this.mAdapter = adapter;
        this.mBackground = new ColorDrawable(resources.getColor(R.color.colorRed));
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int itemPosition = viewHolder.getAdapterPosition();
        View rootView = viewHolder.itemView.getRootView();
        mAdapter.pendingRemove(itemPosition, rootView);
    }

    /**
     * Draws a background under the item row.
     */
    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        View itemView = viewHolder.itemView;

        mBackground.setBounds(
                itemView.getRight() + (int) dX,
                itemView.getTop(),
                itemView.getRight(),
                itemView.getBottom()
        );
        mBackground.draw(c);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

    }
}
