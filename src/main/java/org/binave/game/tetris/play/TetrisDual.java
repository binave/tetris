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
import org.binave.game.tetris.entity.Cell;
import org.binave.game.tetris.entity.Tetromino;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

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
public class TetrisDual extends JPanel {
    private static final long serialVersionUID = 1L;
    /**
     * 以下声明静态图片
     */
    private static BufferedImage state;

    /**
     * 声明方块数组
     */
    private Tetromino[][] tetromino;
    /**
     * 玩家id
     */
    private int[] tetId;
    /**
     * 背景方块图片存储数组
     */
    private byte[][][] backGround;
    /**
     * 去行数组内存地址交换
     */
    private int width;
    private int height;
    /**
     * 声明游戏状态
     */
    private boolean gameStart;
    private boolean gameState;
    /**
     * 动作开关
     */
    private boolean[] turnLeft;     // 手动开启或关闭左移
    private boolean[] allowLeft;        // 是否允许左移
    private boolean[] turnRight;        // 手动开启或关闭右移
    private boolean[] allowRight;       // 是否允许右移
    private boolean[] turnDrop;     // 手动开启或关闭下降
    private boolean[] HardDrop;     // 手动开启硬下降（一降到底）
    private boolean[] autoDrop;     // 定时触发自动下落
    private boolean[] allowDrop;        // 是否允许下降
    private boolean[] turnRotate;       // 手动开启旋转
    /**
     * 游戏点
     */
    private int[] sP;       // 技能点。
    private int[] line;     // 消除行数
    private int[] hard;     // 速度调节
    private int[] win;      // 胜点
    private static int[] times;     // 次数记录，用于调整自动下落频率
    /**
     * 方块显示位置修正
     */
    private static final int booboo = 26;
    /**
     * 旋转计数器变量，用于恢复原角度之用
     */
    private int rotate;

    private TetrisDual(int row, int col) {
        int p = 2;
        tetromino = new Tetromino[p][2];        // 建立两个方块对象，分别用于控制下落或预览，并可交换彼此
        for (int j = 0; j < tetromino.length; j++) {
            tetromino[j][0] = new Tetromino();
            tetromino[j][1] = new Tetromino();
            tetromino[j][1].tetromino();        // 初始化方块属性
        }
        backGround = new byte[p][row][col];     // 静止方块图片存储数组，会将符合条件的移动方块绘制到此数组上
        height = row;
        width = col;
        gameStart = true;       // 允许游戏进行
        gameState = true;       // 允许屏幕刷新
        turnLeft = new boolean[p];
        allowLeft = new boolean[p];
        turnRight = new boolean[p];
        allowRight = new boolean[p];
        turnDrop = new boolean[p];
        HardDrop = new boolean[p];
        autoDrop = new boolean[p];
        allowDrop = new boolean[p];
        turnRotate = new boolean[p];
        tetId = new int[p];
        sP = new int[p];
        line = new int[p];
        win = new int[p];
        times = new int[p];
        hard = new int[p];
        state = ImageLoader.backgroundDual;     // 默认背景
        exchangeTetromino(0);       // 切换默认方块组
        exchangeTetromino(1);
    }

