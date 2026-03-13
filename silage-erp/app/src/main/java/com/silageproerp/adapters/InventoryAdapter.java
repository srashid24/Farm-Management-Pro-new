package com.silageproerp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silageproerp.R;
import com.silageproerp.database.entities.InventoryItem;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    public interface OnInventoryActionListener {
        void onEdit(InventoryItem item);
        void onDelete(InventoryItem item);
    }

    private List<InventoryItem> list;
    private final OnInventoryActionListener listener;

    public InventoryAdapter(List<InventoryItem> list, OnInventoryActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateList(List<InventoryItem> newList) { this.list = newList; notifyDataSetChanged(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        InventoryItem item = list.get(position);
        h.tvName.setText(item.itemName);
        h.tvCategory.setText(item.category);
        h.tvQty.setText(String.format("%.2f %s", item.quantity, item.unit));
        h.tvValue.setText(String.format("$%.2f", item.totalValue));
        h.tvLocation.setText(item.storageLocation != null ? item.storageLocation : "");
        // Highlight low stock
        if (item.quantity <= item.minimumStock) {
            h.tvQty.setTextColor(Color.RED);
            h.tvQty.setText(h.tvQty.getText() + " ⚠ LOW");
        } else {
            h.tvQty.setTextColor(Color.parseColor("#2E7D32"));
        }
        h.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvQty, tvValue, tvLocation;
        View btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_item_name);
            tvCategory = v.findViewById(R.id.tv_category);
            tvQty = v.findViewById(R.id.tv_quantity);
            tvValue = v.findViewById(R.id.tv_value);
            tvLocation = v.findViewById(R.id.tv_location);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
