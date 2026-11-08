package com.example.kasku;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/** Database SQLite untuk riwayat transaksi (data banyak, bertambah terus). */
public class DbHelper extends SQLiteOpenHelper {

    public DbHelper(Context context) {
        super(context, "kasku.db", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE transaksi (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "keterangan TEXT, nominal INTEGER, tanggal TEXT, " +
                "kategori TEXT DEFAULT 'Lain-lain', waktu TEXT DEFAULT '')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) { // data lama tetap aman
            db.execSQL("ALTER TABLE transaksi ADD COLUMN kategori TEXT DEFAULT 'Lain-lain'");
            db.execSQL("ALTER TABLE transaksi ADD COLUMN waktu TEXT DEFAULT ''");
        }
    }

    public void tambah(Transaksi t) {
        ContentValues v = new ContentValues();
        v.put("keterangan", t.keterangan);
        v.put("nominal", t.nominal);
        v.put("tanggal", t.tanggal);
        v.put("kategori", t.kategori);
        v.put("waktu", t.waktu);
        getWritableDatabase().insert("transaksi", null, v);
    }

    public void hapus(long id) {
        getWritableDatabase().delete("transaksi", "id=?", new String[]{String.valueOf(id)});
    }

    /** pola: "2026-11-08" untuk satu hari, atau "2026-11-%" untuk satu bulan */
    public long totalPeriode(String pola) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(nominal),0) FROM transaksi WHERE tanggal LIKE ?",
                new String[]{pola});
        c.moveToFirst();
        long total = c.getLong(0);
        c.close();
        return total;
    }

    public ArrayList<Transaksi> daftarPeriode(String pola) {
        ArrayList<Transaksi> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, keterangan, nominal, tanggal, kategori, waktu " +
                "FROM transaksi WHERE tanggal LIKE ? ORDER BY tanggal DESC, id DESC",
                new String[]{pola});
        while (c.moveToNext()) {
            String kat = c.getString(4);
            String wkt = c.getString(5);
            list.add(new Transaksi(c.getLong(0), c.getString(1), c.getInt(2), c.getString(3),
                    kat == null ? "Lain-lain" : kat, wkt == null ? "" : wkt));
        }
        c.close();
        return list;
    }
}
