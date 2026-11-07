package com.gaikhamar.app.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.gaikhamar.app.util.DateUtil;

public class FarmProvider extends ContentProvider {

    public static final String AUTHORITY = "com.gaikhamar.app.provider";
    public static final Uri URI_SUMMARY = Uri.parse("content://" + AUTHORITY + "/summary");
    public static final Uri URI_ANIMALS = Uri.parse("content://" + AUTHORITY + "/animals");

    private static final int CODE_SUMMARY = 1, CODE_ANIMALS = 2;
    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        MATCHER.addURI(AUTHORITY, "summary", CODE_SUMMARY);
        MATCHER.addURI(AUTHORITY, "animals", CODE_ANIMALS);
    }

    private static final String SUMMARY_SQL = "SELECT "
            + "(SELECT COUNT(*) FROM animals WHERE status = 'ACTIVE') AS animals, "
            + "(SELECT IFNULL(SUM(liters), 0) FROM milk_records WHERE date = ?) AS milk_today, "
            + "(SELECT COUNT(*) FROM feed_types WHERE stockKg <= lowStockLevelKg) AS low_stock, "
            + "(SELECT COUNT(*) FROM vaccinations WHERE nextDueDate != '' AND nextDueDate <= ?) AS vaccines_due, "
            + "(SELECT COUNT(*) FROM animals WHERE status = 'ACTIVE' AND IFNULL(expectedCalvingDate, '') != '' "
            + "AND expectedCalvingDate BETWEEN ? AND ?) AS calving_soon";

    private static final String ANIMALS_SQL = "SELECT id AS _id, farmCattleId, category, breed, "
            + "currentWeightKg, healthStatus FROM animals WHERE status = 'ACTIVE' ORDER BY id DESC";

    @Override
    public boolean onCreate() { return true; }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Context ctx = getContext();
        if (ctx == null) return null;
        SupportSQLiteDatabase db = AppDatabase.get(ctx).getOpenHelper().getReadableDatabase();
        switch (MATCHER.match(uri)) {
            case CODE_SUMMARY: {
                String today = DateUtil.today();
                return db.query(SUMMARY_SQL, new Object[]{today, DateUtil.plusDays(today, 7),
                        today, DateUtil.plusDays(today, 14)});
            }
            case CODE_ANIMALS:
                return db.query(ANIMALS_SQL);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (MATCHER.match(uri)) {
            case CODE_SUMMARY: return "vnd.android.cursor.item/vnd.gaikhamar.summary";
            case CODE_ANIMALS: return "vnd.android.cursor.dir/vnd.gaikhamar.animal";
            default: return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException("Read-only provider");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Read-only provider");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Read-only provider");
    }
}