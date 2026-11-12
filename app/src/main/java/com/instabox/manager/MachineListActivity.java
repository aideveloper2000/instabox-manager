package com.instabox.manager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private ImageView indicatorState;
    private LinearLayout llStart;
    private LinearLayout llStop;
    private LinearLayout llRestart;
    private LinearLayout llPause;
    private LinearLayout llResume;
    private LinearLayout llCommand;
    private TextInputLayout commandLayout;
    private TextInputEditText inputCommand;
    private Button btnExecute;
    private TextView commandOutputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_list);

        Intent intent = getIntent();
        selectedApiKey = intent.getStringExtra("selected_key");

        initViews();
        setupAdapters();
        loadMachines();
        setupActionListeners();
    }

    private void initViews() {
        recyclerMachines = findViewById(R.id.recyclerMachines);
        statusText = findViewById(R.id.textStatus);
        nameText = findViewById(R.id.nameText);
        specsText = findViewById(R.id.textSpecs2);
        templateText = findViewById(R.id.textTemplate2);
        indicatorState = findViewById(R.id.indicatorState);
        llStart = findViewById(R.id.llStart);
        llStop = findViewById(R.id.llStop);
        llRestart = findViewById(R.id.llRestart);
        llPause = findViewById(R.id.llPause);
        llResume = findViewById(R.id.llResume);
        llCommand = findViewById(R.id.llCommand);
        commandLayout = findViewById(R.id.commandLayout);
        inputCommand = findViewById(R.id.inputCommand);
        btnExecute = findViewById(R.id.btnExecute);
        commandOutputText = findViewById(R.id.textCommandOutput);
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
        llCommand.setOnClickListener(v -> {
            commandLayout.setVisibility(View.VISIBLE);
        });
        btnExecute.setOnClickListener(v -> {
            String cmd = inputCommand.getText().toString().trim();
            if (cmd.isEmpty()) {
                Toast.makeText(this, "Enter a command", Toast.LENGTH_SHORT).show();
                return;
            }
            executeCommand(cmd);
        });
    }

    private void loadMachines() {
        if (selectedApiKey == null || selectedApiKey.isEmpty()) {
            Toast.makeText(this, "No API key selected", Toast.LENGTH_SHORT).show();
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
                    if (latestViewedMachine == null && !machines.isEmpty()) {
                        latestViewedMachine = machines.get(0);
                        selectedMachine = latestViewedMachine;
                        showPreview(latestViewedMachine);
                    }
                    Toast.makeText(MachineListActivity.this, "Loaded " + machines.size() + " machines",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MachineListActivity.this, "Failed: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MachineListResponse> call, Throwable t) {
                Toast.makeText(MachineListActivity.this, "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPreview(Machine machine) {
        if (machine == null) {
            statusText.setText("No machine selected");
            nameText.setText("");
            specsText.setText("");
            templateText.setText("");
            indicatorState.setImageResource(R.color.status_unknown);
            llStart.setVisibility(View.GONE);
            llStop.setVisibility(View.GONE);
            llRestart.setVisibility(View.GONE);
            llPause.setVisibility(View.GONE);
            return;
        }

        statusText.setText(machine.getState() != null ? machine.getState() : (machine.getDesiredState() != null ? machine.getDesiredState() : "unknown"));
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

        int statusColor = R.color.status_unknown;
        String state = machine.getState() != null ? machine.getState() : (machine.getDesiredState() != null ? machine.getDesiredState() : "unknown");
        if (state.equals("running") || state.equals("created")) {
            statusColor = R.color.status_running;
        } else if (state.equals("stopped")) {
            statusColor = R.color.status_stopped;
        } else if (state.equals("paused") || state.equals("suspended")) {
            statusColor = R.color.status_paused;
        } else if (state.equals("creating")) {
            statusColor = R.color.status_creating;
        } else if (state.equals("deleting")) {
            statusColor = R.color.status_deleting;
        } else if (state.equals("error")) {
            statusColor = R.color.status_error;
        }
        indicatorState.setImageResource(statusColor);

        llStart.setVisibility(View.VISIBLE);
        llStop.setVisibility(View.VISIBLE);
        llRestart.setVisibility(View.VISIBLE);
        llPause.setVisibility(View.VISIBLE);
        llResume.setVisibility(View.VISIBLE);
    }

    private void startMachine() {
        if (selectedMachine == null || selectedMachine.getId() == null) {
            Toast.makeText(this, "Select a machine to start", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiService service = ApiClient.createApiService(selectedApiKey);
        String idempotencyKey = UUID.randomUUID().toString();
        service.startMachine("Bearer " + selectedApiKey, selectedMachine.getId(), idempotencyKey)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MachineListActivity.this, "Started machine",
                                    Toast.LENGTH_SHORT).show();
                            loadMachines();
                        } else {
                            Toast.makeText(MachineListActivity.this, "Failed: " + response.message(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Toast.makeText(MachineListActivity.this, "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void stopMachine() {
        if (selectedMachine == null || selectedMachine.getId() == null) {
            Toast.makeText(this, "Select a machine to stop", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiService service = ApiClient.createApiService(selectedApiKey);
        String idempotencyKey = UUID.randomUUID().toString();
        service.stopMachine("Bearer " + selectedApiKey, selectedMachine.getId(), idempotencyKey)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MachineListActivity.this, "Stopped machine",
                                    Toast.LENGTH_SHORT).show();
                            loadMachines();
                        } else {
                            Toast.makeText(MachineListActivity.this, "Failed: " + response.message(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Toast.makeText(MachineListActivity.this, "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pauseMachine() {
        if (selectedMachine == null || selectedMachine.getId() == null) {
            Toast.makeText(this, "Select a machine to pause", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiService service = ApiClient.createApiService(selectedApiKey);
        String idempotencyKey = UUID.randomUUID().toString();
        service.suspendMachine("Bearer " + selectedApiKey, selectedMachine.getId(), idempotencyKey)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MachineListActivity.this, "Paused machine",
                                    Toast.LENGTH_SHORT).show();
                            loadMachines();
                        } else {
                            Toast.makeText(MachineListActivity.this, "Failed: " + response.message(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Toast.makeText(MachineListActivity.this, "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resumeMachine() {
        if (selectedMachine == null || selectedMachine.getId() == null) {
            Toast.makeText(this, "Select a machine to resume", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiService service = ApiClient.createApiService(selectedApiKey);
        String idempotencyKey = UUID.randomUUID().toString();
        service.resumeMachine("Bearer " + selectedApiKey, selectedMachine.getId(), idempotencyKey)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MachineListActivity.this, "Resumed machine",
                                    Toast.LENGTH_SHORT).show();
                            loadMachines();
                        } else {
                            Toast.makeText(MachineListActivity.this, "Failed: " + response.message(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Toast.makeText(MachineListActivity.this, "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void restartMachine() {
        if (selectedMachine == null || selectedMachine.getId() == null) {
            Toast.makeText(this, "Select a machine to restart", Toast.LENGTH_SHORT).show();
            return;
        }
        final String machineId = selectedMachine.getId();
        Toast.makeText(this, "Restarting machine…", Toast.LENGTH_SHORT).show();
        ApiService service = ApiClient.createApiService(selectedApiKey);
        String idempotencyKey = UUID.randomUUID().toString();
        service.stopMachine("Bearer " + selectedApiKey, machineId, idempotencyKey)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.isSuccessful()) {
                            waitForStoppedThenStart(machineId, 0);
                        } else {
                            Toast.makeText(MachineListActivity.this, "Restart failed: " + response.message(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Toast.makeText(MachineListActivity.this, "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void waitForStoppedThenStart(final String machineId, final int attempt) {
        if (attempt >= 20) {
            Toast.makeText(this, "Restart timed out waiting for stop", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(MachineListActivity.this, "Restarted machine",
                                                Toast.LENGTH_SHORT).show();
                                        loadMachines();
                                    } else {
                                        Toast.makeText(MachineListActivity.this, "Restart failed: " + r.message(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Object> c, Throwable t) {
                                    Toast.makeText(MachineListActivity.this, "Network error: " + t.getMessage(),
                                            Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Select a machine to run commands", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(MachineListActivity.this, "Command executed",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                commandOutputText.setText("Error: " + response.message());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<ApiService.ExecResult> call, Throwable t) {
                        runOnUiThread(() -> {
                            commandOutputText.setText("Error: " + t.getMessage());
                            Toast.makeText(MachineListActivity.this, "Network error",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }
}
