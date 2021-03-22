package com.example.visitor;

import android.graphics.Bitmap;

// Recyclerview 的模組
public class DataSet {

    private int recordsTotal;

    private String img_path;

    private String name;

    private String company;

    private String phone;

    private String gate;

    private String cardId;

    private Bitmap bbmp;

    private String id;

    public DataSet(String name, String phone, String company, String gate, Bitmap bbmp ,String cardId,String id) {
        //this.recordsTotal = recordsTotal;
        //this.img_path = img_path;
        this.bbmp = bbmp;
        this.name = name;
        this.company = company;
        this.phone = phone;
        this.gate = gate;
        this.cardId = cardId;
        this.id = id;
    }

    public int getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(int recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    public String getImg_path() {
        return img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGate() {
        return gate;
    }

    public void setGate(String gate) {
        this.gate = gate;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public Bitmap getBbmp() {
        return bbmp;
    }

    public void setBbmp(Bitmap bbmp) {
        this.bbmp = bbmp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
