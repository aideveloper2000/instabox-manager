package com.instabox.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MachineDetailActivity extends AppCompatActivity {
    private String apiKey;
    private String machineId;
    private Machine machine;

    private TextView statusText;
    private TextView nameText;
    private TextView specsText;
    private TextView templateText;
    private ImageView indicatorState;
    private LinearLayout llStart;
    private LinearLayout llStop;
    private LinearLayout llRestart;
    private LinearLayout llPause;
    private LinearLayout llCommand;
    private LinearLayout llLogs;
    private LinearLayout llMetrics;
    private LinearLayout llTerminal;
    private LinearLayout llVnc;
    private LinearLayout llSsh;
    private TextInputLayout commandLayout;
    private TextInputEditText inputCommand;
    private Button btnExecute;
    private TextView commandOutputText;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_detail);

        Intent intent = getIntent();
        machineId = intent.getStringExtra("machine_id");
        apiKey = intent.getStringExtra("api_key");

        if (machineId == null || machineId.isEmpty()) {
            Toast.makeText(this, "No machine selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupAdapters();
        loadMachineDetails();
        setupActionListeners();
    }

    private void initViews() {
        statusText = findViewById(R.id.textStatus);
        nameText = findViewById(R.id.textName);
        specsText = findViewById(R.id.textSpecs2);
        templateText = findViewById(R.id.textTemplate2);
        indicatorState = findViewById(R.id.indicatorState);
        llStart = findViewById(R.id.llStart);
        llStop = findViewById(R.id.llStop);
        llRestart = findViewById(R.id.llRestart);
        llPause = findViewById(R.id.llPause);
        llCommand = findViewById(R.id.llCommand);
        commandLayout = findViewById(R.id.commandLayout);
        inputCommand = findViewById(R.id.inputCommand);
        btnExecute = findViewById(R.id.btnExecute);
        commandOutputText = findViewById(R.id.textCommandOutput);
        llLogs = findViewById(R.id.llLogs);
        llMetrics = findViewById(R.id.llMetrics);
    }

    private void setupAdapters() {
        // No adapters needed for detail view
    }

    private void loadMachineDetails() {
        executor.execute(() -> {
            ApiService service = ApiClient.createApiService(apiKey);
            Call<Machine> call = service.getMachine("Bearer " + apiKey, machineId);
            call.enqueue(new Callback<Machine>() {
                @Override
                public void onResponse(Call<Machine> call, Response<Machine> response) {
                    runOnUiThread(() -> {
                        if (response.isSuccessful() && response.body() != null) {
                            machine = response.body();
                            populateMachineData();
                        } else {
                            Toast.makeText(MachineDetailActivity.this, "Failed to load machine: " + response.message(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(Call<Machine> call, Throwable t) {
                    runOnUiThread(() -> {
                        Toast.makeText(MachineDetailActivity.this, "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }

    private void populateMachineData() {
        if (machine == null) {
            commandOutputText.setText("Failed to load machine data");
            return;
        }

        String state = machine.getState() != null ? machine.getState() : (machine.getDesiredState() != null ? machine.getDesiredState() : "unknown");
        statusText.setText(state);
        nameText.setText(machine.getName());

        int statusColor = R.color.status_unknown;
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

        if (machine.getSpec() != null) {
            float memGb = machine.getSpec().getMemMiB() / 1024f;
            specsText.setText(String.format(java.util.Locale.getDefault(), getString(R.string.machine_spec),
                    machine.getSpec().getVcpus(), memGb));
        }

        if (machine.getTemplate() != null && machine.getTemplate().getId() != null) {
            templateText.setText(machine.getTemplate().getId());
        }

        commandOutputText.setText("Select a machine and enter a command to run");
    }

    private void setupActionListeners() {
        llStart.setOnClickListener(v -> startMachine());
        llStop.setOnClickListener(v -> stopMachine());
        llRestart.setOnClickListener(v -> restartMachine());
        llPause.setOnClickListener(v -> pauseMachine());
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
        llLogs.setOnClickListener(v -> Toast.makeText(this, "Logs", Toast.LENGTH_SHORT).show());
        llMetrics.setOnClickListener(v -> Toast.makeText(this, "Metrics", Toast.LENGTH_SHORT).show());
        llTerminal.setOnClickListener(v -> Toast.makeText(this, "Terminal", Toast.LENGTH_SHORT).show());
        llVnc.setOnClickListener(v -> Toast.makeText(this, "VNC", Toast.LENGTH_SHORT).show());
        llSsh.setOnClickListener(v -> Toast.makeText(this, "SSH", Toast.LENGTH_SHORT).show());
    }

    private void startMachine() {
        if (machine == null || machine.getId() == null) {
            Toast.makeText(this, "Machine ID not available", Toast.LENGTH_SHORT).show();
            return;
        }
        executeOperationAction(machine.getId(), () -> {
            ApiService service = ApiClient.createApiService(apiKey);
            String idempotencyKey = java.util.UUID.randomUUID().toString();
            service.startMachine("Bearer " + apiKey, machine.getId(), idempotencyKey);
        });
    }

    private void stopMachine() {
        if (machine == null || machine.getId() == null) {
            Toast.makeText(this, "Machine ID not available", Toast.LENGTH_SHORT).show();
            return;
        }
        executeOperationAction(machine.getId(), () -> {
            ApiService service = ApiClient.createApiService(apiKey);
            String idempotencyKey = java.util.UUID.randomUUID().toString();
            service.stopMachine("Bearer " + apiKey, machine.getId(), idempotencyKey);
        });
    }

    private void restartMachine() {
        if (machine == null || machine.getId() == null) {
            Toast.makeText(this, "Machine ID not available", Toast.LENGTH_SHORT).show();
            return;
        }
        stopMachine();
        Toast.makeText(this, "Restart requested", Toast.LENGTH_SHORT).show();
    }

    private void pauseMachine() {
        if (machine == null || machine.getId() == null) {
            Toast.makeText(this, "Machine ID not available", Toast.LENGTH_SHORT).show();
            return;
        }
        executeOperationAction(machine.getId(), () -> {
            ApiService service = ApiClient.createApiService(apiKey);
            String idempotencyKey = java.util.UUID.randomUUID().toString();
            service.suspendMachine("Bearer " + apiKey, machine.getId(), idempotencyKey);
        });
    }

    private void executeCommand(String command) {
        if (machine == null || machine.getId() == null) {
            Toast.makeText(this, "Machine ID not available", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiService.ExecRequest execRequest = new ApiService.ExecRequest(command);
        ApiService service = ApiClient.createApiService(apiKey);
        String idempotencyKey = java.util.UUID.randomUUID().toString();
        service.execCommand("Bearer " + apiKey, machine.getId(), execRequest, idempotencyKey)
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
                                Toast.makeText(MachineDetailActivity.this, "Command executed", Toast.LENGTH_SHORT).show();
                            } else {
                                commandOutputText.setText("Error: " + response.message());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<ApiService.ExecResult> call, Throwable t) {
                        runOnUiThread(() -> {
                            commandOutputText.setText("Error: " + t.getMessage());
                            Toast.makeText(MachineDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void executeOperationAction(String machineId, ApiService.ApiServiceOperation operation) {
        operation.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
