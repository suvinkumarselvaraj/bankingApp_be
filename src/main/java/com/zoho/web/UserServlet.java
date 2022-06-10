package com.zoho.web;

import com.zoho.database.Database;
import com.zoho.userClass.Users;


import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONArray;
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

                    
                case "/checktransactions":
                    checkTransactions(req,res);
                    break;

                case "/availableCustomers":
                    availableCustomers(req, res);
                    break;
                
                case "/transactionDetails":
                    transactionDetails(req,res);
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
                        changePassword(req,res);
                        break;
                    
                    case "/transactions":
                        transactions(req,res);
                        break;

                    case "/checkPassword":
                        checkPassword(req, res);
                        break;

                    case "/transfer":
                        transfer(req, res);
                        break;

                    case "/login":
                        login(req,res);
                        break;
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
    }
    //login
    public void login(HttpServletRequest req, HttpServletResponse res) throws IOException{
        String jsonBody = new BufferedReader(new InputStreamReader(req.getInputStream())).lines().collect(
            Collectors.joining("\n"));
            JSONObject jObj = new JSONObject(jsonBody);
            System.out.println(jObj);
            Long accountNumber = jObj.getLong("accountNumber");
            String password = jObj.getString("password");
            Users loggedUser = new Database().loginValidate(accountNumber, password);
            if(loggedUser!=null){
                jObj.put("status", "success");
                jObj.put("username", loggedUser.username);
                jObj.put("balance", loggedUser.balance);
                jObj.put("phoneNo", loggedUser.phoneNumber);
                jObj.put("customerId", loggedUser.customerId);
            }
            else{
                jObj.put("status","failure");
            }
            res.getWriter().write(jObj.toString());
        }

    //change password
    public void changePassword(HttpServletRequest req, HttpServletResponse res) throws IOException{
        String jsonBody = new BufferedReader(new InputStreamReader(req.getInputStream())).lines().collect(
            Collectors.joining("\n"));
            JSONObject jObj = new JSONObject(jsonBody);
            System.out.println(jObj);

            long accountNumber =jObj.getLong("accountNumber");
            
            String newPassword =jObj.getString("nPass");
            
            int id = new Database().returnId(accountNumber);
            //check if the new password entered matches the old passwords
            
            System.out.println("line 94 checking for password match");
            if(new Database().checkIfPasswordMatches(accountNumber, newPassword)){
               
                jObj.put("insertion", "failure");
                res.getWriter().write(jObj.toString());
                return;
            }
            else{

                //check if the password count is 3 
                System.out.println("line 104 checking for password count");
                if(new Database().checkPasswordCount(id)){
                    //delete the lastly entered password
                    new Database().deleteLastPassword(id);
                }
                jObj.put("status","success");
                //update in the password_history db
                new Database().insertPasswordIntoPasswordHistory(id,newPassword);
                //update accounts db password
                new Database().updateAccountPassword(id);
                res.getWriter().write(jObj.toString());
                // alert("succesfully changed");
            }
    }
    public void transactionDetails(HttpServletRequest req, HttpServletResponse res) throws IOException{
        Integer id = Integer.parseInt(req.getParameter("id"));
        //collect the information in the array
        JSONArray jArray = new Database().returnUserThroughId(id);
        res.getWriter().write(jArray.toString());
    }
    public void availableCustomers(HttpServletRequest req, HttpServletResponse res) throws IOException{
        JSONArray obj = new Database().returnAvailableCustomers();
        System.out.println("inside customer row section");
        res.getWriter().write(obj.toString());
    }
    //send the balance according to the type
    public long returnBalanceAccordingToType(String type, long balance, long transactionAmount){
        if(type.equals("Opening")||type.equals("deposit")||type.contains("from")){
            return balance + transactionAmount;
        }
        //for withdrawal
    
        return balance - transactionAmount;
    }
    public void transfer(HttpServletRequest req, HttpServletResponse res) throws IOException{
        System.out.println("inside transfer area");
        String jsonBody = new BufferedReader(new InputStreamReader(req.getInputStream())).lines().collect(
                Collectors.joining("\n"));
        JSONObject jObj = new JSONObject(jsonBody);
        System.out.println(jObj);

        //sender section
        Long accountNumber = jObj.getLong("accountNumber");
        int customerId = jObj.getInt("customerId");
        long balance =jObj.getLong("balance");
        long transactionAmount = jObj.getLong("amount");
        String type = jObj.getString("transactionType");

        long newBalance = balance - transactionAmount;
        new Database().insertIntoTransactions(customerId, type,transactionAmount,newBalance);

        //maintenance fee section
        
        String type2 = null;
        if(transactionAmount>5000)
        {
            newBalance = newBalance - 10;
        type2 = jObj.getString("transactionType2");
        new Database().insertIntoTransactions(customerId, type2, 10, newBalance);
        }

        //receiver section
        String transactionType = "Transfer from "+accountNumber+"";
        Long receiverAccountNumber = Long.parseLong(jObj.getString("receiverAccountNumber"));
        long receiverBalance = new Database().returnBalance(receiverAccountNumber);
        int receiverCustomerId = new Database().returnId(receiverAccountNumber);
        if(new Database().insertIntoTransactions(receiverCustomerId, transactionType, transactionAmount, receiverBalance+transactionAmount)){
            jObj.put("status","success");
            jObj.put("balance",(balance-transactionAmount)+"");
        }
        res.getWriter().write(jObj.toString());
    }
    //decide the type of transaction and set the balance accordingly
    public void transactions(HttpServletRequest req, HttpServletResponse res) throws IOException{
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Headers", "*");
        System.out.println("inside transation section");
        String jsonBody = new BufferedReader(new InputStreamReader(req.getInputStream())).lines().collect(
        Collectors.joining("\n"));
        JSONObject jObj = new JSONObject(jsonBody);
        System.out.println(jObj);
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
        
        Long accountNumber = jObj.getLong("accountNumber");
        String password = jObj.getString("oPass");
        System.out.print("inside password check");
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
        System.out.println("inside transaction count area");
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
        Long phoneNumber =Long.parseLong(jObj.getString("phone"));
        Long balance = 10000l;
        Users newUser = new Users(username,password,phoneNumber,balance);
        Database db = new Database();
        System.out.println("about to call the database class");
        Users status = db.insertUser(newUser);
        System.out.println("returned after the calling");
        if(status != null){
            jObj.put("isExistingUser","non-existing");
            jObj.put("status","success");
            jObj.put("customerId",status.customerId);
            jObj.put("username",status.username);
            jObj.put("balance",balance.toString());
            jObj.put("phoneNumber",phoneNumber);
            jObj.put("accountNo",status.accountNumber);
        }else{
            jObj.put("isExistingUser","existing");
         }
         res.getWriter().write(jObj.toString()); 
    }   
   
}
