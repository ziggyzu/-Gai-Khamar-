package com.gaikhamar.app.system;

import android.content.Context;
import android.text.TextUtils;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.ReminderCounts;

import java.util.ArrayList;
import java.util.List;

public final class ReminderHelper {
    private ReminderHelper() {}

    /** Returns null when there is nothing to remind about. */
    public static String format(Context c, ReminderCounts rc) {
        List<String> parts = new ArrayList<>();
        if (rc.vaccinesDue > 0) parts.add(c.getString(R.string.rem_vaccines, String.valueOf(rc.vaccinesDue)));
        if (rc.calvingSoon > 0) parts.add(c.getString(R.string.rem_calving, String.valueOf(rc.calvingSoon)));
        if (rc.lowStock > 0) parts.add(c.getString(R.string.rem_lowstock, String.valueOf(rc.lowStock)));
        if (rc.followUps > 0) parts.add(c.getString(R.string.rem_followups, String.valueOf(rc.followUps)));
        if (rc.withdrawals > 0) parts.add(c.getString(R.string.rem_withdrawals, String.valueOf(rc.withdrawals)));
        return parts.isEmpty() ? null : TextUtils.join("\n", parts);
    }
}