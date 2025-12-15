package UI.Models;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static UI.Models.GetAppPath.getAppPath;

public class SaveUsers {
    private static final String ACCOUNTS_DIR = getAppPath() + "/Accounts";
    private static final String USERS_FILE = ACCOUNTS_DIR + "/users.properties";
    public static Properties users;

    static {
        // 确保Accounts目录存在
        createAccountsDirectory();
        loadUsers();
    }

    // 创建Accounts目录
    private static void createAccountsDirectory() {
        File accountsDir = new File(ACCOUNTS_DIR);
        if (!accountsDir.exists()) {
            if (accountsDir.mkdirs()) {
                System.out.println("创建Accounts目录成功");
            } else {
                System.err.println("创建Accounts目录失败");
            }
        }
    }

    // 为用户创建个人文件夹
    private static boolean createUserDirectory(String username) {
        File userDir = new File(ACCOUNTS_DIR + File.separator + username);
        if (!userDir.exists()) {
            if (userDir.mkdirs()) {
                System.out.println("创建用户文件夹成功: " + username);
                return true;
            } else {
                System.err.println("创建用户文件夹失败: " + username);
                return false;
            }
        }
        return true; // 文件夹已存在
    }

     // 获取用户文件夹路径
    public static String getUserDirectoryPath(String username) {
        return ACCOUNTS_DIR + File.separator + username;
    }

    // 获取用户文件夹对象
    public static File getUserDirectory(String username) {
        return new File(getUserDirectoryPath(username));
    }

    // 检查用户文件夹是否存在
    public static boolean userDirectoryExists(String username) {
        return getUserDirectory(username).exists();
    }

    // 加载用户数据
    private static void loadUsers() {
        users = new Properties();
        File file = new File(USERS_FILE);

        if (file.exists()) {
            try (FileInputStream input = new FileInputStream(file)) {
                users.load(input);
            } catch (IOException e) {
                System.err.println("加载用户数据失败: " + e.getMessage());
            }
        }
    }

    // 保存用户数据到文件
    public static void saveUsersToFile() {
        try (FileOutputStream output = new FileOutputStream(USERS_FILE)) {
            users.store(output, "用户数据 - 用户名和加密密码");
        } catch (IOException e) {
            System.err.println("保存用户数据失败: " + e.getMessage());
        }
    }

    // 获取所有用户名 - 用于验证用户名是否重复
    public static Set<String> getUsersName() {
        return users.stringPropertyNames();
    }

    // 检查用户名是否存在
    public static boolean isUserNameExists(String username) {
        return users.containsKey(username);
    }

    // 保存用户信息
    public static boolean saveUser(String username, String hashedPassword) {
        if (isUserNameExists(username)) {
            System.out.println("用户名已存在: " + username);
            return false;
        }

        // 创建用户文件夹
        if (!createUserDirectory(username)) {
            System.err.println("创建用户文件夹失败，用户注册中止");
            return false;
        }

        // 保存用户信息
        users.setProperty(username, hashedPassword);
        saveUsersToFile();

        // 创建用户配置文件
        createUserConfigFile(username);

        System.out.println("用户注册成功: " + username);
        return true;
    }

    // 创建用户配置文件
    private static void createUserConfigFile(String username) {
        Properties userConfig = new Properties();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();

        userConfig.setProperty("username", username);
        userConfig.setProperty("registration_date", sdf.format(now));
        userConfig.setProperty("last_login", "");

        File configFile = new File(getUserDirectoryPath(username) + File.separator + "user.config");
        try (FileOutputStream output = new FileOutputStream(configFile)) {
            userConfig.store(output, "用户配置文件 - " + username);
        } catch (IOException e) {
            System.err.println("创建用户配置文件失败: " + e.getMessage());
        }
    }

    // 验证用户登录
    public static boolean verifyUser(String username, String password) {
        String storedHash = users.getProperty(username);
        if (storedHash == null) {
            return false;
        }
        return CryptoPassword.verifyPassword(password, storedHash);
    }
}