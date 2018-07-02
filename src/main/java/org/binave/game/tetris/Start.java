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
                            System.err.println("[ERROR] 需要输入在末尾 ip\n\n");
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
                "使用参数：\n\n" +
                        "    --help,   -h\n" +
                        "        显示帮助信息。\n\n" +
                        "    --single, -1\n" +
                        "        经典玩法。\n" +
                        "        方向键“上”转向，“空格”为一落到底\n\n" +
                        "    --dual,   -2\n" +
                        "        同屏双人玩法。\n" +
                        "        主机玩家“方向键”由“WDSA”来代替，“Q”消耗 SP 更换方块，\n" +
                        "        副机玩家使用方向键，“/”消耗 SP 更换方块，\n" +
                        "        “空格”暂停，“ESC”退出游戏\n\n" +
                        "    --online-server,  -a\n" +
                        "        局域网对战，主机。\n" +
                        "        需要先启动，等待副机启动后使用“P”开始，游戏中用“P”结束。\n" +
                        "        操作方法与“经典玩法”相同，“左 SHIFT”消耗 SP 更换方块。\n\n" +
                        "    --online-client,  -b    [ipv4]\n" +
                        "        局域网对战，副机。\n" +
                        "        需要输入主机的 ip 启动\n" +
                        "        操作方法与“经典玩法”相同，“左 SHIFT”消耗 SP 更换方块。\n"
        );
    }
}
