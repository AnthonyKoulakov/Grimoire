package com.example.Grimoire.MainScreen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Grimoire.Backend.Page;
import com.example.Grimoire.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Page page);
        void onSelectionChanged(int selectedCount);
    }

    private List<Page> data;
    private OnItemClickListener listener;
    private Context context;

    private final Set<Page> selected = new HashSet<>();
    private boolean selectionMode = false;

    public MyAdapter(Context context, List<Page> data, OnItemClickListener listener) {
        this.data = data;
        this.listener = listener;
        this.context = context;
    }

    public boolean isSelectionMode(){
        return selectionMode;
    }
    public Set<Page> getSelectedItems(){
        return selected;
    }
    public void exitSelectionMode(){
        selectionMode = false;
        selected.clear();
        notifyDataSetChanged();
        listener.onSelectionChanged(0);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public TextView tagView;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.text_item);
            tagView = view.findViewById(R.id.text_tag);
        }

        public void bind(Context context, final Page page, final boolean isSelected, final Supplier<Boolean> isSelectionMode, final OnItemClickListener listener, final Runnable toggleSelection) {
            textView.setText(page.getTitle());

            // Show tag info
            List<String> tags = page.getTags();
            if (tags != null && !tags.isEmpty()) {
                String tagText = tags.get(0);
                if (tags.size() > 1) {
                    tagText += " +" + (tags.size() - 1);
                }
                tagView.setText(tagText);
            } else {
                tagView.setText("+ Add Tag");
            }

            // Background highlight for selection
            itemView.setBackgroundColor(isSelected ? Color.DKGRAY : Color.TRANSPARENT);

            itemView.setOnClickListener(v -> {
                if (isSelectionMode.get()) {
                    toggleSelection.run();
                } else {
                    listener.onItemClick(page);
                }
            });

            tagView.setOnClickListener(v -> {
                if (context instanceof MainActivity) {
                    ((MainActivity) context).showTagDialog(page);
                }
            });


            itemView.setOnLongClickListener(v -> {
                toggleSelection.run();
                return true;
            });
        }
    }

    @NonNull
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the custom item layout with bigger text
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Page page= data.get(position);
        boolean isSelected= selected.contains(page);

        holder.bind(context, page, isSelected, () -> selectionMode, listener, () -> {
            if (!selectionMode){
                selectionMode = true;
            }
            if (selected.contains(page)){
                selected.remove(page);
            }else{
                selected.add(page);
            }
            //exit if 0 selected
            if (selected.isEmpty()){
                exitSelectionMode();
            }

            notifyItemChanged(position);
            listener.onSelectionChanged(selected.size());
        });
    }


    @Override
    public int getItemCount() {
        return data.size();
    }
}
