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

import org.binave.game.tetris.common.ImageLoader;
import org.binave.game.tetris.common.UDPArrayAlter;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @version 3.0
 *          <b>局域网对战模式（客户端）</b>
 *          服务端：
 *          负责运算以及绘制画面。
 *          向 2P 发送移动（预览）方块位置数据（不要求反馈），
 *          在移动方块到底时，向 2P 发送背景数组数据（要求反馈，若无反馈则重新发送）
 *          接收 2P 的按键指令。
 *          客户端：
 *          接收服务端发送的方块信息（移动方块或预览方块）并及时绘制。
 *          绘制数组中的背景信息。
 *          接收服务端发送的背景信息并及时进行回馈（多次），并将信息存入数组。
 *          向服务端发送按键指令。 规则：同 2.0 。先启动服务端，等待客户端接入。
 *          按键：
 *          方向键负责旋转以及移动，空格键硬降落，左 Shift 消耗 SP 更换方块。
 *          服务端暂停：按 P 键，会同时暂停客户端和服务端。再次按 P 键继续。
 *          客户端暂停：按 ESC 键退出即可，在保证服务端没关闭的情况下再次输入相同的 IP 即可继续游戏。
 */
public class TetrisOnlineClient extends JPanel {
    private static final long serialVersionUID = 1L;
    private UDPArrayAlter uaa;      // 还原服务端发送过来的信息
    private DatagramSocket ds;      // 建立 UDP
    private DatagramPacket dp;      // UDP 单独发送包
    private InetAddress ServerIp;       // 记录服务端 IP
    private int ServerPort;     // 记录服务端端口

    /**
     * 方块显示位置修正
     */
    private static final int booboo = 26;
    private int width;      // 背景宽度
    private int height;     // 背景高度
    private int counter;        // 背景信息成功接收的反馈值

    /**
     * UDP 数据接收数组
     */
    private byte[] udp;
    /**
     * 背景方块图片存储数组
     */
    private int[][] backGround;
    /**
     * 游戏点
     */
    private int[] line;     // 消除行数
    private int[] win;      // 胜点
    private int[] info;     // 方块信息和其他游戏数据

    /**
     * 动作开关
     */
    private boolean turnLeft;       // 手动开启或关闭左移
    private boolean turnRight;      // 手动开启或关闭右移
    private boolean turnDrop;       // 手动开启或关闭下降
    private boolean HardDrop;       // 手动开启硬下降（一降到底）
    private boolean turnRotate;     // 手动开启旋转
    private boolean changeTetromino;        // 发动技能
    private boolean gamePause;      // 是否开启暂停画面

    private TetrisOnlineClient(int row, int col) {
        height = row;
        width = col;
        ServerPort = 8088;
        gamePause = true;
        int p = 2;      // 玩家个数
        uaa = new UDPArrayAlter();
        udp = new byte[76];
        line = new int[p];
        win = new int[p];
        info = new int[4 + 4 + 4 * 8];

        // init
        for (int i = 0; i < info.length; i++) {
            info[i] = 1;
        }

        backGround = new int[p][row];       // 静止方块图片存储数组，会将符合条件的移动方块绘制到此数组上
        try {
            ds = new DatagramSocket();
            dp = new DatagramPacket(udp, udp.length);
        } catch (SocketException ignored) {
        }
    }

