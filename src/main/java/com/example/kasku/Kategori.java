package com.example.kasku;

/** Daftar kategori pengeluaran beserta ikon emoji-nya. */
public class Kategori {
    public static final String[] NAMA  = {"Makan", "Transport", "Jajan", "Lain-lain"};
    private static final String[] IKON = {"\uD83C\uDF5C", "\uD83D\uDE8C", "\uD83E\uDDCB", "\uD83D\uDCE6"};

    public static String ikon(String nama) {
        for (int i = 0; i < NAMA.length; i++) {
            if (NAMA[i].equals(nama)) return IKON[i];
        }
        return IKON[IKON.length - 1];
    }
}
