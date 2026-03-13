package com.silageproerp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.silageproerp.R;
import com.silageproerp.database.entities.Packaging;
import java.util.List;

public class PackagingAdapter extends RecyclerView.Adapter<PackagingAdapter.ViewHolder> {
    public interface PackagingListener { void onEdit(Packaging p); void onDelete(Packaging p); }
    private List<Packaging> data; private final PackagingListener listener;
    public PackagingAdapter(List<Packaging> data, PackagingListener l) { this.data = data; this.listener = l; }
    public void updateData(List<Packaging> d) { this.data = d; notifyDataSetChanged(); }
    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_packaging, p, false));
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Packaging pkg = data.get(pos);
        h.tvSeason.setText(pkg.harvestSeason + " | " + pkg.landName);
        h.tvBales.setText(String.format("%d bales | %.1f tons | %.1f kg/bale", pkg.baleCount, pkg.totalWeightTons, pkg.baleWeightKg));
        h.tvType.setText(pkg.packagingType + " | " + pkg.wrapLayers + " | " + (pkg.wrapColour != null ? pkg.wrapColour : ""));
        h.tvCost.setText(String.format("Wrap: $%.2f | Contractor: $%.2f", pkg.totalWrapCost, pkg.contractorCost));
        h.tvDate.setText(pkg.packagingDate);
        h.btnEdit.setOnClickListener(v -> listener.onEdit(pkg));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(pkg));
    }
    @Override public int getItemCount() { return data.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeason, tvBales, tvType, tvCost, tvDate; ImageButton btnEdit, btnDelete;
        ViewHolder(View v) { super(v);
            tvSeason=v.findViewById(R.id.tv_season); tvBales=v.findViewById(R.id.tv_bales);
            tvType=v.findViewById(R.id.tv_type); tvCost=v.findViewById(R.id.tv_cost);
            tvDate=v.findViewById(R.id.tv_date);
            btnEdit=v.findViewById(R.id.btn_edit); btnDelete=v.findViewById(R.id.btn_delete); }
    }
}
