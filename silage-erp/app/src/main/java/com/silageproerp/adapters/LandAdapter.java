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
import com.silageproerp.database.entities.Land;
import com.silageproerp.helper.ImageHelper;

import java.util.List;

public class LandAdapter extends RecyclerView.Adapter<LandAdapter.ViewHolder> {

    public interface LandListener { void onEdit(Land l); void onDelete(Land l); }

    private List<Land> data;
    private final LandListener listener;

    public LandAdapter(List<Land> data, LandListener listener) { this.data = data; this.listener = listener; }
    public void updateData(List<Land> d) { this.data = d; notifyDataSetChanged(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_land, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Land l = data.get(pos);
        h.tvName.setText(l.fieldName);
        h.tvDetails.setText(String.format("%.1f acres (%.1f ha) | %s | %s", l.sizeAcres, l.sizeHectares, l.cropType, l.contractType));
        h.tvLocation.setText(l.location != null ? l.location : "");
        ImageHelper.loadImage(h.ivPhoto, l.imagePath, R.drawable.ic_field_placeholder);
        h.btnEdit.setOnClickListener(v -> listener.onEdit(l));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(l));
    }

    @Override public int getItemCount() { return data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails, tvLocation;
        ImageView ivPhoto;
        ImageButton btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvDetails = v.findViewById(R.id.tv_details);
            tvLocation = v.findViewById(R.id.tv_location);
            ivPhoto = v.findViewById(R.id.iv_photo);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
