package chien.nc.com.appchat.model;

public class Post {
    String pId,pLikes,pContent,pTime,pImage,uId,uName,uAvatar,pComments;

    public Post(String pId, String pContent, String pTime, String pImage, String uId, String uName,String uAvatar,String pLikes,String pComments) {
        this.pId = pId;
        this.pContent = pContent;
        this.pTime = pTime;
        this.pImage = pImage;
        this.uId = uId;
        this.uName = uName;
        this.uAvatar = uAvatar;
        this.pLikes = pLikes;
        this.pComments = pComments;
    }

    public Post() {
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpContent() {
        return pContent;
    }

    public void setpContent(String pContent) {
        this.pContent = pContent;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }

    public String getpImage() {
        return pImage;
    }

    public void setpImage(String pImage) {
        this.pImage = pImage;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuAvatar() {
        return uAvatar;
    }

    public void setuAvatar(String uAvatar) {
        this.uAvatar = uAvatar;
    }

    public String getpLikes() {
        return pLikes;
    }

    public void setpLikes(String pLikes) {
        this.pLikes = pLikes;
    }

    public String getpComments() {
        return pComments;
    }

    public void setpComments(String pComments) {
        this.pComments = pComments;
    }
}
