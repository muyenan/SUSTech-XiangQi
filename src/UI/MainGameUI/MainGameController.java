package UI.MainGameUI;

import UI.MainUI.MainLauncher;
import UI.Models.GetAppPath;
import UI.Models.GameArchiveManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import static UI.MainUI.MainController.getSessionIdentifier;
import static UI.MainUI.MainController.getUserName;
import static UI.MainUI.MainController.getUserPasswordHash;
import static UI.Models.GetAppPath.getAppPath;
import static UI.Models.ShowAlert.showAlert;

public class MainGameController {

    @FXML
    private Canvas chessBoardCanvas;

    @FXML
    private ImageView AvatarImageView;

    @FXML
    private Button BackMainButton;

    @FXML
    private Label welcomeLabel, SignUpLabel, LastLoginLabel, turnLabel;

    // 常量定义
    private static final int ROWS = 10;
    private static final int COLS = 9;
    private static final int CELL_SIZE = 50;

    // 用于存储自定义加载的字体
    private Font redPieceFont;
    private Font blackPieceFont;

    private String currentUserName = getUserName(); // 确定的用户名 (显示用)
    private String sessionIdentifier = getSessionIdentifier(); // 用户的唯一ID (校验用，游客为null)
    private String lastLoginTime = null; // 上次登录时间

    // 棋子数据
    private ChessPiece[] pieces;
    private MoveRuleValidator ruleValidator; // 规则校验器

    // 游戏状态变量
    private ChessPiece selectedPiece = null;
    private String currentPlayerColor = "RED";
    private double offsetX;
    private double offsetY;
    
    // 新增：用于存储可移动位置
    private boolean[][] validMoves = new boolean[COLS][ROWS];
    
    // 游戏历史记录
    private List<GameMove> gameMoves = new ArrayList<>();
    
    // 当前加载的存档文件名（如果有的话）
    private String currentSaveFileName = null;

