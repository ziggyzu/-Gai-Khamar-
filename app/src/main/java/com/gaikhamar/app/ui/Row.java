package com.gaikhamar.app.ui;

/** One line in a generic list. Many screens turn their data into Rows. */
public class Row {
    public final long id;
    public final String title, subtitle, right, rightSub;
    public final int rightColor;   // 0 = default text color
    public final Object tag;

    public Row(long id, String title, String subtitle, String right, String rightSub, int rightColor, Object tag) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.right = right;
        this.rightSub = rightSub;
        this.rightColor = rightColor;
        this.tag = tag;
    }
}