package com.zoho.userClass;

public class Admin {
    String email;
    String password;
    String adminName;
    public Admin() {

    }

    public void setAdmin(String adminName,String email, String password) {
        this.email = email;
        this.adminName = adminName;
        this.password = password;
    }
    
    public void setEmail(String email){
        this.email = email;
    }

    public String getEmail(){
        return email;
    }

}
