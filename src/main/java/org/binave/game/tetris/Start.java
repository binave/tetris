package org.binave.game.tetris;


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
                        TetrisOnlineServer.main(null);
                        break;
                    case "--online-client":
                    case "-b":
                        if (args.length != 2) {
                            System.err.println("[ERROR] 需要命令末尾输入主机的 ip。\n\n");
                            help();
                            System.exit(1);
                        }
                        TetrisOnlineClient.main(new String[]{args[1]});
                        break;
                    default:
                        help();
                }
        }
    }


    private static void help() {
        System.err.println(
                "usage: java -jar [jar_path] [option]\n\n" +
                        " 使用参数 (option)：\n\n" +
                        "     --help,   -h\n" +
                        "         显示此帮助信息。\n\n" +
                        "     --single, -1\n" +
                        "         经典玩法。\n" +
                        "         使用方向键控制方块。\n" +
                        "         方向键 “上” 用于方块翻转，“空格” 为一落到底。\n" +
                        "         “P” 暂停。\n\n" +
                        "     --dual,   -2\n" +
                        "         同屏双人玩法。\n" +
                        "         主机玩家由“WDSA” 控制方块的移动和翻转，“Q” 消耗 SP 更换方块，\n" +
                        "         副机玩家使用方向键控制方块，“/” 消耗 SP 更换方块，\n" +
                        "         “空格” 暂停，“ESC” 退出游戏。\n\n" +
                        "     双人玩法不再是获得分数，而是通过给对手增加麻烦来搞死对方为获胜条件。\n\n" +
                        "     --online-server,  -a\n" +
                        "         局域网双人玩法，主机。\n" +
                        "         需要先启动，等待副机启动连接后，使用 “P” 开始，游戏中用 “P” 暂停。\n" +
                        "         操作方法与 “经典玩法” 相同，“左 SHIFT” 消耗 SP 更换方块。\n" +
                        "         获胜条件与双人玩法一致。\n\n" +
                        "     --online-client,  -b    [ipv4]\n" +
                        "         局域网双人玩法，副机。\n" +
                        "         需要输入主机的 ip 启动。\n" +
                        "         操作方法与 “经典玩法” 相同，“左 SHIFT”消耗 SP 更换方块。\n" +
                        "         获胜条件与双人玩法一致。\n"

        );
    }
}
