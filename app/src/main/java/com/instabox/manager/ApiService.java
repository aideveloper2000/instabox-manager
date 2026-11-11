package com.instabox.manager;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Body;
import retrofit2.http.Query;

public interface ApiService {
    String BASE_URL = "https://api.instabox.sh/";

    interface ApiServiceOperation {
        void execute();
    }

    @GET("v1/machines")
    Call<List<Machine>> listMachines(
            @Header("Authorization") String authHeader,
            @Header("X-API-Key") String apiKey
    );

    @GET("v1/machines/{id}")
    Call<Machine> getMachine(
            @Header("Authorization") String authHeader,
            @Path("id") String id
    );

    @POST("v1/machines/{id}/start")
    Call<Object> startMachine(
            @Header("Authorization") String authHeader,
            @Path("id") String id,
            @Header("Idempotency-Key") String idempotencyKey
    );

    @POST("v1/machines/{id}/stop")
    Call<Object> stopMachine(
            @Header("Authorization") String authHeader,
            @Path("id") String id,
            @Header("Idempotency-Key") String idempotencyKey
    );

    @POST("v1/machines/{id}/suspend")
    Call<Object> suspendMachine(
            @Header("Authorization") String authHeader,
            @Path("id") String id,
            @Header("Idempotency-Key") String idempotencyKey
    );

    @POST("v1/machines/{id}/resume")
    Call<Object> resumeMachine(
            @Header("Authorization") String authHeader,
            @Path("id") String id,
            @Header("Idempotency-Key") String idempotencyKey
    );

    @POST("v1/machines/{id}/wake")
    Call<Object> wakeMachine(
            @Header("Authorization") String authHeader,
            @Path("id") String id,
            @Header("Idempotency-Key") String idempotencyKey
    );

    @DELETE("v1/machines/{id}")
    Call<Object> deleteMachine(
            @Header("Authorization") String authHeader,
            @Path("id") String id,
            @Header("X-Confirm-Delete") String confirm,
            @Header("Idempotency-Key") String idempotencyKey
    );

    @POST("v1/machines/{id}/fork")
    Call<Object> forkMachine(
            @Header("Authorization") String authHeader,
            @Path("id") String id,
            @Header("Idempotency-Key") String idempotencyKey
    );

    @POST("v1/machines/{id}/exec")
    Call<ExecResult> execCommand(
            @Header("Authorization") String authHeader,
            @Path("id") String id,
            @Body ExecRequest request,
            @Header("Idempotency-Key") String idempotencyKey
    );

    @GET("v1/templates")
    Call<List<Template>> listTemplates(
            @Header("Authorization") String authHeader,
            @Header("X-API-Key") String apiKey
    );

    @GET("v1/orgs")
    Call<List<Org>> listOrgs(
            @Header("Authorization") String authHeader,
            @Header("X-API-Key") String apiKey
    );

    @GET("v1/me")
    Call<Me> getCurrentUser(
            @Header("Authorization") String authHeader,
            @Header("X-API-Key") String apiKey
    );

    @GET("v1/usage/machines")
    Call<Usage> getMachineUsage(
            @Header("Authorization") String authHeader,
            @Header("X-API-Key") String apiKey
    );

    static class ExecRequest {
        private String command;

        public ExecRequest(String command) {
            this.command = command;
        }

        public String getCommand() { return command; }
        public void setCommand(String command) { this.command = command; }
    }

    static class ExecResult {
        private String stdout;
        private String stderr;
        private int exitCode;

        public String getStdout() { return stdout; }
        public void setStdout(String stdout) { this.stdout = stdout; }
        public String getStderr() { return stderr; }
        public void setStderr(String stderr) { this.stderr = stderr; }
        public int getExitCode() { return exitCode; }
        public void setExitCode(int exitCode) { this.exitCode = exitCode; }
    }

    static class Template {
        private String id;
        private String version;
        private String digest;
        private String kernel;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getDigest() { return digest; }
        public void setDigest(String digest) { this.digest = digest; }
        public String getKernel() { return kernel; }
        public void setKernel(String kernel) { this.kernel = kernel; }
    }

    static class Org {
        private String id;
        private String name;
        private String kind;
        private String state;
        private boolean owner;
        private int memberCount;
        private Long createdAt;
        private Long updatedAt;
        private int vcpus;
        private int memMiB;
        private int rootDiskGiB;
        private int allowedSpecs;
        private int maxRunMinutes;
        private int autoSuspendAfterSeconds;
        private boolean free;
        private boolean earlyAllowanceEnded;
        private Long createdAt2;
        private Long updatedAt2;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getKind() { return kind; }
        public void setKind(String kind) { this.kind = kind; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public boolean isOwner() { return owner; }
        public void setOwner(boolean owner) { this.owner = owner; }
        public int getMemberCount() { return memberCount; }
        public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
        public Long getCreatedAt() { return createdAt; }
        public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
        public Long getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
        public int getVcpus() { return vcpus; }
        public void setVcpus(int vcpus) { this.vcpus = vcpus; }
        public int getMemMiB() { return memMiB; }
        public void setMemMiB(int memMiB) { this.memMiB = memMiB; }
        public int getRootDiskGiB() { return rootDiskGiB; }
        public void setRootDiskGiB(int rootDiskGiB) { this.rootDiskGiB = rootDiskGiB; }
        public int getAllowedSpecs() { return allowedSpecs; }
        public void setAllowedSpecs(int allowedSpecs) { this.allowedSpecs = allowedSpecs; }
        public int getMaxRunMinutes() { return maxRunMinutes; }
        public void setMaxRunMinutes(int maxRunMinutes) { this.maxRunMinutes = maxRunMinutes; }
        public int getAutoSuspendAfterSeconds() { return autoSuspendAfterSeconds; }
        public void setAutoSuspendAfterSeconds(int autoSuspendAfterSeconds) { this.autoSuspendAfterSeconds = autoSuspendAfterSeconds; }
        public boolean isFree() { return free; }
        public void setFree(boolean free) { this.free = free; }
        public boolean isEarlyAllowanceEnded() { return earlyAllowanceEnded; }
        public void setEarlyAllowanceEnded(boolean earlyAllowanceEnded) { this.earlyAllowanceEnded = earlyAllowanceEnded; }
        public Long getCreatedAt2() { return createdAt2; }
        public void setCreatedAt2(Long createdAt2) { this.createdAt2 = createdAt2; }
        public Long getUpdatedAt2() { return updatedAt2; }
        public void setUpdatedAt2(Long updatedAt2) { this.updatedAt2 = updatedAt2; }
    }

