package com.example.Grimoire.Settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.Grimoire.R;
import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingViewHolder> {

    private List<SettingOption> optionsList;

    public SettingsAdapter(List<SettingOption> optionsList) {
        this.optionsList = optionsList;
    }

    @NonNull
    @Override
    public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_setting_option, parent, false);
        return new SettingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingViewHolder holder, int position) {
        SettingOption currentOption = optionsList.get(position);
        holder.titleTextView.setText(currentOption.getTitle());
        holder.descriptionTextView.setText(currentOption.getDescription());
        holder.itemView.setOnClickListener(v -> {
            currentOption.getClickAction().run();
        });
    }

    @Override
    public int getItemCount() {
        return optionsList.size();
    }

    static class SettingViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;

        public SettingViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.setting_title);
            descriptionTextView = itemView.findViewById(R.id.setting_description);
        }
    }
}