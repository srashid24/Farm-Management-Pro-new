package com.silageproerp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silageproerp.R;
import com.silageproerp.database.entities.Harvest;
import com.silageproerp.helper.ImageHelper;

import java.util.List;

public class HarvestAdapter extends RecyclerView.Adapter<HarvestAdapter.ViewHolder> {

    public interface HarvestListener { void onEdit(Harvest h); void onDelete(Harvest h); }

    private List<Harvest> data;
    private final HarvestListener listener;

    public HarvestAdapter(List<Harvest> data, HarvestListener listener) { this.data = data; this.listener = listener; }
    public void updateData(List<Harvest> d) { this.data = d; notifyDataSetChanged(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_harvest, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Harvest hr = data.get(pos);
        h.tvSeason.setText(hr.season + " | " + hr.silagType);
        h.tvYield.setText(String.format("%.1f tons | %d bales", hr.yieldTons, hr.baleCount));
        h.tvDate.setText(hr.harvestDate);
        h.tvQuality.setText("Quality: " + hr.quality);
        h.tvMoisture.setText(String.format("Moisture: %.1f%%", hr.moisturePercent));
        ImageHelper.loadImage(h.ivSample, hr.imagePath, R.drawable.ic_silage_placeholder);
        h.btnEdit.setOnClickListener(v -> listener.onEdit(hr));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(hr));
    }

    @Override public int getItemCount() { return data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeason, tvYield, tvDate, tvQuality, tvMoisture;
        ImageView ivSample;
        ImageButton btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvSeason = v.findViewById(R.id.tv_season);
            tvYield = v.findViewById(R.id.tv_yield);
            tvDate = v.findViewById(R.id.tv_date);
            tvQuality = v.findViewById(R.id.tv_quality);
            tvMoisture = v.findViewById(R.id.tv_moisture);
            ivSample = v.findViewById(R.id.iv_sample);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
