package com.example.macksham.friendbuzz;

public class Users {
    private String name;
    private String image;
    private String status;
    private String thumbnail_image;
    private String phone_number;
    private Long online;

    public Users(){

    }
    public Users(String name, String image, String status, String thumbnail_image, String phone_number, Long online) {

        this.name = name;
        this.image = image;
        this.status = status;
        this.thumbnail_image = thumbnail_image;
        this.phone_number = phone_number;
        this.online = online;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumbnail_image() {
        return thumbnail_image;
    }

    public void setThumbnail_image(String thumbnail_image) {
        this.thumbnail_image = thumbnail_image;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public Long getOnline() {
        return online;
    }

    public void setOnline(Long online) {
        this.online = online;
    }
}

