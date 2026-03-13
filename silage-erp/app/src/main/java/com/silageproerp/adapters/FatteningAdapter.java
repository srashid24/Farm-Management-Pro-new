package com.silageproerp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.silageproerp.R;
import com.silageproerp.database.entities.Fattening;
import java.util.List;

public class FatteningAdapter extends RecyclerView.Adapter<FatteningAdapter.ViewHolder> {
    public interface FatteningListener { void onEdit(Fattening f); void onDelete(Fattening f); void onUpdateWeight(Fattening f); }
    private List<Fattening> data; private final FatteningListener listener;
    public FatteningAdapter(List<Fattening> data, FatteningListener l) { this.data = data; this.listener = l; }
    public void updateData(List<Fattening> d) { this.data = d; notifyDataSetChanged(); }
    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_fattening, p, false));
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Fattening f = data.get(pos);
        h.tvCattle.setText(f.cattleTag + " - " + f.cattleName);
        h.tvWeight.setText(String.format("Start: %.1f kg | Now: %.1f kg | Target: %.1f kg", f.startWeightKg, f.currentWeightKg, f.targetWeightKg));
        double gainKg = f.currentWeightKg - f.startWeightKg;
        double totalNeeded = f.targetWeightKg - f.startWeightKg;
        int progress = totalNeeded > 0 ? (int) ((gainKg / totalNeeded) * 100) : 0;
        h.progressBar.setProgress(Math.max(0, Math.min(100, progress)));
        h.tvGain.setText(String.format("+%.1f kg gained (%d%%)", gainKg, progress));
        h.tvFeedCost.setText(String.format("Feed cost: $%.2f | Feed: %.1f kg/day | %s", f.totalFeedCostToDate, f.dailyFeedKg, f.feedType));
        h.tvStatus.setText(f.status);
        h.tvStatus.setTextColor("Active".equals(f.status) ? Color.parseColor("#2E7D32") : Color.GRAY);
        h.btnEdit.setOnClickListener(v -> listener.onEdit(f));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(f));
        h.btnUpdate.setOnClickListener(v -> listener.onUpdateWeight(f));
    }
    @Override public int getItemCount() { return data.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCattle, tvWeight, tvGain, tvFeedCost, tvStatus; ProgressBar progressBar;
        ImageButton btnEdit, btnDelete, btnUpdate;
        ViewHolder(View v) { super(v);
            tvCattle=v.findViewById(R.id.tv_cattle); tvWeight=v.findViewById(R.id.tv_weight);
            tvGain=v.findViewById(R.id.tv_gain); tvFeedCost=v.findViewById(R.id.tv_feed_cost);
            tvStatus=v.findViewById(R.id.tv_status); progressBar=v.findViewById(R.id.progress_bar);
            btnEdit=v.findViewById(R.id.btn_edit); btnDelete=v.findViewById(R.id.btn_delete);
            btnUpdate=v.findViewById(R.id.btn_update_weight); }
    }
}
