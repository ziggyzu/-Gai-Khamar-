package com.gaikhamar.app.ui.cview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gaikhamar.app.R;
import com.google.android.material.card.MaterialCardView;

/** Dashboard tile showing a big number and a label. */
public class CountTileView extends MaterialCardView {

    private final TextView tvCount;

    public CountTileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        float d = getResources().getDisplayMetrics().density;
        setRadius(12 * d);
        setCardElevation(2 * d);

        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        int pad = (int) (16 * d);
        box.setPadding(pad, pad, pad, pad);

        tvCount = new TextView(context);
        tvCount.setText("0");
        tvCount.setTextSize(28);
        tvCount.setTypeface(null, Typeface.BOLD);
        tvCount.setTextColor(context.getColor(R.color.green_900));

        TextView tvLabel = new TextView(context);
        tvLabel.setTextSize(14);

        TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.CountTileView);
        tvLabel.setText(t.getString(R.styleable.CountTileView_tileLabel));
        t.recycle();

        box.addView(tvCount);
        box.addView(tvLabel);
        addView(box);
    }

    public void setCount(int count) {
        tvCount.setText(String.valueOf(count));
    }
}
