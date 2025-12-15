package UI.Models;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

import java.io.File;

public class ShowAlert {
    public static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);

        // 加载自定义字体
        try {
            Font customFont = Font.loadFont(new File("Resource/fonts/仿宋GB2312.ttf").toURI().toString(), 14);
            if (customFont != null) {
                // 创建一个Label并设置字体
                Label contentLabel = new Label(message);
                contentLabel.setFont(customFont);
                // 将Label设置为Alert的内容
                alert.getDialogPane().setContent(contentLabel);
            } else {
                alert.setContentText(message); // 字体加载失败，回退到默认字体
            }
        } catch (Exception e) {
            e.printStackTrace();
            alert.setContentText(message); // 出现异常，回退到默认字体
        }

        alert.showAndWait();
    }
}