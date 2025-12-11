package UI.Models;

import javafx.scene.text.Font;
import java.io.InputStream;

public class FontManager {

    private static final String[] FONT_FILES = {
        "fonts/DuanNingXingShu.ttf",
        "fonts/仿宋_GB2312.ttf"
    };

    public static void loadAllFonts() {
        for (String fontFile : FONT_FILES) {
            String resourcePath = "Resource/" + fontFile;
            try (InputStream is = FontManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    System.err.println("错误: 找不到字体资源文件: " + resourcePath);
                    continue;
                }
                Font loadedFont = Font.loadFont(is, 10); // Size doesn't matter, it just loads the font family.
                if (loadedFont != null) {
                    System.out.println("成功加载CSS字体: " + fontFile + ", Family: " + loadedFont.getFamily());
                } else {
                    System.err.println("警告: Font.loadFont 返回 null 对于: " + resourcePath);
                }
            } catch (Exception e) {
                System.err.println("加载CSS字体失败: " + fontFile);
                e.printStackTrace();
            }
        }
    }
}
