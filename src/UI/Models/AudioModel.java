package UI.Models;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class AudioModel {

    // 单例模式：确保全局只有一个背景音乐在播放
    private static AudioModel instance;
    private MediaPlayer mediaPlayer;

    private AudioModel() {}

    public static AudioModel getInstance() {
        if (instance == null) {
            instance = new AudioModel();
        }
        return instance;
    }

    /**
     * 方法 1: 播放背景音乐
     * 特性: 自动循环 + 异常捕获 + 切换歌曲自动停止上一首
     * @param path 音乐文件的路径 (例如 "/music/bgm.mp3")
     */
    public void playBGM(String path) {
        // 如果当前已经在播放音乐，先停止并释放，防止重叠
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        try {
            // 获取资源
            URL resource = getClass().getResource(path);

            // 防崩溃检查：如果路径写错导致找不到文件，手动抛出异常
            if (resource == null) {
                throw new RuntimeException("路径错误: 找不到文件 " + path);
            }

            // 创建媒体对象
            Media media = new Media(resource.toExternalForm());
            mediaPlayer = new MediaPlayer(media);

            // 设置无限循环 (INDEFINITE)
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            // 设置默认音量
            mediaPlayer.setVolume(0.3);

            // 开始播放
            mediaPlayer.play();
            System.out.println("BGM 正在播放: " + path);

        } catch (Exception e) {
            // 捕获所有异常（文件不存在、格式不支持等），保证程序不闪退
            System.err.println("！！！BGM 加载失败！！！");
            System.err.println("错误信息: " + e.getMessage());
        }
    }

    /**
     * 方法 2: 设置静音状态
     * 特性: 只是让声音消失，进度条在后台继续走 (静默播放)
     * @param isMuted true=静音, false=有声
     */
    public void setMute(boolean isMuted) {
        // 必须判空，防止在音乐没加载成功时调用导致空指针异常
        if (mediaPlayer != null) {
            mediaPlayer.setMute(isMuted);
        }
    }

    /**
     * 辅助方法: 获取当前是否静音
     * 用于这一秒你想知道现在的状态
     */
    public boolean isMuted() {
        if (mediaPlayer != null) {
            return mediaPlayer.isMute();
        }
        return false;
    }
}