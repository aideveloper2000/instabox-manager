package com.instabox.manager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ApiKeyAdapter extends RecyclerView.Adapter<ApiKeyAdapter.ViewHolder> {
    private Context context;
    private List<ApiKey> apiKeys;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onKeyClick(String key, String name);
        void onRemoveClick(int position);
    }

    public ApiKeyAdapter(Context context) {
        this.context = context;
        this.apiKeys = new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setApiKeys(List<ApiKey> apiKeys) {
        this.apiKeys = apiKeys;
        notifyDataSetChanged();
    }

    public void addApiKey(ApiKey key) {
        // The owning Activity mutates the backing list before calling this; only notify here.
        notifyItemInserted(getItemCount() - 1);
    }

    public void removeApiKey(int position) {
        // The owning Activity mutates the backing list before calling this; only notify here.
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_api_key, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApiKey key = apiKeys.get(position);
        holder.nameText.setText(key.getName());

        String keyText = key.getKey();
        if (keyText != null && keyText.length() > 12) {
            keyText = keyText.substring(0, 6) + "…" + keyText.substring(keyText.length() - 4);
        }
        holder.keyText.setText(keyText);

        int statusColor;
        if (key.isSyncFailed()) {
            holder.statusText.setText(R.string.sync_error);
            statusColor = ContextCompat.getColor(context, R.color.error);
        } else if (key.getMachineCount() > 0) {
            holder.statusText.setText(context.getString(R.string.machine_count,
                    key.getMachineCount(), key.getMachineCount() == 1 ? "" : "s"));
            statusColor = ContextCompat.getColor(context, R.color.status_running);
        } else {
            holder.statusText.setText(R.string.not_synced);
            statusColor = ContextCompat.getColor(context, R.color.status_unknown);
        }
        holder.statusText.setTextColor(statusColor);
        holder.indicator.setColorFilter(statusColor);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onKeyClick(key.getKey(), key.getName());
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return apiKeys.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView keyText;
        TextView statusText;
        ImageView indicator;
        ImageButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textKeyName);
            keyText = itemView.findViewById(R.id.textApiKey);
            statusText = itemView.findViewById(R.id.textKeyStatus);
            indicator = itemView.findViewById(R.id.indicatorKey);
            btnRemove = itemView.findViewById(R.id.btnRemoveKey);
        }
    }
}
