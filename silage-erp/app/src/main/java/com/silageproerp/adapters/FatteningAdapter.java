package com.silageproerp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silageproerp.R;
import com.silageproerp.database.entities.Fattening;

import java.util.List;

public class FatteningAdapter extends RecyclerView.Adapter<FatteningAdapter.ViewHolder> {

    public interface OnFatteningActionListener {
        void onEdit(Fattening fattening);
        void onDelete(Fattening fattening);
    }

    private List<Fattening> list;
    private final OnFatteningActionListener listener;

    public FatteningAdapter(List<Fattening> list, OnFatteningActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateList(List<Fattening> newList) { this.list = newList; notifyDataSetChanged(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fattening, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Fattening f = list.get(position);
        h.tvCattleTag.setText("Tag: " + f.cattleTag);
        h.tvCattleName.setText(f.cattleName != null ? f.cattleName : "");
        h.tvProgress.setText(String.format("%.1f kg → %.1f kg (target: %.1f kg)",
                f.startWeightKg, f.currentWeightKg, f.targetWeightKg));
        h.tvGain.setText(String.format("Gain: %.1f kg | ADG: %.2f kg/day",
                f.currentWeightKg - f.startWeightKg, f.dailyGainKg));
        h.tvFeed.setText(f.feedType + " | Daily: " + f.dailyFeedKg + " kg");
        h.tvFeedCost.setText(String.format("Feed Cost: $%.2f", f.totalFeedCostToDate));
        h.tvStatus.setText(f.status);
        h.tvStartDate.setText("Started: " + f.startDate);
        h.btnEdit.setOnClickListener(v -> listener.onEdit(f));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(f));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCattleTag, tvCattleName, tvProgress, tvGain, tvFeed, tvFeedCost, tvStatus, tvStartDate;
        View btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvCattleTag = v.findViewById(R.id.tv_cattle_tag);
            tvCattleName = v.findViewById(R.id.tv_cattle_name);
            tvProgress = v.findViewById(R.id.tv_progress);
            tvGain = v.findViewById(R.id.tv_gain);
            tvFeed = v.findViewById(R.id.tv_feed);
            tvFeedCost = v.findViewById(R.id.tv_feed_cost);
            tvStatus = v.findViewById(R.id.tv_status);
            tvStartDate = v.findViewById(R.id.tv_start_date);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
