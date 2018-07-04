/*
 * Copyright (c) 2015 nidnil@icloud.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.binave.game.tetris.play;

import org.binave.game.tetris.Start;
import org.binave.game.tetris.common.ImageLoader;
import org.binave.game.tetris.common.UDPArrayAlter;
import org.binave.game.tetris.entity.Cell;
import org.binave.game.tetris.entity.Tetromino;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;
import java.util.Timer;

import javax.swing.*;

/**
 * @version 1.02
 *
 *          由定时触发控制画面刷新和自动下降，
 *          其他动作均由键盘监听主动触发动作。
 *
 *          俄罗斯方块主程序
 *          用于创建方块对象，判断方块越界，画面刷新，键盘监听。
 *          <b>注意：</b>这里的方块指的是由四个格子组成的整体，而格子就是Cell，且格子有存在于背景上和不在背景上两种
 */
public class TetrisOnlineServer extends JPanel {
    private static final long serialVersionUID = 1L;
    private DatagramSocket ds;      // UDP socket
    private UDPArrayAlter sua;      // 处理发送数组
    private DatagramPacket sdp;     // 发送包
    private int ClientPort;         // 记录客户端端口号

    /**
     * 声明方块数组
     */
    private Tetromino[][] tetromino;

    /**
     * 背景方块图片存储数组
     */
    private byte[][][] backGround;
    private byte[][] udpBg;     // 1P，2P 的背景发送数组
    private byte[] udpCell;     // 方块发送数组
    /**
     * 声明游戏状态
     */
    private boolean gameStart;
    /**
     * 动作开关
     */
    private boolean[] turnLeft;         // 手动开启或关闭左移
    private boolean[] allowLeft;        // 是否允许左移
    private boolean[] turnRight;        // 手动开启或关闭右移
    private boolean[] allowRight;       // 是否允许右移
    private boolean[] turnDrop;     // 手动开启或关闭下降
    private boolean[] HardDrop;     // 手动开启硬下降（一降到底）
    private boolean[] autoDrop;     // 定时触发自动下落
    private boolean[] allowDrop;            // 是否允许下降
    private boolean[] turnRotate;           // 手动开启旋转
    private boolean[] changeTetromino;      // 是否发动技能
    private boolean[] backGroundSend;       // 是否发送背景信息
    /**
     * 动作开关计数器
     */
    private int[] leftTimes;
    private int[] rightTimes;
    /**
     * 游戏点
     */
    private int[] sP;       // 技能点。
    private int[] line;     // 消除行数
    private int[] hard;     // 速度调节
    private int[] win;      // 胜点
    /**
     * 玩家id
     */
    private int[] tetId;
    private int[] atRow;            // 从下至上第一个空行行数
    private int[] cutLen;           // 记录发送数组有效长度
    private static int[] times;     // 次数记录，用于调整自动下落频率
    private int width;      // 界面宽度
    private int height;     // 界面长度
    /**
     * 方块显示位置修正
     */
    private static final int booboo = 26;
    /**
     * 旋转计数器变量，用于恢复原角度之用
     */
    private int rotate;

    private TetrisOnlineServer(int port, int row, int col) {
        int p = 2;      // 对战人数
        ClientPort = -1;        // 端口初始值
        height = row;
        width = col;
        tetromino = new Tetromino[p][2];        // 建立两个方块对象，分别用于控制下落或预览，并可交换彼此
        for (int j = 0; j < tetromino.length; j++) {
            tetromino[j][0] = new Tetromino();
            tetromino[j][1] = new Tetromino();
            tetromino[j][1].tetromino();        // 初始化方块属性
        }
        udpBg = new byte[2][76];        // 1P 或 2P 的背景，消除行数
        udpCell = new byte[18];         // 1P 和 2P 移动方块，预览方块，SP点，胜点
        backGround = new byte[p][row][col];     // 静止方块图片存储数组，会将符合条件的移动方块绘制到此数组上
        gameStart = true;       // 允许游戏进行
        turnLeft = new boolean[p];
        allowLeft = new boolean[p];
        turnRight = new boolean[p];
        allowRight = new boolean[p];
        turnDrop = new boolean[p];
        HardDrop = new boolean[p];
        autoDrop = new boolean[p];
        allowDrop = new boolean[p];
        turnRotate = new boolean[p];
        changeTetromino = new boolean[p];
        backGroundSend = new boolean[p];
        leftTimes = new int[p];
        rightTimes = new int[p];
        atRow = new int[p];
        cutLen = new int[p];
        tetId = new int[p];
        sP = new int[p];
        line = new int[p];
        win = new int[p * 2];
        times = new int[3];
        hard = new int[p];
        sua = new UDPArrayAlter();
        try {
            ds = new DatagramSocket(port);
            sdp = new DatagramPacket(udpCell, udpCell.length);
        } catch (SocketException ignored) {
        }
        exchangeTetromino(0);       // 切换默认方块组
        exchangeTetromino(1);
    }

