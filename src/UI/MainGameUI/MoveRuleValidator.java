package UI.MainGameUI;

import java.util.Arrays;
import java.util.Optional;

public class MoveRuleValidator {

    private final ChessPiece[] currentPieces;
    private final int ROWS = 10;
    private final int COLS = 9;

    public MoveRuleValidator(ChessPiece[] pieces) {
        this.currentPieces = pieces;
    }

    /**
     * 检查从 (startX, startY) 移动棋子到 (endX, endY) 是否有效。
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @return 移动是否有效。
     */
    public boolean isValidMove(int startX, int startY, int endX, int endY) {

        Optional<ChessPiece> movingPieceOpt = getPieceAt(startX, startY);
        if (movingPieceOpt.isEmpty()) {
            return false; // 起点没有棋子
        }
        ChessPiece movingPiece = movingPieceOpt.get();

        Optional<ChessPiece> targetPieceOpt = getPieceAt(endX, endY);
        if (targetPieceOpt.isPresent() && targetPieceOpt.get().color.equals(movingPiece.color)) {
            return false; // 终点有自己的棋子
        }

        // 1. 检查是否在棋盘范围内
        if (endX < 0 || endX >= COLS || endY < 0 || endY >= ROWS) {
            return false;
        }

        // 2. 调用特定棋子的移动规则
        return switch (movingPiece.type) {
            case "将", "帅" -> isValidGeneralMove(movingPiece, endX, endY);
            case "士" -> isValidAdvisorMove(movingPiece, endX, endY);
            case "象", "相" -> isValidElephantMove(movingPiece, endX, endY);
            case "车", "車" -> isValidChariotMove(movingPiece, endX, endY);
            case "马", "馬" -> isValidHorseMove(movingPiece, endX, endY);
            case "炮", "砲" -> isValidCannonMove(movingPiece, endX, endY);
            case "兵", "卒" -> isValidPawnMove(movingPiece, endX, endY);
            default -> false;
        };
    }

    // 辅助方法：获取指定坐标的棋子
    private Optional<ChessPiece> getPieceAt(int x, int y) {
        return Arrays.stream(currentPieces)
                .filter(p -> p != null && p.x == x && p.y == y)
                .findFirst();
    }

    // 辅助方法：计算两点之间是否有棋子（用于车和炮）
    private long countPiecesBetween(int x1, int y1, int x2, int y2) {
        long count = 0;

        if (x1 == x2) { // 直线垂直移动
            int min = Math.min(y1, y2);
            int max = Math.max(y1, y2);
            for (int y = min + 1; y < max; y++) {
                if (getPieceAt(x1, y).isPresent()) {
                    count++;
                }
            }
        } else if (y1 == y2) { // 直线水平移动
            int min = Math.min(x1, x2);
            int max = Math.max(x1, x2);
            for (int x = min + 1; x < max; x++) {
                if (getPieceAt(x, y1).isPresent()) {
                    count++;
                }
            }
        }
        return count;
    }

    // --- 各种棋子的移动规则实现 ---

    private boolean isValidGeneralMove(ChessPiece piece, int endX, int endY) {
        int dx = Math.abs(endX - piece.x);
        int dy = Math.abs(endY - piece.y);

        // 只能走一步直线
        if (!((dx == 1 && dy == 0) || (dx == 0 && dy == 1))) {
            return false;
        }

        // 必须在九宫格内 (X: 3-5, Y: 红方 7-9, 黑方 0-2)
        if (endX < 3 || endX > 5) {
            return false;
        }
        if (piece.color.equals("RED")) {
            if (endY < 7 || endY > 9) return false;
        } else { // BLACK
            if (endY < 0 || endY > 2) return false;
        }

        // 额外的 "将帅对脸" 规则：
        Optional<ChessPiece> targetPieceOpt = getPieceAt(endX, endY);
        if (targetPieceOpt.isEmpty() && dx == 0) { // 目标为空，垂直移动
            // 检查终点和起点之间是否有其他棋子阻挡
            long piecesBetween = countPiecesBetween(piece.x, piece.y, endX, endY);

            // 如果两个将帅在同一列，并且之间没有棋子，则允许吃掉对方的将帅（特殊情况，这里简化为只检查对脸）
            Optional<ChessPiece> otherGeneralOpt = Arrays.stream(currentPieces)
                    .filter(p -> p != null && p.color.equals(piece.color.equals("RED") ? "BLACK" : "RED") &&
                            (p.type.equals("将") || p.type.equals("帅")))
                    .findFirst();

            if (otherGeneralOpt.isPresent()) {
                ChessPiece otherGeneral = otherGeneralOpt.get();
                if (piece.x == otherGeneral.x && countPiecesBetween(piece.x, piece.y, otherGeneral.x, otherGeneral.y) == 0) {
                    // 如果移动后将帅对脸，且目标不是对方将帅，则不允许移动（这是更复杂的实现，这里只检查基本移动）
                    // 简化规则：我们只在 'isValidMove' 开始时检查目标，这里忽略对脸。
                }
            }
        }

        return true;
    }

