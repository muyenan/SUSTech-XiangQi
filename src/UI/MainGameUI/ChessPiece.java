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

    // 拷贝构造函数
    public ChessPiece(ChessPiece other) {
        this.type = other.type;
        this.color = other.color;
        this.x = other.x;
        this.y = other.y;
    }

    // 方便调试
    @Override
    public String toString() {
        return color + " " + type + " (" + x + "," + y + ")";
    }
}