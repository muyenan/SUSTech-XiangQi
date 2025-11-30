package UI.MainUI;

import UI.MainGameUI.MainGameLauncher;
import UI.loginUI.LoginLauncher;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import static UI.Models.GetAppPath.getAppPath;
import static UI.Models.ShowAlert.showAlert;
import static UI.Models.SaveUsers.users; // 导入 SaveUsers 的 users 属性
import UI.Models.LoginState; // 导入 LoginState 类
import UI.Models.GetAppPath; // 导入 GetAppPath 类

public class MainController {
    @FXML
    private Label welcomeLabel;

    @FXML
    private ImageView AvatarImageView;

    @FXML
    private Label SignUpLabel;

    @FXML
    private Label LastLoginLabel;

    @FXML
    private Button OnlineBattleButton;

    @FXML
    private Button QuickGameButton;

    @FXML
    private Button HistoryButton;

    @FXML
    private Hyperlink LogOutLink;

    public static String username; // 存储显示给用户的名称
    private static String sessionIdentifier; // 用户的唯一ID，游客为 null 或 ""
    private static String lastLoginTime; // 上次登录时间

    // 新增静态变量，用于存储用户的密码哈希，作为存档加密的密钥基础
    private static String passwordHashForEncryption = null;

    @FXML
    private void handleOnlineBattle() {
        try {
            System.out.println("加载联机对战界面");
            showAlert("提示", "联机对战功能暂未开放。");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("未知错误", "请联系管理员!");
        }
    }

    @FXML
    private void handleQuickGame() {
        try {
            System.out.println("加载快速对战界面"); // 修正拼写错误
            Stage currentStage = (Stage) QuickGameButton.getScene().getWindow();
            currentStage.close();

            MainGameLauncher gameLauncher = new MainGameLauncher(username, sessionIdentifier, lastLoginTime);
            gameLauncher.showMainGameWindow(); // 修正方法名

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("未知错误", "请联系管理员!");
        }
    }

    @FXML
    private void handleHistory() {
        try {
            System.out.println("加载历史记录界面");
            
            if (sessionIdentifier == null || sessionIdentifier.isEmpty()) {
                showAlert("提示", "游客模式不能查看历史存档！");
                return;
            }
            
            // 打开新的历史存档界面
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("HistoryArchive.fxml"));
                javafx.scene.Parent root = loader.load();
                
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("历史存档");
                stage.setScene(new javafx.scene.Scene(root, 800, 500));
                stage.setResizable(false);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("错误", "无法打开历史存档界面: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("未知错误", "请联系管理员!");
        }
    }

    @FXML
    private void handleLogOut() {
        try {
            System.out.println("用户登出");
            
            // 清除用户会话信息
            username = null;
            sessionIdentifier = null;
            lastLoginTime = null;
            passwordHashForEncryption = null;

            // 清除自动登录状态
            LoginState.clearState();

            // 检查 login_state.properties 文件是否存在
            File loginStateFile = new File(GetAppPath.getAppPath() + "/login_state.properties");
            if (loginStateFile.exists()) {
                System.out.println("警告：login_state.properties 文件在 clearState() 后仍然存在！");
            } else {
                System.out.println("信息：login_state.properties 文件已成功删除。");
            }
            
            Stage currentStage = (Stage) LogOutLink.getScene().getWindow();
            currentStage.close();

            LoginLauncher loginLauncher = new LoginLauncher();
            loginLauncher.showLoginWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("未知错误", "请联系管理员!");
        }
    }

    // 修改initialize方法为public，以便从MainLauncher调用
    public void initialize(String newUsername, String newSessionIdentifier) {
        initialize(newUsername, newSessionIdentifier, null);
    }
    
    public void initialize(String newUsername, String newSessionIdentifier, String passedLastLoginTime) {
        username = newUsername;
        sessionIdentifier = newSessionIdentifier;
        lastLoginTime = passedLastLoginTime;

        if (sessionIdentifier == null || sessionIdentifier.isEmpty()) {
            // 游客模式
            welcomeLabel.setText("欢迎，" + username);
            SignUpLabel.setText("注册时间: 临时访问");
            LastLoginLabel.setText("本次登录: 临时访问");
            LogOutLink.setText("返回登录");
            return;
        }

        // 注册用户模式
        welcomeLabel.setText("欢迎，" + username);
        LogOutLink.setText("注销");

        // 获取注册时间
        String registrationDate = getConfigProperty(sessionIdentifier, "registration_date", "无记录");

        // 显示上次登录时间（优先使用传递过来的时间，否则从配置文件读取）
        if (lastLoginTime != null) {
            if (lastLoginTime.isEmpty()) {
                LastLoginLabel.setText("上次登录: 无记录");
            } else {
                LastLoginLabel.setText("上次登录: " + lastLoginTime);
            }
        } else {
            // 获取上一次登录时间 (本次登录前的时间)
            String lastLoginBeforeUpdate = getConfigProperty(sessionIdentifier, "last_login", "无记录");
            if (lastLoginBeforeUpdate == null || lastLoginBeforeUpdate.isEmpty() || lastLoginBeforeUpdate.equals("无记录")) {
                LastLoginLabel.setText("上次登录: 无记录");
            } else {
                LastLoginLabel.setText("上次登录: " + lastLoginBeforeUpdate);
            }
        }
        SignUpLabel.setText("注册时间: " + registrationDate);

        // 存储密码哈希用于存档加密
        passwordHashForEncryption = users.getProperty(sessionIdentifier);

        // 显示头像
        try {
            File avatarFile = new File(getAppPath() + "/Accounts/" + sessionIdentifier + "/user_avatar.png");
            if (avatarFile.exists()) {
                Image newAvatar = new Image(avatarFile.toURI().toString());
                AvatarImageView.setImage(newAvatar);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getConfigProperty(String userIdentifier, String key, String defaultValue) {
        String value = defaultValue;
        try {
            // 使用sessionIdentifier访问文件夹
            File configFile = new File(getAppPath() + "/Accounts/" + userIdentifier + "/user.config");
            if (configFile.exists()) {
                Properties prop = new Properties();
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    prop.load(fis);
                    value = prop.getProperty(key, defaultValue);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    private void updateLastLogin(String userIdentifier) {
        File configFile = new File(getAppPath() + "/Accounts/" + userIdentifier + "/user.config");
        if (!configFile.exists()) {
            return;
        }

        Properties properties = new Properties();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try (FileInputStream fis = new FileInputStream(configFile)) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        // 设置本次登录时间
        properties.setProperty("last_login", sdf.format(new Date()));

        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            properties.store(fos, "用户配置文件 - 更新登录时间");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getUserName() { return username; }
    public static String getSessionIdentifier() { return sessionIdentifier; }

    public static String getUserPasswordHash() { return passwordHashForEncryption; }

    public static void setUserName(String newUserName) { username = newUserName; }

    public static void setSessionIdentifier(String newSessionIdentifier) { sessionIdentifier = newSessionIdentifier; }
}