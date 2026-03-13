package com.silageproerp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silageproerp.R;
import com.silageproerp.database.entities.Harvest;

import java.util.List;

public class HarvestAdapter extends RecyclerView.Adapter<HarvestAdapter.ViewHolder> {

    public interface OnHarvestActionListener {
        void onEdit(Harvest harvest);
        void onDelete(Harvest harvest);
    }

    private List<Harvest> list;
    private final OnHarvestActionListener listener;

    public HarvestAdapter(List<Harvest> list, OnHarvestActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateList(List<Harvest> newList) { this.list = newList; notifyDataSetChanged(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_harvest, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Harvest harvest = list.get(position);
        h.tvSeason.setText(harvest.season + " - " + harvest.silagType);
        h.tvDate.setText(harvest.harvestDate);
        h.tvYield.setText(String.format("%.2f tons", harvest.yieldTons));
        h.tvBales.setText(harvest.baleCount + " bales @ " + harvest.baleWeightKg + " kg");
        h.tvQuality.setText("Quality: " + harvest.quality + " | Moisture: " + harvest.moisturePercent + "%");
        h.tvStorage.setText(harvest.storageLocation != null ? harvest.storageLocation : "");
        h.btnEdit.setOnClickListener(v -> listener.onEdit(harvest));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(harvest));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeason, tvDate, tvYield, tvBales, tvQuality, tvStorage;
        View btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvSeason = v.findViewById(R.id.tv_season);
            tvDate = v.findViewById(R.id.tv_date);
            tvYield = v.findViewById(R.id.tv_yield);
            tvBales = v.findViewById(R.id.tv_bales);
            tvQuality = v.findViewById(R.id.tv_quality);
            tvStorage = v.findViewById(R.id.tv_storage);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
