package com.silageproerp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silageproerp.R;
import com.silageproerp.database.entities.Land;

import java.util.List;

public class LandAdapter extends RecyclerView.Adapter<LandAdapter.ViewHolder> {

    public interface OnLandActionListener {
        void onEdit(Land land);
        void onDelete(Land land);
    }

    private List<Land> list;
    private final OnLandActionListener listener;

    public LandAdapter(List<Land> list, OnLandActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateList(List<Land> newList) { this.list = newList; notifyDataSetChanged(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_land, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Land l = list.get(position);
        h.tvFieldName.setText(l.fieldName);
        h.tvBlockId.setText(l.blockId != null ? "Block: " + l.blockId : "");
        h.tvSize.setText(String.format("%.2f ha / %.2f acres", l.sizeHectares, l.sizeAcres));
        h.tvLocation.setText(l.location != null ? l.location : "");
        h.tvContract.setText(l.contractType != null ? l.contractType : "Owned");
        h.btnEdit.setOnClickListener(v -> listener.onEdit(l));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(l));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFieldName, tvBlockId, tvSize, tvLocation, tvContract;
        View btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvFieldName = v.findViewById(R.id.tv_field_name);
            tvBlockId = v.findViewById(R.id.tv_block_id);
            tvSize = v.findViewById(R.id.tv_size);
            tvLocation = v.findViewById(R.id.tv_location);
            tvContract = v.findViewById(R.id.tv_contract);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
