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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MachineListActivity extends AppCompatActivity {
    private RecyclerView recyclerMachines;
    private MachineAdapter machineAdapter;
    private List<Machine> machines;
    private String selectedApiKey;

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
    }

    private void setupAdapters() {
        machines = new ArrayList<>();
        machineAdapter = new MachineAdapter(this);
        machineAdapter.setOnItemClickListener(machine -> showMachineDetails(machine));
        recyclerMachines.setLayoutManager(new LinearLayoutManager(this));
        recyclerMachines.setAdapter(machineAdapter);
    }

    private void setupActionListeners() {
        llStart.setOnClickListener(v -> startMachine());
        llStop.setOnClickListener(v -> stopMachine());
        llRestart.setOnClickListener(v -> {
            // Can't restart without a machine selected
            Toast.makeText(this, "Select a machine first", Toast.LENGTH_SHORT).show();
        });
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
    }

    private void loadMachines() {
        if (selectedApiKey == null || selectedApiKey.isEmpty()) {
            Toast.makeText(this, "No API key selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService service = ApiClient.createApiService(selectedApiKey);
        Call<List<Machine>> call = service.listMachines("Bearer " + selectedApiKey, selectedApiKey);

        call.enqueue(new Callback<List<Machine>>() {
            @Override
            public void onResponse(Call<List<Machine>> call, Response<List<Machine>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    machines.clear();
                    machines.addAll(response.body());
                    machineAdapter.setMachines(machines);
                    Toast.makeText(MachineListActivity.this, "Loaded " + machines.size() + " machines",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MachineListActivity.this, "Failed: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Machine>> call, Throwable t) {
                Toast.makeText(MachineListActivity.this, "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMachineDetails(Machine machine) {
        Intent intent = new Intent(this, MachineDetailActivity.class);
        intent.putExtra("machine_id", machine.getId());
        intent.putExtra("api_key", selectedApiKey);
        startActivity(intent);
    }

    private void startMachine() {
        // Show selection dialog
        Toast.makeText(this, "Select a machine to start", Toast.LENGTH_SHORT).show();
    }

    private void stopMachine() {
        Toast.makeText(this, "Select a machine to stop", Toast.LENGTH_SHORT).show();
    }

    private void pauseMachine() {
        Toast.makeText(this, "Select a machine to pause", Toast.LENGTH_SHORT).show();
    }

    private void executeCommand(String command) {
        // TODO: implement command execution
        commandOutputText.setText("Command: " + command);
        Toast.makeText(this, "Running: " + command, Toast.LENGTH_SHORT).show();
    }
}
