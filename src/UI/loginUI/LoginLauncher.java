package UI.loginUI;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class LoginLauncher {

    public void showLoginWindow() {
        Stage loginStage = new Stage();

        try {
            String iconPath = "Resource/妮可象棋.png";

            java.io.InputStream iconStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(iconPath);

            Image icon = new Image(Objects.requireNonNull(
                    iconStream,
                    "无法找到资源：" + iconPath + " (请检查JAR包内部路径是否正确)"
            ));

            loginStage.getIcons().add(icon);
            System.out.println("JavaFX Stage 图标加载成功。");

        } catch (Exception e) {
            System.err.println("无法加载 JavaFX Stage 图标");
            e.printStackTrace();
        }

        Parent root = null;

        try {
            // 加载FXML文件
            root = FXMLLoader.load(getClass().getResource("LoginUI.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }


        // 创建窗口
        Scene scene = new Scene(root, 800, 500);

        loginStage.setTitle("登录");
        loginStage.setScene(scene);
        loginStage.setResizable(false); // 设定窗口尺寸无法改变

        loginStage.show();
    }
}