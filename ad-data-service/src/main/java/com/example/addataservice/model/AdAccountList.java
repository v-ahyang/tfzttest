package com.example.addataservice.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AdAccountList 类表示 Facebook 广告账户列表，映射到数据库表。
 */
@Entity
@Table(name = "ad_account_lists")
public class AdAccountList {
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "account_status")
    private String accountStatus;

    @Column(name = "currency")
    private String currency;

    @Column(name = "timezone_id")
    private String timezoneId;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "amount_spent")
    private Double amountSpent;

    // Constructors
    public AdAccountList() {}

    public AdAccountList(String id, String name, String accountStatus, String currency,
                         String timezoneId, String businessName, Double amountSpent) {
        this.id = id;
        this.name = name;
        this.accountStatus = accountStatus;
        this.currency = currency;
        this.timezoneId = timezoneId;
        this.businessName = businessName;
        this.amountSpent = amountSpent;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getTimezoneId() { return timezoneId; }
    public void setTimezoneId(String timezoneId) { this.timezoneId = timezoneId; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public Double getAmountSpent() { return amountSpent; }
    public void setAmountSpent(Double amountSpent) { this.amountSpent = amountSpent; }

    @Override
    public String toString() {
        return "AdAccountList{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", accountStatus='" + accountStatus + '\'' +
                ", currency='" + currency + '\'' +
                ", timezoneId='" + timezoneId + '\'' +
                ", businessName='" + businessName + '\'' +
                ", amountSpent=" + amountSpent +
                '}';
    }
}