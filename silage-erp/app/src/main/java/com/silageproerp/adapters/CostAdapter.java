package com.silageproerp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.silageproerp.R;
import com.silageproerp.database.entities.CostEntry;
import java.util.List;

public class CostAdapter extends RecyclerView.Adapter<CostAdapter.ViewHolder> {
    public interface CostListener { void onEdit(CostEntry e); void onDelete(CostEntry e); }
    private List<CostEntry> data; private final CostListener listener;
    public CostAdapter(List<CostEntry> data, CostListener l) { this.data = data; this.listener = l; }
    public void updateData(List<CostEntry> d) { this.data = d; notifyDataSetChanged(); }
    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_cost, p, false));
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        CostEntry e = data.get(pos);
        h.tvCategory.setText(e.category);
        h.tvDesc.setText(e.description);
        h.tvAmount.setText(String.format("$%.2f", e.amount));
        h.tvDetails.setText(String.format("%.2f %s @ $%.2f | %s", e.quantity, e.unit, e.unitCost, e.date));
        h.tvSeason.setText(e.season != null ? e.season : "");
        h.btnEdit.setOnClickListener(v -> listener.onEdit(e));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(e));
    }
    @Override public int getItemCount() { return data.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvDesc, tvAmount, tvDetails, tvSeason; ImageButton btnEdit, btnDelete;
        ViewHolder(View v) { super(v);
            tvCategory=v.findViewById(R.id.tv_category); tvDesc=v.findViewById(R.id.tv_description);
            tvAmount=v.findViewById(R.id.tv_amount); tvDetails=v.findViewById(R.id.tv_details);
            tvSeason=v.findViewById(R.id.tv_season);
            btnEdit=v.findViewById(R.id.btn_edit); btnDelete=v.findViewById(R.id.btn_delete); }
    }
}
