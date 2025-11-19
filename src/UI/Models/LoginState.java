package UI.Models;

import java.io.*;
import java.util.Properties;

public class LoginState {

    private static final String STATE_FILE = GetAppPath.getAppPath() + "/login_state.properties";

    // 保存自动登录用户
    public static void saveLoginState(String username) {
        try {
            Properties prop = new Properties();
            prop.setProperty("current_user", username); // 只保存用户名

            File file = new File(STATE_FILE);
            file.getParentFile().mkdirs();

            try (FileOutputStream fos = new FileOutputStream(file)) {
                prop.store(fos, "Login state");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获取自动登录用户名
    public static String getCurrentUser() {
        try {
            File file = new File(STATE_FILE);
            if (!file.exists()) return null;

            Properties prop = new Properties();
            try (FileInputStream fis = new FileInputStream(file)) {
                prop.load(fis);
            }

            return prop.getProperty("current_user");

        } catch (Exception e) {
            return null;
        }
    }

    // 清除自动登录状态
    public static void clearState() {
        File f = new File(STATE_FILE);
        if (f.exists()) f.delete();
    }
}
