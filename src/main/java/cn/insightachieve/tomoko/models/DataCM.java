package cn.insightachieve.tomoko.models;

public class DataCM {
    private String type;

    private String actualSender;

    private String msgType;

    private Integer subType;

    private String msgId;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getActualSender() {
        return actualSender;
    }

    public void setActualSender(String actualSender) {
        this.actualSender = actualSender;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public Integer getSubType() {
        return subType;
    }

    public void setSubType(Integer subType) {
        this.subType = subType;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
}
