package chien.nc.com.appchat.model;

public class Comment {
    String cId,comment,timestamp,uid,pid;

    public Comment(String cId, String comment, String timestamp, String uid,String pid) {
        this.cId = cId;
        this.comment = comment;
        this.timestamp = timestamp;
        this.uid = uid;
        this.pid = pid;
    }

    public Comment() {
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
