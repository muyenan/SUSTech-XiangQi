package UI.Models;

import java.io.File;

public class GetAppPath {
    public static String getAppPath() {
        String home = System.getProperty("user.home");

        File dir = new File(home, "xiangQi");

        if (!dir.exists()) dir.mkdirs();

        return dir.getAbsolutePath();
    }
}
