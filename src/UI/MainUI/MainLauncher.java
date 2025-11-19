package UI.MainUI;

import UI.MainUI.MainController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainLauncher {

    private String username;
    private String sessionIdentifier;
    private String lastLoginTime;

    public MainLauncher() {
    }

    public MainLauncher(String username, String sessionIdentifier) {
        this.username = username;
        this.sessionIdentifier = sessionIdentifier;
    }
    
    public MainLauncher(String username, String sessionIdentifier, String lastLoginTime) {
        this.username = username;
        this.sessionIdentifier = sessionIdentifier;
        this.lastLoginTime = lastLoginTime;
    }

    public void showMainWindow() {
        Stage loginStage = new Stage();
        Parent root = null;
        FXMLLoader loader = new FXMLLoader();

        try {
            // 加载FXML文件
            loader.setLocation(getClass().getResource("MainUI.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 获取控制器并初始化用户信息
        MainController controller = loader.getController();
        if (controller != null) {
            if (lastLoginTime != null) {
                controller.initialize(username, sessionIdentifier, lastLoginTime);
            } else {
                controller.initialize(username, sessionIdentifier);
            }
        }

        // 创建窗口
        Scene scene = new Scene(root, 800, 500);

        loginStage.setTitle("妮可象棋");
        loginStage.setScene(scene);
        loginStage.setResizable(false); // 设定窗口尺寸无法改变

        loginStage.show();
    }
}