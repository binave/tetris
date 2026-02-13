package org.binave.game.tetris;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.binave.game.tetris.play.TetrisClassic;
import org.binave.game.tetris.play.TetrisDual;
import org.binave.game.tetris.play.TetrisOnlineClient;
import org.binave.game.tetris.play.TetrisOnlineServer;

/**
 * 游戏入口
 *
 */
public class Start {

    public static void main(String[] args) {
        // Native Image 忽略非致命 JNI 错误
        // Win32FontManager 通过 JNI 回调时可能抛出 NoSuchMethodError
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (e instanceof NoSuchMethodError &&
                e.getMessage() != null &&
                e.getMessage().contains("toLowerCase")) {
                // 忽略 String.toLowerCase(Locale) JNI 错误
                return;
            }
            // 其他异常正常输出
            System.err.printf("Exception in thread \"%s\" %s%n", t.getName(), e);
            e.printStackTrace(System.err);
        });

        // Native Image 禁用硬件加速
        // 必须在 AWT 组件初始化之前设置，避免 D3D/OpenGL 相关 JNI 调用
        System.setProperty("sun.java2d.d3d", "false");
        System.setProperty("sun.java2d.opengl", "false");
        System.setProperty("sun.java2d.ddforcevram", "false");

        // Native Image java.home 修复
        // 必须在 AWT 组件初始化之前设置
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            javaHome = System.getenv("JAVA_HOME");
        }
        // 无论 java.home 是否为空，都生成字体配置到临时目录
        String fontConfigDir = getFontConfigDir();
        ensureFontConfig(fontConfigDir);
        if (javaHome == null) {
            System.setProperty("java.home", fontConfigDir);
        }

        if (args == null || args.length == 0) {
            help();
        } else {
            if (args[0] == null) {
                help();
            } else

                switch (args[0].toLowerCase()) {
                    case "--help":
                    case "-h":
                        help();
                        break;
                    case "--single":
                    case "-1":
                        TetrisClassic.main(null);
                        break;
                    case "--dual":
                    case "-2":
                        TetrisDual.main(null);
                        break;
                    case "--online-server":
                    case "-a":
                        TetrisOnlineServer.main(
                                args.length > 1
                                        ? new String[]{args[1]}
                                        : new String[]{null}
                        );
                        break;
                    case "--online-client":
                    case "-b":
                        if (args.length < 2) {
                            System.err.printf("[ERROR] Host IP is required at the end of the command.%n%n");
                            System.exit(1);
                        }
                        TetrisOnlineClient.main(
                                args.length > 2
                                        ? new String[]{args[1], args[2]}
                                        : new String[]{args[1]}
                        );
                        break;
                    default:
                        help();
                }
        }
    }


    private static void help() {
        System.err.println(
                """
                usage: java -jar [jar_path] [option]
                
                Options:
                    --help,   -h
                        Show this help message.
                
                    --single, -1
                        Classic mode.
                        Use arrow keys to control the block.
                        Up arrow rotates the block, Space drops the block instantly, Left Shift consumes SP to swap blocks.
                        Press P to pause, ESC to quit the game.
                
                    --dual,   -2
                        Same-screen two-player mode.
                        Player 1 (host) uses WASD for movement and rotation, Z for instant drop, Q consumes SP to swap blocks.
                        Player 2 uses arrow keys for movement, / for instant drop, . (period) consumes SP to swap blocks.
                        Space pauses the game, ESC quits.
                        In two-player mode, the goal is no longer to score points, but to mess up your opponent and cause them to lose.
                
                    --online-server,  -a  [[port]]
                        LAN two-player mode (host).
                        Must be started first. Optional listening port can be specified. After the client connects, press P to start; press P again during gameplay to pause.
                        Controls are the same as in classic mode.
                        Winning condition is the same as in two-player mode.
                
                    --online-client,  -b  [ipv4] [[port]]
                        LAN two-player mode (client).
                        Requires the host's IP to start.
                        Controls are the same as in classic mode. The client cannot control pause.
                        Winning condition is the same as in two-player mode.
                """);
    }

    /**
     * 从字符串中获得端口，失败的话返回默认端口
     */
    public static int getPort(String portStr) {
        if (portStr == null)
            return 12000;
        try {
            int port = Integer.parseInt(portStr);
            if (port < 1000 || port > 65535)
                throw new NumberFormatException();
            return port;
        } catch (NumberFormatException ignored) {
            System.err.printf("[ERROR] Cannot parse '%s' as a valid port number.%nPort must be between 1000 and 65535.%n", portStr);
            System.exit(2); // 关闭进程
            return -1;
        }
    }

    /**
     * 获取字体配置目录（临时目录 + 应用唯一标识）
     * 格式: $TEMP/tetris-e8f4a2b1/lib/
     */
    private static String getFontConfigDir() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        // 使用应用名 + 固定 UUID 避免与其他应用冲突
        String appDir = "tetris-e8f4a2b1";
        return new File(tmpDir, appDir).getAbsolutePath();
    }

    /**
     * Native Image 字体配置自动生成
     * FontConfiguration 会查找 java.home/lib/fontconfig.properties
     */
    private static void ensureFontConfig(String baseDir) {
        File libDir = new File(baseDir, "lib");
        File fontConfig = new File(libDir, "fontconfig.properties");

        if (fontConfig.exists()) {
            return; // 已存在，无需重复生成
        }

        // Windows 字体配置（硬编码）
        String fontConfigContent = """
            # Font configuration for GraalVM Native Image on Windows
            version=1
            sequence.allfonts=default
            sequence.serif=default
            sequence.sansserif=default
            sequence.monospaced=default
            serif.plain.latin-1=Times New Roman
            serif.bold.latin-1=Times New Roman Bold
            serif.italic.latin-1=Times New Roman Italic
            serif.bolditalic.latin-1=Times New Roman Bold Italic
            sansserif.plain.latin-1=Arial
            sansserif.bold.latin-1=Arial Bold
            sansserif.italic.latin-1=Arial Italic
            sansserif.bolditalic.latin-1=Arial Bold Italic
            monospaced.plain.latin-1=Consolas
            monospaced.bold.latin-1=Consolas Bold
            monospaced.italic.latin-1=Consolas Italic
            monospaced.bolditalic.latin-1=Consolas Bold Italic
            dialog.plain.latin-1=Arial
            dialog.bold.latin-1=Arial Bold
            dialog.italic.latin-1=Arial Italic
            dialog.bolditalic.latin-1=Arial Bold Italic
            dialoginput.plain.latin-1=Consolas
            dialoginput.bold.latin-1=Consolas Bold
            dialoginput.italic.latin-1=Consolas Italic
            dialoginput.bolditalic.latin-1=Consolas Bold Italic
            exclusion.serif.0=-none-
            exclusion.sansserif.0=-none-
            exclusion.monospaced.0=-none-
            filename.Times_New_Roman=TIMES.TTF
            filename.Times_New_Roman_Bold=TIMESBD.TTF
            filename.Times_New_Roman_Italic=TIMESI.TTF
            filename.Times_New_Roman_Bold_Italic=TIMESBI.TTF
            filename.Arial=ARIAL.TTF
            filename.Arial_Bold=ARIALBD.TTF
            filename.Arial_Italic=ARIALI.TTF
            filename.Arial_Bold_Italic=ARIALBI.TTF
            filename.Consolas=CONSOLA.TTF
            filename.Consolas_Bold=CONSOLAB.TTF
            filename.Consolas_Italic=CONSOLAI.TTF
            filename.Consolas_Bold_Italic=CONSOLAZ.TTF
            """;

        try {
            libDir.mkdirs();
            try (FileWriter writer = new FileWriter(fontConfig)) {
                writer.write(fontConfigContent);
            }
            System.out.printf("[Native Image] Font config generated: %s%n", fontConfig.getAbsolutePath());
        } catch (IOException e) {
            System.err.printf("[Native Image] Failed to create font config: %s%n", e.getMessage());
        }
    }

}
