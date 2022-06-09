package com.zoho.database;

import com.zoho.userClass.Users;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class Database {
    String insertUserQuery = "INSERT INTO accounts(account_number, name, balance,phone_no) VALUES(?,?,?,?)";
    String userQuery = "SELECT * FROM accounts";
    String particularUserQuery = "SELECT * FROM accounts WHERE account_number = ?";
    String ifPresentQuery = "SELECT customer_id FROM accounts WHERE phone_no = ?";
    String returnUserIdQuery = "SELECT customer_id FROM accounts WHERE account_number = ?";
    String returnCountQuery = "SELECT COUNT(*) FROM accounts";
    String passwordInsertQuery = "INSERT INTO password_history(customer_id,password) VALUES(?,?)";
    String passwordIdInsertQuery = "UPDATE accounts SET user_password = (SELECT password_id FROM password_history WHERE customer_id = ? ORDER BY created_at DESC LIMIT 1) WHERE customer_id = ?";
    String checkTransactionCountQuery  = "SELECT COUNT(*) FROM transactions WHERE customer_id = ? AND transaction_type NOT IN ('Maintenance fee')";    
    String lastransactionType = "SELECT transaction_type FROM transactions WHERE customer_id = ? ORDER BY created_at DESC LIMIT 1";
    String lastransactionDateQuery = "SELECT created_at FROM transactions WHERE customer_id = ? ORDER BY created_at DESC LIMIT 1";
    String lastPasswordDateQuery = "SELECT created_at FROM password_history WHERE customer_id = ? ORDER BY created_at DESC LIMIT 1";
    String isValidPasswordQuery = "SELECT customer_id FROM accounts WHERE customer_id = ? AND user_password = (SELECT password_id FROM password_history WHERE customer_id = ? AND password = ?";     
    String insertIntoTransactions = "INSERT INTO transactions(customer_id,transaction_type,transaction_amount,balance) VALUES(?,?,?,?)";
    String updateAccountsQuery = "UPDATE accounts SET balance = ? WHERE customer_id = ?";

    public Connection returnConnection() throws ClassNotFoundException, SQLException{
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/banking-app-test", "root", "");
    }

    public long generateAccountNumber(){
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
        
        }
        return 1;
    }

    public int returnId(long accountNumber){
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
        }
        return -1;
    }

    public void insertPasswordIntoPasswordHistory(int customerId,String password){
        Connection con = null;
        PreparedStatement pst = null;
        try{
            con = returnConnection();
            pst = con.prepareStatement(passwordInsertQuery);
            pst.setInt(1, customerId);
            pst.setString(2, password);
            pst.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void updateAccountPassword(int customerId){
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
        }
    }
//inserting the user 
    public Users insertUser(Users user)throws Exception{
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

            //INSERT PASSWORD INTO THE PASSWORD_HISTORY 
            insertPasswordIntoPasswordHistory(customerId, user.password);

            //update accounts
            updateAccountPassword(customerId);

            return new Users(customerId, accountNumber, user.username,user.balance,user.phoneNumber,null);
            
            //INSERT PASSWORD_ID TO ACCOUNTS TABLE
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            
            pst.close();
            con.close();
        }
        return new Users();
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
            ResultSet rst = pst.executeQuery();
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

    public List<Users> returnAllUsers() throws SQLException{
        Connection con = null;
        PreparedStatement pst = null;
        try{
            con = returnConnection();
            pst = con.prepareStatement(userQuery);
            ResultSet rst = pst.executeQuery();
            List<Users> userList = new ArrayList<Users>();
            while(rst.next()){
            Users user = new Users(rst.getInt(1),rst.getLong(2),rst.getString(3),rst.getLong(4),rst.getLong(5),rst.getTimestamp(5));
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
    public int returnTransactionCount(long accountNumber){
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
                ResultSet rst = pst.executeQuery();
                rst.next();
                int total = rst.getInt(1);
                // if( total%10 ==0 && total%5 == 0)
                // return 105;
                if(total%5==0)
                return 5;
                if(total%10==0)
                return 10;  
        }
     }catch(Exception e){
            e.printStackTrace();
        }
        return 0;
    }
    public Timestamp returnCreatedAt(String query, int id){
        Connection con = null;
        PreparedStatement pst = null;
        try{
            con = returnConnection();
            pst = con.prepareStatement(query);
            ResultSet rst = pst.executeQuery();
            rst.next();
            return rst.getTimestamp(1);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    } 
    //check the transaction and password date
    public boolean compareDates(long accountNumber){
        int id = returnId(accountNumber);
        if(returnCreatedAt(lastransactionDateQuery, id).compareTo(returnCreatedAt(lastPasswordDateQuery, id))>0)
        return true;

        return false;
    }

    //check for valid password
    public boolean isValidPassword(long accountNumber, String password){
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
        }
        
        return false;
    } 
    //update the accounts db
    public void updateAccountsDb(long balance, int id){
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
        }
    }

    //INSERT INTO TRANSACTION DB
    public boolean insertIntoTransactions(int customerId,String type,long transactionAmount,long balance ){
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
        }
        return false;
    }
}
