package com.instabox.manager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MachineDetailActivity extends AppCompatActivity {
    private View coordinatorDetail;
    private String apiKey;
    private String machineId;
    private Machine machine;

    private TextView statusText;
    private TextView nameText;
    private TextView specsText;
    private TextView templateText;
    private TextView machineIdText;
    private ImageView indicatorState;
    private MaterialButton llStart;
    private MaterialButton llStop;
    private MaterialButton llRestart;
    private MaterialButton llPause;
    private MaterialButton llResume;
    private MaterialButton llWake;
    private MaterialButton llDelete;
    private MaterialButton llCommand;
    private MaterialButton llLogs;
    private MaterialButton llMetrics;
    private MaterialButton llTerminal;
    private MaterialButton llVnc;
    private MaterialButton llSsh;
    private View commandCard;
    private TextInputLayout commandLayout;
    private TextInputEditText inputCommand;
    private MaterialButton btnExecute;
    private TextView commandOutputText;

    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_detail);

        executor = Executors.newSingleThreadExecutor();

        Intent intent = getIntent();
        machineId = intent.getStringExtra("machine_id");
        apiKey = intent.getStringExtra("api_key");

        if (machineId == null || machineId.isEmpty()) {
            Snackbar.make(findViewById(R.id.coordinatorDetail), R.string.machine_details, Snackbar.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupActionListeners();
        loadMachineDetails();
    }

    private void initViews() {
        coordinatorDetail = findViewById(R.id.coordinatorDetail);
        statusText = findViewById(R.id.textStatus);
        nameText = findViewById(R.id.textName);
        specsText = findViewById(R.id.textSpecs2);
        templateText = findViewById(R.id.textTemplate2);
        machineIdText = findViewById(R.id.textMachineId);
        indicatorState = findViewById(R.id.indicatorState);
        llStart = findViewById(R.id.llStart);
        llStop = findViewById(R.id.llStop);
        llRestart = findViewById(R.id.llRestart);
        llPause = findViewById(R.id.llPause);
        llResume = findViewById(R.id.llResume);
        llWake = findViewById(R.id.llWake);
        llDelete = findViewById(R.id.llDelete);
        llCommand = findViewById(R.id.llCommand);
        llLogs = findViewById(R.id.llLogs);
        llMetrics = findViewById(R.id.llMetrics);
        llTerminal = findViewById(R.id.llTerminal);
        llVnc = findViewById(R.id.llVnc);
        llSsh = findViewById(R.id.llSsh);
        commandCard = findViewById(R.id.commandCard);
        commandLayout = findViewById(R.id.commandLayout);
        inputCommand = findViewById(R.id.inputCommand);
        btnExecute = findViewById(R.id.btnExecute);
        commandOutputText = findViewById(R.id.textCommandOutput);

        com.google.android.material.appbar.MaterialToolbar toolbarDetail = findViewById(R.id.toolbarDetail);
        toolbarDetail.setNavigationOnClickListener(v -> finish());
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
                            Snackbar.make(coordinatorDetail,
                                    getString(R.string.connection_error) + ": " + response.message(),
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(Call<Machine> call, Throwable t) {
                    runOnUiThread(() -> Snackbar.make(coordinatorDetail,
                            getString(R.string.connection_error) + ": " + t.getMessage(),
                            Snackbar.LENGTH_SHORT).show());
                }
            });
        });
    }

    private void populateMachineData() {
        if (machine == null) {
            commandOutputText.setText(R.string.connection_error);
            return;
        }

        String state = machine.getState() != null ? machine.getState()
                : (machine.getDesiredState() != null ? machine.getDesiredState() : "unknown");
        statusText.setText(state);
        nameText.setText(machine.getName());
        machineIdText.setText(machine.getId());
        indicatorState.setColorFilter(statusColor(state));

        if (machine.getSpec() != null) {
            float memGb = machine.getSpec().getMemMiB() / 1024f;
            specsText.setText(String.format(java.util.Locale.getDefault(), getString(R.string.machine_spec),
                    machine.getSpec().getVcpus(), memGb));
        }

        if (machine.getTemplate() != null && machine.getTemplate().getId() != null) {
            templateText.setText(machine.getTemplate().getId());
        }

        commandOutputText.setText(R.string.command_hint);
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
        return ContextCompat.getColor(this, colorRes);
    }

    private void setupActionListeners() {
        llStart.setOnClickListener(v -> startMachine());
        llStop.setOnClickListener(v -> stopMachine());
        llRestart.setOnClickListener(v -> restartMachine());
        llPause.setOnClickListener(v -> pauseMachine());
        llResume.setOnClickListener(v -> resumeMachine());
        llWake.setOnClickListener(v -> wakeMachine());
        llDelete.setOnClickListener(v -> confirmDelete());
        llCommand.setOnClickListener(v -> {
            commandCard.setVisibility(commandCard.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });
        llLogs.setOnClickListener(v -> Snackbar.make(coordinatorDetail, R.string.logs, Snackbar.LENGTH_SHORT).show());
        llMetrics.setOnClickListener(v -> Snackbar.make(coordinatorDetail, R.string.metrics, Snackbar.LENGTH_SHORT).show());
        llTerminal.setOnClickListener(v -> Snackbar.make(coordinatorDetail, R.string.terminal, Snackbar.LENGTH_SHORT).show());
        llVnc.setOnClickListener(v -> Snackbar.make(coordinatorDetail, R.string.vnc, Snackbar.LENGTH_SHORT).show());
        llSsh.setOnClickListener(v -> Snackbar.make(coordinatorDetail, R.string.ssh, Snackbar.LENGTH_SHORT).show());
        btnExecute.setOnClickListener(v -> {
            String cmd = inputCommand.getText() == null ? "" : inputCommand.getText().toString().trim();
            if (cmd.isEmpty()) {
                Snackbar.make(coordinatorDetail, R.string.enter_command, Snackbar.LENGTH_SHORT).show();
                return;
            }
            executeCommand(cmd);
        });
    }

    private Callback<Object> actionCallback(String successMessage) {
        return new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    Snackbar.make(coordinatorDetail, successMessage, Snackbar.LENGTH_SHORT).show();
                    loadMachineDetails();
                } else {
                    Snackbar.make(coordinatorDetail, getString(R.string.connection_error) + ": " + response.message(),
                            Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Snackbar.make(coordinatorDetail, getString(R.string.connection_error) + ": " + t.getMessage(),
                        Snackbar.LENGTH_SHORT).show();
            }
        };
    }

    private void startMachine() {
        if (machine == null || machine.getId() == null) {
            Snackbar.make(coordinatorDetail, R.string.machine_details, Snackbar.LENGTH_SHORT).show();
            return;
        }
        ApiService service = ApiClient.createApiService(apiKey);
        String idempotencyKey = UUID.randomUUID().toString();
        service.startMachine("Bearer " + apiKey, machine.getId(), idempotencyKey)
                .enqueue(actionCallback(getString(R.string.start_machine)));
    }

    private void stopMachine() {
        if (machine == null || machine.getId() == null) {
            Snackbar.make(coordinatorDetail, R.string.machine_details, Snackbar.LENGTH_SHORT).show();
            return;
        }
        ApiService service = ApiClient.createApiService(apiKey);
        String idempotencyKey = UUID.randomUUID().toString();
        service.stopMachine("Bearer " + apiKey, machine.getId(), idempotencyKey)
                .enqueue(actionCallback(getString(R.string.stop_machine)));
    }

    private void pauseMachine() {
        if (machine == null || machine.getId() == null) {
            Snackbar.make(coordinatorDetail, R.string.machine_details, Snackbar.LENGTH_SHORT).show();
            return;
        }
        ApiService service = ApiClient.createApiService(apiKey);
        String idempotencyKey = UUID.randomUUID().toString();
        service.suspendMachine("Bearer " + apiKey, machine.getId(), idempotencyKey)
                .enqueue(actionCallback(getString(R.string.pause_machine)));
    }

    private void resumeMachine() {
        if (machine == null || machine.getId() == null) {
            Snackbar.make(coordinatorDetail, R.string.machine_details, Snackbar.LENGTH_SHORT).show();
            return;
        }
        ApiService service = ApiClient.createApiService(apiKey);
        String idempotencyKey = UUID.randomUUID().toString();
        service.resumeMachine("Bearer " + apiKey, machine.getId(), idempotencyKey)
                .enqueue(actionCallback(getString(R.string.resume_machine)));
    }

    private void wakeMachine() {
        if (machine == null || machine.getId() == null) {
            Snackbar.make(coordinatorDetail, R.string.machine_details, Snackbar.LENGTH_SHORT).show();
            return;
        }
        ApiService service = ApiClient.createApiService(apiKey);
        String idempotencyKey = UUID.randomUUID().toString();
        service.wakeMachine("Bearer " + apiKey, machine.getId(), idempotencyKey)
                .enqueue(actionCallback(getString(R.string.wake_machine)));
    }

    private void confirmDelete() {
        if (machine == null || machine.getId() == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_machine)
                .setMessage(getString(R.string.machine_name) + ": " + machine.getName())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete_machine, (dialog, which) -> deleteMachine())
                .show();
    }

    private void deleteMachine() {
        ApiService service = ApiClient.createApiService(apiKey);
        String idempotencyKey = UUID.randomUUID().toString();
        service.deleteMachine("Bearer " + apiKey, machine.getId(), "true", idempotencyKey)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.isSuccessful()) {
                            Snackbar.make(coordinatorDetail, R.string.delete_machine, Snackbar.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Snackbar.make(coordinatorDetail,
                                    getString(R.string.connection_error) + ": " + response.message(),
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Snackbar.make(coordinatorDetail,
                                getString(R.string.connection_error) + ": " + t.getMessage(),
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void restartMachine() {
        if (machine == null || machine.getId() == null) {
            Snackbar.make(coordinatorDetail, R.string.machine_details, Snackbar.LENGTH_SHORT).show();
            return;
        }
        final String id = machine.getId();
        Snackbar.make(coordinatorDetail, R.string.restart_machine + "…", Snackbar.LENGTH_SHORT).show();
        ApiService service = ApiClient.createApiService(apiKey);
        String idempotencyKey = UUID.randomUUID().toString();
        service.stopMachine("Bearer " + apiKey, id, idempotencyKey)
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.isSuccessful()) {
                            waitForStoppedThenStart(id, 0);
                        } else {
                            Snackbar.make(coordinatorDetail,
                                    getString(R.string.connection_error) + ": " + response.message(),
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Snackbar.make(coordinatorDetail,
                                getString(R.string.connection_error) + ": " + t.getMessage(),
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void waitForStoppedThenStart(final String machineId, final int attempt) {
        if (attempt >= 20) {
            Snackbar.make(coordinatorDetail, R.string.connection_error, Snackbar.LENGTH_SHORT).show();
            return;
        }
        ApiService service = ApiClient.createApiService(apiKey);
        service.getMachine("Bearer " + apiKey, machineId).enqueue(new Callback<Machine>() {
            @Override
            public void onResponse(Call<Machine> call, Response<Machine> response) {
                if (response.isSuccessful() && response.body() != null
                        && "stopped".equals(response.body().getState())) {
                    ApiService svc = ApiClient.createApiService(apiKey);
                    svc.startMachine("Bearer " + apiKey, machineId, UUID.randomUUID().toString())
                            .enqueue(actionCallback(getString(R.string.restart_machine)));
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
        if (machine == null || machine.getId() == null) {
            Snackbar.make(coordinatorDetail, R.string.machine_details, Snackbar.LENGTH_SHORT).show();
            return;
        }
        ApiService.ExecRequest execRequest = new ApiService.ExecRequest(command);
        ApiService service = ApiClient.createApiService(apiKey);
        String idempotencyKey = UUID.randomUUID().toString();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
