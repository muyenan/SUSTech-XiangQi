package UI.MainGameUI;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class MoveRuleValidator {

    private final ChessPiece[] currentPieces;
    private final int ROWS = 10;
    private final int COLS = 9;

    // 用于表示移动验证结果的枚举
    public enum MoveValidationResult {
        VALID,
        INVALID_RULE,
        INVALID_SELF_CHECK
    }

    public MoveRuleValidator(ChessPiece[] pieces) {
        this.currentPieces = pieces;
    }

    /**
     * 检查移动的有效性，并返回详细结果。
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @return MoveValidationResult 枚举，表示移动结果。
     */
    public MoveValidationResult checkMove(int startX, int startY, int endX, int endY) {
        Optional<ChessPiece> movingPieceOpt = getPieceAt(startX, startY);
        if (movingPieceOpt.isEmpty()) {
            return MoveValidationResult.INVALID_RULE;
        }
        ChessPiece movingPiece = movingPieceOpt.get();

        if (endX < 0 || endX >= COLS || endY < 0 || endY >= ROWS) {
            return MoveValidationResult.INVALID_RULE;
        }

        if (!isSimpleValidMove(startX, startY, endX, endY)) {
            return MoveValidationResult.INVALID_RULE;
        }

        // 检查当前是否被将军
        boolean isCurrentlyInCheck = isKingInCheck(movingPiece.color, currentPieces);
        
        // 只有在未被将军时才检查是否会自毙
        if (!isCurrentlyInCheck && isMoveCausingSelfCheck(movingPiece, endX, endY)) {
            return MoveValidationResult.INVALID_SELF_CHECK;
        }

        return MoveValidationResult.VALID;
    }
    
    /**
     * 兼容旧的 isValidMove 方法，供 calculateValidMoves 使用。
     */
    public boolean isValidMove(int startX, int startY, int endX, int endY) {
        return checkMove(startX, startY, endX, endY) == MoveValidationResult.VALID;
    }

    private Optional<ChessPiece> getPieceAt(int x, int y) {
        return Arrays.stream(currentPieces)
                .filter(p -> p != null && p.x == x && p.y == y)
                .findFirst();
    }

    private long countPiecesBetween(int x1, int y1, int x2, int y2, ChessPiece[] pieces) {
        long count = 0;
        if (x1 == x2) {
            int min = Math.min(y1, y2);
            int max = Math.max(y1, y2);
            for (int y = min + 1; y < max; y++) {
                if (getPieceAt(x1, y, pieces).isPresent()) count++;
            }
        } else if (y1 == y2) {
            int min = Math.min(x1, x2);
            int max = Math.max(x1, x2);
            for (int x = min + 1; x < max; x++) {
                if (getPieceAt(x, y1, pieces).isPresent()) count++;
            }
        }
        return count;
    }

    private Optional<ChessPiece> getPieceAt(int x, int y, ChessPiece[] pieces) {
        return Arrays.stream(pieces)
                .filter(p -> p != null && p.x == x && p.y == y)
                .findFirst();
    }

    private boolean isValidGeneralMove(ChessPiece piece, int endX, int endY) {
        int dx = Math.abs(endX - piece.x);
        int dy = Math.abs(endY - piece.y);
        if (!((dx == 1 && dy == 0) || (dx == 0 && dy == 1))) return false;

        if (endX < 3 || endX > 5) return false;
        if (piece.color.equals("RED")) {
            if (endY < 7 || endY > 9) return false;
        } else {
            if (endY < 0 || endY > 2) return false;
        }
        
        return true;
    }

    private boolean isValidAdvisorMove(ChessPiece piece, int endX, int endY) {
        int dx = Math.abs(endX - piece.x);
        int dy = Math.abs(endY - piece.y);
        if (!(dx == 1 && dy == 1)) return false;

        if (endX < 3 || endX > 5) return false;
        if (piece.color.equals("RED")) {
            if (endY < 7 || endY > 9) return false;
        } else {
            if (endY < 0 || endY > 2) return false;
        }
        return true;
    }

    private boolean isValidElephantMove(ChessPiece piece, int endX, int endY) {
        int dx = Math.abs(endX - piece.x);
        int dy = Math.abs(endY - piece.y);
        if (!(dx == 2 && dy == 2)) return false;

        if (piece.color.equals("RED")) {
            if (endY < 5) return false;
        } else {
            if (endY > 4) return false;
        }

        int blockX = (piece.x + endX) / 2;
        int blockY = (piece.y + endY) / 2;
        if (getPieceAt(blockX, blockY).isPresent()) return false;

        return true;
    }

    private boolean isValidChariotMove(ChessPiece piece, int endX, int endY) {
        if (piece.x != endX && piece.y != endY) return false;
        return countPiecesBetween(piece.x, piece.y, endX, endY, currentPieces) == 0;
    }

    private boolean isValidHorseMove(ChessPiece piece, int endX, int endY) {
        int dx = Math.abs(endX - piece.x);
        int dy = Math.abs(endY - piece.y);
        if (!((dx == 1 && dy == 2) || (dx == 2 && dy == 1))) return false;

        int blockX = piece.x;
        int blockY = piece.y;
        if (dx == 1) {
            blockY += (endY > piece.y ? 1 : -1);
        } else {
            blockX += (endX > piece.x ? 1 : -1);
        }
        if (getPieceAt(blockX, blockY).isPresent()) return false;

        return true;
    }

    private boolean isValidCannonMove(ChessPiece piece, int endX, int endY) {
        if (piece.x != endX && piece.y != endY) return false;
        long piecesBetween = countPiecesBetween(piece.x, piece.y, endX, endY, currentPieces);
        if (getPieceAt(endX, endY).isPresent()) {
            return piecesBetween == 1;
        } else {
            return piecesBetween == 0;
        }
    }

    private boolean isValidPawnMove(ChessPiece piece, int endX, int endY) {
        int dx = Math.abs(endX - piece.x);
        int dyAbs = Math.abs(endY - piece.y);

        if (piece.color.equals("RED")) {
            if (endY > piece.y) return false;
        } else {
            if (endY < piece.y) return false;
        }

        if (!((dx == 1 && dyAbs == 0) || (dx == 0 && dyAbs == 1))) return false;

        boolean hasCrossedRiver = piece.color.equals("RED") ? piece.y <= 4 : piece.y >= 5;
        if (!hasCrossedRiver && dx != 0) return false;

        return true;
    }

    public boolean isKingInCheck(String kingColor, ChessPiece[] pieces) {
        Optional<ChessPiece> kingOpt = Arrays.stream(pieces)
                .filter(p -> p != null && p.color.equals(kingColor) && (p.type.equals("将") || p.type.equals("帅")))
                .findFirst();
        if (kingOpt.isEmpty()) return false;
        ChessPiece king = kingOpt.get();
        String opponentColor = kingColor.equals("RED") ? "BLACK" : "RED";

        return Arrays.stream(pieces)
                .filter(p -> p != null && p.color.equals(opponentColor))
                .anyMatch(opponentPiece -> {
                    MoveRuleValidator tempValidator = new MoveRuleValidator(pieces);
                    return tempValidator.isSimpleValidMove(opponentPiece.x, opponentPiece.y, king.x, king.y);
                });
    }

    private boolean isMoveCausingSelfCheck(ChessPiece pieceToMove, int endX, int endY) {
        ChessPiece[] simulatedPieces = Arrays.stream(currentPieces)
                .filter(p -> p.x != endX || p.y != endY)
                .map(ChessPiece::new)
                .toArray(ChessPiece[]::new);

        ChessPiece simulatedMovingPiece = Arrays.stream(simulatedPieces)
                .filter(p -> p.x == pieceToMove.x && p.y == pieceToMove.y)
                .findFirst().orElse(null);
        if (simulatedMovingPiece == null) return true;

        simulatedMovingPiece.x = endX;
        simulatedMovingPiece.y = endY;

        // 检查移动后是否被将军
        if (isKingInCheck(pieceToMove.color, simulatedPieces)) {
            return true;
        }

        // 检查移动后是否将帅对脸
        Optional<ChessPiece> myKingOpt = Arrays.stream(simulatedPieces)
                .filter(p -> p != null && p.color.equals(pieceToMove.color) && (p.type.equals("将") || p.type.equals("帅")))
                .findFirst();
        Optional<ChessPiece> opponentKingOpt = Arrays.stream(simulatedPieces)
                .filter(p -> p != null && !p.color.equals(pieceToMove.color) && (p.type.equals("将") || p.type.equals("帅")))
                .findFirst();

        if (myKingOpt.isPresent() && opponentKingOpt.isPresent()) {
            ChessPiece myKing = myKingOpt.get();
            ChessPiece opponentKing = opponentKingOpt.get();
            if (myKing.x == opponentKing.x) {
                if (countPiecesBetween(myKing.x, myKing.y, opponentKing.x, opponentKing.y, simulatedPieces) == 0) {
                    return true; // 将帅对脸，非法移动
                }
            }
        }

        return false;
    }

    private boolean isSimpleValidMove(int startX, int startY, int endX, int endY) {
        Optional<ChessPiece> movingPieceOpt = getPieceAt(startX, startY);
        if (movingPieceOpt.isEmpty()) return false;
        ChessPiece movingPiece = movingPieceOpt.get();

        Optional<ChessPiece> targetPieceOpt = getPieceAt(endX, endY);
        if (targetPieceOpt.isPresent() && targetPieceOpt.get().color.equals(movingPiece.color)) {
            return false;
        }

        return switch (movingPiece.type) {
            case "将", "帅" -> isValidGeneralMove(movingPiece, endX, endY);
            case "士", "仕" -> isValidAdvisorMove(movingPiece, endX, endY);
            case "象", "相" -> isValidElephantMove(movingPiece, endX, endY);
            case "车", "車" -> isValidChariotMove(movingPiece, endX, endY);
            case "马", "馬" -> isValidHorseMove(movingPiece, endX, endY);
            case "炮", "砲" -> isValidCannonMove(movingPiece, endX, endY);
            case "兵", "卒" -> isValidPawnMove(movingPiece, endX, endY);
            default -> false;
        };
    }
}
