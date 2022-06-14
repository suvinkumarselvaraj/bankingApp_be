package com.zoho.database;

import com.zoho.userClass.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


import org.json.JSONArray;
import org.json.JSONObject;


public class Database {
   private static final String insertUserQuery = "INSERT INTO accounts(account_number, name, balance,phone_no) VALUES(?,?,?,?)";
   private static final String userQuery = "SELECT * FROM accounts";
   //private static final String particularUserQuery = "SELECT * FROM accounts WHERE account_number = ?";
   private static final String particularUserThroughIdQuery = "SELECT * FROM transactions WHERE customer_id = ?";
   private static final String ifPresentQuery = "SELECT customer_id FROM accounts WHERE phone_no = ?";
   private static final String returnUserIdQuery = "SELECT customer_id FROM accounts WHERE account_number = ?";
   private static final String returnCountQuery = "SELECT COUNT(*) FROM accounts";
   private static final String passwordInsertQuery = "INSERT INTO password_history(customer_id,password) VALUES(?,?)";

    //UPDATE accounts INNER JOIN password_history on accounts.customer_id = password_history.customer_id     GROUP BY password_id ORDER BY desc LIMIT 1 
    //String passwordIdInsertQuery = "UPDATE accounts SET accounts.user_password = password_history.password_id FROM accounts INNER JOIN password_history ON accounts.customer_id = password_history.customer_id  WHERE customer_id = ? GROUP BY customer_id ORDER BY created_at DESC LIMIT 1";
    private static final String passwordIdInsertQuery = "UPDATE accounts SET user_password = (SELECT password_id FROM password_history WHERE customer_id = ? ORDER BY created_at DESC LIMIT 1) WHERE customer_id = ?";
    private static final String checkTransactionCountQuery  = "SELECT COUNT(*) FROM transactions WHERE customer_id = ? AND transaction_type NOT LIKE '%fee' AND transaction_type NOT LIKE '%from%'";    
    private static final String lastransactionType = "SELECT transaction_type FROM transactions WHERE customer_id = ? ORDER BY created_at DESC LIMIT 1";
    private static final String lastransactionDateQuery = "SELECT created_at FROM transactions WHERE customer_id = ? ORDER BY created_at DESC LIMIT 1";
    private static final String lastPasswordDateQuery = "SELECT created_at FROM password_history WHERE customer_id = ? ORDER BY created_at DESC LIMIT 1";
    private static final String isValidPasswordQuery = "SELECT customer_id FROM accounts WHERE customer_id = ? AND user_password = (SELECT password_id FROM password_history WHERE customer_id = ? AND password = ?)";     
    private static final String insertIntoTransactions = "INSERT INTO transactions(customer_id,transaction_type,transaction_amount,balance) VALUES(?,?,?,?)";
    private static final String updateAccountsQuery = "UPDATE accounts SET balance = ? WHERE customer_id = ?";
    private static final String returnBalance = "SELECT customer_id, balance FROM accounts WHERE account_number = ?";
    private static final String returnAccountNumber = "SELECT account_number FROM accounts";
    private static final String checkPasswordQuery = "SELECT password from password_history WHERE customer_id = ? ";
    private static final String passwordCountQuery = "SELECT COUNT(*) FROM password_history WHERE customer_id = ?";
    private static final String deleteLastPassword = "DELETE FROM password_history where password_id = (SELECT password_id FROM password_history WHERE customer_id  = ? ORDER BY created_at LIMIT 1)";
    private static final String userInfoString = "SELECT * FROM accounts WHERE account_number = ?  AND user_password = (SELECT password_id FROM password_history WHERE password = ? AND customer_id = ?)";
    private static final String lastTransactionAmountQuery = "SELECT transaction_amount FROM transactions WHERE customer_id = ? ORDER BY created_at DESC";
    private static final String deleteFromTransactionHistory = "DELETE FROM transactions WHERE customer_id = ? ORDER BY created_at DESC 1";
    //admin section
    
    String adminValidatorString = "SELECT admin_name FROM admins WHERE admin_email = ? AND admin_password = ?";

