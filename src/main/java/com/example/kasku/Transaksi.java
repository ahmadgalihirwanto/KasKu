package com.example.kasku;

/** Satu baris pengeluaran (disimpan di database, bukan di SharedPreferences). */
public class Transaksi {
    public final long id;
    public final String keterangan;
    public final int nominal;
    public final String kategori;
    public final String tanggal;
    public final String waktu;

    public Transaksi(long id, String keterangan, int nominal,
                     String kategori, String tanggal, String waktu) {
        this.id = id;
        this.keterangan = keterangan;
        this.nominal = nominal;
        this.kategori = kategori;
        this.tanggal = tanggal;
        this.waktu = waktu;
    }
}
