package com.zoho.web;

import com.zoho.database.Database;
import com.zoho.userClass.User;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;


@WebServlet(urlPatterns =  {"/new","/logout","/checktransactions","/availableCustomers","/accountdetails","/transactiondetails", "/isSessionPresent","/isSameSession","/openAccount","/changePassword","/transactions","/checkPassword","/transfer","/loginuser","/maintenancefee"})
public class UserServlet extends HttpServlet{
   static Logger logger = Logger.getLogger("UserServlet.class");
    static{
      
        try
        {
            FileHandler fh = new FileHandler("C://projects//website//src//main//java//com//zoho//web//UserLog.txt",true);
            logger.addHandler(fh);
            SimpleFormatter sfm = new SimpleFormatter();
            fh.setFormatter(sfm);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

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
                
                case "/transactiondetails":
                    transactionDetails(req,res);
                    break;

                case "/accountdetails":
                    accountdetails(req,res);
                    break;

                case "/isSessionPresent":
                    isSessionPresent(req, res);
                    break;

                case "/isSameSession":
                    isSameSession(req, res);
                    break;
                
                case "/logout":
                    logout(req,res);
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
                        changepassword(req,res);
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

                    case "/loginuser":
                        login(req,res);
                        break;

                    case "/maintenancefee":
                        maintenance(req ,res);
                        break;
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
    }

    public void logout(HttpServletRequest req , HttpServletResponse res) throws IOException{
        JSONObject jObject = new JSONObject();
        HttpSession session = req.getSession(false);
        if (session != null) {
        //   String sessionId = session.getId();
          session.invalidate();
          Cookie[] cookies = req.getCookies();
          
          for (Cookie cookie : cookies) {
                Cookie name = new Cookie(cookie.getName(), cookie.getValue());
                
                name.setMaxAge(0);
                name.setDomain("localhost");
                name.setPath("/");
                res.addCookie(name);
              break;
            }
          }
          jObject.put("log","off");
          res.getWriter().write(jObject.toString());
    }

   //check if the session is same
    public void isSameSession(HttpServletRequest req, HttpServletResponse res) throws IOException{
        JSONObject jObj =  new JSONObject();
        if(compareSession(req, res)){
            jObj.put("isValidUser","success");
        }
        else 
        jObj.put("isValidUser","failure");
       
        res.getWriter().write(jObj.toString());
    }

    //session to check for a valid user
    public boolean compareSession(HttpServletRequest req , HttpServletResponse res){
        HttpSession session = req.getSession(false);
        Cookie[] cookies = req.getCookies();
        String value = null;
        for(Cookie cookie: cookies){
            if(cookie.getName().equals("JSESSIONID")){
                value = cookie.getValue();
                break;
            }
        }
        System.out.println("im printing the session id"+session.getId());
        if(value.equals(session.getId()))
        {
            return true;
        }
        return false;
    }
   
    public void accountdetails(HttpServletRequest req, HttpServletResponse res) {
        JSONObject json = new JSONObject();
        if(compareSession(req, res))  {
            json.put("isValidUser","success");
        }
        else 
        json.put("isValidUser","failure");
        try{
            res.getWriter().write(json.toString());
        }
            catch(Exception e){
                e.printStackTrace();
            }
    }

    public String encryptPassword(String password){
        String encryptedString = new String();
        for(int i=0;i<password.length();i++){ 
            if(password.charAt(i)=='9'){
                encryptedString+='0';
            }
            else
            if(password.charAt(i)=='Z')
            encryptedString+='A';
            else
            if(password.charAt(i)=='z')
            encryptedString+='a';
            else
			encryptedString+=(char)(password.charAt(i)+1); 
		} 
        return encryptedString;
    }

    //function to check for the session existence
    public void  isSessionPresent(HttpServletRequest request, HttpServletResponse response) throws IOException{
        HttpSession session = request.getSession(false);
        JSONObject jObj = new JSONObject();
        System.out.println("checking for the session");
        if(session!=null){
            jObj.put("session","present");
        }
        else
        jObj.put("session","absent");
        System.out.println("hi hello world");
        
        response.getWriter().write(jObj.toString());
    }

    //add maintenance fee of rs. 100
    public void maintenance(HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException{
        //make sure that there is no maintenance fee applied recently
        System.out.println("inside transfer area");
        String jsonBody = new BufferedReader(new InputStreamReader(req.getInputStream())).lines().collect(
                Collectors.joining("\n"));
        JSONObject jObj = new JSONObject(jsonBody);
        System.out.println(jObj);

        //sender section
    
        int customerId = jObj.getInt("customerId");
        long balance =jObj.getLong("balance");
        long transactionAmount = jObj.getLong("amount");
        String type = jObj.getString("transactionType");

        //check the last record
        System.out.println("is the last maintenance is 100");
        System.out.print(new Database().checkLastMaintenanceAmount(customerId));
        if(!new Database().checkLastMaintenanceAmount(customerId))
        {
        balance = balance - transactionAmount;
        new Database().insertIntoTransactions(customerId, type,transactionAmount,balance);
        //insert into accounts
        new Database().updateAccountsDb(balance, customerId); 
        //delete the history
        // deleteHistory(customerId);
        jObj.put("balance",balance);
        jObj.put("status","success");
        }else{
            jObj.put("status","failure");
        }
        
        res.getWriter().write(jObj.toString());
      
    }

    public void deleteHistory(int customerId) throws SQLException{
       //delete the last record
        new Database().deleteHistory(customerId);
        
    }

    public void login(HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException{
        UserServlet.logger.info("logged in");

        String jsonBody = new BufferedReader(new InputStreamReader(req.getInputStream())).lines().collect(
            Collectors.joining("\n"));
            JSONObject jObj = new JSONObject(jsonBody);
            System.out.println(jObj);

            Long accountNumber = jObj.getLong("accountNumber");
            String password = jObj.getString("password");
            String ePassword = encryptPassword(password);
            User loggedUser = new Database().loginValidate(accountNumber, ePassword);

            if(loggedUser!=null){
                jObj.put("status", "success");
                jObj.put("username", loggedUser.username);
                jObj.put("balance", loggedUser.balance);
                jObj.put("phoneNo", loggedUser.phoneNumber);
                jObj.put("customerId", loggedUser.customerId);
                HttpSession session = req.getSession();
                
                // Cookie cookie1 = new Cookie("JSESSION",session.getId());

                // res.addCookie(cookie1);
            }
            else{
                jObj.put("status","failure");
            }
            res.getWriter().write(jObj.toString());
        }

    //change password
    public void changepassword(HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException{
        System.out.println("im inside password blockkkk");
        String jsonBody = new BufferedReader(new InputStreamReader(req.getInputStream())).lines().collect(
            Collectors.joining("\n"));
            JSONObject jObj = new JSONObject(jsonBody);
            System.out.println(jObj);

            long accountNumber =jObj.getLong("accountNumber");
            
            String newPassword =jObj.getString("nPass");
            String encryptedNewPassword = encryptPassword(newPassword);
            int id = new Database().returnId(accountNumber);
            //check if the new password entered matches the old passwords
            
            System.out.println("line 94 checking for password match");
            if(new Database().checkIfPasswordMatches(accountNumber, encryptedNewPassword)){
               
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
                new Database().insertPasswordIntoPasswordHistory(id,encryptedNewPassword);
                //update accounts db password
                new Database().updateAccountPassword(id);
                res.getWriter().write(jObj.toString());
                // alert("succesfully changed");
            }
    }

    public void transactionDetails(HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException{
        Integer id = Integer.parseInt(req.getParameter("id"));
        //collect the information in the array
        JSONArray jArray = new Database().returnUserThroughId(id);
        res.getWriter().write(jArray.toString());
    }

    public void availableCustomers(HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException{
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
    
    public void transfer(HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException{
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
        new Database().updateAccountsDb(newBalance, customerId);
        }

        //receiver section
        String transactionType = "Transfer from "+accountNumber+"";
        Long receiverAccountNumber = Long.parseLong(jObj.getString("receiverAccountNumber"));
        long receiverBalance = new Database().returnBalance(receiverAccountNumber);
        System.out.println(receiverBalance);
        int receiverCustomerId = new Database().returnId(receiverAccountNumber);
        if(new Database().insertIntoTransactions(receiverCustomerId, transactionType, transactionAmount, receiverBalance+transactionAmount)){
            jObj.put("status","success");
            jObj.put("balance",newBalance+"");
        }

        res.getWriter().write(jObj.toString());
    }

    //decide the type of transaction and set the balance accordingly
    public void transactions(HttpServletRequest req, HttpServletResponse res) throws IOException, JSONException, SQLException{
      
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
    public void checkPassword(HttpServletRequest req, HttpServletResponse res) throws IOException, JSONException, SQLException{
       
        String jsonBody = new BufferedReader(new InputStreamReader(req.getInputStream())).lines().collect(
        Collectors.joining("\n"));
        JSONObject jObj = new JSONObject(jsonBody);
        
        Long accountNumber = jObj.getLong("accountNumber");
        String password = jObj.getString("oPass");
        String ePassword = encryptPassword(password);
        System.out.print("inside password check");
        //call a function with the old password that goes to the db to retrieve the latest password to validate
        if(new Database().isValidPassword(accountNumber,ePassword)){
            jObj.put("oldPasswordCheck","success");            
        }
        else
        jObj.put("oldPasswordCheck","failure");
        res.getWriter().write(jObj.toString());

    }

    //checkTransactions to force password change
    public void checkTransactions(HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException{
        System.out.println("inside transaction count area");
      
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
        }else
        if(transactionCount == 10){
            if(!new Database().checkLastMaintenanceAmount(new Database().getUserid(accountNumber)))
                {
                    jObj.put("status5","false");
                    jObj.put("status10","true");
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
      
        String jsonBody = new BufferedReader(new InputStreamReader(req.getInputStream())).lines().collect(
                Collectors.joining("\n"));
        JSONObject jObj = new JSONObject(jsonBody);
        System.out.println(jObj);
        String username = jObj.getString("username");
        String password = jObj.getString("password");
        String ePassword = encryptPassword(password);
        Long phoneNumber =Long.parseLong(jObj.getString("phone"));
        Long balance = 0l;
        User newUser = new User(username,ePassword,phoneNumber,balance);
        Database db = new Database();
        System.out.println("about to call the database class");
        User status = db.insertUser(newUser);
        System.out.println("returned after the calling");
        if(status != null){
            jObj.put("isExistingUser","non-existing");
            jObj.put("status","success");
            jObj.put("customerId",status.customerId);
            jObj.put("username",status.username);
            jObj.put("balance",balance.toString());
            jObj.put("phoneNumber",phoneNumber);
            jObj.put("accountNo",status.accountNumber);

            //set the session. if the session is not null, invalidate to set the new session
            System.out.print("about to call the cookie");
            HttpSession session = req.getSession();
            if(session!=null){
                System.out.println("session is already present inside");
                session.invalidate();
                //once when the server sees that the session in already in use 
                //invalidate the session and ask the client end to redirect to the front page
                HttpSession newSession = req.getSession();
                System.out.print(newSession.getId());
                newSession.setAttribute("user","created");
            }
            else{
                // Cookie cookie = new Cookie("session", "set");
                // Cookie cookie2 = new Cookie("JSESSION",session.getId());
                // res.addCookie(cookie);
                // res.addCookie(cookie2);
            }
        }else{
            jObj.put("isExistingUser","existing");
         }
         res.getWriter().write(jObj.toString()); 
    }     
}
