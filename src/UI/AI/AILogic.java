package UI.AI;

import UI.MainGameUI.ChessPiece;
import UI.MainGameUI.GameMove;
import UI.MainGameUI.MoveRuleValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AILogic {

    // 棋子价值常量
    private static final int KING_VALUE = 10000;
    private static final int ROOK_VALUE = 90;
    private static final int KNIGHT_VALUE = 40;
    private static final int CANNON_VALUE = 45;
    private static final int ADVISOR_VALUE = 20;
    private static final int BISHOP_VALUE = 20;
    private static final int PAWN_VALUE = 10;
    private static final int PROMOTED_PAWN_BONUS = 20;

    // Alpha-Beta剪枝使用的极值
    private static final int INF = 999999;

    /**
     * 获取AI最佳移动
     * 算法：Minimax + Alpha-Beta剪枝
     *
     * @param board 当前棋盘状态
     * @param depth 搜索深度
     * @param aiColor AI执子颜色
     * @param validator 移动规则验证器
     * @return 最佳移动
     */
    public GameMove getBestMoveUsingMinimax(ChessPiece[] board, int depth, String aiColor, MoveRuleValidator validator) {
        List<GameMove> moves = generateLegalMoves(board, aiColor, validator);
        if (moves.isEmpty()) return null;

        // 对移动进行排序，提高Alpha-Beta剪枝效率
        moves.sort((m1, m2) -> {
            // 优先考虑吃子移动
            if (m1.capturedPiece != null && m2.capturedPiece == null) return -1;
            if (m1.capturedPiece == null && m2.capturedPiece != null) return 1;
            // 都吃子或都不吃子，保持原有顺序
            return 0;
        });

        GameMove bestMove = null;
        int alpha = -INF;
        int beta = INF;

        // 对于每个可能的移动
        for (GameMove move : moves) {
            // 模拟移动
            ChessPiece[] nextBoard = makeMove(board, move);

            // 必杀剪枝：如果这步能吃掉对方老将，直接返回
            if (move.capturedPiece != null && 
                (move.capturedPiece.type.equals("帅") || move.capturedPiece.type.equals("将"))) {
                return move;
            }

            // 递归搜索评估分数
            int evalScore = minimax(nextBoard, depth - 1, false, alpha, beta, aiColor, validator);

            // 更新最佳移动
            if (bestMove == null || evalScore > alpha) {
                alpha = evalScore;
                bestMove = move;
            }
        }

        return bestMove;
    }

    /**
     * Minimax算法带Alpha-Beta剪枝
     *
     * @param board 当前棋盘状态
     * @param depth 搜索深度
     * @param isMaximizing 是否最大化玩家（AI）
     * @param alpha Alpha值
     * @param beta Beta值
     * @param aiColor AI执子颜色
     * @param validator 移动规则验证器
     * @return 评估分数
     */
    private int minimax(ChessPiece[] board, int depth, boolean isMaximizing, int alpha, int beta, String aiColor, MoveRuleValidator validator) {
        // 递归终止条件：达到搜索深度或游戏结束
        if (depth == 0 || isGameOver(board)) {
            return evaluateBoard(board, aiColor);
        }

        String turn = isMaximizing ? aiColor : (aiColor.equals("RED") ? "BLACK" : "RED");
        List<GameMove> moves = generateLegalMoves(board, turn, validator);
        
        // 对移动进行排序，提高Alpha-Beta剪枝效率
        moves.sort((m1, m2) -> {
            // 优先考虑吃子移动
            if (m1.capturedPiece != null && m2.capturedPiece == null) return -1;
            if (m1.capturedPiece == null && m2.capturedPiece != null) return 1;
            // 都吃子或都不吃子，保持原有顺序
            return 0;
        });

        if (moves.isEmpty()) {
            // 无路可走的情况
            return isMaximizing ? -INF : INF;
        }

        if (isMaximizing) {
            // AI (最大化玩家) 寻找最大分数
            int maxEval = -INF;
            for (GameMove move : moves) {
                ChessPiece[] nextBoard = makeMove(board, move);
                int evalScore = minimax(nextBoard, depth - 1, false, alpha, beta, aiColor, validator);
                maxEval = Math.max(maxEval, evalScore);
                alpha = Math.max(alpha, evalScore);
                if (beta <= alpha) break; // Alpha-Beta剪枝
            }
            return maxEval;
        } else {
            // 对手 (最小化玩家) 寻找最小分数
            int minEval = INF;
            for (GameMove move : moves) {
                ChessPiece[] nextBoard = makeMove(board, move);
                int evalScore = minimax(nextBoard, depth - 1, true, alpha, beta, aiColor, validator);
                minEval = Math.min(minEval, evalScore);
                beta = Math.min(beta, evalScore);
                if (beta <= alpha) break; // Alpha-Beta剪枝
            }
            return minEval;
        }
    }

    /**
     * 局面评估函数
     * 从AI角度评估：分数越高对AI越有利
     *
     * @param pieces 棋盘状态
     * @param aiColor AI执子颜色
     * @return 评估分数
     */
    private int evaluateBoard(ChessPiece[] pieces, String aiColor) {
        int score = 0;
        for (ChessPiece piece : pieces) {
            int pieceValue = getBaseValue(piece);
            
            // 位置加成
            if (piece.type.equals("兵") && piece.y <= 4) { // 红方兵过河
                pieceValue += PROMOTED_PAWN_BONUS;
            } else if (piece.type.equals("卒") && piece.y >= 5) { // 黑方卒过河
                pieceValue += PROMOTED_PAWN_BONUS;
            }
            
            // 将/帅安全评估
            if (piece.type.equals("帅") || piece.type.equals("将")) {
                // 检查将/帅周围是否有己方棋子保护
                int protection = countProtection(pieces, piece.x, piece.y, piece.color);
                pieceValue += protection * 5; // 每个保护棋子加5分
                
                // 检查是否被对方攻击
                boolean isAttacked = isUnderAttack(pieces, piece.x, piece.y, piece.color);
                if (isAttacked) {
                    pieceValue -= 30; // 如果被攻击，减分
                }
            }

            // 根据AI颜色决定加分还是减分
            if (piece.color.equals(aiColor)) {
                score += pieceValue;
            } else {
                score -= pieceValue;
            }
        }
        
        // 添加一点随机性避免AI在同等局面下总走相同的棋
        score += new Random().nextInt(3) - 1;
        
        return score;
    }
    
    /**
     * 计算指定位置的棋子受到的保护数量
     */
    private int countProtection(ChessPiece[] pieces, int x, int y, String color) {
        int count = 0;
        for (ChessPiece piece : pieces) {
            if (piece.color.equals(color) && !(piece.x == x && piece.y == y)) {
                // 检查这个棋子是否能走到指定位置（保护）
                // 这里简化处理，只检查是否在同一行或同一列
                if ((piece.x == x || piece.y == y) && 
                    Math.abs(piece.x - x) <= 2 && Math.abs(piece.y - y) <= 2) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * 检查指定位置是否被对方攻击
     */
    private boolean isUnderAttack(ChessPiece[] pieces, int x, int y, String myColor) {
        String opponentColor = myColor.equals("RED") ? "BLACK" : "RED";
        for (ChessPiece piece : pieces) {
            if (piece.color.equals(opponentColor)) {
                // 简化检查：只需检查是否在附近
                if (Math.abs(piece.x - x) <= 2 && Math.abs(piece.y - y) <= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取棋子基础价值
     *
     * @param piece 棋子
     * @return 棋子价值
     */
    private int getBaseValue(ChessPiece piece) {
        switch (piece.type) {
            case "帅":
            case "将":
                return KING_VALUE;
            case "车":
                return ROOK_VALUE;
            case "马":
                return KNIGHT_VALUE;
            case "炮":
                return CANNON_VALUE;
            case "仕":
            case "士":
                return ADVISOR_VALUE;
            case "相":
            case "象":
                return BISHOP_VALUE;
            case "兵":
                return PAWN_VALUE;
            case "卒":
                return PAWN_VALUE;
            default:
                return 0;
        }
    }

    /**
     * 生成所有合法移动
     *
     * @param pieces 棋盘状态
     * @param playerColor 玩家颜色
     * @param validator 移动规则验证器
     * @return 合法移动列表
     */
    private List<GameMove> generateLegalMoves(ChessPiece[] pieces, String playerColor, MoveRuleValidator validator) {
        List<GameMove> legalMoves = new ArrayList<>();
        for (ChessPiece piece : pieces) {
            if (piece.color.equals(playerColor)) {
                // 特殊处理将/帅的移动，避免不必要的移动
                if (piece.type.equals("将") || piece.type.equals("帅")) {
                    // 限制将/帅的移动，除非必要时不移动
                    for (int x = 0; x < 9; x++) {
                        for (int y = 0; y < 10; y++) {
                            if (validator.checkMove(piece.x, piece.y, x, y) == MoveRuleValidator.MoveValidationResult.VALID) {
                                // 只有在必要时才允许将/帅移动
                                if (isNecessaryKingMove(pieces, piece, x, y, playerColor, validator)) {
                                    legalMoves.add(new GameMove(piece.x, piece.y, x, y, 0, piece.type, piece.color, getPieceAt(pieces, x, y)));
                                }
                            }
                        }
                    }
                } else {
                    // 其他棋子正常处理
                    for (int x = 0; x < 9; x++) {
                        for (int y = 0; y < 10; y++) {
                            if (validator.checkMove(piece.x, piece.y, x, y) == MoveRuleValidator.MoveValidationResult.VALID) {
                                legalMoves.add(new GameMove(piece.x, piece.y, x, y, 0, piece.type, piece.color, getPieceAt(pieces, x, y)));
                            }
                        }
                    }
                }
            }
        }
        return legalMoves;
    }
    
    /**
     * 判断将/帅的移动是否必要
     */
    private boolean isNecessaryKingMove(ChessPiece[] pieces, ChessPiece king, int toX, int toY, String playerColor, MoveRuleValidator validator) {
        // 如果是吃子，则认为是必要的
        ChessPiece target = getPieceAt(pieces, toX, toY);
        if (target != null) {
            return true;
        }
        
        // 检查是否被将军，如果是，则移动可能是必要的
        boolean isCurrentlyInCheck = isKingInCheck(pieces, king, playerColor, validator);
        if (isCurrentlyInCheck) {
            return true;
        }
        
        // 随机允许一定比例的将/帅移动，避免完全不动
        return Math.random() < 0.2;
    }
    
    /**
     * 检查将/帅是否被将军
     */
    private boolean isKingInCheck(ChessPiece[] pieces, ChessPiece king, String playerColor, MoveRuleValidator validator) {
        String opponentColor = playerColor.equals("RED") ? "BLACK" : "RED";
        
        // 查找将/帅的位置
        int kingX = king.x;
        int kingY = king.y;
        
        // 检查对方是否有棋子可以攻击将/帅
        for (ChessPiece piece : pieces) {
            if (piece.color.equals(opponentColor)) {
                if (validator.checkMove(piece.x, piece.y, kingX, kingY) == MoveRuleValidator.MoveValidationResult.VALID) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * 模拟执行一步棋
     *
     * @param pieces 当前棋盘状态
     * @param move 要执行的移动
     * @return 执行后的棋盘状态
     */
    private ChessPiece[] makeMove(ChessPiece[] pieces, GameMove move) {
        ChessPiece[] newPieces = new ChessPiece[pieces.length];
        for (int i = 0; i < pieces.length; i++) {
            newPieces[i] = new ChessPiece(pieces[i]);
        }

        ChessPiece movedPiece = null;
        for (ChessPiece piece : newPieces) {
            if (piece.x == move.fromX && piece.y == move.fromY) {
                movedPiece = piece;
                break;
            }
        }

        if (movedPiece != null) {
            movedPiece.x = move.toX;
            movedPiece.y = move.toY;
        }

        if (move.capturedPiece != null) {
            List<ChessPiece> pieceList = new ArrayList<>();
            for (ChessPiece piece : newPieces) {
                if (piece.x == move.capturedPiece.x && piece.y == move.capturedPiece.y) {
                    continue;
                }
                pieceList.add(piece);
            }
            newPieces = pieceList.toArray(new ChessPiece[0]);
        }

        return newPieces;
    }

    /**
     * 检查游戏是否结束（一方将领被吃）
     *
     * @param pieces 棋盘状态
     * @return 游戏是否结束
     */
    private boolean isGameOver(ChessPiece[] pieces) {
        boolean redKingFound = false;
        boolean blackKingFound = false;
        for (ChessPiece piece : pieces) {
            if (piece.type.equals("帅")) {
                redKingFound = true;
            }
            if (piece.type.equals("将")) {
                blackKingFound = true;
            }
        }
        return !redKingFound || !blackKingFound;
    }

    /**
     * 获取指定位置的棋子
     *
     * @param pieces 棋盘状态
     * @param x X坐标
     * @param y Y坐标
     * @return 指定位置的棋子，如果没有则返回null
     */
    private ChessPiece getPieceAt(ChessPiece[] pieces, int x, int y) {
        for (ChessPiece piece : pieces) {
            if (piece.x == x && piece.y == y) {
                return piece;
            }
        }
        return null;
    }
}