package UI.signUI;

import UI.Models.AudioModel;
import UI.Models.CryptoPassword;
import UI.Models.SaveUsers;
import UI.loginUI.LoginLauncher;

import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

import static UI.Models.ShowAlert.showAlert;

public class SignController {
    @FXML
    private TextField userNameTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private PasswordField confirmPasswordTextField;

    @FXML
    private ImageView avatarImageView;

    @FXML
    private Hyperlink uploadAvatarLink;

    @FXML
    private Button signUpButton;

    @FXML
    private Hyperlink returnLoginLink;

    @FXML
    private Button muteButton;

    private File selectedFile;

    private final String SOUND_ICON = "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z";
    private final String MUTE_ICON = "M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z";

    @FXML
    public void initialize() {
        // 更新图标显示
        updateMuteIcon();
    }

    @FXML
    public void handleUploadAvatar() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("选择头像文件");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        selectedFile = fileChooser.showOpenDialog(uploadAvatarLink.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // 显示头像
                Image newAvatar = new Image(selectedFile.toURI().toString());
                avatarImageView.setImage(newAvatar);
                System.out.println("成功显示头像");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("显示头像失败");
            }
        }
    }

    @FXML
    public void handleSignUp() {
        String userName = getSignUserName();
        String password = passwordTextField.getText();
        String confirmPassword = confirmPasswordTextField.getText();

        // 验证输入
        if (userName.isEmpty() || password.isEmpty()) {
            showAlert("错误", "用户名或密码不能为空");
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

        if (!password.equals(confirmPassword)) {
            showAlert("错误", "两次输入的密码不一致");
            return;
        }

        if (SaveUsers.isUserNameExists(userName)) {
            showAlert("错误", "用户名已存在");
            return;
        }

            // 加密密码并保存用户
            String hashedPassword = CryptoPassword.hashPassword(password);
            boolean success = SaveUsers.saveUser(userName, hashedPassword);

        if (success) {
            showAlert("欢迎", "恭喜您，注册成功");

            // 创建用户文件
            createUserFiles(userName);

            handleReturnLogin(); // 注册成功后返回登录界面
        } else {
            showAlert("未知错误", "请联系管理员！");
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

    // 创建用户特定的文件
    private void createUserFiles(String username) {
        try {
            String userDir = SaveUsers.getUserDirectoryPath(username);

            // 保存用户头像
            if (selectedFile != null) {
                try {
                    String outputPath = userDir + File.separator + "user_avatar.png";

                    // 调用你的方法进行转换
                    File pngAvatar = convertToPng(selectedFile, outputPath);
                    System.out.println("头像已成功保存为 PNG: " + pngAvatar.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("错误", "保存头像失败，请重试");
                }
            } else {
                InputStream defaultAvatar = getClass().getResourceAsStream("/Resource/DefaultAvatar.png");
                File avatarFile = new File(userDir + File.separator + "user_avatar.png");
                copyFile(defaultAvatar, avatarFile);
            }

            System.out.println("成功创建用户文件夹，路径： " + userDir + File.separator + "user_avatar.png");

        } catch (IOException e) {
            System.err.println("创建用户文件失败: " + e.getMessage());
        }
    }

    // 获取用户（意向）用户名
    public String getSignUserName() {
        return userNameTextField.getText().trim();
    }

    // 复制文件
    private void copyFile(InputStream sourceStream, File dest) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = sourceStream.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }

    // 头像转换为png格式
    private File convertToPng(File inputFile, String outputPath) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);

        if (image == null) {
            throw new IOException("不是有效的图片文件");
        }

        File outputFile = new File(outputPath);

        ImageIO.write(image, "png", outputFile);

        return outputFile;
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
        // 设置颜色为深棕色以匹配你的主题 (#8b4513)，或者黑色 Color.BLACK
        svg.setFill(Color.web("#8b4513"));
        svg.setScaleX(1.5); // 图标放大倍数
        svg.setScaleY(1.5);

        muteButton.setGraphic(svg);
    }
}