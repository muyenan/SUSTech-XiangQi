package UI.FindPassword;

import UI.Models.AudioModel;
import UI.Models.ShowAlert;
import UI.Models.CryptoPassword;
import UI.Models.SaveUsers;
import UI.loginUI.LoginLauncher;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
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
    private Button muteButton;

    private final String SOUND_ICON = "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z";
    private final String MUTE_ICON = "M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z";

    @FXML
    public void initialize() {
        // 更新图标显示
        updateMuteIcon();
    }


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
            Stage currentStage = (Stage) ContinueButton.getScene().getWindow();
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

    private void clearFields() {
        UserNameTextField.clear();
        NewPasswordTextField.clear();
        ConfirmNewPasswordTextField.clear();
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