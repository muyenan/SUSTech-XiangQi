package UI.loginUI;

import UI.FindPassword.FindPasswordLauncher;
import UI.MainUI.MainLauncher;
import UI.Models.LoginState;
import UI.Models.SaveUsers;
import UI.signUI.SignLauncher;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
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
    private CheckBox autoLoginCheckBox;

    // 登录状态
    private String userName;
    private String password;

    private static String currentUserName;
    private static String lastLogin;

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
            Stage currentStage = (Stage) touristVisitButton.getScene().getWindow();
            currentStage.close();

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
            Stage currentStage = (Stage) touristVisitButton.getScene().getWindow();
            currentStage.close();

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
            Stage currentStage = (Stage) touristVisitButton.getScene().getWindow();
            currentStage.close();

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
    
    private String getCurrentLoginTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
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
}