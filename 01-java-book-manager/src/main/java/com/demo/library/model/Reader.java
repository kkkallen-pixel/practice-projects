package com.demo.library.model;

public class Reader {
    private long id;
    private String name;
    private String phone;
    private String cardNo;

    public Reader() {
    }

    public Reader(long id, String name, String phone, String cardNo) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.cardNo = cardNo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }
}
