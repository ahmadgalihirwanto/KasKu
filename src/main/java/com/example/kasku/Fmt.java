package com.example.kasku;

import java.text.NumberFormat;
import java.util.Locale;

/** Pemformat angka ke Rupiah, contoh: 40000 menjadi "Rp 40.000". */
public class Fmt {
    public static String rp(long nilai) {
        return "Rp " + NumberFormat.getInstance(new Locale("id", "ID")).format(nilai);
    }
}
