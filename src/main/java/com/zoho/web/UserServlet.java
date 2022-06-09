package com.zoho.web;

import com.zoho.database.Database;
import com.zoho.userClass.Users;


import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.json.JSONObject;
import java.util.stream.Collectors;

public class UserServlet extends HttpServlet{
    private static final long serialVersionUID = 1L;
    public void doGet(HttpServletRequest req, HttpServletResponse res){
        String action = req.getServletPath();
        try{
            switch(action){
                case "/new":                    //used for testing purpose 
                    showHomePage(req,res);
                    break;

                    
                case "/checkTransactions":
                    checkTransactions(req,res);
                    break;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void doPost(HttpServletRequest req, HttpServletResponse res){

        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Headers", "*");
            String action = req.getServletPath();
            try{
                switch(action){
                    case "/openAccount":
                        openAccount(req,res);
                        break;
                    
                    case "/changePassword":
                        checkPassword(req,res);
                        break;
                    
                    case "/transactions":
                        transactions(req,res);
                        break;
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
    }
    //send the balance according to the type
    public long returnBalanceAccordingToType(String type, long balance, long transactionAmount){
        if(type.equals("Opening")||type.equals("deposit")||type.contains("from")){
            return balance + transactionAmount;
        }
        //for withdrawal
    
        return balance - transactionAmount;
    }

    //decide the type of transaction and set the balance accordingly
    public void transactions(HttpServletRequest req, HttpServletResponse res) throws IOException{
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Headers", "*");
        String jsonBody = new BufferedReader(new InputStreamReader(req.getInputStream())).lines().collect(
        Collectors.joining("\n"));
        JSONObject jObj = new JSONObject(jsonBody);
        int customerId = jObj.getInt("customerId");
        long balance = jObj.getLong("balance");
        long transactionAmount =jObj.getLong("amount");
        String type = jObj.getString("transactionType");
        //handle transaction type    
        balance = returnBalanceAccordingToType(type, balance, transactionAmount);
        //insert into the transactions db and update the balance of accounts db
        if(new Database().insertIntoTransactions(customerId, type, transactionAmount, balance))
        {
            jObj.put("status","success");
            jObj.put("balance",balance+"");    
        }
        res.getWriter().write(jObj.toString());
    }
    //check passwords 
    public void checkPassword(HttpServletRequest req, HttpServletResponse res) throws IOException{
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Headers", "*");
        String jsonBody = new BufferedReader(new InputStreamReader(req.getInputStream())).lines().collect(
        Collectors.joining("\n"));
        JSONObject jObj = new JSONObject(jsonBody);
        
        Long accountNumber = jObj.getLong("acccountNumber");
        String password = jObj.getString("oPass");
        //call a function with the old password that goes to the db to retrieve the latest password to validate
        if(new Database().isValidPassword(accountNumber,password)){
            jObj.put("oldPasswordCheck","success");            
        }
        else
        jObj.put("oldPasswordCheck","failure");
        res.getWriter().write(jObj.toString());

    }

    //checkTransactions to force password change
    public void checkTransactions(HttpServletRequest req, HttpServletResponse res) throws IOException{
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Headers", "*");
        JSONObject jObj = new JSONObject();
        Long accountNumber = Long.parseLong(req.getParameter("acc"));
        int transactionCount = new Database().returnTransactionCount(accountNumber);
        if(transactionCount == 5){
            //CHECKING IF THE LAST TRANSACTION DATE IS GREATER THAN LAST CHANGED PASSWORD
            if(new Database().compareDates(accountNumber)){
                    //then set the object status to 5
                    jObj.put("status5","true");
                    jObj.put("status10","false");
                    jObj.put("status","success");
            }
        }
        else{
            System.out.println("inside failure area");
        }
        res.getWriter().write(jObj.toString());
    }

    public void showHomePage(HttpServletRequest req, HttpServletResponse res){
        System.out.println("Hello world");
    }

    public void openAccount(HttpServletRequest req, HttpServletResponse res) throws Exception{
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Headers", "*");
        String jsonBody = new BufferedReader(new InputStreamReader(req.getInputStream())).lines().collect(
                Collectors.joining("\n"));
        JSONObject jObj = new JSONObject(jsonBody);
        System.out.println(jObj);
        String username = jObj.getString("username");
        String password = jObj.getString("password");
        Long phoneNumber =Long.parseLong(jObj.getString("phoneNumber"));
        Long balance = 10000l;
        Users newUser = new Users(username,password,phoneNumber,balance);
        Database db = new Database();
        Users status = db.insertUser(newUser);
        if(status != null){
            jObj.put("status","success");
            jObj.put("customer_id",status.customerId);
            jObj.put("username",status.username);
            jObj.put("balance",balance.toString());
            jObj.put("phoneNumber",phoneNumber);
        }else{
            jObj.put("status","unsuccessful");
         }
         res.getWriter().write(jObj.toString()); 
    }
    
   
}