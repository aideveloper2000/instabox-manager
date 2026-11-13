package com.instabox.manager;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.instabox.manager.ApiService.MachineListResponse;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private View coordinatorMain;
    private RecyclerView recyclerKeys;
    private ApiKeyAdapter keyAdapter;
    private List<ApiKey> apiKeys;
    private TextInputLayout keyInputLayout;
    private TextInputLayout keyNameInputLayout;
    private TextInputEditText inputApiKey;
    private TextInputEditText inputKeyName;
    private MaterialButton btnAddKey;
    private MaterialButton btnSyncMachines;
    private LinearProgressIndicator progressSync;
    private TextView keyCountText;
    private TextView noKeysText;
    private TextView syncStatusChip;
    private TextView syncStatusText;
    private TextView noMachinesText;
    private RecyclerView recyclerMachines;

    private MachineAdapter machineAdapter;
    private List<Machine> allMachines;

    private int syncPending;
    private boolean hasSynced;
    private final Set<String> failedKeys = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupAdapters();
        loadSavedKeys();
        setupListeners();
        updateSyncUi();
        if (!apiKeys.isEmpty()) {
            syncAll();
        }
    }

    private void initViews() {
        coordinatorMain = findViewById(R.id.coordinatorMain);
        recyclerKeys = findViewById(R.id.recyclerKeys);
        keyInputLayout = findViewById(R.id.keyInputLayout);
        keyNameInputLayout = findViewById(R.id.keyNameInputLayout);
        inputApiKey = findViewById(R.id.inputApiKey);
        inputKeyName = findViewById(R.id.inputKeyName);
        btnAddKey = findViewById(R.id.btnAddKey);
        btnSyncMachines = findViewById(R.id.btnSyncMachines);
        progressSync = findViewById(R.id.progressSync);
        keyCountText = findViewById(R.id.keyCountText);
        noKeysText = findViewById(R.id.noKeysText);
        syncStatusChip = findViewById(R.id.syncStatusChip);
        syncStatusText = findViewById(R.id.syncStatusText);
        noMachinesText = findViewById(R.id.noMachinesText);
        recyclerMachines = findViewById(R.id.recyclerMachines);
    }

    private void setupAdapters() {
        apiKeys = new ArrayList<>();
        keyAdapter = new ApiKeyAdapter(this);
        keyAdapter.setOnItemClickListener(new ApiKeyAdapter.OnItemClickListener() {
            @Override
            public void onKeyClick(String key, String name) {
                Intent intent = new Intent(MainActivity.this, MachineListActivity.class);
                intent.putExtra("selected_key", key);
                startActivity(intent);
            }

            @Override
            public void onRemoveClick(int position) {
                if (position < 0 || position >= apiKeys.size()) {
                    return;
                }
                ApiKey removed = apiKeys.remove(position);
                keyAdapter.removeApiKey(position);
                failedKeys.remove(removed.getKey());
                synchronized (allMachines) {
                    Iterator<Machine> it = allMachines.iterator();
                    while (it.hasNext()) {
                        Machine m = it.next();
                        if (removed.getKey() != null && removed.getKey().equals(m.getApiKey())) {
                            it.remove();
                        }
                    }
                }
                machineAdapter.setMachines(allMachines);
                saveKeys();
                Snackbar.make(coordinatorMain, R.string.key_removed, Snackbar.LENGTH_SHORT).show();
                updateSyncUi();
            }
        });
        recyclerKeys.setLayoutManager(new LinearLayoutManager(this));
        recyclerKeys.setAdapter(keyAdapter);

        allMachines = new ArrayList<>();
        machineAdapter = new MachineAdapter(this);
        machineAdapter.setOnItemClickListener(machine -> {
            if (machine.getId() == null) {
                return;
            }
            Intent intent = new Intent(MainActivity.this, MachineDetailActivity.class);
            intent.putExtra("machine_id", machine.getId());
            intent.putExtra("api_key", machine.getApiKey());
            startActivity(intent);
        });
        recyclerMachines.setLayoutManager(new LinearLayoutManager(this));
        recyclerMachines.setAdapter(machineAdapter);

        machineAdapter.setOnActionListener(new MachineAdapter.OnActionListener() {
            @Override
            public void onPauseClick(Machine machine) {
                if (machine.getId() == null) {
                    return;
                }
                ApiService service = ApiClient.createApiService(machine.getApiKey());
                String idempotencyKey = UUID.randomUUID().toString();
                service.suspendMachine("Bearer " + machine.getApiKey(), machine.getId(), idempotencyKey)
                        .enqueue(actionCallback(getString(R.string.pause_machine)));
            }

            @Override
            public void onResumeClick(Machine machine) {
                if (machine.getId() == null) {
                    return;
                }
                ApiService service = ApiClient.createApiService(machine.getApiKey());
                String idempotencyKey = UUID.randomUUID().toString();
                service.resumeMachine("Bearer " + machine.getApiKey(), machine.getId(), idempotencyKey)
                        .enqueue(actionCallback(getString(R.string.resume_machine)));
            }
        });
    }

    private void setupListeners() {
        btnAddKey.setOnClickListener(v -> {
            String key = inputApiKey.getText() == null ? "" : inputApiKey.getText().toString().trim();
            String name = inputKeyName.getText() == null ? "" : inputKeyName.getText().toString().trim();

            if (key.isEmpty()) {
                keyInputLayout.setError(getString(R.string.api_key_required));
                return;
            }
            if (!key.startsWith("ibk_")) {
                keyInputLayout.setError(getString(R.string.invalid_key));
                return;
            }
            keyInputLayout.setError(null);

            ApiKey apiKey = new ApiKey(key, name.isEmpty() ? "Key " + (apiKeys.size() + 1) : name);
            apiKeys.add(apiKey);
            keyAdapter.addApiKey(apiKey);
            saveKeys();
            inputApiKey.setText("");
            inputKeyName.setText("");
            Snackbar.make(coordinatorMain, R.string.key_added, Snackbar.LENGTH_SHORT).show();
            updateSyncUi();
            syncAll();
        });

        btnSyncMachines.setOnClickListener(v -> {
            if (apiKeys.isEmpty()) {
                Snackbar.make(coordinatorMain, R.string.no_keys, Snackbar.LENGTH_SHORT).show();
                return;
            }
            syncAll();
        });

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_sync) {
                if (apiKeys.isEmpty()) {
                    Snackbar.make(coordinatorMain, R.string.no_keys, Snackbar.LENGTH_SHORT).show();
                } else {
                    syncAll();
                }
                return true;
            }
            return false;
        });
    }

    private Callback<Object> actionCallback(String successMessage) {
        return new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    Snackbar.make(coordinatorMain, successMessage, Snackbar.LENGTH_SHORT).show();
                    syncAll();
                } else {
                    Snackbar.make(coordinatorMain,
                            getString(R.string.connection_error) + ": " + response.message(),
                            Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Snackbar.make(coordinatorMain,
                        getString(R.string.connection_error) + ": " + t.getMessage(),
                        Snackbar.LENGTH_SHORT).show();
            }
        };
    }

    private void saveKeys() {
        Gson gson = new Gson();
        String json = gson.toJson(apiKeys);
        getSharedPreferences("instabox_prefs", MODE_PRIVATE)
                .edit()
                .putString("api_keys", json)
                .apply();
    }

    private void loadSavedKeys() {
        Gson gson = new Gson();
        String json = getSharedPreferences("instabox_prefs", MODE_PRIVATE).getString("api_keys", null);
        if (json != null) {
            Type type = new TypeToken<List<ApiKey>>() {}.getType();
            apiKeys = gson.fromJson(json, type);
            if (apiKeys == null) {
                apiKeys = new ArrayList<>();
            }
            keyAdapter.setApiKeys(apiKeys);
        }
        updateKeyCounts();
    }

    private void syncAll() {
        if (apiKeys.isEmpty()) {
            Snackbar.make(coordinatorMain, R.string.no_keys, Snackbar.LENGTH_SHORT).show();
            return;
        }

        syncPending = 0;
        for (ApiKey key : apiKeys) {
            if (!key.isEnabled()) {
                continue;
            }
            syncPending++;
            syncMachinesWithKey(key.getKey());
        }

        if (syncPending == 0) {
            updateSyncUi();
            return;
        }
        progressSync.setVisibility(View.VISIBLE);
        updateSyncUi();
    }

    private void syncMachinesWithKey(String apiKey) {
        ApiService service = ApiClient.createApiService(apiKey);
        Call<MachineListResponse> call = service.listMachines("Bearer " + apiKey, apiKey);

        call.enqueue(new Callback<MachineListResponse>() {
            @Override
            public void onResponse(Call<MachineListResponse> call, Response<MachineListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Machine> fetched = response.body().getMachines();
                    if (fetched == null) {
                        fetched = new ArrayList<>();
                    }
                    synchronized (allMachines) {
                        Iterator<Machine> it = allMachines.iterator();
                        while (it.hasNext()) {
                            if (apiKey.equals(it.next().getApiKey())) {
                                it.remove();
                            }
                        }
                        for (Machine m : fetched) {
                            m.setApiKey(apiKey);
                            allMachines.add(m);
                        }
                    }
                    failedKeys.remove(apiKey);
                    hasSynced = true;
                    runOnUiThread(() -> {
                        machineAdapter.setMachines(allMachines);
                        updateKeyCounts();
                        keyAdapter.setApiKeys(apiKeys);
                        onSyncComplete();
                    });
                } else {
                    failedKeys.add(apiKey);
                    runOnUiThread(() -> {
                        updateKeyCounts();
                        keyAdapter.setApiKeys(apiKeys);
                        onSyncComplete();
                    });
                }
            }

            @Override
            public void onFailure(Call<MachineListResponse> call, Throwable t) {
                failedKeys.add(apiKey);
                runOnUiThread(() -> {
                    updateKeyCounts();
                    keyAdapter.setApiKeys(apiKeys);
                    onSyncComplete();
                });
            }
        });
    }

    private void onSyncComplete() {
        syncPending--;
        if (syncPending <= 0) {
            syncPending = 0;
            progressSync.setVisibility(View.GONE);
        }
        updateSyncUi();
    }

    private void updateKeyCounts() {
        Map<String, Integer> counts = new HashMap<>();
        for (Machine m : allMachines) {
            String k = m.getApiKey();
            if (k != null) {
                counts.put(k, counts.getOrDefault(k, 0) + 1);
            }
        }
        for (ApiKey key : apiKeys) {
            key.setMachineCount(counts.getOrDefault(key.getKey(), 0));
            key.setSyncFailed(failedKeys.contains(key.getKey()));
        }
    }

    private void updateSyncUi() {
        int total = allMachines.size();
        noMachinesText.setVisibility(total == 0 ? View.VISIBLE : View.GONE);
        keyCountText.setText(String.valueOf(apiKeys.size()));
        noKeysText.setVisibility(apiKeys.isEmpty() ? View.VISIBLE : View.GONE);

        if (syncPending > 0) {
            setStatusChip(R.string.chip_syncing, R.color.primary, R.color.on_primary);
            syncStatusText.setText(R.string.sync_detail_syncing);
            return;
        }

        if (!failedKeys.isEmpty()) {
            setStatusChip(R.string.chip_failed, R.color.error, R.color.on_error);
            syncStatusText.setText(getString(R.string.synced_count_fail, total, plural(total)));
        } else if (!hasSynced) {
            setStatusChip(R.string.chip_not_synced, R.color.surface_variant, R.color.on_surface_variant);
            syncStatusText.setText(R.string.sync_detail_idle);
        } else {
            setStatusChip(R.string.chip_synced, R.color.status_running, R.color.on_primary);
            syncStatusText.setText(getString(R.string.synced_count, total, plural(total), currentTime()));
        }
    }

    private void setStatusChip(int textRes, int bgColorRes, int textColorRes) {
        syncStatusChip.setText(textRes);
        syncStatusChip.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, bgColorRes)));
        syncStatusChip.setTextColor(ContextCompat.getColor(this, textColorRes));
    }

    private String plural(int count) {
        return count == 1 ? "" : "s";
    }

    private String currentTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!apiKeys.isEmpty()) {
            syncAll();
        }
    }
}
