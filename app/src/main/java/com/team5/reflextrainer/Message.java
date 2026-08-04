package com.team5.reflextrainer;

public class Message {
    private String messageId;
    private String senderUid;
    private String senderUsername;
    private String text;
    private long timestamp;

    public Message() { }   // required by Firestore

    public Message(String messageId, String senderUid, String senderUsername,
                   String text, long timestamp) {
        this.messageId = messageId;
        this.senderUid = senderUid;
        this.senderUsername = senderUsername;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String v) { this.messageId = v; }
    public String getSenderUid() { return senderUid; }
    public void setSenderUid(String v) { this.senderUid = v; }
    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String v) { this.senderUsername = v; }
    public String getText() { return text; }
    public void setText(String v) { this.text = v; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long v) { this.timestamp = v; }
}
