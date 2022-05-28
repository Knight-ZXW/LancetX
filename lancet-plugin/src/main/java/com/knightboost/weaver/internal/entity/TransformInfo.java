package com.knightboost.weaver.internal.entity;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * A data sets store all data for transform operation.
 */
public class TransformInfo {

    //weave classes
    public Set<String> hookClasses = new HashSet<>();

    // class->group map
    public Map<String,String> weaveClassGroup = new HashMap<>();

    /**
     * Key for target class's name.
     * Value for all InsertInfo want insert into the target class.
     */
    public Map<String, List<InsertInfo>> insertInfo = new HashMap<>();
    public List<ProxyInfo> proxyInfo = new ArrayList<>();
    public List<TryCatchInfo> tryCatchInfo = new ArrayList<>();
    public List<ReplaceInfo> replaceInfo = new ArrayList<>();
    public List<BeforeCallInfo> beforeCallInfo = new ArrayList<>();

    public TransformInfo(){

    }

    public synchronized void combine(TransformInfo other) {
        other.insertInfo.forEach((key, value) -> insertInfo.computeIfAbsent(key, k -> new LinkedList<>())
                .addAll(value));

        tryCatchInfo.addAll(other.tryCatchInfo);
        proxyInfo.addAll(other.proxyInfo);

        hookClasses.addAll(other.hookClasses);

        weaveClassGroup.putAll(other.weaveClassGroup);
    }

    public synchronized void addInsertInfo(InsertInfo item) {
        insertInfo.computeIfAbsent(item.targetClass,
                k -> new LinkedList<>())
                .add(item);
    }

    public synchronized void addProxyInfo(ProxyInfo item) {
        proxyInfo.add(item);
    }

    public synchronized void addReplaceInfo(ReplaceInfo item) {
        replaceInfo.add(item);
    }

    public synchronized void addBeforeCallInfo(BeforeCallInfo item) {
        item.check();
        beforeCallInfo.add(item);
    }

    public synchronized void addTryCatch(TryCatchInfo t) {
        tryCatchInfo.add(t);
    }

    public synchronized void setHookClasses(Set<String> hookClasses) {
        this.hookClasses = hookClasses;
    }


    @Override
    public String toString() {
        StringBuilder content = new StringBuilder();
        if (insertInfo != null) {
            content.append("\nInsert:\n");
            for (Map.Entry<String, List<InsertInfo>> executeList : insertInfo.entrySet()) {
                content.append(' ').append(executeList.getKey()).append(":\n");
                executeList.getValue().forEach(e -> {
                    content.append("  ").append(e).append("\n");
                });
            }
        }
        if (proxyInfo != null) {
            content.append("Proxy:\n");
            for (ProxyInfo proxyInfo : this.proxyInfo) {
                content.append(' ').append(proxyInfo).append("\n");
            }
        }
        if (tryCatchInfo != null) {
            content.append("TryCatch:\n");
            for (TryCatchInfo tryCatchInfo : this.tryCatchInfo) {
                content.append(' ').append(tryCatchInfo).append("\n");
            }
        }

        if (replaceInfo != null) {
            content.append("ReplaceInfo:\n");
            for (ReplaceInfo info : this.replaceInfo) {
                content.append(' ').append(info).append("\n");
            }
        }
        if (hookClasses != null) {
            content.append("HookClasses:\n");
            for (String hookClasses : this.hookClasses) {
                content.append(' ').append(hookClasses).append("\n");
            }
        }

        if (beforeCallInfo != null) {
            content.append("beforeCallInfos:\n");
            for (BeforeCallInfo info : this.beforeCallInfo) {
                content.append(' ').append(info).append("\n");
            }
        }
        return content.toString();
    }
}
