package com.instabox.manager;

import java.util.List;
import java.util.Map;
import java.io.Serializable;

public class Machine implements Serializable {
    private String id;
    private String name;
    private String state;
    private String desiredState;
    private int revision;
    private String operationId;
    private Template template;
    private Spec spec;
    private Storage storage;
    private Runtime runtime;
    private String autoSuspendAfterSeconds;
    private String maxRunMinutes;
    private String runStartedAt;
    private String cooldownUntil;
    private Map<String, Object> metadata;
    private boolean vault;
    private String guestUser;
    private String orgId;
    private String createdAt;
    private String updatedAt;
    private Snapshot snapshot;
    private Image image;
    private Error lastError;
    private String archiveAt;
    private String deleteAt;
    private String readyAt;
    private String cooldownReason;
    private AbuseLock abuseLock;
    private boolean suspendable;
    private String autoSuspendedAt;
    private List<String> envNames;
    private boolean free;
    private boolean earlyAllowanceEnded;
    private int oomKills;
    private Oom lastOom;
    private String id2;
    private String orgId2;
    private String machineId;
    private String type;
    private String state2;
    private String createdAt2;
    private String updatedAt2;

    // Local-only: which API key this machine was fetched with (not part of the API payload).
    private String apiKey;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public static class Template {
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

    public static class Spec {
        private int vcpus;
        private int memMiB;
        private int persistentDiskGiB;
        private int rootDiskGiB;

        public int getVcpus() { return vcpus; }
        public void setVcpus(int vcpus) { this.vcpus = vcpus; }
        public int getMemMiB() { return memMiB; }
        public void setMemMiB(int memMiB) { this.memMiB = memMiB; }
        public int getPersistentDiskGiB() { return persistentDiskGiB; }
        public void setPersistentDiskGiB(int persistentDiskGiB) { this.persistentDiskGiB = persistentDiskGiB; }
        public int getRootDiskGiB() { return rootDiskGiB; }
        public void setRootDiskGiB(int rootDiskGiB) { this.rootDiskGiB = rootDiskGiB; }
    }

    public static class Storage {
        private int sizeGiB;
        private String mountPath;
        private boolean persistent;
        private boolean synced;
        private int snapshotSeq;

        public int getSizeGiB() { return sizeGiB; }
        public void setSizeGiB(int sizeGiB) { this.sizeGiB = sizeGiB; }
        public String getMountPath() { return mountPath; }
        public void setMountPath(String mountPath) { this.mountPath = mountPath; }
        public boolean isPersistent() { return persistent; }
        public void setPersistent(boolean persistent) { this.persistent = persistent; }
        public boolean isSynced() { return synced; }
        public void setSynced(boolean synced) { this.synced = synced; }
        public int getSnapshotSeq() { return snapshotSeq; }
        public void setSnapshotSeq(int snapshotSeq) { this.snapshotSeq = snapshotSeq; }
    }

    public static class Runtime {
        private boolean ready;
        private boolean processesPreserved;

        public boolean isReady() { return ready; }
        public void setReady(boolean ready) { this.ready = ready; }
        public boolean isProcessesPreserved() { return processesPreserved; }
        public void setProcessesPreserved(boolean processesPreserved) { this.processesPreserved = processesPreserved; }
    }

    public static class Snapshot {
        private String id;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }

    public static class Image {
        private String id;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }

    public static class Error {
        private String code;
        private String message;
        private String requestId;
        private boolean retryable;
        private String operationId;
        private String reason;
        private String hint;
        private String docs;
        private String docsUrl;
        private List<String> requiredScopes;
        private int retryAfterSeconds;
        private String at;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        public boolean isRetryable() { return retryable; }
        public void setRetryable(boolean retryable) { this.retryable = retryable; }
        public String getOperationId() { return operationId; }
        public void setOperationId(String operationId) { this.operationId = operationId; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getHint() { return hint; }
        public void setHint(String hint) { this.hint = hint; }
        public String getDocs() { return docs; }
        public void setDocs(String docs) { this.docs = docs; }
        public String getDocsUrl() { return docsUrl; }
        public void setDocsUrl(String docsUrl) { this.docsUrl = docsUrl; }
        public List<String> getRequiredScopes() { return requiredScopes; }
        public void setRequiredScopes(List<String> requiredScopes) { this.requiredScopes = requiredScopes; }
        public int getRetryAfterSeconds() { return retryAfterSeconds; }
        public void setRetryAfterSeconds(int retryAfterSeconds) { this.retryAfterSeconds = retryAfterSeconds; }
        public String getAt() { return at; }
        public void setAt(String at) { this.at = at; }
    }

    public static class AbuseLock {
        private String at;
        private String explanation;
        private String what;
        private String appeal;
        private String dst;
        private int port;
        private String kind;
        private int count;
        private int coreShare;
        private String process;
        private String pool;

        public String getAt() { return at; }
        public void setAt(String at) { this.at = at; }
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
        public String getWhat() { return what; }
        public void setWhat(String what) { this.what = what; }
        public String getAppeal() { return appeal; }
        public void setAppeal(String appeal) { this.appeal = appeal; }
        public String getDst() { return dst; }
        public void setDst(String dst) { this.dst = dst; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getKind() { return kind; }
        public void setKind(String kind) { this.kind = kind; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public int getCoreShare() { return coreShare; }
        public void setCoreShare(int coreShare) { this.coreShare = coreShare; }
        public String getProcess() { return process; }
        public void setProcess(String process) { this.process = process; }
        public String getPool() { return pool; }
        public void setPool(String pool) { this.pool = pool; }
    }

    public static class Oom {
        private String at;
        private int pid;
        private String process;
        private long rssBytes;
        private long anonRssBytes;
        private boolean cgroup;
        private int uid;

        public String getAt() { return at; }
        public void setAt(String at) { this.at = at; }
        public int getPid() { return pid; }
        public void setPid(int pid) { this.pid = pid; }
        public String getProcess() { return process; }
        public void setProcess(String process) { this.process = process; }
        public long getRssBytes() { return rssBytes; }
        public void setRssBytes(long rssBytes) { this.rssBytes = rssBytes; }
        public long getAnonRssBytes() { return anonRssBytes; }
        public void setAnonRssBytes(long anonRssBytes) { this.anonRssBytes = anonRssBytes; }
        public boolean isCgroup() { return cgroup; }
        public void setCgroup(boolean cgroup) { this.cgroup = cgroup; }
        public int getUid() { return uid; }
        public void setUid(int uid) { this.uid = uid; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getDesiredState() { return desiredState; }
    public void setDesiredState(String desiredState) { this.desiredState = desiredState; }
    public int getRevision() { return revision; }
    public void setRevision(int revision) { this.revision = revision; }
    public String getOperationId() { return operationId; }
    public void setOperationId(String operationId) { this.operationId = operationId; }
    public Template getTemplate() { return template; }
    public void setTemplate(Template template) { this.template = template; }
    public Spec getSpec() { return spec; }
    public void setSpec(Spec spec) { this.spec = spec; }
    public Storage getStorage() { return storage; }
    public void setStorage(Storage storage) { this.storage = storage; }
    public Runtime getRuntime() { return runtime; }
    public void setRuntime(Runtime runtime) { this.runtime = runtime; }
    public String getAutoSuspendAfterSeconds() { return autoSuspendAfterSeconds; }
    public void setAutoSuspendAfterSeconds(String autoSuspendAfterSeconds) { this.autoSuspendAfterSeconds = autoSuspendAfterSeconds; }
    public String getMaxRunMinutes() { return maxRunMinutes; }
    public void setMaxRunMinutes(String maxRunMinutes) { this.maxRunMinutes = maxRunMinutes; }
    public String getRunStartedAt() { return runStartedAt; }
    public void setRunStartedAt(String runStartedAt) { this.runStartedAt = runStartedAt; }
    public String getCooldownUntil() { return cooldownUntil; }
    public void setCooldownUntil(String cooldownUntil) { this.cooldownUntil = cooldownUntil; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public boolean isVault() { return vault; }
    public void setVault(boolean vault) { this.vault = vault; }
    public String getGuestUser() { return guestUser; }
    public void setGuestUser(String guestUser) { this.guestUser = guestUser; }
    public String getOrgId() { return orgId; }
    public void setOrgId(String orgId) { this.orgId = orgId; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public Snapshot getSnapshot() { return snapshot; }
    public void setSnapshot(Snapshot snapshot) { this.snapshot = snapshot; }
    public Image getImage() { return image; }
    public void setImage(Image image) { this.image = image; }
    public Error getLastError() { return lastError; }
    public void setLastError(Error lastError) { this.lastError = lastError; }
    public String getArchiveAt() { return archiveAt; }
    public void setArchiveAt(String archiveAt) { this.archiveAt = archiveAt; }
    public String getDeleteAt() { return deleteAt; }
    public void setDeleteAt(String deleteAt) { this.deleteAt = deleteAt; }
    public String getReadyAt() { return readyAt; }
    public void setReadyAt(String readyAt) { this.readyAt = readyAt; }
    public String getCooldownReason() { return cooldownReason; }
    public void setCooldownReason(String cooldownReason) { this.cooldownReason = cooldownReason; }
    public AbuseLock getAbuseLock() { return abuseLock; }
    public void setAbuseLock(AbuseLock abuseLock) { this.abuseLock = abuseLock; }
    public boolean isSuspendable() { return suspendable; }
    public void setSuspendable(boolean suspendable) { this.suspendable = suspendable; }
    public String getAutoSuspendedAt() { return autoSuspendedAt; }
    public void setAutoSuspendedAt(String autoSuspendedAt) { this.autoSuspendedAt = autoSuspendedAt; }
    public List<String> getEnvNames() { return envNames; }
    public void setEnvNames(List<String> envNames) { this.envNames = envNames; }
    public boolean isFree() { return free; }
    public void setFree(boolean free) { this.free = free; }
    public boolean isEarlyAllowanceEnded() { return earlyAllowanceEnded; }
    public void setEarlyAllowanceEnded(boolean earlyAllowanceEnded) { this.earlyAllowanceEnded = earlyAllowanceEnded; }
    public int getOomKills() { return oomKills; }
    public void setOomKills(int oomKills) { this.oomKills = oomKills; }
    public Oom getLastOom() { return lastOom; }
    public void setLastOom(Oom lastOom) { this.lastOom = lastOom; }
    public String getId2() { return id2; }
    public void setId2(String id2) { this.id2 = id2; }
    public String getOrgId2() { return orgId2; }
    public void setOrgId2(String orgId2) { this.orgId2 = orgId2; }
    public String getMachineId() { return machineId; }
    public void setMachineId(String machineId) { this.machineId = machineId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getState2() { return state2; }
    public void setState2(String state2) { this.state2 = state2; }
    public String getCreatedAt2() { return createdAt2; }
    public void setCreatedAt2(String createdAt2) { this.createdAt2 = createdAt2; }
    public String getUpdatedAt2() { return updatedAt2; }
    public void setUpdatedAt2(String updatedAt2) { this.updatedAt2 = updatedAt2; }

    
}
