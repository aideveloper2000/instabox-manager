package com.instabox.manager;

public class ApiKey {
    private String key;
    private String name;
    private boolean enabled;

    // Local-only runtime state (not persisted).
    private transient int machineCount;
    private transient boolean syncFailed;

    public ApiKey() {
    }

    public ApiKey(String key, String name) {
        this.key = key;
        this.name = name;
        this.enabled = true;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMachineCount() {
        return machineCount;
    }

    public void setMachineCount(int machineCount) {
        this.machineCount = machineCount;
    }

    public boolean isSyncFailed() {
        return syncFailed;
    }

    public void setSyncFailed(boolean syncFailed) {
        this.syncFailed = syncFailed;
    }
}
