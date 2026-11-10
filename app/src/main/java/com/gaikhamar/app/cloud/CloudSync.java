package com.gaikhamar.app.cloud;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;

import androidx.sqlite.db.SupportSQLiteDatabase;

import com.gaikhamar.app.R;
import com.gaikhamar.app.data.AppDatabase;
import com.gaikhamar.app.util.AppPrefs;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Backs up every table to Firestore under farms/{uid}/{table}/{rowId}.
 * The users table is not uploaded, so password hashes stay on the phone.
 */
public final class CloudSync {

    public static final String[] TABLES = {"animals", "feed_types", "feed_logs", "feed_purchases",
            "weight_records", "batches", "milk_records", "breeding_records", "calving_records",
            "health_records", "vaccinations", "farm_contacts", "finance_records", "customers",
            "ledger_entries", "loans", "workers", "attendance"};

    public interface Listener {
        void onDone(boolean ok, int messageRes);
    }

    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final long TIMEOUT_SECONDS = 40;

    private CloudSync() {}

    public static boolean isOnline(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network n = cm.getActiveNetwork();
        if (n == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(n);
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    // ------------------------------------------------------------------ push

    public static void push(Context ctx, Listener listener) {
        final Context app = ctx.getApplicationContext();
        new Thread(() -> {
            boolean ok = false;
            int msg = R.string.sync_failed;
            try {
                String uid = CloudAuth.uid();
                if (uid == null) {
                    msg = R.string.sync_need_login;
                } else {
                    SupportSQLiteDatabase db = AppDatabase.get(app).getOpenHelper().getReadableDatabase();
                    FirebaseFirestore fs = FirebaseFirestore.getInstance();
                    for (String table : TABLES) pushTable(db, fs, uid, table);
                    new AppPrefs(app).setLastSync(System.currentTimeMillis());
                    ok = true;
                    msg = R.string.sync_done;
                }
            } catch (Exception e) {
                ok = false;
                msg = R.string.sync_failed;
            }
            final boolean fok = ok;
            final int fmsg = msg;
            if (listener != null) MAIN.post(() -> listener.onDone(fok, fmsg));
        }).start();
    }

    private static void pushTable(SupportSQLiteDatabase db, FirebaseFirestore fs, String uid, String table)
            throws Exception {
        CollectionReference coll = fs.collection("farms").document(uid).collection(table);
        Set<String> localIds = new HashSet<>();
        WriteBatch batch = fs.batch();
        int pending = 0;

        try (Cursor c = db.query("SELECT * FROM " + table)) {
            while (c.moveToNext()) {
                Map<String, Object> row = rowToMap(c);
                String id = String.valueOf(row.get("id"));
                localIds.add(id);
                batch.set(coll.document(id), row);
                if (++pending == 400) {
                    Tasks.await(batch.commit(), TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    batch = fs.batch();
                    pending = 0;
                }
            }
        }

        // Remove cloud rows that were deleted on this phone
        QuerySnapshot remote = Tasks.await(coll.get(), TIMEOUT_SECONDS, TimeUnit.SECONDS);
        for (DocumentSnapshot d : remote.getDocuments()) {
            if (!localIds.contains(d.getId())) {
                batch.delete(d.getReference());
                if (++pending == 400) {
                    Tasks.await(batch.commit(), TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    batch = fs.batch();
                    pending = 0;
                }
            }
        }
        if (pending > 0) Tasks.await(batch.commit(), TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private static Map<String, Object> rowToMap(Cursor c) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < c.getColumnCount(); i++) {
            String name = c.getColumnName(i);
            switch (c.getType(i)) {
                case Cursor.FIELD_TYPE_INTEGER: m.put(name, c.getLong(i)); break;
                case Cursor.FIELD_TYPE_FLOAT:   m.put(name, c.getDouble(i)); break;
                case Cursor.FIELD_TYPE_STRING:  m.put(name, c.getString(i)); break;
                default:                        m.put(name, null); break;
            }
        }
        return m;
    }

    // --------------------------------------------------------------- restore

    public static void restore(Context ctx, Listener listener) {
        final Context app = ctx.getApplicationContext();
        new Thread(() -> {
            boolean ok = false;
            int msg = R.string.sync_failed;
            try {
                String uid = CloudAuth.uid();
                if (uid == null) {
                    msg = R.string.sync_need_login;
                } else {
                    FirebaseFirestore fs = FirebaseFirestore.getInstance();

                    // Download everything first, so a failure leaves local data untouched
                    Map<String, List<Map<String, Object>>> data = new LinkedHashMap<>();
                    int total = 0;
                    for (String table : TABLES) {
                        QuerySnapshot qs = Tasks.await(
                                fs.collection("farms").document(uid).collection(table).get(),
                                TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        List<Map<String, Object>> rows = new ArrayList<>();
                        for (DocumentSnapshot d : qs.getDocuments()) {
                            Map<String, Object> m = d.getData();
                            if (m != null) rows.add(m);
                        }
                        total += rows.size();
                        data.put(table, rows);
                    }

                    if (total == 0) {
                        msg = R.string.sync_cloud_empty;
                    } else {
                        AppDatabase room = AppDatabase.get(app);
                        SupportSQLiteDatabase db = room.getOpenHelper().getWritableDatabase();
                        db.beginTransaction();
                        try {
                            for (Map.Entry<String, List<Map<String, Object>>> e : data.entrySet()) {
                                db.delete(e.getKey(), null, null);
                                for (Map<String, Object> row : e.getValue()) {
                                    db.insert(e.getKey(), SQLiteDatabase.CONFLICT_REPLACE, toValues(row));
                                }
                            }
                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }
                        room.getInvalidationTracker().refreshVersionsAsync();   // refresh the screens
                        ok = true;
                        msg = R.string.sync_restored;
                    }
                }
            } catch (Exception e) {
                ok = false;
                msg = R.string.sync_failed;
            }
            final boolean fok = ok;
            final int fmsg = msg;
            if (listener != null) MAIN.post(() -> listener.onDone(fok, fmsg));
        }).start();
    }

    private static ContentValues toValues(Map<String, Object> row) {
        ContentValues cv = new ContentValues();
        for (Map.Entry<String, Object> e : row.entrySet()) {
            Object v = e.getValue();
            if (v == null) cv.putNull(e.getKey());
            else if (v instanceof Long) cv.put(e.getKey(), (Long) v);
            else if (v instanceof Integer) cv.put(e.getKey(), (Integer) v);
            else if (v instanceof Double) cv.put(e.getKey(), (Double) v);
            else cv.put(e.getKey(), v.toString());
        }
        return cv;
    }
}