    @FXML
    public void initialize() {

        String userName = currentUserName;
        String userValidationId = sessionIdentifier; // 使用会话标识符进行校验

        welcomeLabel.setText("欢迎，" + userName);

        if (userValidationId != null && !userValidationId.isEmpty()) {
            String registrationTime = getRegistrationTimeFromConfig(userName);

            // 显示上次登录时间（优先使用传递过来的时间，否则从配置文件读取）
            if (this.lastLoginTime != null) {
                if (this.lastLoginTime.isEmpty()) {
                    LastLoginLabel.setText("上次登录: 无记录");
                } else {
                    LastLoginLabel.setText("上次登录: " + this.lastLoginTime);
                }
            } else {
                String lastLoginTime = getLastLoginFromConfig(userName);
                if (lastLoginTime == null || lastLoginTime.isEmpty() || lastLoginTime.equals("无记录")) {
                    LastLoginLabel.setText("上次登录: 无记录");
                } else {
                    LastLoginLabel.setText("上次登录: " + lastLoginTime);
                }
            }
            SignUpLabel.setText("注册时间: " + registrationTime);
        } else {
            LastLoginLabel.setText("本次登录: 临时访问");
            SignUpLabel.setText("注册时间: 临时访问");
        }

        // 计算棋盘偏移量 (用于居中 Canvas)
        int boardWidth = (COLS - 1) * CELL_SIZE;
        int boardHeight = (ROWS - 1) * CELL_SIZE;
        offsetX = (chessBoardCanvas.getWidth() - boardWidth) / 2.0;
        offsetY = (chessBoardCanvas.getHeight() - boardHeight) / 2.0;

        redPieceFont = loadCustomFont("fonts/HanYiWeiBeiJian-1.ttf", 26);
        blackPieceFont = loadCustomFont("fonts/HanYiWeiBeiFan-1.ttf", 26);

        // 初始化棋盘和规则
        initializePieces();
        ruleValidator = new MoveRuleValidator(pieces);

        updateTurnDisplay(); // 更新回合显示
        drawBoard();
        drawPieces();

        chessBoardCanvas.setOnMouseClicked(this::handleCanvasClick);

        // 为注册用户创建初始存档
        if (userValidationId != null && !userValidationId.isEmpty()) {
            String passwordHash = getUserPasswordHash();
            if (passwordHash != null && !passwordHash.isEmpty()) {
                GameArchiveManager archiveManager = new GameArchiveManager(currentUserName, passwordHash);
                // 只有在当前没有加载存档文件时才创建初始存档
                if (currentSaveFileName == null || currentSaveFileName.isEmpty()) {
                    boolean success = archiveManager.saveGame(pieces, gameMoves);
                    if (success) {
                        // 获取刚创建的存档文件名
                        List<GameArchiveManager.SaveFileInfo> saveFiles = archiveManager.getSaveFiles();
                        if (!saveFiles.isEmpty()) {
                            currentSaveFileName = saveFiles.get(0).getFileName();
                            System.out.println("初始存档创建成功: " + currentSaveFileName);
                        }
                    } else {
                        System.err.println("初始存档创建失败");
                    }
                }
            }
        }

        // 显示头像
        try {
            if (userValidationId != null && !userValidationId.isEmpty()) {
                File avatarFile = new File(getAppPath() + "/Accounts/" + userName + "/user_avatar.png");
                if (avatarFile.exists()) {
                    Image newAvatar = new Image(avatarFile.toURI().toString());
                    AvatarImageView.setImage(newAvatar);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 初始化棋子布局
    private void initializePieces() {
        pieces = new ChessPiece[]{
                // 红方简体字
                new ChessPiece("帅", "RED", 4, 9),
                new ChessPiece("仕", "RED", 3, 9),
                new ChessPiece("仕", "RED", 5, 9),
                new ChessPiece("相", "RED", 2, 9),
                new ChessPiece("相", "RED", 6, 9),
                new ChessPiece("车", "RED", 0, 9),
                new ChessPiece("车", "RED", 8, 9),
                new ChessPiece("马", "RED", 1, 9),
                new ChessPiece("马", "RED", 7, 9),
                new ChessPiece("炮", "RED", 1, 7),
                new ChessPiece("炮", "RED", 7, 7),
                new ChessPiece("兵", "RED", 0, 6),
                new ChessPiece("兵", "RED", 2, 6),
                new ChessPiece("兵", "RED", 4, 6),
                new ChessPiece("兵", "RED", 6, 6),
                new ChessPiece("兵", "RED", 8, 6),

                // 黑方繁体字
                new ChessPiece("将", "BLACK", 4, 0),
                new ChessPiece("士", "BLACK", 3, 0),
                new ChessPiece("士", "BLACK", 5, 0),
                new ChessPiece("象", "BLACK", 2, 0),
                new ChessPiece("象", "BLACK", 6, 0),
                new ChessPiece("车", "BLACK", 0, 0),
                new ChessPiece("车", "BLACK", 8, 0),
                new ChessPiece("马", "BLACK", 1, 0),
                new ChessPiece("马", "BLACK", 7, 0),
                new ChessPiece("炮", "BLACK", 1, 2),
                new ChessPiece("炮", "BLACK", 7, 2),
                new ChessPiece("卒", "BLACK", 0, 3),
                new ChessPiece("卒", "BLACK", 2, 3),
                new ChessPiece("卒", "BLACK", 4, 3),
                new ChessPiece("卒", "BLACK", 6, 3),
                new ChessPiece("卒", "BLACK", 8, 3)
        };
    }

    // 更新当前回合的文本显示
    private void updateTurnDisplay() {
        if (turnLabel != null) {
            String colorName = currentPlayerColor.equals("RED") ? "红方" : "黑方";
            turnLabel.setText("当前回合: " + colorName);
            turnLabel.setTextFill(currentPlayerColor.equals("RED") ? Color.RED : Color.BLACK);
        }
    }

    // 切换回合
    private void switchTurn() {
        currentPlayerColor = currentPlayerColor.equals("RED") ? "BLACK" : "RED";
        updateTurnDisplay();
    }

    private void handleCanvasClick(MouseEvent event) {
        int clickedX = (int) Math.round((event.getX() - offsetX) / CELL_SIZE);
        int clickedY = (int) Math.round((event.getY() - offsetY) / CELL_SIZE);

        if (clickedX < 0 || clickedX >= COLS || clickedY < 0 || clickedY >= ROWS) {
            return;
        }

        ChessPiece clickedPiece = getPieceAt(clickedX, clickedY);

        if (selectedPiece == null) {
            // 没有选中棋子，尝试选中
            if (clickedPiece != null) {
                if (clickedPiece.color.equals(currentPlayerColor)) {
                    selectedPiece = clickedPiece;
                    calculateValidMoves(); // 计算可移动位置
                    drawBoard();
                    drawPieces();
                    System.out.println("选中棋子: " + selectedPiece);
                } else {
                    System.out.println("现在是 " + (currentPlayerColor.equals("RED") ? "红方" : "黑方") + " 回合，请移动您的棋子。");
                }
            }
        } else {
            // 已经选中棋子，尝试移动或重新选中
            if (clickedPiece == selectedPiece) {
                // 点击同一棋子，取消选择
                selectedPiece = null;
                clearValidMoves(); // 清除可移动位置
                drawBoard();
                drawPieces();
                System.out.println("取消选择");
            } else if (clickedPiece != null && clickedPiece.color.equals(selectedPiece.color)) {
                // 点击己方其他棋子，更换选择
                selectedPiece = clickedPiece;
                calculateValidMoves(); // 计算可移动位置
                drawBoard();
                drawPieces();
                System.out.println("更换选择: " + selectedPiece);
            } else {
                // 尝试移动到(clickedX, clickedY)
                if (validMoves[clickedX][clickedY]) { // 只有当位置有效时才移动
                    makeMove(selectedPiece, clickedX, clickedY);
                    switchTurn();
                } else {
                    System.out.println("非法移动!");
                }

                // 无论移动是否成功，都取消选择
                selectedPiece = null;
                clearValidMoves(); // 清除可移动位置
                drawBoard();
                drawPieces();
                
                // 每次移动后自动保存游戏
                autoSaveGame();
            }
        }
    }

    // 新增：计算可移动位置
    private void calculateValidMoves() {
        // 先清空之前的有效移动位置
        clearValidMoves();
        
        // 如果没有选中棋子，直接返回
        if (selectedPiece == null) {
            return;
        }
        
        // 遍历棋盘上的所有位置
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                // 检查从选中棋子的位置到当前位置的移动是否合法
                if (ruleValidator.isValidMove(selectedPiece.x, selectedPiece.y, x, y)) {
                    validMoves[x][y] = true;
                }
            }
        }
    }
    
    // 新增：清除可移动位置
    private void clearValidMoves() {
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                validMoves[x][y] = false;
            }
        }
    }

