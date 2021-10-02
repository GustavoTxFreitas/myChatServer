package mychatserver.chatnetwork.utils.classes;

import java.sql.*;
import java.util.ArrayList;
import static mychatserver.globalutils.Misc.*;

public class ChatServerDatabaseHandler {

    static private Connection connection;

    public static void getConnection(){
        String url = "jdbc:mysql://mychat.clisbfuk9q3h.ap-south-1.rds.amazonaws.com:3306/chat_server";
        String user = "admin";
        String password = "B6VF64EPM2GaTxAZ";
        connection = null;
        try{
            customPrint("Attempting to establish connection to the database server.");
            connection = DriverManager.getConnection(url, user, password);
            customPrint("Connection established successfully.");
        }catch (Exception e){
            customPrint("Connection to chat database failed! Exiting...");
            System.exit(1);
        }
    }

    public static ArrayList<String> getAllUsers(){
        try{
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select uname from chat_server.user_details");
            ArrayList<String> userNames = new ArrayList<>();
            while(resultSet.next()){
                userNames.add(resultSet.getString(1));
            }
            return userNames;
        } catch (SQLException e){
            System.out.println(e);
            return null;
        }
    }

}