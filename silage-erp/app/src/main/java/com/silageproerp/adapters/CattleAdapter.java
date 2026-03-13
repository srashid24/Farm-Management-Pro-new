package com.silageproerp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silageproerp.R;
import com.silageproerp.database.entities.Cattle;

import java.util.List;

public class CattleAdapter extends RecyclerView.Adapter<CattleAdapter.ViewHolder> {

    public interface OnCattleActionListener {
        void onEdit(Cattle cattle);
        void onDelete(Cattle cattle);
    }

    private List<Cattle> list;
    private final OnCattleActionListener listener;

    public CattleAdapter(List<Cattle> list, OnCattleActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateList(List<Cattle> newList) { this.list = newList; notifyDataSetChanged(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cattle, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Cattle c = list.get(position);
        h.tvTag.setText("Tag: " + c.tagNumber);
        h.tvName.setText(c.name != null && !c.name.isEmpty() ? c.name : c.breed);
        h.tvBreed.setText(c.breed + " | " + c.gender);
        h.tvWeight.setText(String.format("%.1f kg", c.currentWeightKg));
        h.tvStatus.setText(c.status);
        h.tvOwner.setText(c.ownerName != null ? c.ownerName : "");
        h.btnEdit.setOnClickListener(v -> listener.onEdit(c));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(c));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTag, tvName, tvBreed, tvWeight, tvStatus, tvOwner;
        View btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvTag = v.findViewById(R.id.tv_tag);
            tvName = v.findViewById(R.id.tv_name);
            tvBreed = v.findViewById(R.id.tv_breed);
            tvWeight = v.findViewById(R.id.tv_weight);
            tvStatus = v.findViewById(R.id.tv_status);
            tvOwner = v.findViewById(R.id.tv_owner);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
