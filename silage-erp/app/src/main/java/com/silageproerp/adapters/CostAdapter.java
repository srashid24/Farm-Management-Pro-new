package com.silageproerp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silageproerp.R;
import com.silageproerp.database.entities.CostEntry;

import java.util.List;

public class CostAdapter extends RecyclerView.Adapter<CostAdapter.ViewHolder> {

    public interface OnCostActionListener {
        void onEdit(CostEntry entry);
        void onDelete(CostEntry entry);
    }

    private List<CostEntry> list;
    private final OnCostActionListener listener;

    public CostAdapter(List<CostEntry> list, OnCostActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateList(List<CostEntry> newList) { this.list = newList; notifyDataSetChanged(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cost, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CostEntry e = list.get(position);
        h.tvCategory.setText(e.category);
        h.tvDescription.setText(e.description);
        h.tvAmount.setText(String.format("$%.2f", e.amount));
        h.tvDetails.setText(String.format("%.2f %s @ $%.2f/%s", e.quantity, e.unit, e.unitCost, e.unit));
        h.tvSeason.setText(e.season != null && !e.season.isEmpty() ? e.season : e.date);
        h.tvSupplier.setText(e.supplierName != null ? e.supplierName : "");
        h.btnEdit.setOnClickListener(v -> listener.onEdit(e));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(e));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvDescription, tvAmount, tvDetails, tvSeason, tvSupplier;
        View btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvCategory = v.findViewById(R.id.tv_category);
            tvDescription = v.findViewById(R.id.tv_description);
            tvAmount = v.findViewById(R.id.tv_amount);
            tvDetails = v.findViewById(R.id.tv_details);
            tvSeason = v.findViewById(R.id.tv_season);
            tvSupplier = v.findViewById(R.id.tv_supplier);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
