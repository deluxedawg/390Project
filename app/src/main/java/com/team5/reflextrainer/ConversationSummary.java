package com.team5.reflextrainer;

/** One row in the inbox: the conversation from "my" point of view. Parsed manually
 *  from the conversation doc since the names/avatars/unread maps are keyed by uid. */
public class ConversationSummary {
    private String conversationId;
    private String otherUid;
    private String otherUsername;
    private int otherAvatarId;
    private String lastMessageText;
    private long lastMessageTimestamp;
    private String lastSenderUid;
    private int unreadCount;

    public ConversationSummary() { }

    public ConversationSummary(String conversationId, String otherUid, String otherUsername,
                                int otherAvatarId, String lastMessageText,
                                long lastMessageTimestamp, String lastSenderUid, int unreadCount) {
        this.conversationId = conversationId;
        this.otherUid = otherUid;
        this.otherUsername = otherUsername;
        this.otherAvatarId = otherAvatarId;
        this.lastMessageText = lastMessageText;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastSenderUid = lastSenderUid;
        this.unreadCount = unreadCount;
    }

    public String getConversationId() { return conversationId; }
    public String getOtherUid() { return otherUid; }
    public String getOtherUsername() { return otherUsername; }
    public int getOtherAvatarId() { return otherAvatarId; }
    public String getLastMessageText() { return lastMessageText; }
    public long getLastMessageTimestamp() { return lastMessageTimestamp; }
    public String getLastSenderUid() { return lastSenderUid; }
    public int getUnreadCount() { return unreadCount; }
}
