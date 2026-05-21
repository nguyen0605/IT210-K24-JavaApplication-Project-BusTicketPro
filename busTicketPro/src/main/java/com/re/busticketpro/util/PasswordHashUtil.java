package com.re.busticketpro.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHashUtil {
    private PasswordHashUtil() {
    }

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
