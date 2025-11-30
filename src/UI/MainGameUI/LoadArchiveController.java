package UI.MainGameUI;

import javafx.fxml.FXML;

import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;

import java.io.File;
import java.util.List;

import java.io.InputStream;


import UI.Models.GameArchiveManager;

import static UI.MainUI.MainController.getSessionIdentifier;
import static UI.MainUI.MainController.getUserName;
import static UI.MainUI.MainController.getUserPasswordHash;
import static UI.Models.GetAppPath.getAppPath;
import static UI.Models.ShowAlert.showAlert;

public class LoadArchiveController {

    @FXML
    private Canvas previewChessBoardCanvas;

    @FXML
    private ImageView AvatarImageView;

    @FXML
    private Label welcomeLabel;

    @FXML
    private ListView<String> saveFilesListView;

    @FXML
    private Button loadButton;

    @FXML
    private Button cancelButton;

    private List<GameArchiveManager.SaveFileInfo> saveFiles;
    private GameArchiveManager archiveManager;
    private ChessPiece[] currentPreviewPieces;
    private String currentUserName = getUserName();
    private String sessionIdentifier = getSessionIdentifier();
    private MainGameController mainGameController; // 引用主游戏控制器
    
    // 用于存储自定义加载的字体
    private Font redPieceFont;
    private Font blackPieceFont;
    
    private static final int ROWS = 10;
    private static final int COLS = 9;
    private static final int CELL_SIZE = 50;
    private double offsetX;
    private double offsetY;
    private final Color RED_COLOR = Color.web("#8B0000");


    public void setMainGameController(MainGameController controller) {
        this.mainGameController = controller;
    }

    @FXML
    public void initialize() {
        welcomeLabel.setText("欢迎，" + currentUserName);

        // 显示头像
        try {
            if (sessionIdentifier != null && !sessionIdentifier.isEmpty()) {
                File avatarFile = new File(getAppPath() + "/Accounts/" + currentUserName + "/user_avatar.png");
                if (avatarFile.exists()) {
                    Image newAvatar = new Image(avatarFile.toURI().toString());
                    AvatarImageView.setImage(newAvatar);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 初始化存档管理器
        String passwordHash = getUserPasswordHash();
        if (passwordHash != null && sessionIdentifier != null && !sessionIdentifier.isEmpty()) {
            archiveManager = new GameArchiveManager(currentUserName, passwordHash);
            loadSaveFiles();
        }

        // 添加列表选择事件监听器
        saveFilesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                previewSelectedSave(newValue);
            }
        });

        // 初始化棋子字体
        redPieceFont = loadCustomFont("fonts/HanYiWeiBeiJian-1.ttf", 26);
        blackPieceFont = loadCustomFont("fonts/HanYiWeiBeiFan-1.ttf", 26);
        
        // 计算棋盘偏移量
        int boardWidth = (COLS - 1) * CELL_SIZE;
        int boardHeight = (ROWS - 1) * CELL_SIZE;
        offsetX = (previewChessBoardCanvas.getWidth() - boardWidth) / 2.0;
        offsetY = (previewChessBoardCanvas.getHeight() - boardHeight) / 2.0;
        
        // 默认选择第一个存档进行预览
        if (!saveFilesListView.getItems().isEmpty()) {
            saveFilesListView.getSelectionModel().select(0);
        }
    }

    private void loadSaveFiles() {
        saveFiles = archiveManager.getSaveFiles();
        saveFilesListView.getItems().clear();

        for (GameArchiveManager.SaveFileInfo info : saveFiles) {
            saveFilesListView.getItems().add(info.getDisplayName());
        }
    }

