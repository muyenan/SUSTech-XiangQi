package UI.MainGameUI;

import UI.MainUI.MainLauncher;
import UI.Models.AudioModel;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static UI.MainUI.MainController.getSessionIdentifier;
import static UI.MainUI.MainController.getUserName;
import static UI.Models.ShowAlert.showAlert;

public class ReplayGameController {

    @FXML
    private Canvas chessBoardCanvas;
    @FXML
    private Label moveInfoLabel;
    @FXML
    private Button muteButton;

    // 常量
    private static final int ROWS = 10;
    private static final int COLS = 9;
    private static final int CELL_SIZE = 50;
    private final Color RED_COLOR = Color.web("#8B0000");

    // 字体
    private Font redPieceFont;
    private Font blackPieceFont;

    // 棋盘和棋子数据
    private ChessPiece[] initialPieces;
    private ChessPiece[] currentPieces;
    private List<GameMove> gameMoves;
    private int currentMoveIndex = 0;
    private String currentPlayerColor;

    // 棋盘偏移量
    private double offsetX;
    private double offsetY;

    private final String SOUND_ICON = "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z";
    private final String MUTE_ICON = "M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z";


    @FXML
    public void initialize() {
        // 计算棋盘偏移量
        int boardWidth = (COLS - 1) * CELL_SIZE;
        int boardHeight = (ROWS - 1) * CELL_SIZE;
        offsetX = (chessBoardCanvas.getWidth() - boardWidth) / 2.0;
        offsetY = (chessBoardCanvas.getHeight() - boardHeight) / 2.0;

        // 加载字体
        redPieceFont = loadCustomFont("fonts/HanYiWeiBeiJian-1.ttf", 26);
        blackPieceFont = loadCustomFont("fonts/HanYiWeiBeiFan-1.ttf", 26);

        updateMuteIcon();
    }

    public void initializeData(ChessPiece[] initialPieces, List<GameMove> gameMoves) {
        this.initialPieces = initialPieces;
        this.currentPieces = Arrays.stream(initialPieces).map(p -> new ChessPiece(p)).toArray(ChessPiece[]::new);
        this.gameMoves = gameMoves;
        this.currentMoveIndex = 0;
        this.currentPlayerColor = "RED"; // 初始为红方

        updateMoveInfo();
        drawBoard();
        drawPieces();
    }

    @FXML
    private void handlePrevMove() {
        if (currentMoveIndex > 0) {
            currentMoveIndex--;
            replayToMove(currentMoveIndex);
        } else {
            showAlert("提示", "已经是第一步了。");
        }
    }

    @FXML
    private void handleNextMove() {
        if (currentMoveIndex < gameMoves.size()) {
            currentMoveIndex++;
            replayToMove(currentMoveIndex);
        } else {
            showAlert("提示", "已经是最后一步了。");
        }
    }

    private void replayToMove(int moveIndex) {
        // 从初始状态开始
        currentPieces = Arrays.stream(initialPieces).map(p -> new ChessPiece(p)).toArray(ChessPiece[]::new);
        currentPlayerColor = "RED";

        // 应用到指定步骤
        for (int i = 0; i < moveIndex; i++) {
            GameMove move = gameMoves.get(i);
            applyMove(move);
            // 切换回合
            if (i < moveIndex - 1) { // 在应用最后一步之前切换
                currentPlayerColor = currentPlayerColor.equals("RED") ? "BLACK" : "RED";
            }
        }
        // 在应用所有步骤后，确定当前的回合方
        if (moveIndex > 0) {
            currentPlayerColor = gameMoves.get(moveIndex - 1).pieceColor.equals("RED") ? "BLACK" : "RED";
        }


        updateMoveInfo();
        drawBoard();
        drawPieces();
    }

    private void applyMove(GameMove move) {
        // 找到要移动的棋子
        ChessPiece pieceToMove = null;
        for (ChessPiece p : currentPieces) {
            if (p != null && p.x == move.fromX && p.y == move.fromY) {
                pieceToMove = p;
                break;
            }
        }

        if (pieceToMove != null) {
            // 如果目标位置有棋子，则吃掉它
            if (move.capturedPiece != null) {
                currentPieces = Arrays.stream(currentPieces)
                        .filter(p -> p != null && !(p.x == move.toX && p.y == move.toY))
                        .toArray(ChessPiece[]::new);
            }
            // 移动棋子
            pieceToMove.x = move.toX;
            pieceToMove.y = move.toY;
        }
    }

    private void updateMoveInfo() {
        String colorName = currentPlayerColor.equals("RED") ? "红方" : "黑方";
        moveInfoLabel.setText(String.format("第 %d/%d 步 (轮到%s)", currentMoveIndex, gameMoves.size(), colorName));
        moveInfoLabel.setTextFill(currentPlayerColor.equals("RED") ? RED_COLOR : Color.BLACK);
    }

    @FXML
    private void handleBackToMenu() {
        try {
            Stage currentStage = (Stage) chessBoardCanvas.getScene().getWindow();
            currentStage.close();

            MainLauncher mainLauncher = new MainLauncher(getUserName(), getSessionIdentifier());
            mainLauncher.showMainWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("未知错误", "请联系管理员!");
        }
    }

    @FXML
    private void handleStartFromHere() {
        try {
            Stage currentStage = (Stage) chessBoardCanvas.getScene().getWindow();
            currentStage.close();

            // 获取当前步数之前的历史记录
            List<GameMove> subMoves = new ArrayList<>(gameMoves.subList(0, currentMoveIndex));

            MainGameLauncher gameLauncher = new MainGameLauncher(
                getUserName(),
                getSessionIdentifier(),
                currentPieces,
                subMoves,
                currentPlayerColor
            );
            gameLauncher.showMainGameWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("错误", "无法从当前位置开始游戏: " + e.getMessage());
        }
    }

    @FXML
    private void handlePlayAgain() {
        try {
            Stage currentStage = (Stage) chessBoardCanvas.getScene().getWindow();
            currentStage.close();

            MainGameLauncher gameLauncher = new MainGameLauncher(getUserName(), getSessionIdentifier());
            gameLauncher.showMainGameWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("错误", "无法重新开始游戏: " + e.getMessage());
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

    // 绘制棋子
    private void drawPieces() {
        GraphicsContext gc = chessBoardCanvas.getGraphicsContext2D();
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        double radius = 22;

        for (ChessPiece p : currentPieces) {
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

    private Font loadCustomFont(String filename, double size) {
        String resourcePath = "Resource/" + filename;
        try (InputStream is = ReplayGameController.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("错误: 找不到字体资源文件: " + resourcePath);
                return Font.font("SimSun", size);
            }
            return Font.loadFont(is, size);
        } catch (Exception e) {
            System.err.println("加载字体失败: " + filename);
            e.printStackTrace();
            return Font.font("SimSun", size);
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
        // 设置颜色为深棕色
        svg.setFill(Color.web("#8b4513"));
        svg.setScaleX(1.5); // 图标放大倍数
        svg.setScaleY(1.5);

        muteButton.setGraphic(svg);
    }
}