    /**
     * 局域网俄罗斯方块【客户端入口】
     */
    public static void main(String[] args) {

        if (args == null || args.length == 0) {
            System.exit(1);
            return;
        }

        JFrame frame = new JFrame("Tetris");        // 建立画面
        final TetrisOnlineClient bg = new TetrisOnlineClient(20, 10);       // 设置背景宽高
        frame.add(bg);
        frame.setSize(ImageLoader.backgroundDual.getWidth(), ImageLoader.backgroundDual.getHeight());       // 布画大小
        // frame.setAlwaysOnTop(true);      // 总在最上面
        frame.setUndecorated(true); // 去掉边框
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);       // 关闭画面时停止程序
        frame.setLocationRelativeTo(null);
        bg.start(frame, args[0]);   // 进行动作判定，画面控制
    }

    /**
     * 键盘监听和定时触发，配合游戏状态变量控制游戏状态
     */
    private void action() {
        this.addKeyListener(new KeyAdapter() { // 键盘监听匿名内部类
            public void keyPressed(KeyEvent e) {// 接收按键按下事件
                switch (e.getKeyCode() + (e.getKeyLocation() - 1) * 1000) {
                    case KeyEvent.VK_UP:// 上键按下
                        turnRotate = true;      // 允许顺时针九十度旋转
                        break;
                    case KeyEvent.VK_RIGHT:// 右键按下
                        turnRight = true;       // 允许右移
                        break;
                    case KeyEvent.VK_DOWN:// 下键按下
                        turnDrop = true;        // 允许加速下落
                        break;
                    case KeyEvent.VK_LEFT:// 左键按下
                        turnLeft = true;        // 允许左移
                        break;
                    case KeyEvent.VK_SPACE:// 空格键按下
                        HardDrop = true;        // 允许循环加速下落
                        break;
                    case KeyEvent.VK_SHIFT + 1000:// 左 SHIFT 键按下
                        changeTetromino = true;     // 使用下一个方块
                        break;
                }
            }

            /*
             * 接收按键弹起事件 左右 Ctrl Shift Alt 的 KeyCode 值相同 ，但 KeyLocation 值不同， 左边均为
             * 2，右边均为 3 数字键均为 4，其他为 1。
             */
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode() + (e.getKeyLocation() - 1) * 1000) {
                    case KeyEvent.VK_RIGHT:// 右键弹起
                        turnRight = false;      // 禁止右移
                        break;
                    case KeyEvent.VK_DOWN:// 下键弹起
                        turnDrop = false;       // 禁止加速下落
                        break;
                    case KeyEvent.VK_LEFT:// 左键弹起
                        turnLeft = false;       // 禁止左移
                        break;
                    case KeyEvent.VK_ESCAPE:// Esc 键弹起
                        System.exit(0);     // 强制退出该进程
                        break;
                }
            }
        });
        this.requestFocus();        // 接收键盘监听事件
    }

    /**
     * 死循环获取服务端指令，并发送指令
     */
    private void start(JFrame jf, String inputIp) {

//        // 使用控制台输入
//        Scanner sca = new Scanner(System.in);     // 控制台输入
//        String inputIp = null;
//        do {
//            System.out.println("输入服务端 IP：");
//            inputIp = sca.nextLine();
//        } while (!inputIp.matches("^[0-9]{1,3}(.[0-9]{1,3}){3}$"));       // 判断输入格式
//
//

        if (inputIp == null || !inputIp.matches("^[0-9]{1,3}(.[0-9]{1,3}){3}$")) {
            System.err.println("输入服务端 IP");
            System.exit(1);
        }

        jf.setVisible(true);        // 显示画面
        action();       // 启动将盘将听
        uaa.link(udp);      // 链接发送、接收数组准备处理
        uaa.offer(false);       // 不是操纵指令
        uaa.offer(false);       // 空占位（无用）
        uaa.offer(63, 6);       // 请求启动的指令
        try {
            ServerIp = InetAddress.getByName(inputIp);
            dp.setAddress(ServerIp);        // 设置 UDP 目标 IP
            dp.setPort(ServerPort);     // 设置 UDP 目标端口号
            dp.setData(udp);        // 设置预发送数组
            dp.setLength(uaa.close());      // 设置有效发送长度
            ds.send(dp);        // 发送启动指令
        } catch (Exception ignored) {
        }

        while (true) {
            try {
                // 接收服务端 udp 数据阻塞方法
                dp.setData(udp);        // 设置预发送数组
                Arrays.fill(udp, (byte) 0);     // 清空接收数组
                ds.receive(dp);     // 接收服务端信息
                if (ServerIp.getHostName()
                        .equals(dp.getAddress().getHostName())) {// 判断是否是服务端发送的信息
                    uaa.link(udp);      // 链接欲处理数组
                    if (uaa.poll(1) == 0) {// 判断传入的是否是背景信息
                        cell();     // 处理预览方块和下落方块信息
                        repaint();      // 以固定频率刷新画面
                    } else {
                        backGround();       // 处理背景信息
                    }
                } else {
                    dp.setAddress(ServerIp);        // 如果不是对应服务端发送的信息，重置服务端 IP 和端口号
                    dp.setPort(ServerPort);
                }
            } catch (IOException ignored) {
            }
            // 重置按键开关
            turnRotate = HardDrop = changeTetromino = false;
        }
    }

    private void cell() throws IOException {
        gamePause = uaa.poll();     // 是否加载暂停画面
        int i = 0;      // 按照协议导出数据
        for (; i < 4; i++) {
            info[i] = uaa.poll(5);
        }
        for (; i < 8; i++) {
            info[i] = uaa.poll(3);
        }
        while (i < info.length) {
            info[i++] = uaa.poll(2);
            info[i++] = uaa.poll(1);
            info[i++] = uaa.poll(4) - 1;
            info[i++] = uaa.poll(5) - 1;
        }
        if (win[0] != info[2] || win[1] != info[3]) {
            Arrays.fill(backGround[0], 0);      // 如果结束一局，清空背景
            Arrays.fill(backGround[1], 0);
        }
        win[0] = info[2];       // 记录胜负，可以依此判断是否一局结束
        win[1] = info[3];
        uaa.offer(true);        // true 是操纵指令
        uaa.offer(turnRotate);      // 出入操纵指令
        uaa.offer(turnRight);
        uaa.offer(turnDrop);
        uaa.offer(turnLeft);
        uaa.offer(HardDrop);
        uaa.offer(changeTetromino);
        dp.setData(udp);        // 准备并发送操纵指令给服务端
        dp.setLength(uaa.close());
        ds.send(dp);
    }

    /**
     * 将信息存入背景数组
     *
     */
    private void backGround() throws IOException {
        int i = uaa.poll(1);        // 是1P 还是 2P
        line[i] = counter = uaa.poll(6);        // 对应的消除行数
        Arrays.fill(backGround[i], 0);      // 清除背景信息
        for (int j = height; j > 0; j--) {
            // 将十个三位数值存入对应数组
            backGround[i][j - 1] = uaa.poll(15);        // 批量提取背景信息
            backGround[i][j - 1] += uaa.poll(15) << 15;
            if (j < height - 2 && backGround[i][j - 1] == 0)
                break;      // 如果遇到空行，则不继续
        }
        uaa.offer(false);       // 不是操纵指令
        uaa.offer(i, 1);        // 设置成功接收背景编号
        uaa.offer(counter, 6);      // 插入返回成功接收数值
        dp.setData(udp);
        dp.setLength(uaa.close());
        for (int j = 3; j > 0; j--) {
            ds.send(dp);        // 为确保服务端成功接收，发送三次
        }
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
        g.setFont(font[0]);     // 设置文字大小字体等
        g.setColor(fontColor[2]);       // 设置文字颜色
        g.drawString("2P SP: " + info[1], 290, 155); // 显示得分
        g.drawString(" Line: " + line[1], 290, 175); // 显示行数
        g.setColor(fontColor[0]);       // 设置文字颜色
        g.drawString("1P SP: " + info[0], 420, 225); // 显示得分
        g.drawString(" Line: " + line[0], 420, 245); // 显示行数

        g.setFont(font[1]);     // 设置文字大小字体等
        g.setColor(fontColor[1]);       // 设置文字颜色
        g.drawString(win[1] + ":" + win[0], 380, 200); // 显示得分

        g.translate(15, -12); // 调整相对位置
        for (int j = 8; j < info.length; j += 8) {
            // 在背景上绘制方块。
            g.drawImage(ImageLoader.color[info[4] - 1], info[j] * booboo + 340, info[j + 1]
                    * booboo + booboo + 275, null);
            if (info[j + 3] > -1)
                g.drawImage(ImageLoader.color[info[5] - 1], info[j + 2] * booboo + 512,
                        info[j + 3] * booboo + booboo, null);
            g.drawImage(ImageLoader.color[info[6] - 1], info[j + 4] * booboo + 340,
                    info[j + 5] * booboo + booboo + 23, null);
            if (info[j + 7] > -1)
                g.drawImage(ImageLoader.color[info[7] - 1], info[j + 6] * booboo,
                        info[j + 7] * booboo + booboo, null);
        }
        // 绘制背景，根据背景数组中的值调用 color 数组中相应下标的图片绘制于背景之上
        for (int row = height - 1; row >= 0; row--) {
            int inCell0 = 0, inCell1 = 0;
            for (int col = 1; col <= width; col++) {
                if ((inCell1 = backGround[1][row] << 32 - col * 3 >>> 32 - 3) != 0)
                    g.drawImage(ImageLoader.color[inCell1 - 1], (col - 1) * booboo, row
                            * booboo + booboo, null);
                if ((inCell0 = backGround[0][row] << 32 - col * 3 >>> 32 - 3) != 0)
                    g.drawImage(ImageLoader.color[inCell0 - 1], (col - 1) * booboo + 512,
                            row * booboo + booboo, null);
            }
            if (row < height - 1 && backGround[0][row] == 0
                    && backGround[1][row] == 0)
                break;
        }
        if (gamePause) {
            g.drawImage(ImageLoader.pause, 0, 0, null); // 覆盖背景图片
            g.setFont(font[2]);
            g.setColor(fontColor[2]);
            g.drawString("Wait...", 300, 220);
        }
    }
}
