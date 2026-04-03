import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class Main {
    public static void main(String[] args) {
        // 动态加载 lib 目录下的所有 JAR 文件
        loadJarsFromLib();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new TAPortalApp());
    }

    /**
     * 从 lib 目录动态加载所有 JAR 文件到类路径
     */
    private static void loadJarsFromLib() {
        // 获取 lib 目录路径（相对于当前类文件位置）
        File libDir = new File("lib");
        if (!libDir.exists() || !libDir.isDirectory()) {
            // 尝试从项目根目录查找
            libDir = new File("../lib");
        }
        if (!libDir.exists() || !libDir.isDirectory()) {
            libDir = new File("TA_Job_Application_Module/lib");
        }
        if (!libDir.exists() || !libDir.isDirectory()) {
            libDir = new File(System.getProperty("user.dir"), "lib");
        }

        File[] jarFiles = libDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            return;
        }

        try {
            // 获取系统类加载器
            ClassLoader loader = Main.class.getClassLoader();
            if (loader == null) {
                loader = ClassLoader.getSystemClassLoader();
            }

            // 通过反射添加 JAR 文件到类路径
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);

            for (File jar : jarFiles) {
                try {
                    URL url = jar.toURI().toURL();
                    method.invoke(loader, url);
                    System.out.println("[Main] Loaded JAR: " + jar.getName());
                } catch (Exception e) {
                    System.err.println("[Main] Failed to load JAR: " + jar.getName());
                }
            }
        } catch (Exception e) {
            System.err.println("[Main] Failed to load JARs: " + e.getMessage());
        }
    }
}
