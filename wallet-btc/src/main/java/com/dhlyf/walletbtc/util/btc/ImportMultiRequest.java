package com.dhlyf.walletbtc.util.btc;

public class ImportMultiRequest {
    private ScriptPubKey scriptPubKey;
    private String timestamp;
    private String label;
    private boolean watchonly;

    public ImportMultiRequest(String address, String label) {
        this.scriptPubKey = new ScriptPubKey(address);
        // 使用 "now" 或具体的时间戳
        this.timestamp = "now";
        this.label = label;
        this.watchonly = true;
    }

    // Getters and Setters
    public ScriptPubKey getScriptPubKey() {
        return scriptPubKey;
    }

    public void setScriptPubKey(ScriptPubKey scriptPubKey) {
        this.scriptPubKey = scriptPubKey;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isWatchonly() {
        return watchonly;
    }

    public void setWatchonly(boolean watchonly) {
        this.watchonly = watchonly;
    }
}



