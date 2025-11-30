package UI.MainGameUI;

// 记录每一步移动的数据结构
public class GameMove {
    public int fromX;
    public int fromY;
    public int toX;
    public int toY;
    public long timestamp;
    public String pieceName;
    public String pieceColor;
    public ChessPiece capturedPiece; // 新增字段，用于记录被吃掉的棋子

    public GameMove(int fromX, int fromY, int toX, int toY, long timestamp, String pieceName, String pieceColor, ChessPiece capturedPiece) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.timestamp = timestamp;
        this.pieceName = pieceName;
        this.pieceColor = pieceColor;
        this.capturedPiece = capturedPiece;
    }
}