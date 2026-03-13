package com.silageproerp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silageproerp.R;
import com.silageproerp.database.entities.Packaging;

import java.util.List;

public class PackagingAdapter extends RecyclerView.Adapter<PackagingAdapter.ViewHolder> {

    public interface OnPackagingActionListener {
        void onEdit(Packaging packaging);
        void onDelete(Packaging packaging);
    }

    private List<Packaging> list;
    private final OnPackagingActionListener listener;

    public PackagingAdapter(List<Packaging> list, OnPackagingActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateList(List<Packaging> newList) { this.list = newList; notifyDataSetChanged(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_packaging, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Packaging p = list.get(position);
        h.tvSeason.setText(p.harvestSeason + " - " + p.landName);
        h.tvBales.setText(p.baleCount + " bales | " + String.format("%.2f tons", p.totalWeightTons));
        h.tvType.setText(p.packagingType + " | " + p.wrapLayers);
        h.tvWrapCost.setText(String.format("Wrap: $%.2f | Contractor: $%.2f", p.totalWrapCost, p.contractorCost));
        h.tvDate.setText(p.packagingDate);
        h.tvStorage.setText(p.storageLocation != null ? p.storageLocation : "");
        h.btnEdit.setOnClickListener(v -> listener.onEdit(p));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(p));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeason, tvBales, tvType, tvWrapCost, tvDate, tvStorage;
        View btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvSeason = v.findViewById(R.id.tv_season);
            tvBales = v.findViewById(R.id.tv_bales);
            tvType = v.findViewById(R.id.tv_pack_type);
            tvWrapCost = v.findViewById(R.id.tv_wrap_cost);
            tvDate = v.findViewById(R.id.tv_date);
            tvStorage = v.findViewById(R.id.tv_storage);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
