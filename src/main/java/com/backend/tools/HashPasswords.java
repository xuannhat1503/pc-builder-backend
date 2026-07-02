package com.backend.tools;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * Simple CLI utility to update users' password_hash by email.
 * Usage:
 * mvn -q exec:java -Dexec.mainClass=com.backend.tools.HashPasswords -Dexec.args="jdbcUrl dbUser dbPass email1:plain1 email2:plain2"
 */
public class HashPasswords {

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: <jdbcUrl> <dbUser> <dbPass> <email:plain> [email:plain ...]");
            System.exit(2);
        }
        String jdbc = args[0];
        String dbUser = args[1];
        String dbPass = args[2];
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        try (Connection conn = DriverManager.getConnection(jdbc, dbUser, dbPass)) {
            String sql = "UPDATE users SET password_hash = ? WHERE email = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 3; i < args.length; i++) {
                    String[] parts = args[i].split(":", 2);
                    if (parts.length != 2) continue;
                    String email = parts[0];
                    String plain = parts[1];
                    String hash = encoder.encode(plain);
                    ps.setString(1, hash);
                    ps.setString(2, email);
                    int updated = ps.executeUpdate();
                    System.out.printf("Updated %s -> %d rows\n", email, updated);
                }
            }
        }
    }
}
