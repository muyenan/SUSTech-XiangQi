package UI.loginUI;

import UI.FindPassword.FindPasswordLauncher;
import UI.MainUI.MainLauncher;
import UI.Models.AudioModel;
import UI.Models.LoginState;
import UI.Models.SaveUsers;
import UI.signUI.SignLauncher;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
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

public class LoginController {

    @FXML
    private TextField userNameTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private Button loginButton;

    @FXML
    private Button touristVisitButton;

    @FXML
    private Button signUpButton;

    @FXML
    private Hyperlink findPasswordLink;

    @FXML
    private CheckBox autoLoginCheckBox;

    @FXML
    private Button muteButton;

    // 登录状态
    private String userName;
    private String password;

    private static String currentUserName;
    private static String lastLogin;

    private final String SOUND_ICON = "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z";
    private final String MUTE_ICON = "M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z";


    @FXML
    public void initialize() {
        // 更新图标显示
        updateMuteIcon();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        userName = userNameTextField.getText().trim();
        password = passwordTextField.getText().trim();

        if (userName.isEmpty() || password.isEmpty()) {
            showAlert("错误", "用户名或密码不能为空");
            return;
        }

        if (!SaveUsers.verifyUser(userName, password)) {
            showAlert("错误", "用户名或密码不正确");
            passwordTextField.clear();
            return;
        }

        // 登录成功 - 先获取上次登录时间
        lastLogin = getLastLoginFromConfig(userName);

        // 更新本次登录时间
        updateLastLogin(userName);

        showAlert("欢迎", "您已成功登录\n上次登录时间：" + lastLogin);

        if (autoLoginCheckBox.isSelected()) {
            LoginState.saveLoginState(userName);
        }

        currentUserName = userName;

        // 打开主界面，传递上次登录时间
        try {
            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            currentStage.close();

            MainLauncher mainLauncher = new MainLauncher(userName, userName, lastLogin);
            mainLauncher.showMainWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("未知错误", "请联系管理员!");
        }
    }

    @FXML
    private void handleTouristVisit(ActionEvent event) {
        currentUserName = "";
        lastLogin = null;

        try {
            // 关闭当前界面
            Stage currentStage = (Stage) touristVisitButton.getScene().getWindow();
            currentStage.close();

            // 打开警告界面
            WarningLauncher warningLauncher = new WarningLauncher();
            warningLauncher.showWarningWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("未知错误", "请联系管理员!");
        }
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        try {
            // 关闭当前界面
            Stage currentStage = (Stage) signUpButton.getScene().getWindow();
            currentStage.close();

            // 打开注册界面
            SignLauncher signLauncher = new SignLauncher();
            signLauncher.showSignWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("未知错误", "请联系管理员!");
        }
    }

    @FXML
    private void handleFindPassword(ActionEvent event) {
        try {
            // 关闭当前窗口
            Stage currentStage = (Stage) findPasswordLink.getScene().getWindow();
            currentStage.close();

            // 打开找回密码界面
            FindPasswordLauncher findPasswordLauncher = new FindPasswordLauncher();
            findPasswordLauncher.showFindPasswordWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("未知错误", "请联系管理员!");
        }
    }

    public String getLastLoginFromConfig(String userName) {
        String lastLogin = "";
        try {
            File configFile = new File(getAppPath() + "/Accounts/" + userName + "/user.config");
            if (configFile.exists()) {
                Properties prop = new Properties();
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    prop.load(fis);
                    lastLogin = prop.getProperty("last_login", "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastLogin;
    }

    public static String getUserName() { return currentUserName; }

    public static String getLastLogin() { return lastLogin; }

    private void updateLastLogin(String userName) {
        File configFile = new File(getAppPath() + "/Accounts/" + userName + "/user.config");
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

        properties.setProperty("last_login", sdf.format(new Date()));

        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            properties.store(fos, "用户配置文件 - 更新登录时间");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}