    // 实际执行移动并更新棋子数组 (保持不变)
    private void makeMove(ChessPiece pieceToMove, int newX, int newY) {
        ChessPiece targetPiece = getPieceAt(newX, newY);
        if (targetPiece != null) {
            pieces = Arrays.stream(pieces)
                    .filter(p -> p != targetPiece)
                    .toArray(ChessPiece[]::new);
        }

        // 记录移动历史
        gameMoves.add(new GameMove(pieceToMove.x, pieceToMove.y, newX, newY, System.currentTimeMillis()));

        pieceToMove.x = newX;
        pieceToMove.y = newY;
        ruleValidator = new MoveRuleValidator(pieces);
        System.out.println(pieceToMove.type + " 移动到 (" + newX + ", " + newY + ")");
    }

    // 获取指定坐标的棋子
    private ChessPiece getPieceAt(int x, int y) {
        return Arrays.stream(pieces)
                .filter(p -> p != null && p.x == x && p.y == y)
                .findFirst()
                .orElse(null);
    }

    // 自动保存游戏
    private void autoSaveGame() {
        // 仅对注册用户自动保存
        if (sessionIdentifier == null || sessionIdentifier.isEmpty()) {
            return;
        }

        String passwordHash = getUserPasswordHash();
        if (passwordHash == null || passwordHash.isEmpty()) {
            return;
        }

        GameArchiveManager archiveManager = new GameArchiveManager(currentUserName, passwordHash);
        boolean success;
        
        // 如果当前有加载的存档文件，则更新该文件，否则创建新文件
        if (currentSaveFileName != null && !currentSaveFileName.isEmpty()) {
            success = archiveManager.updateGame(currentSaveFileName, pieces, gameMoves);
        } else {
            success = archiveManager.saveGame(pieces, gameMoves);
            // 如果是新创建的存档，更新currentSaveFileName
            if (success) {
                List<GameArchiveManager.SaveFileInfo> saveFiles = archiveManager.getSaveFiles();
                if (!saveFiles.isEmpty()) {
                    currentSaveFileName = saveFiles.get(0).getFileName();
                }
            }
        }

        if (success) {
            System.out.println("游戏自动保存成功");
        } else {
            System.err.println("游戏自动保存失败");
        }
    }

