package chien.nc.com.appchat.model;

public class RequestFriend {
    private String uid;
    private String status;

    public RequestFriend(String uid, String status) {
        this.uid = uid;
        this.status = status;
    }

    public RequestFriend() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