    private void previewSelectedSave(String selectedDisplayName) {
        if (saveFiles == null || saveFiles.isEmpty()) return;

        // 找到对应的存档文件信息
        GameArchiveManager.SaveFileInfo selectedFileInfo = null;
        for (GameArchiveManager.SaveFileInfo info : saveFiles) {
            if (info.getDisplayName().equals(selectedDisplayName)) {
                selectedFileInfo = info;
                break;
            }
        }

        if (selectedFileInfo != null) {
            GameArchiveManager.GameArchiveData loadedData = archiveManager.loadGame(selectedFileInfo.getFileName());
            if (loadedData != null) {
                currentPreviewPieces = loadedData.pieces;
                drawPreviewBoard();
            }
        }
    }

    private void drawPreviewBoard() {
        if (currentPreviewPieces == null) return;
        drawBoard();
        drawPieces();
    }
    
    private void drawBoard() {
        GraphicsContext gc = previewChessBoardCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, previewChessBoardCanvas.getWidth(), previewChessBoardCanvas.getHeight());
        
        Color boardColor = Color.web("#eecfa1");
        Color lineColor = Color.web("#5c4033");

        // 背景色
        gc.setFill(boardColor);
        gc.fillRect(0, 0, previewChessBoardCanvas.getWidth(), previewChessBoardCanvas.getHeight());

        // 棋盘区域背景
        int boardWidth = (COLS - 1) * CELL_SIZE;
        int boardHeight = (ROWS - 1) * CELL_SIZE;
        gc.setFill(boardColor);
        gc.fillRect(offsetX, offsetY, boardWidth, boardHeight);

        gc.setStroke(lineColor);

        // 横线
        for (int i = 0; i < ROWS; i++) {
            double y = i * CELL_SIZE + offsetY;
            gc.setLineWidth((i == 0 || i == ROWS - 1) ? 3 : 2);
            gc.strokeLine(offsetX, y, boardWidth + offsetX, y);
        }

        // 竖线
        for (int i = 0; i < COLS; i++) {
            double x = i * CELL_SIZE + offsetX;
            gc.setLineWidth((i == 0 || i == COLS - 1) ? 3 : 2);
            if (i == 0 || i == COLS - 1) {
                gc.strokeLine(x, offsetY, x, boardHeight + offsetY);
            } else {
                gc.strokeLine(x, offsetY, x, 4 * CELL_SIZE + offsetY);
                gc.strokeLine(x, 5 * CELL_SIZE + offsetY, x, boardHeight + offsetY);
            }
        }
        
        gc.setLineWidth(2);
        // 九宫格
        gc.strokeLine(3 * CELL_SIZE + offsetX, 7 * CELL_SIZE + offsetY, 5 * CELL_SIZE + offsetX, 9 * CELL_SIZE + offsetY);
        gc.strokeLine(5 * CELL_SIZE + offsetX, 7 * CELL_SIZE + offsetY, 3 * CELL_SIZE + offsetX, 9 * CELL_SIZE + offsetY);
        gc.strokeLine(3 * CELL_SIZE + offsetX, offsetY, 5 * CELL_SIZE + offsetX, 2 * CELL_SIZE + offsetY);
        gc.strokeLine(5 * CELL_SIZE + offsetX, offsetY, 3 * CELL_SIZE + offsetX, 2 * CELL_SIZE + offsetY);

        // 楚河汉界
        gc.setFill(lineColor);
        gc.setFont(Font.font("KaiTi", 24));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        double textY = 4.5 * CELL_SIZE + offsetY;
        gc.fillText("楚 河", 2 * CELL_SIZE + offsetX, textY);
        gc.fillText("汉 界", 6 * CELL_SIZE + offsetX, textY);

