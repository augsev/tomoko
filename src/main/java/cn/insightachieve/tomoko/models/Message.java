package cn.insightachieve.tomoko.models;

import com.google.gson.annotations.SerializedName;

public class Message {
    @SerializedName("cn.ia.msg.uid")
    private String sender;

    @SerializedName("cn.ia.msg.user")
    private String senderName;

    @SerializedName("cn.ia.msg.id")
    private String msgId;

    @SerializedName("cn.ia.msg.content")
    private String content;

    @SerializedName("cn.ia.msg.group")
    private String group;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sender='" + sender + '\'' +
                ", senderName='" + senderName + '\'' +
                ", msgId='" + msgId + '\'' +
                ", content='" + content + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
