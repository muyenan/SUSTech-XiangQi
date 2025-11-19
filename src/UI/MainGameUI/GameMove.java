package UI.MainGameUI;

// 记录每一步移动的数据结构
public class GameMove {
    public int fromX;
    public int fromY;
    public int toX;
    public int toY;
    public long timestamp;

    public GameMove(int fromX, int fromY, int toX, int toY, long timestamp) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.timestamp = timestamp;
    }
}