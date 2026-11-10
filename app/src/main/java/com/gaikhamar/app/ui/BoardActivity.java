package com.gaikhamar.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gaikhamar.app.R;
import com.gaikhamar.app.cloud.CloudAuth;
import com.gaikhamar.app.databinding.ActivityListBinding;
import com.gaikhamar.app.util.FormDialog;
import com.gaikhamar.app.util.NumFmt;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Live shared lists in Firebase Realtime Database: "prices" or "tasks". */
public class BoardActivity extends BaseActivity {

    public static final String EXTRA_KIND = "kind";

    private ActivityListBinding b;
    private boolean prices;
    private DatabaseReference ref;
    private ValueEventListener listener;
    private RowAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        prices = "prices".equals(getIntent().getStringExtra(EXTRA_KIND));
        b.tvTitle.setText(prices ? R.string.market_prices : R.string.farm_tasks);
        b.tvEmpty.setText(prices ? R.string.no_prices : R.string.no_tasks);

        String uid = CloudAuth.uid();
        if (uid == null) {
            fail(R.string.no_cloud_account);
            return;
        }
        try {
            ref = FirebaseDatabase.getInstance().getReference("farms").child(uid).child(prices ? "prices" : "tasks");
        } catch (Exception e) {
            fail(R.string.board_error);
            return;
        }

        adapter = new RowAdapter(this::onRowClick);
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);
        b.fab.setOnClickListener(v -> {
            if (prices) showAddPrice(); else showAddTask();
        });

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Row> rows = new ArrayList<>();
                int i = 0;
                for (DataSnapshot c : snapshot.getChildren()) {
                    rows.add(prices ? priceRow(i++, c) : taskRow(i++, c));
                }
                Collections.reverse(rows);   // newest first
                adapter.submitList(rows);
                b.tvEmpty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BoardActivity.this, R.string.board_error, Toast.LENGTH_SHORT).show();
            }
        };
        ref.addValueEventListener(listener);   // updates arrive live
    }

    private void fail(int msgRes) {
        b.fab.setVisibility(View.GONE);
        b.tvEmpty.setText(msgRes);
        b.tvEmpty.setVisibility(View.VISIBLE);
    }

    private Row priceRow(int id, DataSnapshot c) {
        String item = c.child("item").getValue(String.class);
        Double price = c.child("price").getValue(Double.class);
        String date = c.child("date").getValue(String.class);
        String note = c.child("note").getValue(String.class);

        String[] codes = getResources().getStringArray(R.array.price_item_codes);
        String[] labels = getResources().getStringArray(R.array.price_item_labels);
        String title = item == null ? "" : item;
        for (int i = 0; i < codes.length && i < labels.length; i++) {
            if (codes[i].equals(item)) title = labels[i];
        }
        String sub = (date == null ? "" : date) + (note == null || note.isEmpty() ? "" : "  |  " + note);
        return new Row(id, title, sub, getString(R.string.fmt_money, NumFmt.n(price == null ? 0 : price)),
                "", 0, new String[]{c.getKey(), "0"});
    }

    private Row taskRow(int id, DataSnapshot c) {
        String title = c.child("title").getValue(String.class);
        String who = c.child("assignee").getValue(String.class);
        Boolean done = c.child("done").getValue(Boolean.class);
        boolean isDone = done != null && done;
        return new Row(id, title == null ? "" : title, who == null ? "" : who,
                getString(isDone ? R.string.task_done : R.string.task_todo), "",
                getColor(isDone ? R.color.green_700 : R.color.amber_700),
                new String[]{c.getKey(), isDone ? "1" : "0"});
    }

    private void onRowClick(Row row) {
        String[] tag = (String[]) row.tag;
        String key = tag[0];
        if (prices) {
            new MaterialAlertDialogBuilder(this)
                    .setItems(new String[]{getString(R.string.delete)},
                            (d, which) -> ref.child(key).removeValue())
                    .show();
        } else {
            boolean done = "1".equals(tag[1]);
            String[] items = {getString(done ? R.string.task_mark_todo : R.string.task_mark_done),
                    getString(R.string.delete)};
            new MaterialAlertDialogBuilder(this)
                    .setItems(items, (d, which) -> {
                        if (which == 0) ref.child(key).child("done").setValue(!done);
                        else ref.child(key).removeValue();
                    })
                    .show();
        }
    }

    private void showAddPrice() {
        String[] codes = getResources().getStringArray(R.array.price_item_codes);
        String[] labels = getResources().getStringArray(R.array.price_item_labels);
        new FormDialog.Builder(this, R.string.add_price)
                .date("date", R.string.date)
                .choice("item", R.string.price_item, labels)
                .number("price", R.string.price_value)
                .text("note", R.string.note)
                .show(v -> {
                    if (v.num("price") <= 0) {
                        Toast.makeText(this, R.string.err_price, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    Map<String, Object> m = new HashMap<>();
                    m.put("item", codes[v.choice("item")]);
                    m.put("price", v.num("price"));
                    m.put("date", v.str("date"));
                    m.put("note", v.str("note"));
                    ref.push().setValue(m);
                    return true;
                });
    }

    private void showAddTask() {
        new FormDialog.Builder(this, R.string.add_task)
                .text("title", R.string.task_title)
                .text("who", R.string.assignee)
                .show(v -> {
                    if (v.str("title").isEmpty()) {
                        Toast.makeText(this, R.string.err_task, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    Map<String, Object> m = new HashMap<>();
                    m.put("title", v.str("title"));
                    m.put("assignee", v.str("who"));
                    m.put("done", false);
                    ref.push().setValue(m);
                    return true;
                });
    }

    @Override
    protected void onDestroy() {
        if (ref != null && listener != null) ref.removeEventListener(listener);
        super.onDestroy();
    }
}