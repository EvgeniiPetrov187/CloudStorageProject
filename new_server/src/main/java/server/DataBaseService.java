package server;

import java.sql.*;


public class DataBaseService {
    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement insSet;
    private static PreparedStatement selSet;
    private static PreparedStatement selNickSet;
    private static PreparedStatement upNick;
    private static PreparedStatement findNick;
    private ResultSet rs;
    private ResultSet nickRS;


    private static void prepareAllStatements() throws SQLException {
        insSet = connection.prepareStatement("INSERT INTO cloud_storage.users (login, password, nickname) VALUES (?, ?, ?);");
        selSet = connection.prepareStatement("SELECT login FROM cloud_storage.users WHERE login=? AND password=?;");
        selNickSet = connection.prepareStatement("SELECT nickname FROM cloud_storage.users WHERE login=? AND password=?;");
        upNick = connection.prepareStatement("UPDATE cloud_storage.users SET nickname=? WHERE nickname=?;");
        findNick = connection.prepareStatement("SELECT nickname FROM cloud_storage.users WHERE id=?;");
    }


    public boolean registration(String login, String password, String nickname) {
        try {
            selSet.setString(1, login);
            selSet.setString(2, password);
            selSet.executeQuery();
            rs = selSet.executeQuery();
            if (login.equals(rs.getString("login")))
                return false;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try {
            insSet.setString(1, login);
            insSet.setString(2, password);
            insSet.setString(3, nickname);
            insSet.executeUpdate();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public String getNicknameByLoginAndPassword(String login, String password) {
        try {
            selNickSet.setString(1, login);
            selNickSet.setString(2, password);
            selNickSet.executeQuery();
            nickRS = selNickSet.executeQuery();
            nickRS.next();
            return nickRS.getString("nickname");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public boolean changeNick(String nickname, String newNickName) {
        try {
            upNick.setString(1, newNickName);
            upNick.setString(2, nickname);
            upNick.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mysql", "root", "13barsik!Z");
            statement = connection.createStatement();
            prepareAllStatements();
            System.out.println("Connected with database");
            return true;
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public void disconnect() {
        try {
            insSet.close();
            selSet.close();
            selNickSet.close();
            upNick.close();
            findNick.close();
            connection.close();
            statement.close();
            System.out.println("Disconnected from database");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