    /**
     * 双打俄罗斯方块的【入口】
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Tetris");        // 建立画面
        final TetrisDual bg = new TetrisDual(20, 10);       // 设置背景宽高
        frame.add(bg);
        frame.setSize(ImageLoader.backgroundDual.getWidth(), ImageLoader.backgroundDual.getHeight());       // 布画大小
        frame.setAlwaysOnTop(true);     // 总在最上面
        frame.setUndecorated(true);     // 去掉边框
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);       // 关闭画面时停止程序
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);     // 显示画面
        bg.action();        // 调用定时触发和键盘监听
    }

    /**
     * 键盘监听和定时触发，配合游戏状态变量控制游戏状态
     */
    private void action() {
        this.addKeyListener(new KeyAdapter() { // 键盘监听匿名内部类
            public void keyPressed(KeyEvent e) {// 接收按键按下事件
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A:// A键按下
                        turnLeft[0] = true;     // 允许左移
                        break;
                    case KeyEvent.VK_W:// W键按下
                        turnRotate[0] = true;       // 允许顺时针九十度旋转
                        break;
                    case KeyEvent.VK_D:// D键按下
                        turnRight[0] = true;        // 允许右移
                        break;
                    case KeyEvent.VK_S:// S键按下
                        turnDrop[0] = true;     // 允许加速下落
                        break;
                    case KeyEvent.VK_LEFT:// 左键按下
                        turnLeft[1] = true;     // 允许左移
                        break;
                    case KeyEvent.VK_UP:// 上键按下
                        turnRotate[1] = true;       // 允许顺时针九十度旋转
                        break;
                    case KeyEvent.VK_RIGHT:// 右键按下
                        turnRight[1] = true;        // 允许右移
                        break;
                    case KeyEvent.VK_DOWN:// 下键按下
                        turnDrop[1] = true;     // 允许加速下落
                        break;
                }
            }

            /*
             * 接收按键弹起事件 左右 Ctrl Shift Alt 的 KeyCode 值相同 ，但 KeyLocation 值不同， 左边均为
             * 2，右边均为 3 数字键均为 4，其他为 1。
             */
            public void keyReleased(KeyEvent e) {
                switch ((e.getKeyLocation() - 1) * 1000 + e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:// Esc 键弹起
                        System.exit(0);     // 强制退出该进程
                        break;
                    case KeyEvent.VK_A:// A键弹起
                        turnLeft[0] = false;        // 禁止左移
                        break;
                    case KeyEvent.VK_D:// D键弹起
                        turnRight[0] = false;       // 禁止右移
                        break;
                    case KeyEvent.VK_S:// S键弹起
                        turnDrop[0] = false;        // 禁止加速左下落
                        break;
                    case KeyEvent.VK_LEFT:// 左键弹起
                        turnLeft[1] = false;        // 禁止左移
                        break;
                    case KeyEvent.VK_RIGHT:// 右键弹起
                        turnRight[1] = false;       // 禁止右移
                        break;
                    case KeyEvent.VK_DOWN:// 下键弹起
                        turnDrop[1] = false;        // 禁止加速左下落
                        break;
                    case KeyEvent.VK_Z:// Z 键弹起
                        HardDrop[0] = true;     // 允许循环加速下落
                        break;
                    case KeyEvent.VK_PAGE_DOWN:// PageDown 弹起
                        HardDrop[1] = true;     // 允许循环加速下落
                        break;
                    case KeyEvent.VK_Q:// Q 键弹起
                        exTet(0);       // 使用下一个方块
                        break;
                    case KeyEvent.VK_SLASH:// '/' 键弹起
                        exTet(1);       // 使用下一个方块
                        break;
                    case KeyEvent.VK_SPACE:// 空格键弹起
                        if (state != ImageLoader.backgroundDual) {
                            if (state == ImageLoader.game_over) {// 重新开始游戏
                                initialise();       // 初始化静态方块，sP、消除行数
                            }
                            state = ImageLoader.backgroundDual;
                            gameStart = true;       // 从暂停状态变为游戏进行状态
                            gameState = true;       // 允许屏幕刷新
                        } else {// 暂停
                            state = ImageLoader.pause;      // 设置背4景图片为暂停
                            gameStart = false;      // 禁止游戏运行
                        }
                        break;
                }
            }
        });
        this.requestFocus();        // 接收键盘监听事件

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() { // 定时触发匿名內部類
            public void run() {
                times[0] = times[0] > 8 - ((--hard[0] > 0 ? hard[0] : 0) > 0 ? 9
                        : 0) ? 0 : times[0] + 1;        // 当随时自减的 hard 大于 0 ，令
                // times 一直等于 0；
                times[1] = times[1] > 8 - ((--hard[1] > 0 ? hard[1] : 0) > 0 ? 9
                        : 0) ? 0 : times[1] + 1;
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
            line[i] = sP[i] = hard[i] = 0;
            exchangeTetromino(i);       // 交换预览方块与下落方块
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    backGround[i][row][col] = 0;        // 初始化静态方块背景
                }
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
     * 消耗SP更换方块
     *
     * @param i
     */
    private void exTet(int i) {
        if (sP[i] > 0) {// 如果没有 SP 将无法使用变更方块技能
            sP[i]--;
            exchangeTetromino(i);
        }
    }

    /**
     * 处理背景，奖励
     *
     * @param i
     */
    private void start(int i) {
        if (!autoDrop[i] && !turnDrop[i] && !turnLeft[i] && !turnRight[i]
                && !turnRotate[i] && !HardDrop[i])
            return;     // 没有动作指令时空循环
        rotate = 3;     // 初始化旋转计数器为3
        if (turnRotate[i]) // 如果允许旋转
            rotateRight(i);     // 进行顺时针九十度旋转
        move(i);
        autoDrop[i] = turnRotate[i] = false;        // 关闭自动下降和允许旋转开关
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
        for (Cell t : tetromino[i][tetId[i]].cells) {// 遍历移动方块的格子
            if (!allowDrop[i] && t.row >= 0 && (autoDrop[i] || turnDrop[i])) {// 方块下方为边界或背景方块：
                backGround[i][t.row][t.col] = t.img;        // 将方块的颜色信息存入背景数组
                if (++index == 4) {// 循环四次后执行
                    HardDrop[i] = false;        // 停止自我循环
                    subLine(i);
                    exchangeTetromino(i);       // 将预览方块的属性赋予下落方块，并给预览方块重新赋予随机属性
                    return;
                }
            } else {
                t.col += turnLeft[i] && allowLeft[i] ? -1 : 0;      // 如果左移被允许，则方块左移
                t.col += turnRight[i] && allowRight[i] ? 1 : 0;     // 如果右移被允许，则方块右移
                t.row += (turnDrop[i] || autoDrop[i]) && allowDrop[i] ? 1 : 0;      // 如果自动下落或手动下落被允许，则方块下落
            }
            // 如果方块中的格子处在 -1 行上，则不允许一降到底
            HardDrop[i] = t.row >= 0 && HardDrop[i];
        }
        if (HardDrop[i]) {// 如果允许一降到底，则自我循环
            autoDrop[i] = true;     // 开启自动下落
            move(i);        // 触发本方法
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
                System.arraycopy(backGround[i], 0, backGround[i], 1, row++);
                backGround[i][0] = tmp;
                line[i]++;      // 记录行数
            }
            // 如果有格子的行数已经达到顶层，先达到 60 行者为胜。
            if (row == 0 || line[1 - i] >= 60) {
                win[1 - i] += 1;
                state = ImageLoader.game_over;      // 更换游戏结束的背景
                gameStart = false;      // 判定游戏结束
                break;      // 跳出此循环
            }
            if (row < height - 1 && inCell == 0)
                break;      // 如果不是末尾行且此行没有格子，则跳出循环
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
            rotateRight(i);     // 触发本方法
    }

    /**
     * 绘制画面
     */
    public void paint(Graphics g) {
        if (gameState)// 用于防止多次调用
            g.drawImage(state, 0, 0, null); // 覆盖背景图片
        if (!gameStart) {// 如果游戏状态为停止
            gameState = false;      // 防止多次覆盖背景
            return;     // 跳出此方法
        }
        g.setFont(new Font("Monospaced", Font.BOLD, 20));       // 设置文字大小字体等
        g.setColor(new Color(0x777333));        // 设置文字颜色
        g.drawString(win[0] + ":" + win[1], 380, 200); // 显示得分
        g.setFont(new Font("Monospaced", Font.BOLD, 15));       // 设置文字大小字体等
        g.setColor(new Color(0x777777));        // 设置文字颜色
        g.drawString("1P SP: " + sP[0], 290, 155); // 显示得分
        g.drawString(" Line: " + line[0], 290, 175); // 显示行数
        g.setColor(new Color(0x333777));        // 设置文字颜色
        g.drawString("2P SP: " + sP[1], 420, 225); // 显示得分
        g.drawString(" Line: " + line[1], 420, 245); // 显示行数
        g.translate(15, -12); // 调整相对位置
        for (int j = 0; j < tetromino[0][0].cells.length; j++) {
            Cell ready0 = tetromino[0][1 - tetId[0]].cells[j];
            Cell ready1 = tetromino[1][1 - tetId[1]].cells[j];
            Cell run0 = tetromino[0][tetId[0]].cells[j];
            Cell run1 = tetromino[1][tetId[1]].cells[j];
            // 在背景上绘制方块，根据对象的 imgColor 属性绘制 color 数组中对应下标的图片。
            g.drawImage(ImageLoader.color[ready0.img - 1], ready0.col * booboo + 340,
                    ready0.row * booboo + booboo + 23, null);
            g.drawImage(ImageLoader.color[ready1.img - 1], ready1.col * booboo + 340,
                    ready1.row * booboo + booboo + 275, null);
            if (run0.row > -1)
                g.drawImage(ImageLoader.color[run0.img - 1], run0.col * booboo, run0.row
                        * booboo + booboo, null);
            if (run1.row > -1)
                g.drawImage(ImageLoader.color[run1.img - 1], run1.col * booboo + 512,
                        run1.row * booboo + booboo, null);
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
    }
}