    // 绘制棋盘
    private void drawBoard() {
        GraphicsContext gc = chessBoardCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, chessBoardCanvas.getWidth(), chessBoardCanvas.getHeight());
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        int boardWidth = (COLS - 1) * CELL_SIZE;
        int boardHeight = (ROWS - 1) * CELL_SIZE;

        // 背景色
        gc.setFill(Color.rgb(220, 179, 92));
        gc.fillRect(offsetX, offsetY, boardWidth, boardHeight);

        // 绘制可移动位置的高亮效果
        gc.setFill(Color.LIGHTBLUE); // 保持颜色不变
        gc.setGlobalAlpha(0.7); // 提高不透明度，使其更清晰可见
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                if (validMoves[x][y]) {
                    double posX = x * CELL_SIZE + offsetX;
                    double posY = y * CELL_SIZE + offsetY;
                    gc.fillOval(posX - 10, posY - 10, 20, 20);
                }
            }
        }
        gc.setGlobalAlpha(1.0); // 恢复透明度

        // 横线
        for (int i = 0; i < ROWS; i++) {
            double y = i * CELL_SIZE + offsetY;
            gc.strokeLine(offsetX, y, boardWidth + offsetX, y);
        }

        // 竖线
        for (int i = 0; i < COLS; i++) {
            double x = i * CELL_SIZE + offsetX;
            if (i == 0 || i == COLS - 1) {
                gc.strokeLine(x, offsetY, x, boardHeight + offsetY);
            } else {
                gc.strokeLine(x, offsetY, x, 4 * CELL_SIZE + offsetY);
                gc.strokeLine(x, 5 * CELL_SIZE + offsetY, x, boardHeight + offsetY);
            }
        }

        // 九宫格
        gc.strokeLine(3 * CELL_SIZE + offsetX, 7 * CELL_SIZE + offsetY, 5 * CELL_SIZE + offsetX, 9 * CELL_SIZE + offsetY);
        gc.strokeLine(5 * CELL_SIZE + offsetX, 7 * CELL_SIZE + offsetY, 3 * CELL_SIZE + offsetX, 9 * CELL_SIZE + offsetY);
        gc.strokeLine(3 * CELL_SIZE + offsetX, 0 * CELL_SIZE + offsetY, 5 * CELL_SIZE + offsetX, 2 * CELL_SIZE + offsetY);
        gc.strokeLine(5 * CELL_SIZE + offsetX, 0 * CELL_SIZE + offsetY, 3 * CELL_SIZE + offsetX, 2 * CELL_SIZE + offsetY);

        // 楚河汉界
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("FangSong_GB2312", 24));

        // 确保文字水平和垂直居中
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        // 棋盘中线的X坐标 (第4列)
        double centerX = 4.0 * CELL_SIZE + offsetX;
        // 楚河汉界的Y坐标 (4.5行)
        double textY = 4.5 * CELL_SIZE + offsetY;

        gc.fillText("楚 河 汉 界", centerX, textY);

        // 炮/兵 的标记点
        int[] paoBingCols = {1, 7, 0, 2, 4, 6, 8};
        int[] paoRows = {2, 7};
        int[] bingZuoRows = {3, 6};

        for (int x : paoBingCols) {
            if (x == 1 || x == 7) {
                for (int y : paoRows) drawCross(gc, x, y);
            }
            if (x % 2 == 0 || x == 4) { // 兵卒位
                for (int y : bingZuoRows) drawCross(gc, x, y);
            }
        }
    }

    private void drawCross(GraphicsContext gc, int col, int row) {
        double x = col * CELL_SIZE + offsetX;
        double y = row * CELL_SIZE + offsetY;
        double len = 8;
        double shortLen = 3;

        gc.setLineWidth(1.5);
        gc.setStroke(Color.BLACK);

        if (col > 0) {
            gc.strokeLine(x - len, y - shortLen, x - len, y + shortLen);
        }
        if (col < COLS - 1) {
            gc.strokeLine(x + len, y - shortLen, x + len, y + shortLen);
        }
        if (row > 0) {
            gc.strokeLine(x - shortLen, y - len, x + shortLen, y - len);
        }
        if (row < ROWS - 1) {
            gc.strokeLine(x - shortLen, y + len, x + shortLen, y + len);
        }
    }

    // 棋子绘制
    private void drawPieces() {
        GraphicsContext gc = chessBoardCanvas.getGraphicsContext2D();

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        double radius = 22;

        for (ChessPiece p : pieces) {
            double x = p.x * CELL_SIZE + offsetX;
            double y = p.y * CELL_SIZE + offsetY;

            // 绘制棋子背景圆
            gc.setFill(Color.LIGHTYELLOW);
            gc.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);

            // 绘制棋子边框
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.strokeOval(x - radius, y - radius, 2 * radius, 2 * radius);

            // 绘制选中标记
            if (p == selectedPiece) {
                gc.setStroke(Color.BLUE);
                gc.setLineWidth(3);
                gc.strokeOval(x - radius - 3, y - radius - 3, 2 * radius + 6, 2 * radius + 6);
                gc.setStroke(Color.BLACK); // 恢复画笔颜色
                gc.setLineWidth(2);
            }
            
            // 如果这个位置是可以吃的棋子，绘制红色边框
            if (validMoves[p.x][p.y]) {
                gc.setStroke(Color.RED);
                gc.setLineWidth(3);
                gc.strokeOval(x - radius - 5, y - radius - 5, 2 * radius + 10, 2 * radius + 10);
                gc.setStroke(Color.BLACK); // 恢复画笔颜色
                gc.setLineWidth(2);
            }

            // 绘制棋子文字
            if (p.color.equals("RED")) {
                gc.setFill(Color.RED);
                // 使用预加载的红方字体
                gc.setFont(redPieceFont);
            } else {
                gc.setFill(Color.BLACK);
                // 使用预加载的黑方字体
                gc.setFont(blackPieceFont);
            }

            gc.fillText(p.type, x, y); // 移除了微调值，确保文字完全居中
        }
    }

    @FXML
    private void handleSaveGame() {
        if (sessionIdentifier == null || sessionIdentifier.isEmpty()) {
            // 游客模式不提示错误，静默返回
            return;
        }

        String passwordHash = getUserPasswordHash();
        if (passwordHash == null) {
            // 静默返回，不提示错误
            return;
        }

        GameArchiveManager archiveManager = new GameArchiveManager(currentUserName, passwordHash);
        boolean success;
        
        // 如果当前有加载的存档文件，则更新该文件，否则创建新文件
        if (currentSaveFileName != null && !currentSaveFileName.isEmpty()) {
            success = archiveManager.updateGame(currentSaveFileName, pieces, gameMoves);
        } else {
            success = archiveManager.saveGame(pieces, gameMoves);
        }

        if (success) {
            // 显示保存成功提示
            showAlert("提示", "游戏保存成功");
            System.out.println("游戏保存成功");
        } else {
            // 显示保存失败提示
            showAlert("错误", "游戏保存失败");
            System.err.println("保存失败");
        }
    }

    @FXML
    private void handleLoadGame() {
        if (sessionIdentifier == null || sessionIdentifier.isEmpty()) {
            showAlert("提示", "游客模式不能加载存档！");
            return;
        }

        String passwordHash = getUserPasswordHash();
        if (passwordHash == null) {
            showAlert("错误", "无法获取用户密钥，加载存档失败！");
            return;
        }

        // 打开新的存档加载界面
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LoadArchive.fxml"));
            Parent root = loader.load();
            
            // 获取控制器并设置主游戏控制器引用
            LoadArchiveController loadController = loader.getController();
            loadController.setMainGameController(this);
            
            Stage stage = new Stage();
            stage.setTitle("加载存档");
            stage.setScene(new Scene(root, 800, 500));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("错误", "无法打开存档加载界面: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToMenu() {
        System.out.println("返回主菜单");
        if (!(sessionIdentifier == null || sessionIdentifier.isEmpty())) {
            // 静默保存游戏，不显示提示框
            handleSaveGame();
        }

        try {
            Stage currentStage = (Stage) BackMainButton.getScene().getWindow();
            currentStage.close();

            MainLauncher mainLauncher = new MainLauncher(currentUserName, sessionIdentifier);
            mainLauncher.showMainWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("未知错误", "请联系管理员!");
        }
    }

    public static class UserSavePath {
        public static String getUserSaveDir(String username) {
            String appPath = GetAppPath.getAppPath();
            File userDir = new File(appPath, "Accounts/" + username + "/Saves");
            if (!userDir.exists()) userDir.mkdirs();
            return userDir.getAbsolutePath();
        }
    }

    public String getLastLoginFromConfig(String userName) {
        String lastLogin = "无记录";
        try {
            File configFile = new File(getAppPath() + "/Accounts/" + userName + "/user.config");
            if (configFile.exists()) {
                Properties prop = new Properties();
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    prop.load(fis);
                    String value = prop.getProperty("last_login");
                    if (value != null && !value.isEmpty()) {
                        lastLogin = value;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            lastLogin = "读取失败";
        }
        return lastLogin;
    }

    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getRegistrationTimeFromConfig(String userName) {
        String registrationTime = "无记录";
        try {
            File configFile = new File(getAppPath() + "/Accounts/" + userName + "/user.config");
            if (configFile.exists()) {
                Properties prop = new Properties();
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    prop.load(fis);
                    String value = prop.getProperty("registration_date");
                    if (value != null && !value.isEmpty()) {
                        registrationTime = value;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            registrationTime = "读取失败";
        }
        return registrationTime;
    }

    private String getCurrentLoginTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    private void updateLastLogin(String userName) {
        File configFile = new File(getAppPath() + "/Accounts/" + userName + "/user.config");
        if (!configFile.exists()) {
            return;
        }

        Properties properties = new Properties();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try (FileInputStream fis = new FileInputStream(configFile)) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        properties.setProperty("last_login", sdf.format(new Date()));

        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            properties.store(fos, "用户配置文件 - 更新登录时间");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String RandomCodeGenerator() {
        Random random = new Random();
        int num = random.nextInt(1000000);
        String result = String.format("%06d", num);
        return result;
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

    // 用于接收加载的游戏数据
    public void loadGameData(GameArchiveManager.GameArchiveData data) {
        if (data != null) {
            pieces = data.pieces;
            gameMoves = data.moves != null ? data.moves : new ArrayList<>();
            ruleValidator = new MoveRuleValidator(pieces);
            drawBoard();
            drawPieces();
        }
    }
    
    // 用于接收加载的游戏数据和文件名
    public void loadGameData(GameArchiveManager.GameArchiveData data, String fileName) {
        if (data != null) {
            pieces = data.pieces;
            gameMoves = data.moves != null ? data.moves : new ArrayList<>();
            ruleValidator = new MoveRuleValidator(pieces);
            currentSaveFileName = fileName; // 记录当前加载的存档文件名
            drawBoard();
            drawPieces();
        }
    }
}