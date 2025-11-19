package UI.signUI;

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

    private File selectedFile;

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
}