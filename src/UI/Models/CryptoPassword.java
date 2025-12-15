package UI.Models;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoPassword {

    // 生成加盐哈希密码
    public static String hashPassword(String password) {
        try {
            // 生成随机盐
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            // 密码 + 盐
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            // 组合盐和哈希值: salt:hash
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    // 验证密码
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // 解码存储的哈希值
            byte[] combined = Base64.getDecoder().decode(storedHash);

            // 提取盐 (前16字节)
            byte[] salt = new byte[16];
            System.arraycopy(combined, 0, salt, 0, salt.length);

            // 重新计算哈希
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] testHash = md.digest(password.getBytes());

            // 比较哈希值 (后32字节)
            for (int i = 0; i < testHash.length; i++) {
                if (testHash[i] != combined[i + salt.length]) {
                    return false;
                }
            }
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}