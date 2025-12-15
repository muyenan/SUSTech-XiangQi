package UI.MainUI;

import UI.MainGameUI.MainGameLauncher;
import UI.Models.AudioModel;
import UI.loginUI.LoginLauncher;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static UI.Models.GetAppPath.getAppPath;
import static UI.Models.ShowAlert.showAlert;
import static UI.Models.SaveUsers.users;
import UI.Models.LoginState;
import UI.Models.GetAppPath;

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
    private Button AIBattleButton;

    @FXML
    private Button QuickGameButton;

    @FXML
    private Button HistoryButton;

    @FXML
    private Hyperlink LogOutLink;

    @FXML
    private Button muteButton;

    public static String username; // 存储显示给用户的名称
    private static String sessionIdentifier; // 用户的唯一ID，游客为 null 或 ""
    private static String lastLoginTime; // 上次登录时间

    private static String passwordHashForEncryption = null;
    private final String SOUND_ICON = "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z";
    private final String MUTE_ICON = "M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z";

    @FXML
    private void handleAIBattle() {
        List<String> choices = Arrays.asList("简单", "中等", "困难");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("中等", choices);
        dialog.setTitle("人机对战");
        dialog.setHeaderText("请选择人机难度");
        dialog.setContentText("难度:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(difficulty -> {
            try {
                System.out.println("加载人机对战界面，难度：" + difficulty);

                Stage currentStage = (Stage) AIBattleButton.getScene().getWindow();
                currentStage.close();

                MainGameLauncher gameLauncher = new MainGameLauncher(username, sessionIdentifier, lastLoginTime, "AI", difficulty);
                gameLauncher.showMainGameWindow();

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("未知错误", "请联系管理员!");
            }
        });
    }

    @FXML
    private void handleQuickGame() {
        try {
            System.out.println("加载快速对战界面");
            Stage currentStage = (Stage) QuickGameButton.getScene().getWindow();
            currentStage.close();

            MainGameLauncher gameLauncher = new MainGameLauncher(username, sessionIdentifier, lastLoginTime, "Local", null);
            gameLauncher.showMainGameWindow();

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

            try {
                // 关闭当前页面
                Stage currentStage = (Stage) HistoryButton.getScene().getWindow();
                currentStage.close();

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

    @FXML
    public void initialize() {
        // 更新图标显示
        updateMuteIcon();
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

    @FXML
    private void handleMuteAction() {
        try {
            // 切换静音状态
            boolean isMuted = AudioModel.getInstance().isMuted();
            AudioModel.getInstance().setMute(!isMuted);

            // 刷新图标
            updateMuteIcon();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMuteIcon() {
        if (muteButton == null) return;

        boolean isMuted = AudioModel.getInstance().isMuted();

        SVGPath svg = new SVGPath();
        svg.setContent(isMuted ? MUTE_ICON : SOUND_ICON);
        // 设置颜色为深棕色
        svg.setFill(Color.web("#8b4513"));
        svg.setScaleX(1.5); // 图标放大倍数
        svg.setScaleY(1.5);

        muteButton.setGraphic(svg);
    }

    public static String getUserName() { return username; }
    public static String getSessionIdentifier() { return sessionIdentifier; }

    public static String getUserPasswordHash() { return passwordHashForEncryption; }

    public static void setUserName(String newUserName) { username = newUserName; }

    public static void setSessionIdentifier(String newSessionIdentifier) { sessionIdentifier = newSessionIdentifier; }
}