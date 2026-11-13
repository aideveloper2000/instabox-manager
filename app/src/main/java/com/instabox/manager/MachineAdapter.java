package com.instabox.manager;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MachineAdapter extends RecyclerView.Adapter<MachineAdapter.ViewHolder> {
    private Context context;
    private List<Machine> machines;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onMachineClick(Machine machine);
    }

    public MachineAdapter(Context context) {
        this.context = context;
        this.machines = new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setMachines(List<Machine> machines) {
        this.machines = machines;
        notifyDataSetChanged();
    }

    public void addMachines(List<Machine> machines) {
        int start = this.machines.size();
        this.machines.addAll(machines);
        notifyItemRangeInserted(start, machines.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_machine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Machine m = machines.get(position);
        holder.nameText.setText(m.getName());

        String state = m.getState() != null ? m.getState()
                : (m.getDesiredState() != null ? m.getDesiredState() : "unknown");
        holder.stateText.setText(state);

        int statusColor = statusColor(state);
        holder.indicator.setColorFilter(statusColor);
        holder.stateText.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.surface_variant)));
        holder.stateText.setTextColor(statusColor);

        if (m.getSpec() != null) {
            float memGb = m.getSpec().getMemMiB() / 1024f;
            holder.specsText.setText(String.format(Locale.getDefault(),
                    context.getString(R.string.machine_spec),
                    m.getSpec().getVcpus(), memGb));
            holder.specsText.setVisibility(View.VISIBLE);
        } else {
            holder.specsText.setText("");
        }

        if (m.getTemplate() != null && m.getTemplate().getId() != null) {
            holder.templateText.setText(m.getTemplate().getId());
            holder.templateText.setVisibility(View.VISIBLE);
        } else {
            holder.templateText.setText("");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMachineClick(m);
            }
        });
    }

    private int statusColor(String state) {
        if (state.equals("running") || state.equals("created")) {
            return ContextCompat.getColor(context, R.color.status_running);
        } else if (state.equals("stopped")) {
            return ContextCompat.getColor(context, R.color.status_stopped);
        } else if (state.equals("paused") || state.equals("suspended")) {
            return ContextCompat.getColor(context, R.color.status_paused);
        } else if (state.equals("creating")) {
            return ContextCompat.getColor(context, R.color.status_creating);
        } else if (state.equals("deleting")) {
            return ContextCompat.getColor(context, R.color.status_deleting);
        } else if (state.equals("error")) {
            return ContextCompat.getColor(context, R.color.status_error);
        }
        return ContextCompat.getColor(context, R.color.status_unknown);
    }

    @Override
    public int getItemCount() {
        return machines.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView stateText;
        TextView specsText;
        TextView templateText;
        ImageView indicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textMachineName);
            stateText = itemView.findViewById(R.id.textMachineState);
            specsText = itemView.findViewById(R.id.textSpecs);
            templateText = itemView.findViewById(R.id.textTemplate);
            indicator = itemView.findViewById(R.id.indicatorState);
        }
    }
}
