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
import com.silageproerp.database.entities.Cattle;
import com.silageproerp.helper.ImageHelper;
import java.util.List;

public class CattleAdapter extends RecyclerView.Adapter<CattleAdapter.ViewHolder> {
    public interface CattleListener { void onEdit(Cattle c); void onDelete(Cattle c); }
    private List<Cattle> data; private final CattleListener listener;
    public CattleAdapter(List<Cattle> data, CattleListener l) { this.data = data; this.listener = l; }
    public void updateData(List<Cattle> d) { this.data = d; notifyDataSetChanged(); }
    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_cattle, p, false));
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Cattle c = data.get(pos);
        h.tvTag.setText("Tag: " + c.tagNumber);
        h.tvName.setText(c.name != null && !c.name.isEmpty() ? c.name : c.tagNumber);
        h.tvBreed.setText(c.breed + " | " + c.gender);
        h.tvWeight.setText(String.format("%.1f kg", c.currentWeightKg));
        h.tvStatus.setText(c.status);
        h.tvOwner.setText("Owner: " + (c.ownerName != null ? c.ownerName : ""));
        ImageHelper.loadImage(h.ivPhoto, c.imagePath, R.drawable.ic_cattle_placeholder);
        h.btnEdit.setOnClickListener(v -> listener.onEdit(c));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(c));
    }
    @Override public int getItemCount() { return data.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTag, tvName, tvBreed, tvWeight, tvStatus, tvOwner; ImageView ivPhoto; ImageButton btnEdit, btnDelete;
        ViewHolder(View v) { super(v);
            tvTag=v.findViewById(R.id.tv_tag); tvName=v.findViewById(R.id.tv_name);
            tvBreed=v.findViewById(R.id.tv_breed); tvWeight=v.findViewById(R.id.tv_weight);
            tvStatus=v.findViewById(R.id.tv_status); tvOwner=v.findViewById(R.id.tv_owner);
            ivPhoto=v.findViewById(R.id.iv_photo); btnEdit=v.findViewById(R.id.btn_edit); btnDelete=v.findViewById(R.id.btn_delete); }
    }
}
