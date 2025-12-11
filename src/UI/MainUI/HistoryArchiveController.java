package UI.MainUI;

import UI.MainGameUI.GameMove;
import UI.MainGameUI.MainGameLauncher;
import UI.Models.AudioModel;
import UI.Models.GameArchiveManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static UI.MainUI.MainController.getSessionIdentifier;
import static UI.MainUI.MainController.getUserName;
import static UI.MainUI.MainController.getUserPasswordHash;
import static UI.Models.GetAppPath.getAppPath;
import static UI.Models.ShowAlert.showAlert;

public class HistoryArchiveController {

    @FXML
    private Canvas previewChessBoardCanvas;

    @FXML
    private ImageView AvatarImageView;

    @FXML
    private Label welcomeLabel;

    @FXML
    private ListView<String> saveFilesListView;

    @FXML
    private Button startGameButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button muteButton;

    @FXML
    private Button importButton;

    @FXML
    private Button exportButton;

    private List<GameArchiveManager.SaveFileInfo> saveFiles;
    private GameArchiveManager archiveManager;
    private GameArchiveManager.GameArchiveData currentPreviewData;
    private GameMove lastMove; // 用于高亮显示上一步
    private String currentUserName = getUserName();
    private String sessionIdentifier = getSessionIdentifier();
    
    // 用于存储自定义加载的字体
    private Font redPieceFont;
    private Font blackPieceFont;
    
    private static final int ROWS = 10;
    private static final int COLS = 9;
    private static final int CELL_SIZE = 50;
    private double offsetX;
    private double offsetY;
    private final Color RED_COLOR = Color.web("#8B0000");

    private final String SOUND_ICON = "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z";
    private final String MUTE_ICON = "M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z";


