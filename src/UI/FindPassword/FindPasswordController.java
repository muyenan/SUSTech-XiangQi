package UI.FindPassword;

import UI.Models.ShowAlert;
import UI.Models.CryptoPassword;
import UI.Models.SaveUsers;
import UI.loginUI.LoginLauncher;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import static UI.Models.ShowAlert.showAlert;

public class FindPasswordController {
    @FXML
    private TextField UserNameTextField;

    @FXML
    private PasswordField NewPasswordTextField;

    @FXML
    private PasswordField ConfirmNewPasswordTextField;

    @FXML
    private Button ContinueButton;

    @FXML
    private Hyperlink returnLoginLink;


    @FXML
    public void handleContinue() {
        try {
            handleContinueInternal();
        } catch (Exception e) {
            System.err.println("运行错误： " + e.getMessage());
            e.printStackTrace();
            ShowAlert.showAlert("未知错误", "请联系管理员！");
        }
    }

    private void handleContinueInternal() {
        String userName = UserNameTextField.getText().trim();
        String newPassword = NewPasswordTextField.getText();
        String confirmPassword = ConfirmNewPasswordTextField.getText();

        // 验证输入
        if (userName.isEmpty() || newPassword.isEmpty()) {
            ShowAlert.showAlert("错误", "用户名或密码不能为空");
            return;
        }

        if (!SaveUsers.isUserNameExists(userName)) {
            ShowAlert.showAlert("错误", "用户不存在");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            ShowAlert.showAlert("错误", "两次输入的密码不一致");
            return;
        }

        String storedOldHash = SaveUsers.users.getProperty(userName);
        if (storedOldHash == null) {
            ShowAlert.showAlert("错误", "文件损坏，请联系管理员！");
            return;
        }

        if (CryptoPassword.verifyPassword(newPassword, storedOldHash)) {
            ShowAlert.showAlert("错误", "新密码不能与旧密码相同");
            return;
        }

        // 检查用户名是否包含非法字符（防止目录遍历攻击）
        if (userName.contains("..") || userName.contains("/") || userName.contains("\\") ||
                userName.contains(":") || userName.contains("*") || userName.contains("?") ||
                userName.contains("\"") || userName.contains("<") || userName.contains(">") ||
                userName.contains("|")) {
            showAlert("错误", "用户名不能包含特殊符号");
            return;
        }

        // 更新密码
        String newHashedPassword = CryptoPassword.hashPassword(newPassword);
        SaveUsers.users.setProperty(userName, newHashedPassword);
        SaveUsers.saveUsersToFile();

        ShowAlert.showAlert("成功", "密码修改成功");
        System.out.println("密码修改成功: " + userName);

        clearFields();

        // 跳转回登录界面
        try {
            // 关闭当前窗口
            Stage currentStage = (Stage) returnLoginLink.getScene().getWindow();
            currentStage.close();

            // 返回登录界面
            LoginLauncher loginLauncher = new LoginLauncher();
            loginLauncher.showLoginWindow();

            System.out.println("返回登录界面");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("返回登录界面失败" + e.getMessage());
        }
    }

    @FXML
    public void handleReturnLogin() {
        try {
            Stage currentStage = (Stage) returnLoginLink.getScene().getWindow();
            currentStage.close();

            // 返回登录界面
            LoginLauncher loginLauncher = new LoginLauncher();
            loginLauncher.showLoginWindow();

            System.out.println("返回登录界面");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("返回登录界面失败" + e.getMessage());
        }
    }

    private void clearFields() {
        UserNameTextField.clear();
        NewPasswordTextField.clear();
        ConfirmNewPasswordTextField.clear();
    }
}