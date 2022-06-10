package com.zoho.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONObject;

import com.zoho.database.Database;

import java.util.stream.Collectors;




public class AdminServlet extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException{
        String path = req.getServletPath();
        switch (path) {
    
        case "/allUsers":
    allUsers(req,res);
    break;
        }
    }
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException{
        String path = req.getServletPath();
        switch (path) {
            case "/admin/login":
                adminValidate(req,res);
                break;
            
              
            default:
                break;
        }

    }
    public void allUsers(HttpServletRequest req, HttpServletResponse res) throws IOException{
        JSONArray array = new JSONArray();
        array = new Database().sendAllUsers();
        res.getWriter().write(array.toString());
    }
    public void adminValidate(HttpServletRequest req, HttpServletResponse res) throws IOException{
        String jsonBody = new BufferedReader(new InputStreamReader(req.getInputStream())).lines().collect(
            Collectors.joining("\n"));
            JSONObject jObj = new JSONObject(jsonBody);
            String email = jObj.getString("adminEmail");
            String password = jObj.getString("adminPassword");
            String adminName =new Database().validateAdmin(email, password); 
            if(adminName!=null){
                jObj.put("status","success");
                jObj.put("name",adminName);
            }
            else 
            jObj.put("status","failure");

            res.getWriter().write(jObj.toString());
    }
    
}
