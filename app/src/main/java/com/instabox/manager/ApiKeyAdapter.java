package com.instabox.manager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.instabox.manager.R;

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
        apiKeys.add(key);
        notifyItemInserted(getItemCount() - 1);
    }

    public void removeApiKey(int position) {
        apiKeys.remove(position);
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
        if (keyText != null && keyText.length() > 20) {
            keyText = keyText.substring(0, 8) + "..." + keyText.substring(keyText.length() - 8);
        }
        holder.keyText.setText(keyText);

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onKeyClick(key.getKey(), key.getName());
            }
        });

        holder.keyText.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClick(position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return apiKeys.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView keyText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textKeyName);
            keyText = itemView.findViewById(R.id.textApiKey);
        }
    }
}
