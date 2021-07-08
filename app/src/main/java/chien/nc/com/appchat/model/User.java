package chien.nc.com.appchat.model;

public class User {
    private String id;
    private String name;
    private String username;
    private String email;
    private String imageURL;
    private String status;
    private String imageCover;
    private String story;
    private String lastOnline;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(String lastOnline) {
        this.lastOnline = lastOnline;
    }

    public User(String id, String name, String username, String imageURL, String email, String status, String imageCover, String story,String lastOnline) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.imageURL = imageURL;
        this.email = email;
        this.status = status;
        this.imageCover = imageCover;
        this.story = story;
        this.lastOnline = lastOnline;
    }

    public User() {

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getImageCover() {
        return imageCover;
    }

    public void setImageCover(String search) {
        this.imageCover = search;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }
}
