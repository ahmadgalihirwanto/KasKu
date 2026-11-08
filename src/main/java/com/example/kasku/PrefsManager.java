package com.example.kasku;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Pembungkus SharedPreferences untuk pengaturan KasKu.
 * File XML tersimpan di: /data/data/com.example.kasku/shared_prefs/kasku_prefs.xml
 */
public class PrefsManager {
    private static final String PREF_NAME     = "kasku_prefs";
    private static final String KEY_NAMA      = "nama_panggilan";
    private static final String KEY_BATAS     = "batas_harian";
    private static final String KEY_GELAP     = "tema_gelap";
    private static final String KEY_KATEGORI  = "kategori_terakhir";
    private static final String KEY_MODE      = "mode_bulan";
    private static final String KEY_BUDGET    = "budget_bulanan";

    private final SharedPreferences prefs;

    public PrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // apply() menulis ke disk di thread latar, jadi UI tidak tersendat
    public void simpanNama(String nama)       { prefs.edit().putString(KEY_NAMA, nama).apply(); }
    public void simpanBatas(int batas)        { prefs.edit().putInt(KEY_BATAS, batas).apply(); }
    public void simpanTemaGelap(boolean g)    { prefs.edit().putBoolean(KEY_GELAP, g).apply(); }
    public void simpanKategori(String k)      { prefs.edit().putString(KEY_KATEGORI, k).apply(); }
    public void simpanModeBulan(boolean b)    { prefs.edit().putBoolean(KEY_MODE, b).apply(); }
    public void simpanBudgetBulan(int b)      { prefs.edit().putInt(KEY_BUDGET, b).apply(); }

    public String ambilNama()      { return prefs.getString(KEY_NAMA, ""); }
    public int ambilBatas()        { return prefs.getInt(KEY_BATAS, 50000); }
    public boolean isTemaGelap()   { return prefs.getBoolean(KEY_GELAP, false); }
    public String ambilKategori()  { return prefs.getString(KEY_KATEGORI, "Makan"); }
    public boolean isModeBulan()   { return prefs.getBoolean(KEY_MODE, false); }
    public int ambilBudgetBulan()  { return prefs.getInt(KEY_BUDGET, 1500000); }
}
