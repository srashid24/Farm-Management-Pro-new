package com.silageproerp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silageproerp.R;
import com.silageproerp.database.entities.Farmer;

import java.util.List;

public class FarmerAdapter extends RecyclerView.Adapter<FarmerAdapter.ViewHolder> {

    public interface OnFarmerActionListener {
        void onEdit(Farmer farmer);
        void onDelete(Farmer farmer);
    }

    private List<Farmer> list;
    private final OnFarmerActionListener listener;

    public FarmerAdapter(List<Farmer> list, OnFarmerActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateList(List<Farmer> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_farmer, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Farmer f = list.get(position);
        h.tvName.setText(f.name);
        h.tvPhone.setText(f.phone != null ? f.phone : "");
        h.tvAddress.setText(f.address != null ? f.address : "");
        h.tvNationalId.setText(f.nationalId != null ? "ID: " + f.nationalId : "");
        h.itemView.setOnClickListener(v -> listener.onEdit(f));
        h.itemView.setOnLongClickListener(v -> { listener.onDelete(f); return true; });
        h.btnEdit.setOnClickListener(v -> listener.onEdit(f));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(f));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvAddress, tvNationalId;
        View btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvPhone = v.findViewById(R.id.tv_phone);
            tvAddress = v.findViewById(R.id.tv_address);
            tvNationalId = v.findViewById(R.id.tv_national_id);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
