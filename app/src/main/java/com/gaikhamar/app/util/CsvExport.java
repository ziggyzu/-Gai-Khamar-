package com.gaikhamar.app.util;

import com.gaikhamar.app.data.entity.FinanceRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class CsvExport {
    private CsvExport() {}

    public static void write(File out, List<FinanceRecord> list) throws IOException {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8)) {
            w.write("\uFEFF");   // lets Excel read Bangla text correctly
            w.write("date,type,category,amount,note,animal_id\n");
            for (FinanceRecord r : list) {
                w.write(r.date + "," + r.type + "," + r.category + "," + r.amount + ","
                        + esc(r.note) + "," + r.animalId + "\n");
            }
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        String t = s.replace("\"", "\"\"");
        return (t.contains(",") || t.contains("\n") || t.contains("\"")) ? "\"" + t + "\"" : t;
    }
}