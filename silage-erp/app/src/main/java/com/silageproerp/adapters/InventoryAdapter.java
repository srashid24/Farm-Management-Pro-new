package com.silageproerp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silageproerp.R;
import com.silageproerp.database.entities.InventoryItem;
import com.silageproerp.helper.ImageHelper;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    public interface InventoryListener { void onEdit(InventoryItem i); void onDelete(InventoryItem i); }

    private List<InventoryItem> data;
    private final InventoryListener listener;

    public InventoryAdapter(List<InventoryItem> data, InventoryListener listener) { this.data = data; this.listener = listener; }
    public void updateData(List<InventoryItem> d) { this.data = d; notifyDataSetChanged(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        InventoryItem item = data.get(pos);
        h.tvName.setText(item.itemName);
        h.tvCategory.setText(item.category);
        h.tvQty.setText(String.format("%.1f %s", item.quantity, item.unit));
        h.tvValue.setText(String.format("Value: $%.2f", item.totalValue));
        h.tvLocation.setText(item.storageLocation != null ? item.storageLocation : "");
        boolean lowStock = item.quantity <= item.minimumStock;
        h.tvQty.setTextColor(lowStock ? Color.RED : Color.parseColor("#2E7D32"));
        ImageHelper.loadImage(h.ivPhoto, item.imagePath, R.drawable.ic_inventory_placeholder);
        h.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override public int getItemCount() { return data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvQty, tvValue, tvLocation;
        ImageView ivPhoto;
        ImageButton btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvCategory = v.findViewById(R.id.tv_category);
            tvQty = v.findViewById(R.id.tv_qty);
            tvValue = v.findViewById(R.id.tv_value);
            tvLocation = v.findViewById(R.id.tv_location);
            ivPhoto = v.findViewById(R.id.iv_photo);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
