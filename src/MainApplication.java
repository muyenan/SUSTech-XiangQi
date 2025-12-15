import UI.Models.AudioModel;
import UI.Models.FontManager;
import UI.Models.LoginState;
import UI.MainUI.MainLauncher;
import UI.loginUI.LoginLauncher;

import javafx.application.Application;
import javafx.stage.Stage;

import java.awt.*;

public class MainApplication {

    public static void main(String[] args) {
        Application.launch(JavaFXApp.class, args);
    }

    public static class JavaFXApp extends Application {

        @Override
        public void start(Stage primaryStage) {
            // 使用AWT强制设置Dock图标
            setAwtDockIcon();

            // Load all fonts for CSS
            FontManager.loadAllFonts();

            AudioModel.getInstance().playBGM("/Resource/BGM.mp3");

            String lastUser = LoginState.getCurrentUser();

            if (lastUser != null && !lastUser.isEmpty()) {
                // 自动登录时不更新登录时间，直接传递用户名
                new MainLauncher(lastUser, lastUser).showMainWindow();
                System.out.println("自动登录用户：" + lastUser);
            } else {
                new LoginLauncher().showLoginWindow();
                System.out.println("显示登录界面");
            }
        }

        private void setAwtDockIcon() {
            if (Taskbar.isTaskbarSupported()) {
                Taskbar taskbar = Taskbar.getTaskbar();
                try {
                    String iconPath = "Resource/妮可象棋.png";

                    // 加载AWT Image
                    java.awt.Image awtIcon = Toolkit.getDefaultToolkit().getImage(
                            getClass().getClassLoader().getResource(iconPath)
                    );

                    // 强制设置 Dock 图标
                    taskbar.setIconImage(awtIcon);
                    System.out.println("AWT Taskbar Dock 图标设置成功。");
                } catch (Exception e) {
                    System.err.println("AWT Taskbar 设置 Dock 图标失败：" + e.getMessage());
                }
            }
        }
    }
}
