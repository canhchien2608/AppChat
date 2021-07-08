package chien.nc.com.appchat.model;

public class MessageGroupChat {
    String message,sender,time,type,status;

    public MessageGroupChat() {
    }

    public MessageGroupChat(String message, String sender, String time, String type, String status) {
        this.message = message;
        this.sender = sender;
        this.time = time;
        this.type = type;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
