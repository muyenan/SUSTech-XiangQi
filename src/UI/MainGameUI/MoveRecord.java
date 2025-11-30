package UI.MainGameUI;

/**
 * MoveRecord 类用于表示棋局中的一次移动记录
 * 包含了详细的移动信息，如步数、玩家、棋子类型及移动位置
 */
public class MoveRecord {
    private int moveNumber;      // 第几步
    private String playerColor;  // 哪一方 (RED 或 BLACK)
    private String pieceType;    // 棋子类型 (帅/将, 仕/士, 相/象, 马, 车, 炮, 兵/卒)
    private int fromX, fromY;    // 起始位置
    private int toX, toY;        // 目标位置

    /**
     * 构造函数
     *
     * @param moveNumber 步数
     * @param playerColor 玩家颜色
     * @param pieceType 棋子类型
     * @param fromX 起始X坐标
     * @param fromY 起始Y坐标
     * @param toX 目标X坐标
     * @param toY 目标Y坐标
     */
    public MoveRecord(int moveNumber, String playerColor, String pieceType, int fromX, int fromY, int toX, int toY) {
        this.moveNumber = moveNumber;
        this.playerColor = playerColor;
        this.pieceType = pieceType;
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
    }

    // Getter 方法
    public int getMoveNumber() { return moveNumber; }
    public String getPlayerColor() { return playerColor; }
    public String getPieceType() { return pieceType; }
    public int getFromX() { return fromX; }
    public int getFromY() { return fromY; }
    public int getToX() { return toX; }
    public int getToY() { return toY; }

    /**
     * 获取格式化的移动描述
     *
     * @return 格式化后的移动描述字符串
     */
    public String getFormattedMoveDescription() {
        String player = playerColor.equals("RED") ? "红方" : "黑方";
        return String.format("第%d步：%s将%s从(%d,%d)移动到(%d,%d)",
                moveNumber, player, pieceType, fromX, fromY, toX, toY);
    }

    @Override
    public String toString() {
        return getFormattedMoveDescription();
    }
}