    /**
     * 局域网俄罗斯方块【服务端入口】
     */
    public static void main(String[] args) {

        int port = Start.getPort(args[0]);

        System.out.println("监听 " + port + " 端口");

        JFrame frame = new JFrame("Tetris");        // 建立画面
        final TetrisOnlineServer bg = new TetrisOnlineServer(port, 20, 10);   // 设置背景宽高
        frame.add(bg);
        frame.setSize(ImageLoader.backgroundDual.getWidth(), ImageLoader.backgroundDual.getHeight());       // 布画大小
//         frame.setAlwaysOnTop(true);        // 总在最上面
        frame.setUndecorated(true);         // 去掉边框
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);       // 关闭画面时停止程序
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);     // 显示画面
        bg.action();                // 调用定时触发和键盘监听
    }

    /**
     * 建立一个线程接收客户端发送信息，键盘监听和定时触发，配合游戏状态变量控制游戏状态
     */
    private void action() {
        Thread receive = new Thread() {         // 重写线程方法
            private byte[] rec = new byte[1];       // 接收客户端信息的数组
            private DatagramPacket rdp = new DatagramPacket(rec, rec.length);       // UDP接收包
            private UDPArrayAlter rua = new UDPArrayAlter();        // 提取接收数组的信息

            public void run() {
                rua.link(rec);      // 链接接收数组
                while (true) {
                    try {
                        ds.receive(rdp);        // 接收客户端 DatagramPacket 包
                        rua.resetPoll();        // 重置处理位置
                        if (ClientPort == -1 && !rua.poll() && !rua.poll()
                                && rua.poll(6) == 63) {// 确定新客户端 ip、端口
                            sdp.setAddress(rdp.getAddress());
                            sdp.setPort(ClientPort = rdp.getPort());
                        } else if (
                                rdp.getAddress().getHostName().equals(sdp.getAddress().getHostName())
                                ) {
                            // 判断是否是客户端发送的信息
                            times[2] = 0;       // 重置延迟计数器
                            if (rua.poll()) {   // 如果是客户端键盘指令则读取指令
                                turnRotate[1] = rua.poll();
                                turnRight[1] = rua.poll();
                                turnDrop[1] = rua.poll();
                                turnLeft[1] = rua.poll();
                                HardDrop[1] = rua.poll();
                                changeTetromino[1] = rua.poll();
                            } else {    // 如果不是按键指令
                                int i = rua.poll(1);
                                if (rua.poll(6) == line[i])// 是否是背景回馈信息，表示客户端成功接到背景信息
                                    backGroundSend[i] = false;      // 关闭继续发送背景信息
                                if (!gameStart && rua.peek() == 63) {// 是否是客户端请求重链接的信息
                                    backGroundSend[0] = backGroundSend[1] = true;       // 发送背景
                                    sdp.setPort(rdp.getPort());     // 保存客户端的新端口号
                                }
                            }
                        }
                    } catch (IOException ignored) {
                    }
                }
            }
        };
        receive.start();

        this.addKeyListener(new KeyAdapter() {      // 键盘监听匿名内部类
            public void keyPressed(KeyEvent e) {    // 接收按键按下事件
                switch (e.getKeyCode() + (e.getKeyLocation() - 1) * 1000) {
                    case KeyEvent.VK_UP:        // 上键按下
                        turnRotate[0] = true;   // 允许顺时针九十度旋转
                        break;
                    case KeyEvent.VK_RIGHT:     // 右键按下
                        turnRight[0] = true;    // 允许右移
                        break;
                    case KeyEvent.VK_DOWN:      // 下键按下
                        turnDrop[0] = true;     // 允许加速下落
                        break;
                    case KeyEvent.VK_LEFT:      // 左键按下
                        turnLeft[0] = true;     // 允许左移
                        break;
                    case KeyEvent.VK_SPACE:     // 空格键按下
                        HardDrop[0] = true;     // 允许循环加速下落
                        break;
                    case KeyEvent.VK_SHIFT + 1000:  // 左 SHIFT 键按下
                        changeTetromino[0] = true;  // 使用下一个方块
                        break;
                }
            }

            /**
             * 接收按键弹起事件 左右 Ctrl Shift Alt 的 KeyCode 值相同 ，但 KeyLocation 值不同， 左边均为
             * 2，右边均为 3 数字键均为 4，其他为 1。
             */
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode() + (e.getKeyLocation() - 1) * 1000) {
                    case KeyEvent.VK_RIGHT:     // 右键弹起
                        turnRight[0] = false;   // 禁止右移
                        break;
                    case KeyEvent.VK_DOWN:      // 下键弹起
                        turnDrop[0] = false;    // 禁止加速下落
                        break;
                    case KeyEvent.VK_LEFT:      // 左键弹起
                        turnLeft[0] = false;    // 禁止左移
                        break;
                    case KeyEvent.VK_P:         // P 键弹起切换游戏暂停、开始状态
                        gameStart = !gameStart;
                        break;
                    case KeyEvent.VK_ESCAPE:    // Esc 键弹起
                        System.exit(0);   // 强制退出该进程
                        break;
                }
            }
        });
        this.requestFocus();        // 接收键盘监听事件

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {    // 定时触发匿名內部類
            public void run() {
                times[0] = times[0] > 8 - ((--hard[0] > 0 ? hard[0] : 0) > 0 ? 9
                        : 0) ? 0 : times[0] + 1;        // 当随时自减的 hard 大于 0 ，令
                // times 一直等于 0；
                times[1] = times[1] > 8 - ((--hard[1] > 0 ? hard[1] : 0) > 0 ? 9
                        : 0) ? 0 : times[1] + 1;
                times[2] = times[2] > 50 ? 50 : times[2] + 1;       // 限制延迟计数器上限

                if (gameStart) {// 游戏运行时
                    if (times[0] == 0)
                        autoDrop[0] = true;     // 间隔自动下落
                    if (times[1] == 0)
                        autoDrop[1] = true;
                    start(0);       // 进行动作判定，画面控制
                    start(1);
                }

                repaint();      // 以固定频率刷新画面
            }
        }, 70, 70);
    }

    /**
     * 重新开始游戏。 初始化静态方块背景，游戏得分、难度等级、消除行数
     */
    private void initialise() {
        for (int i = 0; i < 2; i++) {
            line[i] = sP[i] = hard[i] = 0;      // 重置游戏数值
            exchangeTetromino(i);       // 交换预览方块与下落方块
            for (int row = 0; row < height; row++) {
                Arrays.fill(backGround[i][row], (byte) 0);      // 初始化静态方块背景
            }
        }
    }

    /**
     * 交换预览方块与下落方块
     *
     * @param i
     */
    private void exchangeTetromino(int i) {
        tetromino[i][tetId[i]].tetromino();     // 初始化方块属性
        tetId[i] = 1 - tetId[i];        // 交换下标
        move(-1, 3, i);     // 修正方块初始坐标
    }

    /**
     * 处理背景，奖励
     *
     * @param i
     */
    private void start(int i) {
        if (times[2] > 15)// 如果延迟过大，则认为丢失客户端并暂停
            gameStart = false;
        if (!gameStart && !autoDrop[i] && !turnDrop[i] && !turnLeft[i]
                && !turnRight[i] && !turnRotate[i] && !HardDrop[i])
            return;     // 没有动作指令时空循环

        if (win[2] != win[0] || win[3] != win[1]) {// 预备开始下一局
            win[2] = win[0];
            win[3] = win[1];
            initialise();       // 初始化静态方块，sP、消除行数
        }
        rotate = 3;     // 初始化旋转计数器为3
        if (turnRotate[i]) // 如果允许旋转
            rotateRight(i);     // 进行顺时针九十度旋转
        if (changeTetromino[i] && sP[i] > 0) {// 消耗SP发动变更方块技能
            sP[i]--;
            exchangeTetromino(i);
        }
        move(i);
        // 关闭自动下降和允许旋转开关
        autoDrop[i] = turnRotate[i] = HardDrop[i] = changeTetromino[i] = false;
        if (backGroundSend[i]) {// 发送背景信息
            try {
                sdp.setData(udpBg[i]);      // 选定预发送数组
                sdp.setLength(cutLen[i]);       // 设置有效发送长度
                if (ClientPort != -1)
                    ds.send(sdp);       // 链接客户端后开始发送数组
            } catch (IOException e) {
            }
        }
    }

    /**
     * 接收动作指令并执行合理的指令
     *
     * @param i
     */
    private void move(int i) {
        allowLeft[i] = allowRight[i] = allowDrop[i] = true;     // 初始化下降触碰开关
        limit(i);       // 测试越界并赋予动作开关状态
        int index = 0;      // 初始化临时计数器
        // 记录键盘按键连按（按住）周期。
        leftTimes[i] = turnLeft[i] ? leftTimes[i] + 1 : 0;
        rightTimes[i] = turnRight[i] ? rightTimes[i] + 1 : 0;
        for (Cell t : tetromino[i][tetId[i]].cells) {// 遍历移动方块的格子
            if (!allowDrop[i] && t.row >= 0 && (autoDrop[i] || turnDrop[i])) {// 方块下方为边界或背景方块：
                backGround[i][t.row][t.col] = tetromino[i][tetId[i]].imgColor;      // 将方块的颜色信息存入背景数组
                if (++index == 4) {// 循环四次后执行
                    HardDrop[i] = false;        // 停止自我循环
                    subLine(i);     // 去行
                    exchangeTetromino(i);       // 将预览方块的属性赋予下落方块，并给预览方块重新赋予随机属性
                    return;
                }
            } else {
                // 如果左移被允许，并且不处于按键连按第二周期，则方块左移
                t.col += leftTimes[i] != 2 && turnLeft[i] && allowLeft[i] ? -1 : 0;
                // 同上右移
                t.col += rightTimes[i] != 2 && turnRight[i] && allowRight[i] ? 1 : 0;
                // 如果自动下落或手动下落被允许，则方块下落
                t.row += (turnDrop[i] || autoDrop[i]) && allowDrop[i] ? 1 : 0;
            }
            // 如果方块中的格子处在 -1 行上，则不允许一降到底
            HardDrop[i] = t.row < 0 ? false : HardDrop[i];
        }
        if (HardDrop[i]) {// 如果允许一降到底，则自我循环
            autoDrop[i] = true;     // 开启自动下落
            move(i);        // 递归触发本方法
        }
    }

    private void move(int row, int col, int i) { // 传入坐标组并修正初始位置
        for (Cell t : tetromino[i][tetId[i]].cells) {
            t.row += row;
            t.col += col;
        }
    }

    private void subLine(int i) {
        int subLine = 0;        // 初始化加倍奖励计数器
        int line4 = 0;      // 初始化第四行计数器
        atRow[i] = 0;       // 初始化有效行数
        for (int row = height - 1; row >= 0; row--) {
            int inCell = 0; // 初始化背景格子计数器，记录有格子的个数
            for (int col = 0; col < width; col++) {// 从下至上遍历背景
                if (backGround[i][row][col] != 0)
                    inCell++;       // 统计背景中一行格子个数
            }
            if (inCell == width) {// 如果行已满
                if (++subLine == 3)
                    line4 = row;        // 连续去三行时记录第四行的新位置
                byte[] tmp = backGround[i][row];        // 备份消除行地址
                for (int j = 0; j < width; j++) {
                    tmp[j] = 0;     // 重置消除行的数组数值
                }
                System.arraycopy(backGround[i], 0, backGround[i], 1, row++);        // 去行
                backGround[i][0] = tmp;     // 还原消除行，保证没有行丢失
                line[i]++;      // 记录行数
            }
            // 如果有格子的行数已经达到顶层，先达到 60 行者为胜。
            if (row == 0 || line[1 - i] >= 60) {
                win[1 - i] += 1;
                gameStart = false;      // 判定游戏结束
                return;     // 跳出此方法
            }
            if (row < height - 1 && inCell == 0)
                break;      // 如果不是末尾行且此行没有格子，则跳出循环
            atRow[i] = row;     // 记录有效行数
        }
        switch (subLine) {// 根据消除行数选择奖励
            case 2:
                sP[i] += 3;     // 获得三个sP
                break;
            case 3:
                sP[i] += 5;     // 获得五个sP
                moveLine(line4, i);     // 传出第四行数组的地址
                break;
            case 4:
                sP[1 - i] = sP[1 - i] - sP[i] / 2 > 0 ? sP[1 - i] - sP[i] / 2 : 0;      // 扣掉对手sP点
                sP[i] += 7;     // 获得七个sP
                hard[1 - i] = 60;       // 给对手加速五秒
                break;
        }
        sP[i] = sP[i] > 30 ? 30 : sP[i];        // 限制 SP 个数为三十
        sandLine(i);        // 发送背景数组
    }

    /**
     * 将自己消除三行之上的一行移送到对手最下一行之下。 交换两个二位数组中的一个一维数组
     */
    private void moveLine(int line4, int i) {
        byte[] tmp = backGround[1 - i][0];      // 备份对手背景数组第一行的地址
        System.arraycopy(backGround[1 - i], 1, backGround[1 - i], 0, height - 1);       // 对手整个将背景上移一行
        backGround[1 - i][height - 1] = backGround[i][line4];       // 交换对手数组
        System.arraycopy(backGround[i], 0, backGround[i], 1, line4);        // 消除第四行
        backGround[i][0] = tmp;     // 交换对手数组
        atRow[1 - i] -= atRow[1 - i] > 0 ? 1 : 0;
        sandLine(1 - i);        // 发送对手数组信息
    }

    /**
     * 在方块落入背景时，向客户端发送背景信息，并等待回复
     *
     * @param i
     */
    private void sandLine(int i) {
        Arrays.fill(udpBg[i], (byte) 0);        // 清空 udpBg 传输数组内容
        backGroundSend[i] = true;       // 准备发送背景
        sua.link(udpBg[i]);     // 链接背景传输数组
        sua.offer(true);        // 向 udpBg[i] 中加入数字，说明这是背景数组
        sua.offer(i, 1);        // 向 udpBg[i] 中加入数字，区别 1P，2P
        sua.offer(line[i], 6);      // 向 udpBg[i] 中加入数字，显示 1P，2P 的消除行数
        for (int row = height - 1; row >= atRow[i]; row--) {
            // 遇到空行则不再继续
            for (byte b : backGround[i][row]) {
                sua.offer(b, 3);        // 将每个方块的颜色写入 3 位
            }
        }
        cutLen[i] = sua.close();        // 记录有效数组长度
    }

    /**
     * 左、右、下、旋转越界、重叠判断。 遍历方块，测试方块周围的状态并依此设置方块动作开关。 当动作开关为 false 时，将不允许执行相应的動作。
     */
    private void limit(int i) {
        for (Cell t : tetromino[i][tetId[i]].cells) {
            if (t.row < 0) {// 如果方块处在上边界之上，则仅允许自动下落
                turnLeft[i] = turnRight[i] = turnDrop[i] = HardDrop[i] = false;
                break;
            }
            if (turnLeft[i]
                    && (t.row < 0 || t.col == 0 || backGround[i][t.row][t.col - 1] != 0))// 同上，左面
                // 如果方块左方是边界或方块，则关闭左移开关
                allowLeft[i] = false;
            if (turnRight[i]
                    && (t.row < 0 || t.col == width - 1 || backGround[i][t.row][t.col + 1] != 0))// 同上，右面
                // 如果方块右方是边界或方块，则关闭右移开关
                allowRight[i] = false;
            if (t.row == height - 1 || backGround[i][t.row + 1][t.col] != 0)
                // 如果方块下方是边界或方块，则关闭下降开关
                allowDrop[i] = false;
        }
    }

    /**
     * 测试顺时针旋转 90 度 交换 row 与 col 的坐标并左右翻转 在交换过程中需要减去与0,0的间距，然后用2减去col，再将间距加回来。
     * 在0.0点交换 row 与 col 会得到与目标图样左右颠倒的图样， 这时候用 2 减去 col 则会得到顺时针旋转 90
     * 度的图样。再移动回原来的位置即可实现翻转
     */
    private void rotateRight(int i) {
        // 初始化空列计数器，获得背景数组长宽。
        int tmp, amend = 0, rowMin = height, colMin = width;
        for (Cell t : tetromino[i][tetId[i]].cells) {// 获得下落方块距 0,0 点最短距离
            rowMin = t.row < rowMin ? t.row : rowMin; // 获取距 0,0 点最小 row 值
            colMin = t.col < colMin ? t.col : colMin;       // 获取距 0,0 点最小 col 值
        }
        for (Cell t : tetromino[i][tetId[i]].cells) {// 交換 row 與 col 坐標
            tmp = t.row - rowMin;
            t.row = t.col - colMin + rowMin; // 将 col 值赋予 row
            t.col = 2 - tmp + colMin; // 将左右翻转后的 row 值赋予 col
            amend += colMin != t.col ? 1 : 0;       // 记录左一列是否为空
        }
        if (amend == 4)// 如果左一列均为空
            move(0, -1, i);     // 方块左移
        if (turnRotate[i]) // 防止此方法自我循环时执行此处
            for (Cell t : tetromino[i][tetId[i]].cells) {// 測試是否越界、重合
                if (t.row > height - 1 || t.row < 0 || t.col > width - 1
                        || t.col < 0 || backGround[i][t.row][t.col] != 0)
                    // 如果下落方块越界，或与背景中静态方块重合则关闭旋转开关
                    turnRotate[i] = false;
            }
        if (!turnRotate[i] && rotate-- > 0)// 如果旋转开关关闭，则再旋转三次回到原位
            rotateRight(i);     // 递归触发本方法
    }

    private Font[] font = {new Font("Monospaced", Font.BOLD, 15),
            new Font("Monospaced", Font.BOLD, 20),
            new Font("Monospaced", Font.BOLD, 35)};     // 设置文字大小字体等

    private Color[] fontColor = {new Color(0x333777), new Color(0x777333),
            new Color(0x777777)};       // 设置文字颜色

    /**
     * 绘制画面
     */
    public void paint(Graphics g) {
        g.drawImage(ImageLoader.backgroundDual, 0, 0, null); // 覆盖背景图片
        g.setFont(font[1]);
        g.setColor(fontColor[1]);
        g.drawString(win[0 + 2] + ":" + win[1 + 2], 380, 200); // 显示得分
        g.setFont(font[0]);
        g.setColor(fontColor[2]);
        g.drawString("1P SP: " + sP[0], 290, 155); // 显示得分
        g.drawString(" Line: " + line[0], 290, 175); // 显示行数
        g.setColor(fontColor[0]);
        g.drawString("2P SP: " + sP[1], 420, 225); // 显示得分
        g.drawString(" Line: " + line[1], 420, 245); // 显示行数
        g.translate(15, -12); // 调整相对位置

        Tetromino[] t = {tetromino[0][1 - tetId[0]],// 1P 预览方块
                tetromino[0][tetId[0]], // 1P 移动方块
                tetromino[1][1 - tetId[1]],// 2P 预览方块
                tetromino[1][tetId[1]] // 2P 移动方块
        };
        sua.link(udpCell);      // 链接方块发送数组并初始化链接计数器
        sua.offer(false);       // 不是背景信息（是方块信息）
        sua.offer(!gameStart);      // 是否加载暂停画面
        sua.offer(sP[0], 5);        // 1P 的SP点
        sua.offer(sP[1], 5);        // 2P 的SP点
        sua.offer(win[0 + 2], 5);       // 1P 胜点
        sua.offer(win[1 + 2], 5);       // 2P 胜点
        sua.offer(t[0].imgColor, 3);        // 1P 预览方块颜色
        sua.offer(t[1].imgColor, 3);        // 1P 移动方块颜色
        sua.offer(t[2].imgColor, 3);        // 2P 预览方块颜色
        sua.offer(t[3].imgColor, 3);        // 2P 移动方块颜色
        for (int j = 0; j < 4; j++) {
            Cell[] c = {t[0].cells[j], t[1].cells[j], t[2].cells[j],
                    t[3].cells[j]};
            // 在背景上绘制方块，根据对象的 imgColor 属性绘制 color 数组中对应下标的图片。
            g.drawImage(ImageLoader.color[t[0].imgColor - 1], c[0].col * booboo + 340, c[0].row
                    * booboo + booboo + 23, null);
            sua.offer(c[0].col, 2);     // 加入预览方块列坐标
            sua.offer(c[0].row, 1);
            if (c[1].row > -1)// 仅显示零行及以下的方块
                g.drawImage(ImageLoader.color[t[1].imgColor - 1], c[1].col * booboo, c[1].row
                        * booboo + booboo, null);
            sua.offer(c[1].col + 1, 4);     // 防止出现负数
            sua.offer(c[1].row + 1, 5);
            g.drawImage(ImageLoader.color[t[2].imgColor - 1], c[2].col * booboo + 340, c[2].row
                    * booboo + booboo + 275, null);
            sua.offer(c[2].col, 2);
            sua.offer(c[2].row, 1);
            if (c[3].row > -1)
                g.drawImage(ImageLoader.color[t[3].imgColor - 1], c[3].col * booboo + 512,
                        c[3].row * booboo + booboo, null);
            sua.offer(c[3].col + 1, 4);
            sua.offer(c[3].row + 1, 5);
        }
        try {
            sdp.setData(udpCell);       // 设定预发送数组
            sdp.setLength(sua.close());     // 设定发送数组有效长度
            if (ClientPort != -1)
                ds.send(sdp);       // 向客户端发送移动方块和预览方块信息
        } catch (IOException e) {
        }

        // 绘制背景，根据背景数组中的值调用 color 数组中相应下标的图片绘制于背景之上
        for (int row = height - 1; row >= 0; row--) {
            int inCell0 = 0, inCell1 = 0;
            for (int col = 0; col < width; col++) {
                if (backGround[0][row][col] != 0) {
                    inCell0++;
                    g.drawImage(ImageLoader.color[backGround[0][row][col] - 1], col
                            * booboo, row * booboo + booboo, null);
                }
                if (backGround[1][row][col] != 0) {
                    inCell1++;
                    g.drawImage(ImageLoader.color[backGround[1][row][col] - 1], col
                            * booboo + 512, row * booboo + booboo, null);
                }
            }
            if (row < height - 1 && inCell0 == 0 && inCell1 == 0)
                break;
        }
        if (!gameStart) {
            g.drawImage(ImageLoader.pause, 0, 0, null); // 覆盖背景图片
            g.setFont(font[2]);
            g.setColor(fontColor[2]);
            g.drawString(times[2] > 15 ? "    Offline..."
                    : "Press \"P\" to Start", 210, 220);
        }
    }
}
