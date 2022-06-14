package com.zoho.userClass;

import java.sql.Timestamp;

public class User {
    public String username;
    public long accountNumber;
    public int customerId;;
    public long balance;
    public String password;
    public Timestamp createdAt;
    public long phoneNumber;

    public User(){

    }
    public User( int customerId, long accountNo, String username,long phoneNumber, long balance,Timestamp createdAt ){
        this.username = username;
        this.customerId = customerId;
        this.accountNumber = accountNo;
        this.createdAt = createdAt;
        this.phoneNumber = phoneNumber;
        this.balance = balance;
        
    }
    public User(  String username, String password,long phoneNumber, long balance){
        this.username = username;
        this.password = password;
        this.balance = balance;
        this.phoneNumber = phoneNumber;
        
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp created_at) {
        this.createdAt = created_at;
    }

    
    
}
