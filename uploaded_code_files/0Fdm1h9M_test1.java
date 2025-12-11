package com.example.SpringREST.controller;
import java.sql.Connection;
import java.sql.Statement;
import java.security.MessageDigest;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class test1 {
    public static void main(String[] args) throws Exception {
        String userInput = "1 OR 1=1";

        Connection conn = null;
        Statement stmt = conn.createStatement();
        stmt.executeQuery("SELECT * FROM users WHERE id = " + userInput); // SQLi

        // Command injection
        Runtime.getRuntime().exec("ls " + userInput);

        // Hardcoded secret
        String secret = "SuperSecret123";

        // Weak crypto
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        // XSS
       jakarta.servlet.http.HttpServletResponse response = null;

        response.getWriter().print(userInput);

        // Insecure deserialization
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("data.ser"));
        Object obj = ois.readObject();

        // Path traversal
        File f = new File("../../etc/passwd");
    }
}

