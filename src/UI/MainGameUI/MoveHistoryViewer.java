package UI.MainGameUI;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.util.List;

/**
 * MoveHistoryViewer 类用于显示游戏的移动历史记录
 */
public class MoveHistoryViewer {

    @FXML
    private ListView<String> moveHistoryListView;

    @FXML
    private Button closeButton;

    private List<MoveRecord> moveRecords;

    /**
     * 设置要显示的移动记录
     *
     * @param records 移动记录列表
     */
    public void setMoveRecords(List<MoveRecord> records) {
        this.moveRecords = records;
        displayMoveHistory();
    }

    /**
     * 在列表视图中显示移动历史
     */
    private void displayMoveHistory() {
        if (moveRecords != null && !moveRecords.isEmpty()) {
            for (MoveRecord record : moveRecords) {
                moveHistoryListView.getItems().add(record.toString());
            }
        } else {
            moveHistoryListView.getItems().add("暂无移动记录");
        }
    }

    /**
     * 关闭窗口
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}