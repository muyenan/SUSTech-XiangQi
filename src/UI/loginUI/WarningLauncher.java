package UI.loginUI;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WarningLauncher {

    public void showWarningWindow() {
        Stage loginStage = new Stage();
        Parent root = null;

        try {
            // 加载FXML文件
            root = FXMLLoader.load(getClass().getResource("WarningUI.fxml"));

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }


        // 创建窗口
        Scene scene = new Scene(root, 550, 300);

        loginStage.setTitle("警告");
        loginStage.setScene(scene);
        loginStage.setResizable(false); // 设定窗口尺寸无法改变

        loginStage.show();
    }
}