        // 炮/兵 的标记点
        int[][] markerCoords = {
            {2,1}, {2,7}, // 黑炮
            {3,0}, {3,2}, {3,4}, {3,6}, {3,8}, // 黑卒
            {6,0}, {6,2}, {6,4}, {6,6}, {6,8}, // 红兵
            {7,1}, {7,7}  // 红炮
        };
        for (int[] coord : markerCoords) {
            drawMarker(gc, coord[1], coord[0]);
        }
    }

    private void drawMarker(GraphicsContext gc, int col, int row) {
        double x = col * CELL_SIZE + offsetX;
        double y = row * CELL_SIZE + offsetY;
        double gap = 5;
        double len = 15;
        
        gc.setLineWidth(2);
        gc.setStroke(Color.web("#5c4033"));

        // Top-Left
        if (col > 0) {
            gc.strokeLine(x - gap, y - gap, x - gap - len, y - gap);
            gc.strokeLine(x - gap, y - gap, x - gap, y - gap - len);
        }
        // Top-Right
        if (col < COLS - 1) {
            gc.strokeLine(x + gap, y - gap, x + gap + len, y - gap);
            gc.strokeLine(x + gap, y - gap, x + gap, y - gap - len);
        }
        // Bottom-Left
        if (col > 0) {
            gc.strokeLine(x - gap, y + gap, x - gap - len, y + gap);
            gc.strokeLine(x - gap, y + gap, x - gap, y + gap + len);
        }
        // Bottom-Right
        if (col < COLS - 1) {
            gc.strokeLine(x + gap, y + gap, x + gap + len, y + gap);
            gc.strokeLine(x + gap, y + gap, x + gap, y + gap + len);
        }
    }

    private void drawPieces() {
        GraphicsContext gc = previewChessBoardCanvas.getGraphicsContext2D();
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        double radius = 22;

        for (ChessPiece p : currentPreviewPieces) {
            if (p == null) continue;
            
            double x = p.x * CELL_SIZE + offsetX;
            double y = p.y * CELL_SIZE + offsetY;

            // 绘制棋子背景圆
            gc.setFill(Color.web("#fdf5e6"));
            gc.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);

            // 绘制棋子边框
            Color pieceColor = p.color.equals("RED") ? RED_COLOR : Color.BLACK;
            gc.setStroke(pieceColor);
            gc.setLineWidth(2);
            gc.strokeOval(x - radius, y - radius, 2 * radius, 2 * radius);
            
            // 绘制内圈
            gc.setLineWidth(1);
            gc.strokeOval(x - radius + 3, y - radius + 3, 2 * radius - 6, 2 * radius - 6);

            // 绘制棋子文字
            gc.setFill(pieceColor);
            gc.setFont(p.color.equals("RED") ? redPieceFont : blackPieceFont);
            gc.fillText(p.type, x, y);
        }
    }

    @FXML
    private void handleLoad() {
        String selectedDisplayName = saveFilesListView.getSelectionModel().getSelectedItem();
        if (selectedDisplayName == null) {
            showAlert("提示", "请选择一个存档文件");
            return;
        }

        // 找到对应的存档文件信息
        GameArchiveManager.SaveFileInfo selectedFileInfo = null;
        for (GameArchiveManager.SaveFileInfo info : saveFiles) {
            if (info.getDisplayName().equals(selectedDisplayName)) {
                selectedFileInfo = info;
                break;
            }
        }

        if (selectedFileInfo != null) {
            GameArchiveManager.GameArchiveData loadedData = archiveManager.loadGame(selectedFileInfo.getFileName());
            if (loadedData != null) {
                // 将数据传递回主游戏控制器
                if (mainGameController != null) {
                    mainGameController.loadGameData(loadedData, selectedFileInfo.getFileName());
                }
                
                // 关闭当前窗口
                Stage stage = (Stage) loadButton.getScene().getWindow();
                stage.close();
                
                showAlert("提示", "游戏加载成功！");
            } else {
                showAlert("错误", "加载失败，可能文件已损坏或密钥错误！");
            }
        }
    }

    private Font loadCustomFont(String filename, double size) {
        String resourcePath = "/Resource/" + filename;

        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("错误: 找不到字体资源文件: " + resourcePath);
                // 找不到时，返回一个通用的回退字体
                return Font.font("SimSun", size);
            }
            // 成功加载字体
            return Font.loadFont(is, size);
        } catch (Exception e) {
            System.err.println("加载字体失败: " + filename);
            e.printStackTrace();
            return Font.font("SimSun", size); // 失败的回退
        }
    }
    
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}