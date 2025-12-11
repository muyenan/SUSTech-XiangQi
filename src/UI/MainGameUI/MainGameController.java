package UI.MainGameUI;

import UI.MainUI.MainLauncher;
import UI.Models.AudioModel;
import UI.Models.GetAppPath;
import UI.Models.GameArchiveManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
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

    @FXML private Canvas chessBoardCanvas;
    @FXML private ImageView AvatarImageView;
    @FXML private Button BackMainButton;
    @FXML private Label welcomeLabel, SignUpLabel, LastLoginLabel, turnLabel;
    @FXML private StackPane victoryOverlayPane;
    @FXML private Label victoryLabel;
    @FXML private Button replayButton;
    @FXML private StackPane checkAlertPane;
    @FXML private Label checkAlertLabel;
    @FXML private Button muteButton;


    // 常量定义
    private static final int ROWS = 10;
    private static final int COLS = 9;
    private static final int CELL_SIZE = 50;
    private final Color RED_COLOR = Color.web("#8B0000");
    
    // 为游客模式定义常量，用于临时存档
    private static final String GUEST_USER = "__GUEST__";
    private static final String GUEST_PASS_HASH = "temporary_guest_password_hash_for_undo";

    // 用于存储自定义加载的字体
    private Font redPieceFont;
    private Font blackPieceFont;
    private Font redCheckFont;
    private Font blackCheckFont;

    private String currentUserName = getUserName(); // 确定的用户名 (显示用)
    private String sessionIdentifier = getSessionIdentifier(); // 用户的唯一ID (校验用，游客为null)
    private String lastLoginTime = null; // 上次登录时间

    // 棋子数据
    private ChessPiece[] pieces;
    private ChessPiece[] initialPieces; // 保存棋盘初始状态
    private MoveRuleValidator ruleValidator; // 规则校验器

    // 游戏状态变量
    private ChessPiece selectedPiece = null;
    private String currentPlayerColor = "RED";
    private double offsetX;
    private double offsetY;
    private boolean isGameOver = false; // 游戏结束标志
    
    // 用于存储可移动位置
    private boolean[][] validMoves = new boolean[COLS][ROWS];
    
    // 游戏历史记录
    private List<GameMove> gameMoves = new ArrayList<>();
    
    // 用于高亮显示上一步
    private GameMove lastMove = null;
    
    // 当前加载的存档文件名（如果有的话）
    private String currentSaveFileName = null;

    private final String SOUND_ICON = "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z";
    private final String MUTE_ICON = "M16.5 12c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z";


    @FXML
    public void initialize() {

        updateMuteIcon();

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

            // 显示头像
            try {
                File avatarFile = new File(getAppPath() + "/Accounts/" + sessionIdentifier + "/user_avatar.png");
                if (avatarFile.exists()) {
                    Image newAvatar = new Image(avatarFile.toURI().toString());
                    AvatarImageView.setImage(newAvatar);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LastLoginLabel.setText("本次登录: 临时访问");
            SignUpLabel.setText("注册时间: 临时访问");

            // 显示头像
            try {
                File avatarFile = new File(getAppPath() + "/Accounts/" + sessionIdentifier + "/user_avatar.png");
                if (avatarFile.exists()) {
                    Image newAvatar = new Image(avatarFile.toURI().toString());
                    AvatarImageView.setImage(newAvatar);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 计算棋盘偏移量 (用于居中 Canvas)
        int boardWidth = (COLS - 1) * CELL_SIZE;
        int boardHeight = (ROWS - 1) * CELL_SIZE;
        offsetX = (chessBoardCanvas.getWidth() - boardWidth) / 2.0;
        offsetY = (chessBoardCanvas.getHeight() - boardHeight) / 2.0;

        // 为棋子和将军特效分别加载字体
        redPieceFont = loadCustomFont("fonts/HanYiWeiBeiJian-1.ttf", 26);
        blackPieceFont = loadCustomFont("fonts/HanYiWeiBeiFan-1.ttf", 26);
        redCheckFont = loadCustomFont("fonts/HanYiWeiBeiJian-1.ttf", 60);
        blackCheckFont = loadCustomFont("fonts/HanYiWeiBeiFan-1.ttf", 60);

        // 初始化棋盘和规则
        initializePieces();
        ruleValidator = new MoveRuleValidator(pieces);

        updateTurnDisplay(); // 更新回合显示
        drawBoard();
        drawPieces();

        chessBoardCanvas.setOnMouseClicked(this::handleCanvasClick);
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
        // 保存初始棋子状态用于回放
        initialPieces = Arrays.stream(pieces).map(p -> new ChessPiece(p)).toArray(ChessPiece[]::new);
    }

    // 更新当前回合的文本显示
    private void updateTurnDisplay() {
        if (turnLabel != null) {
            if (isGameOver) {
                turnLabel.setText("游戏结束");
                turnLabel.setTextFill(Color.GRAY);
            } else {
                String colorName = currentPlayerColor.equals("RED") ? "红方" : "黑方";
                turnLabel.setText("当前回合: " + colorName);
                turnLabel.setTextFill(currentPlayerColor.equals("RED") ? RED_COLOR : Color.BLACK);
            }
        }
    }

    // 切换回合
    private void switchTurn() {
        currentPlayerColor = currentPlayerColor.equals("RED") ? "BLACK" : "RED";
        updateTurnDisplay();
    }

    private void handleCanvasClick(MouseEvent event) {
        if (isGameOver) {
            System.out.println("游戏已结束，无法移动棋子。");
            return;
        }

        int clickedX = (int) Math.round((event.getX() - offsetX) / CELL_SIZE);
        int clickedY = (int) Math.round((event.getY() - offsetY) / CELL_SIZE);

        if (clickedX < 0 || clickedX >= COLS || clickedY < 0 || clickedY >= ROWS) {
            return;
        }

        ChessPiece clickedPiece = getPieceAt(clickedX, clickedY);
        boolean moved = false;

        if (selectedPiece == null) {
            if (clickedPiece != null && clickedPiece.color.equals(currentPlayerColor)) {
                selectedPiece = clickedPiece;
                calculateValidMoves();
                drawBoard();
                drawPieces();
            }
        } else {
            if (clickedPiece == selectedPiece) {
                selectedPiece = null;
                clearValidMoves();
                drawBoard();
                drawPieces();
            } else if (clickedPiece != null && clickedPiece.color.equals(selectedPiece.color)) {
                selectedPiece = clickedPiece;
                calculateValidMoves();
                drawBoard();
                drawPieces();
            } else {
                MoveRuleValidator.MoveValidationResult result = ruleValidator.checkMove(selectedPiece.x, selectedPiece.y, clickedX, clickedY);

                switch (result) {
                    case VALID:
                        makeMove(selectedPiece, clickedX, clickedY);
                        if (!isGameOver) {
                            String opponentColor = currentPlayerColor.equals("RED") ? "BLACK" : "RED";
                            if (isStalemate(opponentColor)) {
                                // 无路可走，判断是绝杀还是困毙
                                if (ruleValidator.isKingInCheck(opponentColor, pieces)) {
                                    // 被将军，是绝杀
                                    handleGameEnd(getKing(opponentColor), true);
                                } else {
                                    // 没有被将军，是困毙
                                    handleGameEndDraw();
                                }
                            } else {
                                switchTurn();
                            }
                        }
                        moved = true;
                        break;
                    case INVALID_SELF_CHECK:
                        showAlert("提示", "不能“自毙”！此移动将使己方被将军。");
                        break;
                    case INVALID_RULE:
                        System.out.println("非法移动!");
                        break;
                }

                selectedPiece = null;
                clearValidMoves();
                drawBoard();
                drawPieces();

                if (moved && !isGameOver) {
                    autoSaveGame();
                }
            }
        }
    }

    // 计算可移动位置
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
    
    // 清除可移动位置
    private void clearValidMoves() {
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                validMoves[x][y] = false;
            }
        }
    }

    // 实际执行移动并更新棋子数组
    private void makeMove(ChessPiece pieceToMove, int newX, int newY) {
        ChessPiece targetPiece = getPieceAt(newX, newY);
        ChessPiece capturedPiece = null;
        if (targetPiece != null) {
            capturedPiece = targetPiece;
            pieces = Arrays.stream(pieces)
                    .filter(p -> p != targetPiece)
                    .toArray(ChessPiece[]::new);
        }

        // 记录移动历史
        GameMove move = new GameMove(pieceToMove.x, pieceToMove.y, newX, newY, System.currentTimeMillis(), pieceToMove.type, pieceToMove.color, capturedPiece);
        gameMoves.add(move);
        lastMove = move;

        pieceToMove.x = newX;
        pieceToMove.y = newY;
        ruleValidator = new MoveRuleValidator(pieces);
        System.out.println(pieceToMove.type + " 移动到 (" + newX + ", " + newY + ")");

        // 检查胜利条件
        if (capturedPiece != null && (capturedPiece.type.equals("帅") || capturedPiece.type.equals("将"))) {
            handleGameEnd(capturedPiece, false);
        } else {
            // 检查是否吃子，如果吃子则显示吃子特效
            if (capturedPiece != null) {
                handleCaptureEffect(pieceToMove.color, newX, newY);
            }
            
            // 检查是否将军
            String opponentColor = pieceToMove.color.equals("RED") ? "BLACK" : "RED";
            if (ruleValidator.isKingInCheck(opponentColor, pieces)) {
                handleCheckEffect(pieceToMove.color);
            }
        }
    }

    private void handleGameEnd(ChessPiece king, boolean isStalemate) {
        isGameOver = true;
        updateTurnDisplay();
        String winner;
        String victoryMessage;

        if (isStalemate) {
            winner = king.color.equals("RED") ? "黑方" : "红方";
            victoryMessage = winner + "获胜 (绝杀)";
        } else {
            winner = king.color.equals("RED") ? "黑方" : "红方";
            victoryMessage = winner + "获胜";
        }
        
        victoryLabel.setText(victoryMessage);
        
        // 移除所有可能的颜色样式
        victoryLabel.getStyleClass().removeAll("victory-title-red", "victory-title-black", "draw-title");
        replayButton.getStyleClass().removeAll("victory-button-red", "victory-button-black");

        if ("红方".equals(winner)) {
            victoryLabel.getStyleClass().add("victory-title-red");
            replayButton.getStyleClass().add("victory-button-red");
        } else {
            victoryLabel.getStyleClass().add("victory-title-black");
            replayButton.getStyleClass().add("victory-button-black");
        }
        victoryOverlayPane.setVisible(true);
    }

    private void handleGameEndDraw() {
        isGameOver = true;
        updateTurnDisplay();
        String victoryMessage = "和棋 (困毙)";

        victoryLabel.setText(victoryMessage);

        // 移除所有可能的颜色样式并添加和棋样式
        victoryLabel.getStyleClass().removeAll("victory-title-red", "victory-title-black");
        victoryLabel.getStyleClass().add("draw-title");
        replayButton.getStyleClass().removeAll("victory-button-red", "victory-button-black");

        victoryOverlayPane.setVisible(true);
    }

    @FXML
    private void handleReplay() {
        victoryOverlayPane.setVisible(false);
        // 关闭当前游戏窗口并打开回放窗口
        try {
            Stage currentGameStage = (Stage) chessBoardCanvas.getScene().getWindow();
            currentGameStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("ReplayGame.fxml"));
            Parent root = loader.load();

            ReplayGameController replayController = loader.getController();
            replayController.initializeData(initialPieces, gameMoves);

            Stage replayStage = new Stage();
            replayStage.setTitle("对局回放");
            replayStage.setScene(new Scene(root, 770, 500));
            replayStage.setResizable(false);
            replayStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("错误", "无法加载回放界面: " + e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        victoryOverlayPane.setVisible(false);
        handleBackToMenu();
    }


    // 获取指定坐标的棋子
    private ChessPiece getPieceAt(int x, int y) {
        return Arrays.stream(pieces)
                .filter(p -> p != null && p.x == x && p.y == y)
                .findFirst()
                .orElse(null);
    }
    
    private ChessPiece getKing(String color) {
        String kingType = color.equals("RED") ? "帅" : "将";
        return Arrays.stream(pieces)
                .filter(p -> p.type.equals(kingType) && p.color.equals(color))
                .findFirst()
                .orElse(null);
    }

    // 自动保存游戏
    private void autoSaveGame() {
        if (isGameOver || gameMoves.isEmpty()) return; // 游戏结束后或没有移动则不保存

        String effectiveUserName;
        String effectivePasswordHash;
        boolean isGuest = (sessionIdentifier == null || sessionIdentifier.isEmpty());

        if (isGuest) {
            effectiveUserName = GUEST_USER;
            effectivePasswordHash = GUEST_PASS_HASH;
        } else {
            effectiveUserName = currentUserName;
            effectivePasswordHash = getUserPasswordHash();
        }

        if (effectivePasswordHash == null || effectivePasswordHash.isEmpty()) {
            return; // 无法在没有密钥的情况下保存
        }

        GameArchiveManager archiveManager = new GameArchiveManager(effectiveUserName, effectivePasswordHash);
        boolean success;
        
        if (currentSaveFileName != null && !currentSaveFileName.isEmpty()) {
            success = archiveManager.updateGame(currentSaveFileName, pieces, gameMoves, currentPlayerColor);
        } else {
            // 此情况理论上应由 initialize 处理，但作为后备
            success = archiveManager.saveGame(pieces, gameMoves, currentPlayerColor);
            if (success) {
                List<GameArchiveManager.SaveFileInfo> saveFiles = archiveManager.getSaveFiles();
                if (!saveFiles.isEmpty()) {
                    currentSaveFileName = saveFiles.get(0).getFileName();
                    if (isGuest) {
                        String savePath = GameArchiveManager.getUserSaveDir(GUEST_USER);
                        new File(savePath, currentSaveFileName).deleteOnExit();
                    }
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
        
        Color boardColor = Color.web("#eecfa1");
        Color lineColor = Color.web("#5c4033");

        // 背景色
        gc.setFill(boardColor);
        gc.fillRect(0, 0, chessBoardCanvas.getWidth(), chessBoardCanvas.getHeight());

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
        
        // 新增：绘制上一步的标记
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
    
    // 绘制上一步高亮
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

    // 棋子绘制
    private void drawPieces() {
        GraphicsContext gc = chessBoardCanvas.getGraphicsContext2D();

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        double radius = 22;

        // 绘制可移动位置的高亮效果
        gc.setFill(Color.web("green", 0.6));
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                if (validMoves[x][y] && getPieceAt(x, y) == null) {
                    double posX = x * CELL_SIZE + offsetX;
                    double posY = y * CELL_SIZE + offsetY;
                    gc.fillOval(posX - 5, posY - 5, 10, 10);
                }
            }
        }

        for (ChessPiece p : pieces) {
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


            // 绘制选中标记
            if (p == selectedPiece) {
                gc.setStroke(Color.BLUE);
                gc.setLineWidth(3);
                gc.strokeOval(x - radius - 3, y - radius - 3, 2 * radius + 6, 2 * radius + 6);
            }
            
            // 如果这个位置是可以吃的棋子，绘制红色边框
            if (validMoves[p.x][p.y] && getPieceAt(p.x, p.y) != null) {
                gc.setStroke(Color.RED);
                gc.setLineWidth(3);
                gc.strokeOval(x - radius - 5, y - radius - 5, 2 * radius + 10, 2 * radius + 10);
            }

            // 绘制棋子文字
            gc.setFill(pieceColor);
            gc.setFont(p.color.equals("RED") ? redPieceFont : blackPieceFont);
            gc.fillText(p.type, x, y);
        }
    }

    @FXML
    private void handleSaveGame() {
        if (isGameOver) {
            showAlert("提示", "游戏已结束，无法保存。");
            return;
        }
        if (gameMoves.isEmpty()) {
            showAlert("提示", "没有移动，无需保存。");
            return;
        }
        if (sessionIdentifier == null || sessionIdentifier.isEmpty()) {
            // 游客模式不提示错误，静默返回
            showAlert("提示", "游客模式无法手动保存游戏。");
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
            success = archiveManager.updateGame(currentSaveFileName, pieces, gameMoves, currentPlayerColor);
        } else {
            success = archiveManager.saveGame(pieces, gameMoves, currentPlayerColor);
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
        boolean isGuest = (sessionIdentifier == null || sessionIdentifier.isEmpty());

        if (!isGuest && !isGameOver && !gameMoves.isEmpty()) {
            // 注册用户且游戏未结束且有移动：静默保存游戏
            autoSaveGame();
        } else if (isGuest) {
            // 游客：删除临时存档
            if (currentSaveFileName != null && !currentSaveFileName.isEmpty()) {
                String savePath = GameArchiveManager.getUserSaveDir(GUEST_USER);
                File tempFile = new File(savePath, currentSaveFileName);
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        System.out.println("临时游客存档已删除: " + currentSaveFileName);
                    } else {
                        System.err.println("删除临时游客存档失败: " + currentSaveFileName);
                    }
                }
            }
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

    @FXML
    private void handleUndoMove() {
        if (isGameOver) {
            showAlert("提示", "游戏已结束，无法悔棋。");
            return;
        }
        if (gameMoves.isEmpty()) {
            showAlert("提示", "没有可以悔棋的步骤了！");
            return;
        }

        // 移除最后一步
        GameMove lastMoveRecord = gameMoves.remove(gameMoves.size() - 1);
        lastMove = gameMoves.isEmpty() ? null : gameMoves.get(gameMoves.size() - 1);

        // 恢复棋子位置
        // 首先，找到移动的棋子。它现在在 toX, toY
        ChessPiece movedPiece = null;
        for (ChessPiece piece : pieces) {
            if (piece.x == lastMoveRecord.toX && piece.y == lastMoveRecord.toY) {
                // 为了更精确地匹配，可以比较棋子类型和颜色
                if (piece.type.equals(lastMoveRecord.pieceName) && piece.color.equals(lastMoveRecord.pieceColor)) {
                    movedPiece = piece;
                    break;
                }
            }
        }
        
        if (movedPiece != null) {
            movedPiece.x = lastMoveRecord.fromX;
            movedPiece.y = lastMoveRecord.fromY;
        }

        // 如果上一步是吃子，则需要恢复被吃的棋子
        if (lastMoveRecord.capturedPiece != null) {
            // 将被吃的棋子重新添加到棋子数组中
            List<ChessPiece> pieceList = new ArrayList<>(Arrays.asList(pieces));
            pieceList.add(lastMoveRecord.capturedPiece);
            pieces = pieceList.toArray(new ChessPiece[0]);
        }

        // 切换回上一回合
        switchTurn();

        // 更新规则校验器
        ruleValidator = new MoveRuleValidator(pieces);

        // 重新绘制棋盘
        drawBoard();
        drawPieces();

        // 自动保存悔棋后的状态
        autoSaveGame();
    }

    /**
     * 处理玩家投降操作
     */
    @FXML
    private void handleSurrender() {
        if (isGameOver) {
            showAlert("提示", "游戏已结束。");
            return;
        }

        // 确认是否投降
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认投降");
        alert.setHeaderText(null);
        String contentText = "确定要投降吗？这将直接结束游戏并判对方获胜。";
        alert.setContentText(contentText);
        
        try {
            Font customFont = Font.loadFont(new File("Resource/fonts/SIMFANG.TTF").toURI().toString(), 14);
            if (customFont != null) {
                Label contentLabel = new Label(contentText);
                contentLabel.setFont(customFont);
                alert.getDialogPane().setContent(contentLabel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ButtonType buttonTypeYes = new ButtonType("确定");
        ButtonType buttonTypeNo = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonTypeYes) {
            // 投降方为当前玩家，胜利方为对方
            String loserColor = currentPlayerColor;
            String winnerColor = loserColor.equals("RED") ? "黑方" : "红方";
            String loserName = loserColor.equals("RED") ? "红方" : "黑方";
            
            // 创建一个假的被吃掉的棋子（帅或将）来表示投降
            String loserKingType = loserColor.equals("RED") ? "帅" : "将";
            ChessPiece fakeCapturedPiece = new ChessPiece(loserKingType, loserColor, -1, -1);
            
            // 结束游戏，对方获胜
            handleGameEndWithSurrender(fakeCapturedPiece, winnerColor, loserName);
        }
    }

    /**
     * 处理因投降而结束的游戏
     * @param king 被"吃掉"的棋子（实际是投降方的帅或将）
     * @param winner 获胜方名称
     * @param loser 投降方名称
     */
    private void handleGameEndWithSurrender(ChessPiece king, String winner, String loser) {
        isGameOver = true;
        updateTurnDisplay();
        String victoryMessage = winner + "获胜 (" + loser + "投降)";

        victoryLabel.setText(victoryMessage);
        
        // 移除所有可能的颜色样式
        victoryLabel.getStyleClass().removeAll("victory-title-red", "victory-title-black", "draw-title");
        replayButton.getStyleClass().removeAll("victory-button-red", "victory-button-black");

        // 根据获胜方设置样式
        if (winner.equals("红方")) {
            victoryLabel.getStyleClass().add("victory-title-red");
            replayButton.getStyleClass().add("victory-button-red");
        } else {
            victoryLabel.getStyleClass().add("victory-title-black");
            replayButton.getStyleClass().add("victory-button-black");
        }
        victoryOverlayPane.setVisible(true);
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

    private Font loadCustomFont(String filename, double size) {
        String resourcePath = "Resource/" + filename;

        try (InputStream is = MainGameController.class.getClassLoader().getResourceAsStream(resourcePath)) {
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
            lastMove = gameMoves.isEmpty() ? null : gameMoves.get(gameMoves.size() - 1);
            currentPlayerColor = data.currentPlayerColor != null ? data.currentPlayerColor : "RED";
            ruleValidator = new MoveRuleValidator(pieces);
            updateTurnDisplay();
            drawBoard();
            drawPieces();
        }
    }
    
    // 用于接收加载的游戏数据和文件名
    public void loadGameData(GameArchiveManager.GameArchiveData data, String fileName) {
        if (data != null) {
            loadGameData(data);
            currentSaveFileName = fileName; // 记录当前加载的存档文件名
        }
    }

    private void handleCheckEffect(String attackerColor) {
        checkAlertPane.setVisible(true);
        checkAlertLabel.setText("将"); // 默认显示"将"
        checkAlertLabel.getStyleClass().removeAll("check-alert-red", "check-alert-black");

        if (attackerColor.equals("RED")) {
            checkAlertLabel.getStyleClass().add("check-alert-red");
            checkAlertLabel.setFont(redCheckFont);
        } else {
            checkAlertLabel.getStyleClass().add("check-alert-black");
            checkAlertLabel.setFont(blackCheckFont);
        }

        // 创建动画
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(checkAlertLabel.opacityProperty(), 0),
                        new KeyValue(checkAlertLabel.scaleXProperty(), 3),
                        new KeyValue(checkAlertLabel.scaleYProperty(), 3),
                        new KeyValue(checkAlertLabel.rotateProperty(), 0)
                ),
                new KeyFrame(Duration.millis(500),
                        new KeyValue(checkAlertLabel.opacityProperty(), 1),
                        new KeyValue(checkAlertLabel.scaleXProperty(), 1),
                        new KeyValue(checkAlertLabel.scaleYProperty(), 1),
                        new KeyValue(checkAlertLabel.rotateProperty(), -10)
                ),
                new KeyFrame(Duration.millis(1500), // 停留1秒
                        new KeyValue(checkAlertLabel.opacityProperty(), 1)
                ),
                new KeyFrame(Duration.millis(2000), // 0.5秒后消失
                        new KeyValue(checkAlertLabel.opacityProperty(), 0)
                )
        );

        timeline.setOnFinished(event -> checkAlertPane.setVisible(false));
        timeline.play();
    }

    /**
     * 处理吃子特效，显示"吃"字
     * @param attackerColor 攻击方颜色
     * @param x 吃子位置的x坐标
     * @param y 吃子位置的y坐标
     */
    private void handleCaptureEffect(String attackerColor, int x, int y) {
        checkAlertPane.setVisible(true);
        checkAlertLabel.setText("吃"); // 显示"吃"
        checkAlertLabel.getStyleClass().removeAll("check-alert-red", "check-alert-black");

        if (attackerColor.equals("RED")) {
            checkAlertLabel.getStyleClass().add("check-alert-red");
            checkAlertLabel.setFont(redCheckFont);
        } else {
            checkAlertLabel.getStyleClass().add("check-alert-black");
            checkAlertLabel.setFont(blackCheckFont);
        }

        // 创建动画
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(checkAlertLabel.opacityProperty(), 0),
                        new KeyValue(checkAlertLabel.scaleXProperty(), 3),
                        new KeyValue(checkAlertLabel.scaleYProperty(), 3),
                        new KeyValue(checkAlertLabel.rotateProperty(), 0)
                ),
                new KeyFrame(Duration.millis(500),
                        new KeyValue(checkAlertLabel.opacityProperty(), 1),
                        new KeyValue(checkAlertLabel.scaleXProperty(), 1),
                        new KeyValue(checkAlertLabel.scaleYProperty(), 1),
                        new KeyValue(checkAlertLabel.rotateProperty(), -10)
                ),
                new KeyFrame(Duration.millis(1500), // 停留1秒
                        new KeyValue(checkAlertLabel.opacityProperty(), 1)
                ),
                new KeyFrame(Duration.millis(2000), // 0.5秒后消失
                        new KeyValue(checkAlertLabel.opacityProperty(), 0)
                )
        );

        timeline.setOnFinished(event -> checkAlertPane.setVisible(false));
        timeline.play();
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
        // 设置颜色为深棕色
        svg.setFill(Color.web("#8b4513"));
        svg.setScaleX(1.5); // 图标放大倍数
        svg.setScaleY(1.5);

        muteButton.setGraphic(svg);
    }

    private boolean isStalemate(String playerColor) {
        // 遍历该玩家的所有棋子
        for (ChessPiece piece : pieces) {
            if (piece.color.equals(playerColor)) {
                // 遍历棋盘上的所有位置
                for (int x = 0; x < COLS; x++) {
                    for (int y = 0; y < ROWS; y++) {
                        // 检查是否存在任何一个合法的移动
                        if (ruleValidator.checkMove(piece.x, piece.y, x, y) == MoveRuleValidator.MoveValidationResult.VALID) {
                            return false; // 只要找到一个合法移动，就不是绝杀
                        }
                    }
                }
            }
        }
        return true; // 如果所有棋子都没有任何合法移动，则是绝杀
    }
}
