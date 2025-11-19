package UI.loginUI;

import UI.MainUI.MainLauncher;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.util.Random;

import static UI.Models.ShowAlert.showAlert;

public class WarningController {
    @FXML
    private Button returnBackButton;

    @FXML
    private Button continueButton;

    @FXML
    public void handleReturnBack() {
        try {
            Stage currentStage = (Stage) returnBackButton.getScene().getWindow();
            currentStage.close();

            // 打开登录界面
            LoginLauncher loginUI = new LoginLauncher();
            loginUI.showLoginWindow();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("加载失败: " + e.getMessage());
            showAlert("未知错误", "请联系管理员!");
        }
    }

    @FXML
    public void handleContinue() {
        try {
            // 生成游客用户名 (仅用于显示)
            String guestUserName = "游客" + RandomCodeGenerator();

            // 关闭当前窗口
            Stage currentStage = (Stage) continueButton.getScene().getWindow();
            currentStage.close();

            // 打开主界面，传递游客信息
            MainLauncher mainLauncher = new MainLauncher(guestUserName, null);
            mainLauncher.showMainWindow();

            System.out.println("成功以 " + guestUserName + " 身份打开主界面");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("加载失败: " + e.getMessage());
            showAlert("未知错误", "请联系管理员!");
        }
    }

    public String RandomCodeGenerator() {
        Random random = new Random();
        int num = random.nextInt(1000000);
        String result = String.format("%06d", num);
        return result;
    }
}