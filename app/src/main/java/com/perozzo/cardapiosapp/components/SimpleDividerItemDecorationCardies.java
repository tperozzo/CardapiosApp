package com.perozzo.cardapiosapp.components;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

import com.perozzo.cardapiosapp.R;

/**
 * Created by Perozzo on 06/06/2017.
 */

public class SimpleDividerItemDecorationCardies extends RecyclerView.ItemDecoration {
    private Drawable mDivider;
    public Context mContext;

    public SimpleDividerItemDecorationCardies(Context context) {
        mContext = context;
        mDivider = context.getResources().getDrawable(R.drawable.line_divider);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            Resources r = mContext.getResources();
            int px  = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    16,
                    r.getDisplayMetrics()
            );

            mDivider.setBounds(left + px, top, right - px, bottom);
            mDivider.draw(c);
        }
    }
}
