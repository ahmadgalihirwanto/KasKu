package com.example.kasku;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private interface Hasil { void ok(String teks); }

    private PrefsManager prefs;
    private DbHelper db;
    private TransaksiAdapter adapter;

    private TextView tvTanggal, tvSapaan, tvSisa, tvTerpakai, tvStatus;
    private TextView tvNamaVal, tvBatasVal, tvBudgetVal, tvKosong, tvSisaLabel, tvJudulList;
    private boolean modeBulan = false;
    private LinearProgressIndicator progress;
    private SwitchMaterial swTema;
    private RecyclerView rv;
    private ExtendedFloatingActionButton fab;
    private boolean dialogNamaTampil = false;

    private String hariIni() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = new PrefsManager(this);
        // Terapkan tema dari SharedPreferences sebelum layar dibuat
        AppCompatDelegate.setDefaultNightMode(prefs.isTemaGelap()
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DbHelper(this);
        tvTanggal = findViewById(R.id.tvTanggal);
        tvSapaan = findViewById(R.id.tvSapaan);
        tvSisa = findViewById(R.id.tvSisa);
        tvTerpakai = findViewById(R.id.tvTerpakai);
        tvStatus = findViewById(R.id.tvStatus);
        tvNamaVal = findViewById(R.id.tvNamaVal);
        tvBatasVal = findViewById(R.id.tvBatasVal);
        tvBudgetVal = findViewById(R.id.tvBudgetVal);
        tvKosong = findViewById(R.id.tvKosong);
        progress = findViewById(R.id.progress);
        swTema = findViewById(R.id.swTema);
        rv = findViewById(R.id.rvTransaksi);
        fab = findViewById(R.id.fabTambah);

        adapter = new TransaksiAdapter(this::hapus);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        findViewById(R.id.btnNama).setOnClickListener(v -> dialogNama());
        findViewById(R.id.btnBatas).setOnClickListener(v -> dialogBatas());
        findViewById(R.id.btnBudget).setOnClickListener(v -> dialogBudget());
        tvSisaLabel = findViewById(R.id.tvSisaLabel);
        tvJudulList = findViewById(R.id.tvJudulList);

        modeBulan = prefs.isModeBulan();
        MaterialButtonToggleGroup toggle = findViewById(R.id.toggleMode);
        toggle.check(modeBulan ? R.id.btnModeBulan : R.id.btnModeHari);
        toggle.addOnButtonCheckedListener((group, id, checked) -> {
            if (!checked) return;
            modeBulan = (id == R.id.btnModeBulan);
            prefs.simpanModeBulan(modeBulan);
            tampil();
        });
        fab.setOnClickListener(v -> dialogTambah());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Dibaca ulang dari SharedPreferences setiap layar tampil
        tampil();
        if (prefs.ambilNama().isEmpty()) {
            dialogNama(); // pertama kali dipakai
        }
    }

    private void tampil() {
        String nama = prefs.ambilNama();
        tvTanggal.setText(new SimpleDateFormat(modeBulan ? "MMMM yyyy" : "EEEE, d MMMM yyyy",
                new Locale("id", "ID")).format(new Date()));
        tvSapaan.setText("Halo, " + nama + "!");
        tvNamaVal.setText(nama.isEmpty() ? "-" : nama);
        tvBatasVal.setText(Fmt.rp(prefs.ambilBatas()));
        tvBudgetVal.setText(Fmt.rp(prefs.ambilBudgetBulan()));

        swTema.setOnCheckedChangeListener(null);
        swTema.setChecked(prefs.isTemaGelap());
        swTema.setOnCheckedChangeListener((b, gelap) -> {
            prefs.simpanTemaGelap(gelap);
            AppCompatDelegate.setDefaultNightMode(gelap
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO);
        });

        muatData();
    }

    private void muatData() {
        long batasHarian = prefs.ambilBatas(); // dipakai mode "Hari ini"
        Calendar cal = Calendar.getInstance();
        int hariDalamBulan = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int hariKe = cal.get(Calendar.DAY_OF_MONTH);

        String pola;
        long anggaran;
        if (modeBulan) {
            pola = new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date()) + "-%";
            anggaran = prefs.ambilBudgetBulan(); // budget bulanan diisi sendiri oleh pengguna
            tvSisaLabel.setText("Sisa uang bulan ini");
            tvJudulList.setText("Pengeluaran bulan ini");
        } else {
            pola = hariIni();
            anggaran = batasHarian;
            tvSisaLabel.setText("Sisa uang hari ini");
            tvJudulList.setText("Pengeluaran hari ini");
        }

        long total = db.totalPeriode(pola);
        long sisa = anggaran - total;
        int persen = anggaran > 0 ? (int) Math.min(100L, total * 100L / anggaran) : 0;

        tvSisa.setText(Fmt.rp(sisa));
        tvTerpakai.setText("Terpakai " + Fmt.rp(total) + " dari " + Fmt.rp(anggaran));
        progress.setProgressCompat(persen, true);

        String status;
        if (sisa < 0) {
            progress.setIndicatorColor(0xFFFF5A5F);
            tvSisa.setTextColor(0xFFFF8A8E);
            status = modeBulan ? "Melebihi anggaran bulan ini." : "Melebihi batas hari ini. Tahan dulu jajannya!";
        } else if (persen >= 80) {
            progress.setIndicatorColor(0xFFFFA726);
            tvSisa.setTextColor(0xFFFFC93C);
            status = "Hampir habis, hati-hati.";
        } else {
            progress.setIndicatorColor(0xFF2EC4B6);
            tvSisa.setTextColor(0xFFFFC93C);
            status = "Aman, pengeluaran masih terkendali.";
        }
        if (modeBulan) {
            int hariSisa = hariDalamBulan - hariKe + 1; // termasuk hari ini
            status += " Rata-rata " + Fmt.rp(total / Math.max(1, hariKe)) + " per hari.";
            if (sisa > 0) {
                status += " Sisa budget cukup " + Fmt.rp(sisa / Math.max(1, hariSisa)) + " per hari.";
            }
        }
        tvStatus.setText(status);

        ArrayList<Transaksi> list = db.daftarPeriode(pola);
        adapter.setData(list, modeBulan);
        tvKosong.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void hapus(Transaksi t) {
        db.hapus(t.id);
        muatData();
        Snackbar.make(findViewById(android.R.id.content), "Pengeluaran dihapus", Snackbar.LENGTH_LONG)
                .setAnchorView(fab)
                .setAction("Urungkan", v -> {
                    db.tambah(t);
                    muatData();
                })
                .show();
    }

    private AlertDialog dialogTeks(String judul, String hint, int inputType, int maxLen,
                                   String awal, boolean bisaBatal, Hasil hasil) {
        View v = getLayoutInflater().inflate(R.layout.dialog_input, null);
        TextInputLayout til = v.findViewById(R.id.tilInput);
        TextInputEditText et = v.findViewById(R.id.etInput);
        til.setHint(hint);
        et.setInputType(inputType);
        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLen)});
        if (awal != null) {
            et.setText(awal);
            et.setSelection(et.length());
        }

        MaterialAlertDialogBuilder b = new MaterialAlertDialogBuilder(this)
                .setTitle(judul)
                .setView(v)
                .setPositiveButton("Simpan", null)
                .setCancelable(bisaBatal);
        if (bisaBatal) b.setNegativeButton("Batal", null);

        AlertDialog dlg = b.create();
        dlg.setOnShowListener(d -> dlg.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(x -> {
                    String s = et.getText() == null ? "" : et.getText().toString().trim();
                    if (s.isEmpty()) {
                        til.setError("Wajib diisi");
                        return;
                    }
                    hasil.ok(s);
                    dlg.dismiss();
                }));
        dlg.show();
        return dlg;
    }

    private void dialogNama() {
        if (dialogNamaTampil) return;
        dialogNamaTampil = true;
        boolean bisaBatal = !prefs.ambilNama().isEmpty();
        AlertDialog dlg = dialogTeks("Siapa nama panggilanmu?", "Nama panggilan",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS, 20,
                prefs.ambilNama(), bisaBatal, s -> {
                    prefs.simpanNama(s);
                    tampil();
                });
        dlg.setOnDismissListener(d -> dialogNamaTampil = false);
    }

    private void dialogBatas() {
        dialogTeks("Batas pengeluaran harian", "Batas harian (Rp)",
                InputType.TYPE_CLASS_NUMBER, 9,
                String.valueOf(prefs.ambilBatas()), true, s -> {
                    int nilai = Integer.parseInt(s);
                    if (nilai <= 0) {
                        Toast.makeText(this, "Batas harus lebih dari 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    prefs.simpanBatas(nilai);
                    tampil();
                });
    }

    private void dialogBudget() {
        dialogTeks("Budget bulanan", "Budget bulan ini (Rp)",
                InputType.TYPE_CLASS_NUMBER, 9,
                String.valueOf(prefs.ambilBudgetBulan()), true, s -> {
                    int nilai = Integer.parseInt(s);
                    if (nilai <= 0) {
                        Toast.makeText(this, "Budget harus lebih dari 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    prefs.simpanBudgetBulan(nilai);
                    tampil();
                });
    }

    private void dialogTambah() {
        View v = getLayoutInflater().inflate(R.layout.dialog_tambah, null);
        TextInputLayout tilKet = v.findViewById(R.id.tilKet);
        TextInputLayout tilNom = v.findViewById(R.id.tilNominal);
        TextInputEditText etKet = v.findViewById(R.id.etKet);
        TextInputEditText etNom = v.findViewById(R.id.etNominal);
        ChipGroup cg = v.findViewById(R.id.cgKategori);

        String terakhir = prefs.ambilKategori(); // kategori yang dipakai terakhir kali
        for (String k : Kategori.NAMA) {
            Chip c = new Chip(this);
            c.setId(View.generateViewId());
            c.setText(Kategori.ikon(k) + " " + k);
            c.setTag(k);
            c.setCheckable(true);
            cg.addView(c);
            if (k.equals(terakhir)) c.setChecked(true);
        }

        AlertDialog dlg = new MaterialAlertDialogBuilder(this)
                .setTitle("Catat pengeluaran")
                .setView(v)
                .setPositiveButton("Simpan", null)
                .setNegativeButton("Batal", null)
                .create();

        dlg.setOnShowListener(d -> dlg.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(x -> {
                    String ket = etKet.getText() == null ? "" : etKet.getText().toString().trim();
                    String nom = etNom.getText() == null ? "" : etNom.getText().toString().trim();
                    tilKet.setError(ket.isEmpty() ? "Wajib diisi" : null);
                    tilNom.setError(nom.isEmpty() ? "Wajib diisi" : null);
                    if (ket.isEmpty() || nom.isEmpty()) return;

                    int nominal = Integer.parseInt(nom);
                    if (nominal <= 0) {
                        tilNom.setError("Harus lebih dari 0");
                        return;
                    }

                    String kategori = "Lain-lain";
                    for (int i = 0; i < cg.getChildCount(); i++) {
                        Chip ch = (Chip) cg.getChildAt(i);
                        if (ch.isChecked()) kategori = (String) ch.getTag();
                    }
                    prefs.simpanKategori(kategori); // diingat untuk pencatatan berikutnya

                    String waktu = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                    db.tambah(new Transaksi(0, ket, nominal, kategori, hariIni(), waktu));
                    muatData();
                    dlg.dismiss();
                }));
        dlg.show();
    }
}
