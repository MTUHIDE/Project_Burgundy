package models;

import controllers.ApplicationComponents.Roles;

public class UsersModel {
    private String displayName;
    private String email;
    private boolean emailVerified;
    private String photoURL;
    private String uid;
    private String phoneNumber;
    private Roles role;
    private Boolean isCoach;
    private String auth_id;

    public UsersModel() {

    }

    public UsersModel(String displayName, String email, boolean emailVerified, String photoURL, String uid, String phoneNumber, Roles role, Boolean isCoach, String auth_id) {
        this.displayName = displayName;
        this.email = email;
        this.emailVerified = emailVerified;
        this.photoURL = photoURL;
        this.uid = uid;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.isCoach = isCoach;
        this.auth_id = auth_id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role.role();
    }

    public void setRole(Roles role) {
        this.role = role;
    }

    public Boolean isCoach() {
        return isCoach || (getRole() != null && getRole().equals("Coach"));
    }

    public void setIsCoach(Boolean coach) {
        isCoach = coach;
    }

    public String getAuth_id() {
        return auth_id;
    }

    public void setAuth_id(String ID) {
        this.auth_id = ID;
    }
}