    private boolean isValidAdvisorMove(ChessPiece piece, int endX, int endY) {
        int dx = Math.abs(endX - piece.x);
        int dy = Math.abs(endY - piece.y);

        // 必须走斜对角一步 (dx=1, dy=1)
        if (!(dx == 1 && dy == 1)) {
            return false;
        }

        // 必须在九宫格内 (X: 3-5, Y: 红方 7-9, 黑方 0-2)
        if (endX < 3 || endX > 5) {
            return false;
        }
        if (piece.color.equals("RED")) {
            if (endY < 7 || endY > 9) return false;
        } else { // BLACK
            if (endY < 0 || endY > 2) return false;
        }
        return true;
    }

    private boolean isValidElephantMove(ChessPiece piece, int endX, int endY) {
        int dx = Math.abs(endX - piece.x);
        int dy = Math.abs(endY - piece.y);

        // 必须走日字对角两步 (dx=2, dy=2)
        if (!(dx == 2 && dy == 2)) {
            return false;
        }

        // 不能过河 (红方 Y <= 9 && Y >= 5; 黑方 Y >= 0 && Y <= 4)
        if (piece.color.equals("RED")) {
            if (endY < 5) return false;
        } else { // BLACK
            if (endY > 4) return false;
        }

        // 塞象眼 (检查斜对角线的中心点是否有棋子)
        int blockX = (piece.x + endX) / 2;
        int blockY = (piece.y + endY) / 2;
        if (getPieceAt(blockX, blockY).isPresent()) {
            return false; // 象眼被塞
        }

        return true;
    }

    private boolean isValidChariotMove(ChessPiece piece, int endX, int endY) {
        // 必须走直线
        if (piece.x != endX && piece.y != endY) {
            return false;
        }

        // 检查路径上是否有棋子阻挡
        long piecesBetween = countPiecesBetween(piece.x, piece.y, endX, endY);

        // 目标为空或有对方棋子时，路径上必须没有棋子
        return piecesBetween == 0;
    }

    private boolean isValidHorseMove(ChessPiece piece, int endX, int endY) {
        int dx = Math.abs(endX - piece.x);
        int dy = Math.abs(endY - piece.y);

        // 必须走日字 (dx=1, dy=2) 或 (dx=2, dy=1)
        if (!((dx == 1 && dy == 2) || (dx == 2 && dy == 1))) {
            return false;
        }

        // 蹩马腿 (检查日字拐角处是否有棋子阻挡)
        int blockX = piece.x;
        int blockY = piece.y;

        if (dx == 1) { // 竖着走两格，横着走一格
            blockY += (endY > piece.y ? 1 : -1); // 蹩腿在 Y 轴的第一步
        } else { // 横着走两格，竖着走一格
            blockX += (endX > piece.x ? 1 : -1); // 蹩腿在 X 轴的第一步
        }

        if (getPieceAt(blockX, blockY).isPresent()) {
            return false; // 马腿被蹩
        }

        return true;
    }

    private boolean isValidCannonMove(ChessPiece piece, int endX, int endY) {
        // 必须走直线
        if (piece.x != endX && piece.y != endY) {
            return false;
        }

        long piecesBetween = countPiecesBetween(piece.x, piece.y, endX, endY);
        Optional<ChessPiece> targetPieceOpt = getPieceAt(endX, endY);

        if (targetPieceOpt.isPresent()) {
            // 目标有棋子（吃子）：路径上必须**恰好**有一个棋子作为“炮架”
            return piecesBetween == 1;
        } else {
            // 目标为空（移动）：路径上必须**没有**棋子
            return piecesBetween == 0;
        }
    }

    private boolean isValidPawnMove(ChessPiece piece, int endX, int endY) {
        int dx = Math.abs(endX - piece.x);
        int dy = endY - piece.y; // 使用非绝对值，因为兵只能向前或平走

        // 1. 兵卒不能后退
        if (piece.color.equals("RED")) {
            if (dy > 0) return false; // 红方 y 坐标减小是前进 (9->0)
        } else { // BLACK
            if (dy < 0) return false; // 黑方 y 坐标增大是前进 (0->9)
        }

        // 2. 只能走一步
        if (!((dx == 1 && dy == 0) || (dx == 0 && Math.abs(dy) == 1))) {
            return false;
        }

        // 3. 过河前的限制：只能直走 (dy != 0)
        boolean hasCrossedRiver;
        if (piece.color.equals("RED")) {
            hasCrossedRiver = piece.y <= 4;
        } else {
            hasCrossedRiver = piece.y >= 5;
        }

        if (!hasCrossedRiver) {
            // 未过河，不能横走
            return dx == 0;
        }

        // 已过河，可以直走或横走一步
        return (dx == 1 && dy == 0) || (dx == 0 && Math.abs(dy) == 1);
    }
}