package com.gaikhamar.app.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.entity.CategoryTotal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public final class PdfReport {
    private static final int W = 595, H = 842, M = 40;

    private PdfReport() {}

    public static void create(Context c, File out, String farm, String period, double income, double expense,
                              List<CategoryTotal> incomeCats, List<CategoryTotal> expenseCats) throws IOException {
        PdfDocument doc = new PdfDocument();
        Pager p = new Pager(doc);
        p.line(farm, 20, true);
        p.line(c.getString(R.string.r_title) + ": " + period, 14, false);
        p.gap();
        p.row(c.getString(R.string.income), money(c, income), 14, true);
        p.row(c.getString(R.string.expense), money(c, expense), 14, true);
        p.row(c.getString(R.string.profit), money(c, income - expense), 14, true);
        p.gap();
        p.line(c.getString(R.string.r_income_by), 14, true);
        for (CategoryTotal t : incomeCats) {
            p.row(LabelHelper.finCategory(c, t.category), money(c, t.total), 12, false);
        }
        p.gap();
        p.line(c.getString(R.string.r_expense_by), 14, true);
        for (CategoryTotal t : expenseCats) {
            p.row(LabelHelper.finCategory(c, t.category), money(c, t.total), 12, false);
        }
        p.finish();
        try (FileOutputStream fos = new FileOutputStream(out)) {
            doc.writeTo(fos);
        }
        doc.close();
    }

    private static String money(Context c, double v) {
        return c.getString(R.string.fmt_money, NumFmt.n(v));
    }

    private static class Pager {
        final PdfDocument doc;
        PdfDocument.Page page;
        Canvas canvas;
        int no = 0;
        float y;
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        Pager(PdfDocument d) {
            doc = d;
            newPage();
        }

        void newPage() {
            if (page != null) doc.finishPage(page);
            no++;
            page = doc.startPage(new PdfDocument.PageInfo.Builder(W, H, no).create());
            canvas = page.getCanvas();
            y = M + 10;
        }

        void ensure(float h) {
            if (y + h > H - M) newPage();
        }

        void line(String t, float size, boolean bold) {
            paint.setTextSize(size);
            paint.setFakeBoldText(bold);
            ensure(size + 8);
            canvas.drawText(t, M, y, paint);
            y += size + 8;
        }

        void row(String left, String right, float size, boolean bold) {
            paint.setTextSize(size);
            paint.setFakeBoldText(bold);
            ensure(size + 8);
            canvas.drawText(left, M, y, paint);
            canvas.drawText(right, W - M - paint.measureText(right), y, paint);
            y += size + 8;
        }

        void gap() { y += 10; }

        void finish() { doc.finishPage(page); }
    }
}