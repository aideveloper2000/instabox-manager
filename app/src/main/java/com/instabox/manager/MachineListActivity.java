package com.instabox.manager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.instabox.manager.ApiService.MachineListResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MachineListActivity extends AppCompatActivity {
    private View coordinatorList;
    private RecyclerView recyclerMachines;
    private MachineAdapter machineAdapter;
    private List<Machine> machines;
    private String selectedApiKey;

    private Machine selectedMachine;
    private Machine latestViewedMachine;

    private TextView statusText;
    private TextView nameText;
    private TextView specsText;
    private TextView templateText;
    private TextView noMachinesText;
    private ImageView indicatorState;
    private MaterialButton llStart;
    private MaterialButton llStop;
    private MaterialButton llRestart;
    private MaterialButton llPause;
    private MaterialButton llResume;
    private MaterialButton llWake;
    private TextInputLayout commandLayout;
    private TextInputEditText inputCommand;
    private MaterialButton btnExecute;
    private TextView commandOutputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_list);

        Intent intent = getIntent();
        selectedApiKey = intent.getStringExtra("selected_key");

        initViews();
        showPreview(null);
        setupAdapters();
        loadMachines();
        setupActionListeners();
    }

    private void initViews() {
        coordinatorList = findViewById(R.id.coordinatorList);
        recyclerMachines = findViewById(R.id.recyclerMachines);
        statusText = findViewById(R.id.textStatus);
        nameText = findViewById(R.id.nameText);
        specsText = findViewById(R.id.textSpecs2);
        templateText = findViewById(R.id.textTemplate2);
        noMachinesText = findViewById(R.id.noMachinesText);
        indicatorState = findViewById(R.id.indicatorState);
        llStart = findViewById(R.id.llStart);
        llStop = findViewById(R.id.llStop);
        llRestart = findViewById(R.id.llRestart);
        llPause = findViewById(R.id.llPause);
        llResume = findViewById(R.id.llResume);
        llWake = findViewById(R.id.llWake);
        commandLayout = findViewById(R.id.commandLayout);
        inputCommand = findViewById(R.id.inputCommand);
        btnExecute = findViewById(R.id.btnExecute);
        commandOutputText = findViewById(R.id.textCommandOutput);

        com.google.android.material.appbar.MaterialToolbar toolbarList = findViewById(R.id.toolbarList);
        toolbarList.setNavigationOnClickListener(v -> finish());
    }

    private void setupAdapters() {
        machines = new ArrayList<>();
        machineAdapter = new MachineAdapter(this);
        machineAdapter.setOnItemClickListener(machine -> {
            latestViewedMachine = machine;
            selectedMachine = machine;
            showPreview(machine);
            Intent intent = new Intent(this, MachineDetailActivity.class);
            intent.putExtra("machine_id", machine.getId());
            intent.putExtra("api_key", selectedApiKey);
            startActivity(intent);
        });
        recyclerMachines.setLayoutManager(new LinearLayoutManager(this));
        recyclerMachines.setAdapter(machineAdapter);
    }

    private void setupActionListeners() {
        llStart.setOnClickListener(v -> startMachine());
        llStop.setOnClickListener(v -> stopMachine());
        llRestart.setOnClickListener(v -> restartMachine());
        llPause.setOnClickListener(v -> pauseMachine());
        llResume.setOnClickListener(v -> resumeMachine());
        llWake.setOnClickListener(v -> wakeMachine());
        btnExecute.setOnClickListener(v -> {
            String cmd = inputCommand.getText() == null ? "" : inputCommand.getText().toString().trim();
            if (cmd.isEmpty()) {
                Snackbar.make(coordinatorList, R.string.enter_command, Snackbar.LENGTH_SHORT).show();
                return;
            }
            executeCommand(cmd);
        });
    }

    private void loadMachines() {
        if (selectedApiKey == null || selectedApiKey.isEmpty()) {
            Snackbar.make(coordinatorList, R.string.select_key, Snackbar.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService service = ApiClient.createApiService(selectedApiKey);
        Call<MachineListResponse> call = service.listMachines("Bearer " + selectedApiKey, selectedApiKey);

        call.enqueue(new Callback<MachineListResponse>() {
            @Override
            public void onResponse(Call<MachineListResponse> call, Response<MachineListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    machines.clear();
                    List<Machine> fetched = response.body().getMachines();
                    if (fetched != null) {
                        machines.addAll(fetched);
                    }
                    machineAdapter.setMachines(machines);
                    noMachinesText.setVisibility(machines.isEmpty() ? View.VISIBLE : View.GONE);
                    if (latestViewedMachine == null && !machines.isEmpty()) {
                        latestViewedMachine = machines.get(0);
                        selectedMachine = latestViewedMachine;
                        showPreview(latestViewedMachine);
                    } else if (machines.isEmpty()) {
                        showPreview(null);
                    }
                } else {
                    Snackbar.make(coordinatorList, getString(R.string.connection_error) + ": " + response.message(),
                            Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MachineListResponse> call, Throwable t) {
                Snackbar.make(coordinatorList, getString(R.string.connection_error) + ": " + t.getMessage(),
                        Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void showPreview(Machine machine) {
        if (machine == null) {
            statusText.setText(R.string.no_machine_selected);
            nameText.setText(R.string.no_machines);
            specsText.setText(R.string.sync_detail_idle);
            templateText.setText("");
            indicatorState.setColorFilter(androidx.core.content.ContextCompat.getColor(this, R.color.status_unknown));
            llStart.setVisibility(View.GONE);
            llStop.setVisibility(View.GONE);
            llRestart.setVisibility(View.GONE);
            llPause.setVisibility(View.GONE);
            llResume.setVisibility(View.GONE);
            llWake.setVisibility(View.GONE);
            return;
        }

        String state = machine.getState() != null ? machine.getState()
                : (machine.getDesiredState() != null ? machine.getDesiredState() : "unknown");
        statusText.setText(state);
        nameText.setText(machine.getName());

        if (machine.getSpec() != null) {
            float memGb = machine.getSpec().getMemMiB() / 1024f;
            specsText.setText(String.format(Locale.getDefault(), getString(R.string.machine_spec),
                    machine.getSpec().getVcpus(), memGb));
        } else {
            specsText.setText("");
        }

        if (machine.getTemplate() != null && machine.getTemplate().getId() != null) {
            templateText.setText(machine.getTemplate().getId());
        } else {
            templateText.setText("");
        }

        indicatorState.setColorFilter(statusColor(state));

        boolean running = "running".equals(state) || "created".equals(state);
        boolean stopped = "stopped".equals(state);
        boolean paused = "paused".equals(state) || "suspended".equals(state);
        setActionEnabled(llStart, stopped);
        setActionEnabled(llStop, running || paused);
        setActionEnabled(llRestart, running || paused || stopped);
        setActionEnabled(llPause, running);
        setActionEnabled(llResume, paused);
        setActionEnabled(llWake, paused || stopped);

        llStart.setVisibility(View.VISIBLE);
        llStop.setVisibility(View.VISIBLE);
        llRestart.setVisibility(View.VISIBLE);
        llPause.setVisibility(View.VISIBLE);
        llResume.setVisibility(View.VISIBLE);
        llWake.setVisibility(View.VISIBLE);
    }

    private void setActionEnabled(MaterialButton button, boolean enabled) {
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1f : 0.4f);
    }

    private int statusColor(String state) {
        int colorRes;
        if (state.equals("running") || state.equals("created")) {
            colorRes = R.color.status_running;
        } else if (state.equals("stopped")) {
            colorRes = R.color.status_stopped;
        } else if (state.equals("paused") || state.equals("suspended")) {
            colorRes = R.color.status_paused;
        } else if (state.equals("creating")) {
            colorRes = R.color.status_creating;
        } else if (state.equals("deleting")) {
            colorRes = R.color.status_deleting;
        } else if (state.equals("error")) {
            colorRes = R.color.status_error;
        } else {
            colorRes = R.color.status_unknown;
        }
        return androidx.core.content.ContextCompat.getColor(this, colorRes);
    }

    private void startMachine() {
        if (selectedMachine == null || selectedMachine.getId() == null) {
            Snackbar.make(coordinatorList, R.string.select_key, Snackbar.LENGTH_SHORT).show();
            return;
        }
        ApiService service = ApiClient.createApiService(selectedApiKey);
        String idempotencyKey = UUID.randomUUID().toString();
        service.startMachine("Bearer " + selectedApiKey, selectedMachine.getId(), idempotencyKey)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.isSuccessful()) {
                            Snackbar.make(coordinatorList, R.string.start_machine, Snackbar.LENGTH_SHORT).show();
                            loadMachines();
                        } else {
                            Snackbar.make(coordinatorList, getString(R.string.connection_error) + ": " + response.message(),
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Snackbar.make(coordinatorList, getString(R.string.connection_error) + ": " + t.getMessage(),
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void stopMachine() {
        if (selectedMachine == null || selectedMachine.getId() == null) {
            Snackbar.make(coordinatorList, R.string.select_key, Snackbar.LENGTH_SHORT).show();
            return;
        }
        ApiService service = ApiClient.createApiService(selectedApiKey);
        String idempotencyKey = UUID.randomUUID().toString();
        service.stopMachine("Bearer " + selectedApiKey, selectedMachine.getId(), idempotencyKey)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.isSuccessful()) {
                            Snackbar.make(coordinatorList, R.string.stop_machine, Snackbar.LENGTH_SHORT).show();
                            loadMachines();
                        } else {
                            Snackbar.make(coordinatorList, getString(R.string.connection_error) + ": " + response.message(),
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Snackbar.make(coordinatorList, getString(R.string.connection_error) + ": " + t.getMessage(),
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void pauseMachine() {
        if (selectedMachine == null || selectedMachine.getId() == null) {
            Snackbar.make(coordinatorList, R.string.select_key, Snackbar.LENGTH_SHORT).show();
            return;
        }
        ApiService service = ApiClient.createApiService(selectedApiKey);
        String idempotencyKey = UUID.randomUUID().toString();
        service.suspendMachine("Bearer " + selectedApiKey, selectedMachine.getId(), idempotencyKey)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.isSuccessful()) {
                            Snackbar.make(coordinatorList, R.string.pause_machine, Snackbar.LENGTH_SHORT).show();
                            loadMachines();
                        } else {
                            Snackbar.make(coordinatorList, getString(R.string.connection_error) + ": " + response.message(),
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Snackbar.make(coordinatorList, getString(R.string.connection_error) + ": " + t.getMessage(),
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void resumeMachine() {
        if (selectedMachine == null || selectedMachine.getId() == null) {
            Snackbar.make(coordinatorList, R.string.select_key, Snackbar.LENGTH_SHORT).show();
            return;
        }
        ApiService service = ApiClient.createApiService(selectedApiKey);
        String idempotencyKey = UUID.randomUUID().toString();
        service.resumeMachine("Bearer " + selectedApiKey, selectedMachine.getId(), idempotencyKey)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.isSuccessful()) {
                            Snackbar.make(coordinatorList, R.string.resume_machine, Snackbar.LENGTH_SHORT).show();
                            loadMachines();
                        } else {
                            Snackbar.make(coordinatorList, getString(R.string.connection_error) + ": " + response.message(),
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Snackbar.make(coordinatorList, getString(R.string.connection_error) + ": " + t.getMessage(),
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void wakeMachine() {
        if (selectedMachine == null || selectedMachine.getId() == null) {
            Snackbar.make(coordinatorList, R.string.select_key, Snackbar.LENGTH_SHORT).show();
            return;
        }
        ApiService service = ApiClient.createApiService(selectedApiKey);
        String idempotencyKey = UUID.randomUUID().toString();
        service.wakeMachine("Bearer " + selectedApiKey, selectedMachine.getId(), idempotencyKey)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.isSuccessful()) {
                            Snackbar.make(coordinatorList, R.string.wake_machine, Snackbar.LENGTH_SHORT).show();
                            loadMachines();
                        } else {
                            Snackbar.make(coordinatorList, getString(R.string.connection_error) + ": " + response.message(),
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Snackbar.make(coordinatorList, getString(R.string.connection_error) + ": " + t.getMessage(),
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void restartMachine() {
        if (selectedMachine == null || selectedMachine.getId() == null) {
            Snackbar.make(coordinatorList, R.string.select_key, Snackbar.LENGTH_SHORT).show();
            return;
        }
        final String machineId = selectedMachine.getId();
        Snackbar.make(coordinatorList, R.string.restart_machine + "…", Snackbar.LENGTH_SHORT).show();
        ApiService service = ApiClient.createApiService(selectedApiKey);
        String idempotencyKey = UUID.randomUUID().toString();
        service.stopMachine("Bearer " + selectedApiKey, machineId, idempotencyKey)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.isSuccessful()) {
                            waitForStoppedThenStart(machineId, 0);
                        } else {
                            Snackbar.make(coordinatorList, getString(R.string.connection_error) + ": " + response.message(),
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Snackbar.make(coordinatorList, getString(R.string.connection_error) + ": " + t.getMessage(),
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void waitForStoppedThenStart(final String machineId, final int attempt) {
        if (attempt >= 20) {
            Snackbar.make(coordinatorList, R.string.connection_error, Snackbar.LENGTH_SHORT).show();
            return;
        }
        ApiService service = ApiClient.createApiService(selectedApiKey);
        service.getMachine("Bearer " + selectedApiKey, machineId).enqueue(new Callback<Machine>() {
            @Override
            public void onResponse(Call<Machine> call, Response<Machine> response) {
                if (response.isSuccessful() && response.body() != null
                        && "stopped".equals(response.body().getState())) {
                    ApiService svc = ApiClient.createApiService(selectedApiKey);
                    svc.startMachine("Bearer " + selectedApiKey, machineId, UUID.randomUUID().toString())
                            .enqueue(new Callback<Object>() {
                                @Override
                                public void onResponse(Call<Object> c, Response<Object> r) {
                                    if (r.isSuccessful()) {
                                        Snackbar.make(coordinatorList, R.string.restart_machine,
                                                Snackbar.LENGTH_SHORT).show();
                                        loadMachines();
                                    } else {
                                        Snackbar.make(coordinatorList,
                                                getString(R.string.connection_error) + ": " + r.message(),
                                                Snackbar.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Object> c, Throwable t) {
                                    Snackbar.make(coordinatorList,
                                            getString(R.string.connection_error) + ": " + t.getMessage(),
                                            Snackbar.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    new Handler(Looper.getMainLooper())
                            .postDelayed(() -> waitForStoppedThenStart(machineId, attempt + 1), 1500);
                }
            }

            @Override
            public void onFailure(Call<Machine> call, Throwable t) {
                new Handler(Looper.getMainLooper())
                        .postDelayed(() -> waitForStoppedThenStart(machineId, attempt + 1), 1500);
            }
        });
    }

    private void executeCommand(String command) {
        if (selectedMachine == null || selectedMachine.getId() == null) {
            Snackbar.make(coordinatorList, R.string.select_key, Snackbar.LENGTH_SHORT).show();
            return;
        }
        ApiService.ExecRequest execRequest = new ApiService.ExecRequest(command);
        ApiService service = ApiClient.createApiService(selectedApiKey);
        String idempotencyKey = UUID.randomUUID().toString();
        service.execCommand("Bearer " + selectedApiKey, selectedMachine.getId(), execRequest, idempotencyKey)
                .enqueue(new Callback<ApiService.ExecResult>() {
                    @Override
                    public void onResponse(Call<ApiService.ExecResult> call, Response<ApiService.ExecResult> response) {
                        runOnUiThread(() -> {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiService.ExecResult result = response.body();
                                StringBuilder output = new StringBuilder("Exit code: " + result.getExitCode() + "\n");
                                if (result.getStdout() != null) {
                                    output.append("STDOUT:\n").append(result.getStdout()).append("\n");
                                }
                                if (result.getStderr() != null && !result.getStderr().isEmpty()) {
                                    output.append("STDERR:\n").append(result.getStderr());
                                }
                                commandOutputText.setText(output.toString());
                            } else {
                                commandOutputText.setText("Error: " + response.message());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<ApiService.ExecResult> call, Throwable t) {
                        runOnUiThread(() -> commandOutputText.setText("Error: " + t.getMessage()));
                    }
                });
    }
}
