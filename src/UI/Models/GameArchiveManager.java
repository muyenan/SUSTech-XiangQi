package UI.Models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import UI.MainGameUI.ChessPiece;
import UI.MainGameUI.GameMove;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.spec.KeySpec;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static UI.Models.GetAppPath.getAppPath;

public class GameArchiveManager {

    // 加密常量
    private static final String ENCRYPT_ALGORITHM = "AES";
    private static final String PBE_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String CIPHER_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final byte[] SALT = "ChineseChessSaltForArchive".getBytes(StandardCharsets.UTF_8);

    private final String username;
    private final String encryptionKeyBasis;

    public GameArchiveManager(String username, String encryptionKeyBasis) {
        this.username = username;
        this.encryptionKeyBasis = encryptionKeyBasis;
    }

    private Key deriveKey() throws Exception {
        KeySpec spec = new PBEKeySpec(encryptionKeyBasis.toCharArray(), SALT, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBE_ALGORITHM);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, ENCRYPT_ALGORITHM);
    }

    private String encrypt(String plainText) throws Exception {
        Key key = deriveKey();
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private String decrypt(String encryptedText) throws Exception {
        Key key = deriveKey();
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public boolean saveGame(ChessPiece[] pieces, List<GameMove> moves, String currentPlayerColor, boolean isAIGame) {
        return saveGame(pieces, moves, currentPlayerColor, isAIGame, null);
    }

    public boolean saveGame(ChessPiece[] pieces, List<GameMove> moves, String currentPlayerColor) {
        return saveGame(pieces, moves, currentPlayerColor, false, null);
    }

    public boolean saveGame(ChessPiece[] pieces, List<GameMove> moves, String currentPlayerColor, boolean isAIGame, String difficulty) {
        if (encryptionKeyBasis == null || encryptionKeyBasis.isEmpty()) {
            System.err.println("存档失败：加密密钥基础 (密码哈希) 为空。");
            return false;
        }

        String saveDir = getUserSaveDir(username);
        File dirFile = new File(saveDir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        // 构造文件名，包含游戏模式和难度信息
        String prefix = isAIGame ? "人机" : "save";
        if (isAIGame && difficulty != null && !difficulty.isEmpty()) {
            prefix += "_" + difficulty;
        }
        String fileName = prefix + "_" + System.currentTimeMillis() + ".json";
        File saveFile = new File(saveDir, fileName);

        // 创建存档数据对象
        GameArchiveData archiveData = new GameArchiveData(pieces, moves, currentPlayerColor);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonText = gson.toJson(archiveData);

        try (FileWriter writer = new FileWriter(saveFile)) {
            String encryptedData = encrypt(jsonText);
            writer.write(encryptedData);
            return true;
        } catch (Exception e) {
            System.err.println("保存或加密游戏数据时出错: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public GameArchiveData loadGame(String fileName) {
        if (encryptionKeyBasis == null || encryptionKeyBasis.isEmpty()) {
            System.err.println("加载失败：解密密钥基础 (密码哈希) 为空。");
            return null;
        }

        File saveFile = new File(getUserSaveDir(username), fileName);
        if (!saveFile.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
            String encryptedData = reader.readLine();
            if (encryptedData == null || encryptedData.isEmpty()) {
                System.err.println("存档文件内容为空或格式错误: " + fileName);
                return null;
            }

            String jsonText = decrypt(encryptedData);
            Gson gson = new Gson();

            return gson.fromJson(jsonText, GameArchiveData.class);

        } catch (Exception e) {
            System.err.println("加载或解密游戏数据时出错 (可能密钥错误): " + e.getMessage());
            return null;
        }
    }

    public List<SaveFileInfo> getSaveFiles() {
        File saveDir = new File(getUserSaveDir(username));
        if (!saveDir.exists()) return new ArrayList<>();

        File[] files = saveDir.listFiles((dir, name) -> name.endsWith(".json") && (name.startsWith("save_") || name.startsWith("人机_")));

        if (files == null) return new ArrayList<>();

        List<SaveFileInfo> saveFileInfos = new ArrayList<>();
        for (File file : files) {
            try {
                GameArchiveData data = loadGame(file.getName());
                if (data != null) {
                    saveFileInfos.add(new SaveFileInfo(file.getName(), data.lastMoveTimestamp));
                }
            } catch (Exception e) {
                // Ignore files that can't be parsed
            }
        }

        // 按时间倒序
        saveFileInfos.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        return saveFileInfos;
    }

    // 存档数据结构类
    public static class GameArchiveData {
        public ChessPiece[] pieces;
        public List<GameMove> moves;
        public String currentPlayerColor;
        public long lastMoveTimestamp;

        public GameArchiveData(ChessPiece[] pieces, List<GameMove> moves, String currentPlayerColor) {
            this.pieces = pieces;
            this.moves = moves;
            this.currentPlayerColor = currentPlayerColor;
            if (moves != null && !moves.isEmpty()) {
                this.lastMoveTimestamp = moves.get(moves.size() - 1).timestamp;
            } else {
                this.lastMoveTimestamp = System.currentTimeMillis();
            }
        }
    }

    public static class SaveFileInfo {
        private final String fileName;
        private final long timestamp;

        public SaveFileInfo(String fileName, long timestamp) {
            this.fileName = fileName;
            this.timestamp = timestamp;
        }

        public String getFileName() { return fileName; }
        public long getTimestamp() { return timestamp; }

        public String getDisplayName() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String prefix = fileName.startsWith("人机_") ? "[人机对战] " : "";
            return prefix + "存档时间: " + sdf.format(new Date(timestamp));
        }
    }

    public static String getUserSaveDir(String username) {
        String appPath = getAppPath();
        File userDir = new File(appPath, "Accounts/" + username + "/Saves");
        if (!userDir.exists()) {
            userDir.mkdirs();
        }
        return userDir.getAbsolutePath();
    }

    // 新增方法：更新已有的存档文件
    public boolean updateGame(String fileName, ChessPiece[] pieces, List<GameMove> moves, String currentPlayerColor) {
        if (encryptionKeyBasis == null || encryptionKeyBasis.isEmpty()) {
            System.err.println("存档失败：加密密钥基础 (密码哈希) 为空。");
            return false;
        }

        String saveDir = getUserSaveDir(username);
        File saveFile = new File(saveDir, fileName);

        // 创建存档数据对象
        GameArchiveData archiveData = new GameArchiveData(pieces, moves, currentPlayerColor);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonText = gson.toJson(archiveData);

        try (FileWriter writer = new FileWriter(saveFile)) {
            String encryptedData = encrypt(jsonText);
            writer.write(encryptedData);
            return true;
        } catch (Exception e) {
            System.err.println("保存或加密游戏数据时出错: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}