package com.knightboost.lancet.internal.entity;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * A data center that store all information of weave
 */
public class TransformInfo {


    public List<String> weaverClasses =new ArrayList<>();
    /**
     * Key for target class's name.
     * Value for all InsertInfo want insert into the target class.
     */
    public Map<String, List<InsertInfo>> insertInfo = new HashMap<>();
    public List<ProxyInfo> proxyInfo = new ArrayList<>();
    public List<TryCatchInfo> tryCatchInfo = new ArrayList<>();
    public List<ReplaceInfo> replaceInfo = new ArrayList<>();
    public List<ReplaceInvokeInfo> replaceInvokes = new ArrayList<>();

    public TransformInfo(){

    }

    public List<TryCatchInfo> getTryCatchInfo(){
        return  this.tryCatchInfo;
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

    public synchronized void addReplaceInvokes(ReplaceInvokeInfo item) {
        replaceInvokes.add(item);
    }

    public synchronized void addTryCatch(TryCatchInfo t) {
        tryCatchInfo.add(t);
    }

    public void addWeaverClasses(List<String> weaverClasses){
        this.weaverClasses.addAll(weaverClasses);
    }

    public void addWeaverClass(String weaverClass){
        this.weaverClasses.add(weaverClass);
    }

    public boolean isWeaverClass(String weaverClass){
        return this.weaverClasses.contains(weaverClass);
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
        return content.toString();
    }
}
