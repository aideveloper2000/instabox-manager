package com.instabox.manager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerKeys;
    private ApiKeyAdapter keyAdapter;
    private List<ApiKey> apiKeys;
    private TextInputEditText inputApiKey;
    private TextInputEditText inputKeyName;
    private Button btnAddKey;
    private FloatingActionButton fabRefresh;
    private TextView loadingText;
    private RecyclerView recyclerMachines;

    private MachineAdapter machineAdapter;
    private List<Machine> allMachines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupAdapters();
        loadSavedKeys();
        setupListeners();
        syncAll();
    }

    private void initViews() {
        recyclerKeys = findViewById(R.id.recyclerKeys);
        inputApiKey = findViewById(R.id.inputApiKey);
        inputKeyName = findViewById(R.id.inputKeyName);
        btnAddKey = findViewById(R.id.btnAddKey);
        fabRefresh = findViewById(R.id.fabRefresh);
        loadingText = findViewById(R.id.loadingText);
        recyclerMachines = findViewById(R.id.recyclerMachines);
    }

    private void setupAdapters() {
        apiKeys = new ArrayList<>();
        keyAdapter = new ApiKeyAdapter(this);
        keyAdapter.setOnItemClickListener(new ApiKeyAdapter.OnItemClickListener() {
            @Override
            public void onKeyClick(String key, String name) {
                // Navigate to machine list with selected key
                Intent intent = new Intent(MainActivity.this, MachineListActivity.class);
                intent.putExtra("selected_key", key);
                startActivity(intent);
            }

            @Override
            public void onRemoveClick(int position) {
                apiKeys.remove(position);
                keyAdapter.removeApiKey(position);
                saveKeys();
                Toast.makeText(MainActivity.this, "Key removed", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerKeys.setLayoutManager(new LinearLayoutManager(this));
        recyclerKeys.setAdapter(keyAdapter);
    }

    private void setupListeners() {
        btnAddKey.setOnClickListener(v -> {
            String key = inputApiKey.getText().toString().trim();
            String name = inputKeyName.getText().toString().trim();

            if (key.isEmpty()) {
                inputApiKey.setError("API key is required");
                return;
            }

            if (!key.startsWith("ibk_")) {
                Toast.makeText(this, "Please enter a valid Instabox API key (ibk_...)", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiKey apiKey = new ApiKey(key, name.isEmpty() ? "Key " + (apiKeys.size() + 1) : name);
            apiKeys.add(apiKey);
            keyAdapter.addApiKey(apiKey);
            saveKeys();
            inputApiKey.setText("");
            inputKeyName.setText("");
            Toast.makeText(this, "Key added", Toast.LENGTH_SHORT).show();
        });

        fabRefresh.setOnClickListener(v -> {
            if (apiKeys.isEmpty()) {
                Toast.makeText(this, "Add at least one API key first", Toast.LENGTH_SHORT).show();
                return;
            }
            syncAll();
        });

        inputApiKey.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                btnAddKey.setEnabled(!s.toString().trim().isEmpty());
            }
        });
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
            if (apiKeys == null) apiKeys = new ArrayList<>();
            keyAdapter.setApiKeys(apiKeys);
        }
    }

    private void syncAll() {
        if (apiKeys.isEmpty()) {
            Toast.makeText(this, "No API keys added", Toast.LENGTH_SHORT).show();
            return;
        }

        // Enable refresh FAB, then sync each key
        loadingText.setVisibility(View.VISIBLE);
        btnAddKey.setEnabled(false);

        for (final ApiKey key : apiKeys) {
            if (!key.isEnabled()) continue;
            syncMachinesWithKey(key.getKey());
        }
    }

    private void syncMachinesWithKey(String apiKey) {
        ApiService service = ApiClient.createApiService(apiKey);
        Call<List<Machine>> call = service.listMachines("Bearer " + apiKey, apiKey);

        call.enqueue(new Callback<List<Machine>>() {
            @Override
            public void onResponse(Call<List<Machine>> call, Response<List<Machine>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final boolean[] found = {false};
                    synchronized(allMachines) {
                        for (Machine m : response.body()) {
                            boolean exists = false;
                            for (Machine existing : allMachines) {
                                if (existing.getId() != null && existing.getId().equals(m.getId())) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                allMachines.add(m);
                                found[0] = true;
                            }
                        }
                    }
                    if (found[0]) {
                        runOnUiThread(() -> {
                            machineAdapter.notifyDataSetChanged();
                            Toast.makeText(MainActivity.this, "Synced " + response.body().size() + " machines",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Sync failed: " + response.message(),
                            Toast.LENGTH_SHORT).show());
                }
                runOnUiThread(() -> finishSync());
            }

            @Override
            public void onFailure(Call<List<Machine>> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show());
                runOnUiThread(() -> finishSync());
            }
        });
    }

    private void finishSync() {
        runOnUiThread(() -> {
            loadingText.setVisibility(View.GONE);
            btnAddKey.setEnabled(true);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!apiKeys.isEmpty()) {
            syncAll();
        }
    }
}