    static class Me {
        private String id;
        private String name;
        private String email;
        private String kind;
        private String state;
        private String avatarUrl;
        private Long createdAt;
        private String orgId;
        private int vcpus;
        private int memMiB;
        private int rootDiskGiB;
        private int maxRunMinutes;
        private int autoSuspendAfterSeconds;
        private Long createdAt2;
        private String orgName;
        private Boolean orgOwner;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getKind() { return kind; }
        public void setKind(String kind) { this.kind = kind; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public Long getCreatedAt() { return createdAt; }
        public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
        public String getOrgId() { return orgId; }
        public void setOrgId(String orgId) { this.orgId = orgId; }
        public int getVcpus() { return vcpus; }
        public void setVcpus(int vcpus) { this.vcpus = vcpus; }
        public int getMemMiB() { return memMiB; }
        public void setMemMiB(int memMiB) { this.memMiB = memMiB; }
        public int getRootDiskGiB() { return rootDiskGiB; }
        public void setRootDiskGiB(int rootDiskGiB) { this.rootDiskGiB = rootDiskGiB; }
        public int getMaxRunMinutes() { return maxRunMinutes; }
        public void setMaxRunMinutes(int maxRunMinutes) { this.maxRunMinutes = maxRunMinutes; }
        public int getAutoSuspendAfterSeconds() { return autoSuspendAfterSeconds; }
        public void setAutoSuspendAfterSeconds(int autoSuspendAfterSeconds) { this.autoSuspendAfterSeconds = autoSuspendAfterSeconds; }
        public Long getCreatedAt2() { return createdAt2; }
        public void setCreatedAt2(Long createdAt2) { this.createdAt2 = createdAt2; }
        public String getOrgName() { return orgName; }
        public void setOrgName(String orgName) { this.orgName = orgName; }
        public Boolean getOrgOwner() { return orgOwner; }
        public void setOrgOwner(Boolean orgOwner) { this.orgOwner = orgOwner; }
    }

    static class Usage {
        private String entity;
        private String kind;
        private long storage;
        private long saved;
        private double totalCost;
        private double thisMonthCost;
        private String currency;
        private long creditBalance;
        private long used;
        private long limit;
        private int maxMachines;
        private int usedMachines;
        private long usedSeconds;
        private long paidSeconds;
        private long freeSeconds;
        private String period;
        private String window;
        private List<String> instances;

        public String getEntity() { return entity; }
        public void setEntity(String entity) { this.entity = entity; }
        public String getKind() { return kind; }
        public void setKind(String kind) { this.kind = kind; }
        public long getStorage() { return storage; }
        public void setStorage(long storage) { this.storage = storage; }
        public long getSaved() { return saved; }
        public void setSaved(long saved) { this.saved = saved; }
        public double getTotalCost() { return totalCost; }
        public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
        public double getThisMonthCost() { return thisMonthCost; }
        public void setThisMonthCost(double thisMonthCost) { this.thisMonthCost = thisMonthCost; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public long getCreditBalance() { return creditBalance; }
        public void setCreditBalance(long creditBalance) { this.creditBalance = creditBalance; }
        public long getUsed() { return used; }
        public void setUsed(long used) { this.used = used; }
        public long getLimit() { return limit; }
        public void setLimit(long limit) { this.limit = limit; }
        public int getMaxMachines() { return maxMachines; }
        public void setMaxMachines(int maxMachines) { this.maxMachines = maxMachines; }
        public int getUsedMachines() { return usedMachines; }
        public void setUsedMachines(int usedMachines) { this.usedMachines = usedMachines; }
        public long getUsedSeconds() { return usedSeconds; }
        public void setUsedSeconds(long usedSeconds) { this.usedSeconds = usedSeconds; }
        public long getPaidSeconds() { return paidSeconds; }
        public void setPaidSeconds(long paidSeconds) { this.paidSeconds = paidSeconds; }
        public long getFreeSeconds() { return freeSeconds; }
        public void setFreeSeconds(long freeSeconds) { this.freeSeconds = freeSeconds; }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public String getWindow() { return window; }
        public void setWindow(String window) { this.window = window; }
        public List<String> getInstances() { return instances; }
        public void setInstances(List<String> instances) { this.instances = instances; }
    }
}
