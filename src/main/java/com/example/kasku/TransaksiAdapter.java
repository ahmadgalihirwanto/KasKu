package com.example.kasku;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TransaksiAdapter extends RecyclerView.Adapter<TransaksiAdapter.VH> {

    public interface Listener {
        void onHapus(Transaksi t);
    }

    private final ArrayList<Transaksi> data = new ArrayList<>();
    private final Listener listener;
    private boolean tampilTanggal = false;

    public TransaksiAdapter(Listener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(ArrayList<Transaksi> baru, boolean tampilTanggal) {
        this.tampilTanggal = tampilTanggal;
        data.clear();
        data.addAll(baru);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaksi, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Transaksi t = data.get(position);
        h.tvIkon.setText(Kategori.ikon(t.kategori));
        h.tvKet.setText(t.keterangan);
        String meta = t.waktu.isEmpty() ? t.kategori : t.kategori + " \u00B7 " + t.waktu;
        if (tampilTanggal) meta = tglPendek(t.tanggal) + " \u00B7 " + meta;
        h.tvMeta.setText(meta);
        h.tvNominal.setText(Fmt.rp(t.nominal));
        h.btnHapus.setOnClickListener(v -> listener.onHapus(t));
    }

    private static String tglPendek(String yyyyMMdd) {
        try {
            return new SimpleDateFormat("d MMM", new Locale("id", "ID"))
                    .format(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(yyyyMMdd));
        } catch (ParseException e) {
            return yyyyMMdd;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvIkon, tvKet, tvMeta, tvNominal;
        final ImageButton btnHapus;

        VH(@NonNull View v) {
            super(v);
            tvIkon = v.findViewById(R.id.tvIkon);
            tvKet = v.findViewById(R.id.tvKet);
            tvMeta = v.findViewById(R.id.tvMeta);
            tvNominal = v.findViewById(R.id.tvNominal);
            btnHapus = v.findViewById(R.id.btnHapus);
        }
    }
}
