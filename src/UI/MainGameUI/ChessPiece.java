package UI.MainGameUI;

// 棋子数据模型
public class ChessPiece {
    public String type;
    public String color;
    public int x;
    public int y;

    public ChessPiece(String type, String color, int x, int y) {
        this.type = type;
        this.color = color;
        this.x = x;
        this.y = y;
    }

    // 方便调试
    @Override
    public String toString() {
        return color + " " + type + " (" + x + "," + y + ")";
    }
}