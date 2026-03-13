package com.silageproerp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silageproerp.R;
import com.silageproerp.database.entities.Farmer;

import java.util.List;

public class FarmerAdapter extends RecyclerView.Adapter<FarmerAdapter.ViewHolder> {

    public interface FarmerListener { void onEdit(Farmer f); void onDelete(Farmer f); }

    private List<Farmer> data;
    private final FarmerListener listener;

    public FarmerAdapter(List<Farmer> data, FarmerListener listener) {
        this.data = data; this.listener = listener;
    }

    public void updateData(List<Farmer> newData) { this.data = newData; notifyDataSetChanged(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_farmer, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Farmer f = data.get(pos);
        h.tvName.setText(f.name);
        h.tvPhone.setText(f.phone != null ? f.phone : "");
        h.tvAddress.setText(f.address != null ? f.address : "");
        h.btnEdit.setOnClickListener(v -> listener.onEdit(f));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(f));
    }

    @Override public int getItemCount() { return data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvAddress;
        ImageButton btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvPhone = v.findViewById(R.id.tv_phone);
            tvAddress = v.findViewById(R.id.tv_address);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