    @FXML
    public void initialize() {
        updateMuteIcon();
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

        // 初始化棋子字体
        redPieceFont = loadCustomFont("fonts/HanYiWeiBeiJian-1.ttf", 26);
        blackPieceFont = loadCustomFont("fonts/HanYiWeiBeiFan-1.ttf", 26);

        // 初始化存档管理器
        String passwordHash = getUserPasswordHash();
        if (passwordHash != null && sessionIdentifier != null && !sessionIdentifier.isEmpty()) {
            archiveManager = new GameArchiveManager(currentUserName, passwordHash);
            loadSaveFiles();
        }
        
        // 计算棋盘偏移量
        int boardWidth = (COLS - 1) * CELL_SIZE;
        int boardHeight = (ROWS - 1) * CELL_SIZE;
        offsetX = (previewChessBoardCanvas.getWidth() - boardWidth) / 2.0;
        offsetY = (previewChessBoardCanvas.getHeight() - boardHeight) / 2.0;

        // 添加列表选择事件监听器
        saveFilesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                previewSelectedSave(newValue);
            }
        });

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
                currentPreviewData = loadedData;
                if (loadedData.moves != null && !loadedData.moves.isEmpty()) {
                    lastMove = loadedData.moves.get(loadedData.moves.size() - 1);
                } else {
                    lastMove = null;
                }
                drawPreviewBoard();
            }
        }
    }

    private void drawPreviewBoard() {
        if (currentPreviewData == null || currentPreviewData.pieces == null) return;
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
        gc.setFont(loadCustomFont("fonts/DuanNingXingShu.ttf", 24));
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

        // 绘制上一步的标记
        if (lastMove != null) {
            drawLastMoveHighlight(gc, lastMove.fromX, lastMove.fromY);
            drawLastMoveHighlight(gc, lastMove.toX, lastMove.toY);
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

    private void drawLastMoveHighlight(GraphicsContext gc, int col, int row) {
        double x = col * CELL_SIZE + offsetX;
        double y = row * CELL_SIZE + offsetY;
        double cornerSize = 10; // The length of each corner line
        double gap = CELL_SIZE / 2.0 - 5; // Gap from the center point

        gc.setStroke(Color.web("#3498db", 0.8)); // A nice blue color
        gc.setLineWidth(3);

        // Top-left corner
        gc.strokeLine(x - gap, y - gap, x - gap + cornerSize, y - gap);
        gc.strokeLine(x - gap, y - gap, x - gap, y - gap + cornerSize);

        // Top-right corner
        gc.strokeLine(x + gap, y - gap, x + gap - cornerSize, y - gap);
        gc.strokeLine(x + gap, y - gap, x + gap, y - gap + cornerSize);

        // Bottom-left corner
        gc.strokeLine(x - gap, y + gap, x - gap + cornerSize, y + gap);
        gc.strokeLine(x - gap, y + gap, x - gap, y + gap - cornerSize);

        // Bottom-right corner
        gc.strokeLine(x + gap, y + gap, x + gap - cornerSize, y + gap);
        gc.strokeLine(x + gap, y + gap, x + gap, y + gap - cornerSize);
    }

    private void drawPieces() {
        GraphicsContext gc = previewChessBoardCanvas.getGraphicsContext2D();
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        double radius = 22;

        for (UI.MainGameUI.ChessPiece p : currentPreviewData.pieces) {
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
    private void handleStartGame() {
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
                // 关闭当前窗口
                Stage stage = (Stage) startGameButton.getScene().getWindow();
                stage.close();
                
                // 启动游戏并加载存档
                try {
                    MainGameLauncher gameLauncher = new MainGameLauncher(currentUserName, sessionIdentifier, loadedData, selectedFileInfo.getFileName());
                    gameLauncher.showMainGameWindow();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("错误", "无法启动游戏: " + e.getMessage());
                }
            } else {
                showAlert("错误", "加载失败，可能文件已损坏或密钥错误！");
            }
        }
    }

    @FXML
    private void handleDelete() {
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
            // 确认删除
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("确认删除");
            alert.setHeaderText(null);
            
            String contentText = "确定要删除存档 \"" + selectedDisplayName + "\" 吗？此操作不可撤销。";
            try {
                Font customFont = Font.loadFont(new File("Resource/fonts/SIMFANG.TTF").toURI().toString(), 14);
                if (customFont != null) {
                    Label contentLabel = new Label(contentText);
                    contentLabel.setFont(customFont);
                    alert.getDialogPane().setContent(contentLabel);
                } else {
                    alert.setContentText(contentText); // Fallback
                }
            } catch (Exception e) {
                e.printStackTrace();
                alert.setContentText(contentText); // Fallback
            }
            
            ButtonType buttonTypeYes = new ButtonType("确定");
            ButtonType buttonTypeNo = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
            
            // 创建final变量供lambda表达式使用
            final GameArchiveManager.SaveFileInfo finalSelectedFileInfo = selectedFileInfo;
            final String finalSelectedDisplayName = selectedDisplayName;
            
            alert.showAndWait().ifPresent(result -> {
                if (result == buttonTypeYes) {
                    // 执行删除操作
                    File saveFile = new File(GameArchiveManager.getUserSaveDir(currentUserName), finalSelectedFileInfo.getFileName());
                    if (saveFile.delete()) {
                        // 从列表中移除
                        saveFilesListView.getItems().remove(finalSelectedDisplayName);
                        saveFiles.remove(finalSelectedFileInfo);
                        showAlert("提示", "存档删除成功！");
                        
                        // 清空预览
                        previewChessBoardCanvas.getGraphicsContext2D().clearRect(0, 0, previewChessBoardCanvas.getWidth(), previewChessBoardCanvas.getHeight());
                        
                        // 选择下一个项目或清空选择
                        if (!saveFilesListView.getItems().isEmpty()) {
                            saveFilesListView.getSelectionModel().select(0);
                            // 手动触发预览更新
                            String nextSelected = saveFilesListView.getSelectionModel().getSelectedItem();
                            if (nextSelected != null) {
                                previewSelectedSave(nextSelected);
                            }
                        }
                    } else {
                        showAlert("错误", "存档删除失败！");
                    }
                }
            });
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();

        MainLauncher mainLauncher = new MainLauncher(currentUserName, MainController.getSessionIdentifier());
        mainLauncher.showMainWindow();
    }

    @FXML
    private void handleImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导入存档");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("象棋存档文件", "*.xiangqi"),
            new FileChooser.ExtensionFilter("所有文件", "*.*")
        );
        File selectedFile = fileChooser.showOpenDialog(importButton.getScene().getWindow());

        if (selectedFile != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                Gson gson = new Gson();
                GameArchiveManager.GameArchiveData importedData = gson.fromJson(reader, GameArchiveManager.GameArchiveData.class);

                if (importedData != null && importedData.pieces != null && importedData.currentPlayerColor != null) {
                    // 使用当前用户的密钥保存（重新加密）
                    boolean success = archiveManager.saveGame(
                        importedData.pieces,
                        importedData.moves,
                        importedData.currentPlayerColor
                    );

                    if (success) {
                        showAlert("成功", "存档已成功导入并使用您的密钥保存。");
                        // 重新加载存档列表
                        loadSaveFiles();
                        // 默认选择新导入的存档（它将是列表中的第一个）
                        if (!saveFilesListView.getItems().isEmpty()) {
                            saveFilesListView.getSelectionModel().select(0);
                        }
                    } else {
                        showAlert("错误", "导入存档失败。");
                    }
                } else {
                    showAlert("错误", "文件内容无效或格式不正确。");
                }
            } catch (IOException e) {
                showAlert("错误", "读取文件时出错: " + e.getMessage());
                e.printStackTrace();
            } catch (JsonSyntaxException e) {
                showAlert("错误", "JSON 解析失败，请确保文件是有效的未加密存档。");
                e.printStackTrace();
            } catch (Exception e) {
                showAlert("错误", "发生未知错误: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleExport() {
        String selectedDisplayName = saveFilesListView.getSelectionModel().getSelectedItem();
        if (selectedDisplayName == null) {
            showAlert("提示", "请选择一个存档文件进行导出");
            return;
        }

        GameArchiveManager.SaveFileInfo selectedFileInfo = null;
        for (GameArchiveManager.SaveFileInfo info : saveFiles) {
            if (info.getDisplayName().equals(selectedDisplayName)) {
                selectedFileInfo = info;
                break;
            }
        }

        if (selectedFileInfo != null) {
            // 1. 解密存档
            GameArchiveManager.GameArchiveData archiveData = archiveManager.loadGame(selectedFileInfo.getFileName());
            if (archiveData == null) {
                showAlert("错误", "无法加载（解密）存档，导出失败。");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("导出为无加密存档");
            fileChooser.setInitialFileName(selectedFileInfo.getFileName().replace(".json", ".xiangqi"));
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("象棋存档文件", "*.xiangqi"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
            );
            File selectedFile = fileChooser.showSaveDialog(exportButton.getScene().getWindow());

            if (selectedFile != null) {
                // 2. 转换为JSON并写入文件
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String jsonText = gson.toJson(archiveData);

                try (FileWriter writer = new FileWriter(selectedFile)) {
                    writer.write(jsonText);
                    showAlert("成功", "存档已成功导出为无加密的象棋存档文件！");
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("错误", "导出存档时发生文件写入错误！");
                }
            }
        }
    }

    private Font loadCustomFont(String filename, double size) {
        String resourcePath = "Resource/" + filename;

        try (InputStream is = HistoryArchiveController.class.getClassLoader().getResourceAsStream(resourcePath)) {
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
