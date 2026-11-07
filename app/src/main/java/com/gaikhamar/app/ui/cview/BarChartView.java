package com.gaikhamar.app.ui.cview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.gaikhamar.app.R;

/** Draws two bars (income and expense) for each month. */
public class BarChartView extends View {

    private final Paint incomePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint expensePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint();
    private final float density;
    private String[] labels = new String[0];
    private double[] income = new double[0];
    private double[] expense = new double[0];

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        incomePaint.setColor(context.getColor(R.color.green_700));
        expensePaint.setColor(context.getColor(R.color.red_600));
        textPaint.setColor(0xFF666666);
        textPaint.setTextSize(11 * density);
        textPaint.setTextAlign(Paint.Align.CENTER);
        axisPaint.setColor(0xFFBBBBBB);
    }

    public void setData(String[] labels, double[] income, double[] expense) {
        this.labels = labels;
        this.income = income;
        this.expense = expense;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        int n = labels.length;
        if (n == 0) return;

        float w = getWidth(), h = getHeight();
        float base = h - 22 * density;
        float chartH = base - 8 * density;

        double max = 1;
        for (int i = 0; i < n; i++) max = Math.max(max, Math.max(income[i], expense[i]));

        float slot = w / n;
        float barW = slot * 0.28f;
        c.drawLine(0, base, w, base, axisPaint);

        for (int i = 0; i < n; i++) {
            float cx = slot * i + slot / 2;
            float ih = (float) (income[i] / max * chartH);
            float eh = (float) (expense[i] / max * chartH);
            c.drawRect(cx - barW - 2, base - ih, cx - 2, base, incomePaint);
            c.drawRect(cx + 2, base - eh, cx + barW + 2, base, expensePaint);
            c.drawText(labels[i], cx, h - 6 * density, textPaint);
        }
    }
}