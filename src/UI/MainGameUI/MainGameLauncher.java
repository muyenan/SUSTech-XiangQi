package UI.MainGameUI;

import UI.Models.GameArchiveManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class MainGameLauncher {

    private String username;
    private String sessionIdentifier;
    private GameArchiveManager.GameArchiveData loadedData;
    private String loadedFileName;
    private String lastLoginTime;
    private List<GameMove> gameMoves; // 新增，用于从特定状态启动

    public MainGameLauncher() {
        // 默认构造函数
    }

    public MainGameLauncher(String username, String sessionIdentifier) {
        this.username = username;
        this.sessionIdentifier = sessionIdentifier;
    }
    
    public MainGameLauncher(String username, String sessionIdentifier, GameArchiveManager.GameArchiveData loadedData) {
        this.username = username;
        this.sessionIdentifier = sessionIdentifier;
        this.loadedData = loadedData;
    }
    
    public MainGameLauncher(String username, String sessionIdentifier, GameArchiveManager.GameArchiveData loadedData, String loadedFileName) {
        this.username = username;
        this.sessionIdentifier = sessionIdentifier;
        this.loadedData = loadedData;
        this.loadedFileName = loadedFileName;
    }
    
    public MainGameLauncher(String username, String sessionIdentifier, String lastLoginTime) {
        this.username = username;
        this.sessionIdentifier = sessionIdentifier;
        this.lastLoginTime = lastLoginTime;
    }

    // 新增构造函数：从特定棋局状态启动
    public MainGameLauncher(String username, String sessionIdentifier, ChessPiece[] pieces, List<GameMove> moves, String currentPlayerColor) {
        this.username = username;
        this.sessionIdentifier = sessionIdentifier;
        this.loadedData = new GameArchiveManager.GameArchiveData(pieces, moves, currentPlayerColor);
        this.gameMoves = moves;
    }

    public void showMainGameWindow() {
        Stage loginStage = new Stage();
        Parent root = null;
        FXMLLoader loader = new FXMLLoader();

        try {
            // 加载FXML文件
            loader.setLocation(getClass().getResource("MainGame.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 获取控制器并初始化用户信息
        MainGameController controller = loader.getController();
        if (controller != null) {
            // 如果有加载的数据，则传递给控制器
            if (loadedData != null) {
                if (loadedFileName != null && !loadedFileName.isEmpty()) {
                    controller.loadGameData(loadedData, loadedFileName);
                } else {
                    controller.loadGameData(loadedData);
                }
            }
            
            // 传递上次登录时间
            if (lastLoginTime != null) {
                controller.setLastLoginTime(lastLoginTime);
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
