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
    private Button muteButton;

    private final String SOUND_ICON = "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z";
    private final String MUTE_ICON = "M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z";

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