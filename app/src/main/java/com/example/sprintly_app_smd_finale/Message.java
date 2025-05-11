package com.example.sprintly_app_smd_finale;

public class Message {
    public static final int TYPE_USER = 1;
    public static final int TYPE_BOT = 2;

    private String content;
    private String codeBlock;
    private int type;
    private long timestamp;

    public Message(String content, int type) {
        this.content = content;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.codeBlock = null;
    }

    public Message(String content, String codeBlock, int type) {
        this.content = content;
        this.codeBlock = codeBlock;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCodeBlock() {
        return codeBlock;
    }

    public void setCodeBlock(String codeBlock) {
        this.codeBlock = codeBlock;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean hasCodeBlock() {
        return codeBlock != null && !codeBlock.isEmpty();
    }
}