    public Connection returnConnection() throws ClassNotFoundException, SQLException{
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/banking-app-test", "root", "");
    }
    public void deleteHistory(int customerId) throws SQLException{
        Connection con = null;
        PreparedStatement pst = null;
        try{
            con = returnConnection();
            pst = con.prepareStatement(deleteFromTransactionHistory);
            pst.setInt(1, customerId);
            pst.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
        }
        finally{
            pst.close();
            con.close();
        }
    }
    //maintenance amount check
    public boolean checkLastMaintenanceAmount(int customerId) throws SQLException{
        Connection con = null;
        PreparedStatement pst = null;
        try{
            con = returnConnection();
            pst = con.prepareStatement(lastTransactionAmountQuery);
            pst.setInt(1, customerId);
            ResultSet  rst = pst.executeQuery();
            if(rst.next())
            {   if(rst.getLong(1)==100)
                return true;
            }
        }catch(Exception e ){
            e.printStackTrace();
        }finally{
            pst.close();
            con.close();
        }
        return false;
    }
    //SEND ALL USERS
        public JSONArray sendAllUsers() throws SQLException{
            Connection con = null;
            PreparedStatement pst = null;
            JSONArray jArray = new JSONArray();
            try{
                con = returnConnection();
                pst = con.prepareStatement(userQuery);
                ResultSet rst = pst.executeQuery();
                
                while(rst.next()){
                    JSONObject jObject = new JSONObject();
                    jObject.put("customer_id",rst.getInt(1));
                    jObject.put("account_number",rst.getLong(2));
                    jObject.put("name",rst.getString(3));
                    jObject.put("balance",rst.getLong(4));
                    jObject.put("phoneNo",rst.getLong(5));
                    jObject.put("created_at",rst.getDate(7));
                    jArray.put(jObject);
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                // pst.close();
                // con.close();
            }
            return jArray;
        }
    //validate admin
    public String validateAdmin(String email, String password) throws SQLException{
        Connection con = null;
        PreparedStatement pst = null;
        try{
            con = returnConnection();
            pst = con.prepareStatement(adminValidatorString);

        System.out.println("admin validation");
            pst.setString(1, email);
            pst.setString(2, password);
            System.out.println("post admin validation");
            ResultSet rst = pst.executeQuery();
           
            if(rst.next())
            return rst.getString("admin_name");
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            pst.close();
            con.close();
        }
        return null;
    }

    //login handler
    public User loginValidate(long accountNumber, String password ) throws SQLException{
        Connection con = null;
        PreparedStatement pst = null;

        try{
            con = returnConnection();
            pst = con.prepareStatement(userInfoString);
            pst.setLong(1,accountNumber);
            pst.setString(2, password);
            int customerId = returnId(accountNumber);
            pst.setLong(3, customerId);
            ResultSet rst = pst.executeQuery();
            if(rst.next()){
                return new User(customerId,accountNumber,rst.getString("name"),rst.getLong("phone_no"),rst.getLong("balance"),rst.getTimestamp("created_at"));
            }
        }catch(Exception e ){
            e.printStackTrace();
        }finally{
            pst.close();
            con.close();
        }
        return null;
    }
    //delete last passowrd
    public void deleteLastPassword(int customerId) throws SQLException{
        Connection con = null;
        PreparedStatement pst = null;
        try{
            con = returnConnection();
            pst = con.prepareStatement(deleteLastPassword);
            pst.setInt(1, customerId);
            pst.executeUpdate();
        }catch(Exception e ){e.printStackTrace();
        }finally{
            pst.close();
            con.close();
        }
    }
    //checkif the entered password matches with the passwords from password history
    public boolean checkIfPasswordMatches(long accountNumber, String newPassword) throws SQLException{

        System.out.println("line 64 in db class checking for matching password");
        System.out.println(newPassword);
        int id = returnId(accountNumber);
        System.out.println(id);
        System.out.println(accountNumber);
        Connection con = null;
        PreparedStatement pst = null;
        try
        {
            con = returnConnection();
            pst = con.prepareStatement(checkPasswordQuery);
            pst.setInt(1, id);
            ResultSet rst = pst.executeQuery();
        System.out.println("im hereeeeeeeeee");
             while(rst.next()){
            String password = rst.getString(1);
            if(password.equals(newPassword))
            return true;
        }   
    }catch(Exception e){
        e.printStackTrace();
    }finally{
        pst.close();
        con.close();
    }
        return false;
    }
    public JSONArray returnUserThroughId(int customer_id) throws SQLException{
        Connection con = null;
        PreparedStatement pst = null;
        JSONArray jArray = new JSONArray();
        System.out.println("im here");
        try{
            con = returnConnection();
            pst = con.prepareStatement(particularUserThroughIdQuery);
            pst.setInt(1, customer_id);
            ResultSet rst = pst.executeQuery();
            while (rst.next())
            {
            JSONObject jObject = new JSONObject();
            jObject.put("transactionType",rst.getString(2));
            jObject.put("amount",rst.getLong(3));
            jObject.put("balance",rst.getLong(4));
            jObject.put("date",rst.getDate(5));
            jArray.put(jObject);
            }
    }
    catch(Exception e){
        e.printStackTrace();
    }finally{
        pst.close();
        con.close();
    }
    return jArray;
}
    //return balance according to account number
    public long returnBalance(long accountNumber) throws SQLException{
        Connection con = null;
        PreparedStatement pst = null;
        try{
            con = returnConnection();
            pst = con.prepareStatement(returnBalance);
            pst.setLong(1, accountNumber);
            ResultSet rst = pst.executeQuery();
            rst.next();
            return rst.getLong(2);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            pst.close();
            con.close();
        }
        return 0;
    }
    public long generateAccountNumber() throws SQLException{
        Connection con = null;
        PreparedStatement pst = null;
        try{
            con = returnConnection();
            pst = con.prepareStatement(returnCountQuery);
            ResultSet rst = pst.executeQuery();
            rst.next();
            int count = rst.getInt(1);
            if (count == 0){
                return 11011;
            }
            return (11011*count + 11011);
        }catch(Exception e){
            e.printStackTrace();
        }
        finally{
            pst.close();
            con.close();
        }
        return 1;
    }
    //return all accountNumber
    public JSONArray returnAvailableCustomers() throws SQLException{
        Connection con = null;
        PreparedStatement  pst = null;
        JSONArray array = new JSONArray();
        try{
            con = returnConnection();
            pst = con.prepareStatement(returnAccountNumber);
            ResultSet rst = pst.executeQuery();
            while(rst.next() ){
                JSONObject jObject = new JSONObject();
                Long acc = rst.getLong(1);
                jObject.put("accountNumber",acc);
                array.put(jObject);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            pst.close();
            con.close();
        }
        System.out.println(array);
        return array;


    }
    //return id to a account number
    public int returnId(long accountNumber) throws SQLException{
        Connection con = null;
        PreparedStatement  pst = null;
        try{
            con = returnConnection();
            pst = con.prepareStatement(returnUserIdQuery);
            pst.setLong(1, accountNumber);
            ResultSet rst = pst.executeQuery();
            rst.next();
            int id = rst.getInt(1);
            return id;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            pst.close();
            con.close();
        }
        return -1;
    }

    public boolean checkPasswordCount(int customer_id) throws SQLException{
        Connection con = null;
        PreparedStatement pst = null;
        try{
            con = returnConnection();
            pst = con.prepareStatement(passwordCountQuery);
            pst.setInt(1, customer_id);
            ResultSet rst = pst.executeQuery();
            rst.next();
            if(rst.getInt(1)==3)
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            pst.close();
            con.close();
        }
        return false;
    }
    public void insertPasswordIntoPasswordHistory(int customerId,String password) throws SQLException{
        Connection con = null;
        PreparedStatement pst = null;
        //find the count of passwords from the transaction_history db
        try{
            con = returnConnection();
            pst = con.prepareStatement(passwordInsertQuery);
            pst.setInt(1, customerId);
            pst.setString(2, password);
            pst.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            pst.close();
            con.close();
        }
    }
    public void updateAccountPassword(int customerId) throws SQLException{
        Connection con = null;
        PreparedStatement pst = null;
        try{
            con = returnConnection();
            pst = con.prepareStatement(passwordIdInsertQuery);
            pst.setInt(1, customerId);
            pst.setInt(2, customerId);
            pst.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            pst.close();
            con.close();
        }
    }
//inserting the user 
    public User insertUser(User user)throws Exception{
        
        System.out.println("im called");
        System.out.println(checkIfUserPresent(user.username, user.phoneNumber));
        if(checkIfUserPresent(user.username, user.phoneNumber))
        return null;
        Connection con = null;
        PreparedStatement pst = null;
        long accountNumber = generateAccountNumber();
  
        try{
            con = returnConnection();
            pst = con.prepareStatement(insertUserQuery);
            pst.setLong(1,accountNumber);
            pst.setString(2, user.username);
            pst.setLong(3, user.balance);
            pst.setLong(4, user.phoneNumber);
            pst.executeUpdate();
            
            //get the id            
            int customerId = returnId(accountNumber);
            System.out.println(customerId);
            //INSERT PASSWORD INTO THE PASSWORD_HISTORY 
            insertPasswordIntoPasswordHistory(customerId, user.password);

            //update accounts
            updateAccountPassword(customerId);

            return new User(customerId, accountNumber, user.username,user.balance,user.phoneNumber,null);
            
            //INSERT PASSWORD_ID TO ACCOUNTS TABLE
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            
            pst.close();
            con.close();
        }
        return null;
    }
    public int getUserid(long accountNumber){

        return 0;
    }
//check if the user exists already
    public boolean checkIfUserPresent(String username, long phoneNumber) throws SQLException{
        Connection con = null;
        PreparedStatement pst = null;
        try{
            con = returnConnection();
            
            pst = con.prepareStatement(ifPresentQuery);
            pst.setLong(1, phoneNumber);
            System.out.println(phoneNumber);
            ResultSet rst = pst.executeQuery();

            System.out.println(rst.next());
            if(rst.next())  
            return true;

            return false;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            pst.close();
            con.close();
        }
        return false;
    }

    public List<User> returnAllUsers() throws SQLException{
        Connection con = null;
        PreparedStatement pst = null;
        try{
            con = returnConnection();
            pst = con.prepareStatement(userQuery);
            ResultSet rst = pst.executeQuery();
            List<User> userList = new ArrayList<User>();
            while(rst.next()){
            User user = new User(rst.getInt(1),rst.getLong(2),rst.getString(3),rst.getLong(4),rst.getLong(5),rst.getTimestamp(5));
                userList.add(user);
            }
            return userList;
        }catch(Exception e){
            e.printStackTrace();
        }
        finally{
            pst.close();
            con.close();   
        }
        return null;
    }
    //check if the last trasaction is of maintenace fee
    public boolean returnIsMaintenance(int id) throws SQLException{
        Connection con = null;
        PreparedStatement pst = null;
        try{
            con = returnConnection();
            pst = con.prepareStatement(lastransactionType);
            pst.setInt(1, id);
            ResultSet rst = pst.executeQuery();
            rst.next();
            if(rst.getString(1).equals("Maintenance fee ="))
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }
        finally {
            pst.close();
            con.close();
        }
        return false;
    }

    //check transaction count
    public int returnTransactionCount(long accountNumber) throws SQLException{
        //find the id of the account number
        Connection con = null;
        PreparedStatement pst = null;
        int id = returnId(accountNumber);
        try{
            con = returnConnection();
            //find if the last transaction is of type maintenance
            if(!returnIsMaintenance(id))
            {   //if not check for the transaction count
                pst = con.prepareStatement(checkTransactionCountQuery);
                pst.setInt(1, id);
                ResultSet rst = pst.executeQuery();
                rst.next();
                int total = rst.getInt(1);
                // if( total%10 ==0 && total%5 == 0)
                // return 105;
                if(total%10==0)
                return 10;  

                if(total%5==0)
                return 5;
                
        }
     }catch(Exception e){
            e.printStackTrace();
        }finally{
            pst.close();
            con.close();
        }
        return 0;
    }
    public Timestamp returnCreatedAt(String query, int id) throws SQLException{
        Connection con = null;
        PreparedStatement pst = null;
        try{
            con = returnConnection();
            pst = con.prepareStatement(query);
            pst.setInt(1, id);
            ResultSet rst = pst.executeQuery();
            rst.next();
            return rst.getTimestamp(1);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            pst.close();
            con.close();
        }
        return null;
    } 
    //check the transaction and password date
    public boolean compareDates(long accountNumber) throws SQLException{
        int id = returnId(accountNumber);
        if(returnCreatedAt(lastransactionDateQuery, id).compareTo(returnCreatedAt(lastPasswordDateQuery, id))>0)
        return true;

        return false;
    }

    //check for valid password
    public boolean isValidPassword(long accountNumber, String password) throws SQLException{
        int id = returnId(accountNumber);
        Connection con = null;
        PreparedStatement pst = null;
        try{
            con = returnConnection();
            pst = con.prepareStatement(isValidPasswordQuery);
            pst.setInt(1, id);
            pst.setInt(2, id);
            pst.setString(3, password);
            ResultSet rst = pst.executeQuery();
            if(rst.next())
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            pst.close();
            con.close();
        }
        
        return false;
    } 
    //update the accounts db
    public void updateAccountsDb(long balance, int id) throws SQLException{
        Connection con = null;
        PreparedStatement pst = null;
        try{
            con = returnConnection();
            pst = con.prepareStatement(updateAccountsQuery);
            pst.setLong(1, balance);
            pst.setInt(2, id);
            pst.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            pst.close();
            con.close();
        }
    }

    //INSERT INTO TRANSACTION DB
    public boolean insertIntoTransactions(int customerId,String type,long transactionAmount,long balance ) throws SQLException{
        Connection con = null;
        PreparedStatement pst = null;
        try{
            con = returnConnection();
            pst = con.prepareStatement(insertIntoTransactions);
            pst.setInt(1, customerId);
            pst.setString(2, type);
            pst.setLong(3, transactionAmount);
            pst.setLong(4, balance);
            pst.executeUpdate();
            updateAccountsDb(balance, customerId);
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            pst.close();
            con.close();
        }
        return false;
    }
}
