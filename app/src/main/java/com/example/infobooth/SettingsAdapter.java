package com.example.infobooth;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    private List<SettingOption> options;

    public SettingsAdapter(List<SettingOption> options) {
        this.options = options;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.setting_title);
            description = itemView.findViewById(R.id.setting_description);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_setting, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SettingOption option = options.get(position);
        holder.title.setText(option.getTitle());
        holder.description.setText(option.getDescription());

        holder.itemView.setOnClickListener(v -> option.getAction().run());
    }

    @Override
    public int getItemCount() {
        return options.size();
    }
}

