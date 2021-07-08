package chien.nc.com.appchat.model;

public class GroupChat {
    String groupid,groupname,timestamp,creator,avatar;

    public GroupChat(String groupid, String groupname, String timestamp, String creator, String avatar) {
        this.groupid = groupid;
        this.groupname = groupname;
        this.timestamp = timestamp;
        this.creator = creator;
        this.avatar = avatar;
    }

    public GroupChat() {
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
