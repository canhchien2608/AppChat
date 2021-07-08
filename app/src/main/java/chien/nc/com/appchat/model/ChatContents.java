package chien.nc.com.appchat.model;

public class ChatContents {
    private String sender;
    private String receiver;
    private String mess;
    private String time;
    private boolean seen;
    private String type;
    private String mid;
    private String status;

    public ChatContents(String sender, String receiver, String mess, String time, boolean seen, String type, String mid, String status) {
        this.sender = sender;
        this.receiver = receiver;
        this.mess = mess;
        this.time = time;
        this.seen = seen;
        this.type = type;
        this.mid = mid;
        this.status = status;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ChatContents() {
    }

    public String getTime() {
        return time;
    }



    public void setTime(String time) {
        this.time = time;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMess() {
        return mess;
    }

    public void setMess(String mess) {
        this.mess = mess;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
