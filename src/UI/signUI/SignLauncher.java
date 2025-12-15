package UI.signUI;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SignLauncher {

    public void showSignWindow() {
        Stage loginStage = new Stage();
        Parent root = null;

        try {
            // 加载FXML文件
            root = FXMLLoader.load(getClass().getResource("SignUp.fxml"));

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }


        // 创建窗口
        Scene scene = new Scene(root, 800, 500);

        loginStage.setTitle("注册");
        loginStage.setScene(scene);
        loginStage.setResizable(false); // 设定窗口尺寸无法改变

        loginStage.show();
    }
}