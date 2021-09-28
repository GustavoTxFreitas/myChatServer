package mychatserver.credentialnetwork.utils.classes;

/*
    Handles all the database related operations of the CredentialServer class
*/

import java.sql.*;
import static mychatserver.globalutils.Misc.*;

public class CredentialDatabaseHandler {

    public static final int SIGNUP_SUCCESS = 1;
    public static final int SIGNUP_ALIAS_EXISTS_ERROR = 2;
    public static final int SIGNUP_EMAIL_EXISTS_ERROR = 3;
    public static final int SIGNUP_INTERNAL_ERROR = 4;

    static private Connection connection;

    // Sets the connection of the class
    public static void getConnection(){
        String url = "jdbc:mysql://mychat.clisbfuk9q3h.ap-south-1.rds.amazonaws.com:3306/chat_server";
        String user = "admin";
        String password = "B6VF64EPM2GaTxAZ";
        connection = null;
        try{
            customPrint("Attempting to establish connection to the database server from CredentialDatabaseHandler...");
            connection = DriverManager.getConnection(url, user, password);
            customPrint("Connection established successfully!");
        }catch (Exception e){
            System.out.println(e);
        }
    }

    // // Constructor
    // public CredentialDatabaseHandler(){
    //     this.getConnection();
    // }

    // Checks if the user name exists or not
    public static synchronized boolean isAliasValid(String alias){
        try{
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select uname from chat_server.user_details where uname = '"+alias+"'");
            while(rs.next()){
                if(rs.getString(1).equals(alias))
                    return true;
            }
            return false;
        }catch (SQLException e){
            System.out.println(e);
            return false;
        }
    }

    // Checks if the login is valid or not
    public static synchronized boolean isLoginValid(String alias, String password){
        try{
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select password from chat_server.user_details where uname = '"+alias+"'");
            while(rs.next()){
                if(rs.getString(1).equals(password))
                    return true;
            }
            return false;
        }catch(SQLException e){
            System.out.println(e);
            return false;
        }
    }

    public static synchronized int createAccount(String firstName, String lastName, String email, String password, String alias){
        if (isAliasValid(alias))
            return SIGNUP_ALIAS_EXISTS_ERROR;
        try{
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select email from chat_server.user_details where email = '"+email+"'");
            while(rs.next()){
                if (rs.getString(1) == email)
                    return SIGNUP_EMAIL_EXISTS_ERROR;
            }
            int uid = (int)(Math.random()*1000000);
            PreparedStatement ps = connection.prepareStatement("insert into chat_server.user_details values (?,?,?,?,?,?)");
            ps.setInt(1, uid);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setString(4, email);
            ps.setString(5, password);
            ps.setString(6, alias);
            ps.executeUpdate();
            return SIGNUP_SUCCESS;
        }catch (SQLException e){
            System.out.println(e);
            return SIGNUP_INTERNAL_ERROR;
        }
    }

}