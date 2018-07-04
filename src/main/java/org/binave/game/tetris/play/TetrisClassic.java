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

import javax.swing.*;

/**
 * @version 1.02
 *          由定时触发控制画面刷新和自动下降，其他动作均由键盘监听主动触发动作。
 */
public class TetrisClassic extends JPanel {
    private static final long serialVersionUID = 1L;
    private static int times;       // 次数记录，用于调整自动下落频率
    /**
     * 以下声明静态图片
     */
    private static BufferedImage state;

    /**
     * 声明方块数组
     */
    private Tetromino[] tetromino;
    /**
     * 声明游戏状态
     */
    private boolean gameStart;
    private boolean gameState;
    /**
     * 动作开关
     */
    private boolean turnLeft;       // 允许左移开关，手动触发
    private boolean allowLeft;      // 允许左移开关，手动触发
    private boolean turnRight;      // 允许右移开关，手动触发
    private boolean allowRight;     // 允许右移开关，手动触发
    private boolean turnDrop;       // 允许下降开关，手动触发
    private boolean hardDrop;       // 一降到底开关，手动触发
    private boolean autoDrop;       // 自动下落开关，定时触发
    private boolean allowDrop;      // 下降触碰开关
    private boolean turnRotate;     // 允许旋转开关，手动触发
    /**
     * 方块显示位置修正
     */
    private static final int booboo = 26;
    /**
     * 背景方块图片存储数组
     */
    private byte[][] backGround;
    private int width;
    private int height;
    /**
     * 方块组交替下标
     */
    private int i;
    /**
     * 旋转计数器变量，用于恢复原角度之用
     */
    private int rotate;
    /**
     * SP 消除行数 难度等级
     */
    private int sP;
    private int line;
    private int level;

    private TetrisClassic(int row, int col) {
        backGround = new byte[row][col];        // 静止方块图片存储数组，会将符合条件的移动方块绘制到此数组上
        height = row;
        width = col;
        tetromino = new Tetromino[2];       // 建立两个方块对象，分别用于控制下落或预览，并可交换彼此
        tetromino[0] = new Tetromino();     // 预交换方块1
        tetromino[1] = new Tetromino();     // 预交换方块2
        tetromino[1 - i].tetromino();       // 初始化方块属性
        state = ImageLoader.background;     // 默认背景
        allowDrop = true;
        gameStart = true;       // 允许游戏进行
        gameState = true;       // 允许屏幕刷新
        exchangeTetromino();        // 切换默认方块组
    }


    /**
     * 经典俄罗斯方块【入口】
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Tetris");        // 建立画面
        final TetrisClassic bg = new TetrisClassic(20, 10);     // 设置背景宽高
        frame.add(bg);
        frame.setSize(ImageLoader.background.getWidth(), ImageLoader.background.getHeight());       // 布画大小
        frame.setAlwaysOnTop(true);     // 总在最上面
        frame.setUndecorated(true);     // 去掉边框
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);       // 关闭画面时停止程序
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);     // 显示画面
        bg.action();        // 调用定时触发和键盘监听
    }

    /**
     * 键盘监听和定时触发，配合游戏状态变量控制游戏状态
     */
    private void action() {
        this.addKeyListener(new KeyAdapter() { // 键盘监听匿名内部类
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {       // 接收按键事件并激发下方的switch
                    case KeyEvent.VK_LEFT:
                        turnLeft = true;        // 左键允许左移
                        break;
                    case KeyEvent.VK_RIGHT:
                        turnRight = true;       // 右键允许右移
                        break;
                    case KeyEvent.VK_DOWN:
                        turnDrop = true;        // 下键允许加速左下落
                        break;
                    case KeyEvent.VK_UP:
                        turnRotate = true;      // 上键允许顺时针九十度旋转
                        break;
                }
            }

            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode() + (e.getKeyLocation() - 1) * 1000) {
                    case KeyEvent.VK_LEFT:
                        turnLeft = false;       // 左键允许左移
                        break;
                    case KeyEvent.VK_RIGHT:
                        turnRight = false;      // 右键允许右移
                        break;
                    case KeyEvent.VK_DOWN:
                        turnDrop = false;       // 下键允许加速左下落
                        break;
                    case KeyEvent.VK_UP:
                        turnRotate = false;     // 上键允许顺时针九十度旋转
                        break;
                    case KeyEvent.VK_SPACE:
                        hardDrop = true;        // 空格键允许循环加速下落
                        break;
                    case KeyEvent.VK_P:
                        gameStart = !gameStart;      // 暂停或继续游戏运行
                        if (gameStart) {
                            if (state == ImageLoader.game_over) {// 重新开始游戏
                                initialise();       // 初始化静态方块，sP、消除行数
                            }
                            state = ImageLoader.background;
                            gameState = true;
                        } else state = ImageLoader.pause;
                        break;
                    case KeyEvent.VK_SHIFT + 1000:// 左 SHIFT 键按下，消耗SP使用下一个方块
                        exTet();
                        break;
                    case KeyEvent.VK_Q:         // Q 键弹起退出游戏
                    case KeyEvent.VK_ESCAPE:    // Esc 键弹起退出游戏
                        System.exit(0);
                        break;
                }
            }
        });
        this.requestFocus();        // 接收键盘监听事件

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() { // 定时触发匿名內部類
            public void run() {
                times = times > 8 - level ? 0 : times + 1;      // 增加难度，周期重置计数
                if (gameStart && times == 0)
                    autoDrop = true;        // 计数满足条件则允许自动下落
                start();        // 以固定频率进行判断或执行方法
                repaint();      // 以固定频率刷新画面
            }
        }, 70, 70);
    }

    // 【重新开始游戏】初始化静态方块背景，游戏得分、难度等级、消除行数
    private void initialise() {
        line = 0;
        sP = 0;
        level = 0;
        exchangeTetromino();        // 交换预览方块与下落方块
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                backGround[row][col] = 0;       // 初始化静态方块背景
            }
        }
    }

    /**
     * 消耗SP更换方块
     */
    private void exTet() {
        if (sP > 0) {       // 如果没有 SP 将无法使用变更方块技能
            sP--;
            exchangeTetromino();
        }
    }

    private void exchangeTetromino() {      // 【交换预览方块与下落方块】
        tetromino[i].tetromino();       // 初始化方块属性
        i = 1 - i;      // 交换下标
        move(-1, 3);        // 修正方块初始坐标
    }

    private void start() {
        if (!gameStart && !autoDrop && !turnDrop && !turnLeft && !turnRight
                && !turnRotate && !hardDrop)
            return;     // 没有动作指令时空循环
        rotate = 3;     // 初始化旋转计数器为3
        if (turnRotate) // 如果允许旋转
            rotateRight();      // 进行顺时针九十度旋转
        move();
        // 关闭开关变量
        autoDrop = turnRotate = false;
    }

    private void move() {       // 【接收动作指令并执行合理的指令】
        allowLeft = allowRight = allowDrop = true;      // 初始化下降触碰开关
        limit();        // 测试越界并赋予动作开关状态
        int index = 0;      // 初始化临时计数器
        for (Cell t : tetromino[i].cells) {     // 遍历移动方块的格子
            if (!allowDrop && t.row >= 0 && (autoDrop || turnDrop)) {       // 方块下方为边界或背景方块：
                backGround[t.row][t.col] = t.img;       // 将方块的颜色信息存入背景数组
                if (++index == 4) {     // 循环四次后执行
                    hardDrop = false;       // 停止自我循环
                    subLine();      // 减行
                    exchangeTetromino();        // 将预览方块的属性赋予下落方块，并给预览方块重新赋予随机属性
                    return;
                }
            } else {
                t.col += turnLeft && allowLeft ? -1 : 0;        // 如果左移被允许，则方块左移
                t.col += turnRight && allowRight ? 1 : 0;       // 如果右移被允许，则方块右移
                t.row += (autoDrop || turnDrop) && allowDrop ? 1 : 0;       // 如果自动下落或手动下落被允许，则方块下落
            }
            // 如果方块中的格子处在 -1 行上，则不允许一降到底
            hardDrop = t.row >= 0 && hardDrop;
        }
        if (hardDrop) {     // 如果允许一降到底，则自我循环
            autoDrop = true;        // 开启自动下落
            move();     // 触发本方法
        }
    }

    public void move(int row, int col) { // 传入坐标组并修正初始位置
        for (Cell t : tetromino[i].cells) {
            t.row += row;
            t.col += col;
        }
    }

    private void subLine() {
        int subLine = 0;        // 初始化加倍奖励计数器
        for (int row = height - 1; row >= 0; row--) {
            int inCell = 0; // 初始化背景格子计数器，记录有格子的个数
            for (int col = 0; col < width; col++) {     // 从下至上遍历背景
                if (backGround[row][col] != 0)
                    inCell++;       // 统计背景中一行格子个数
            }
            if (inCell == width) {
                byte[] tmp = backGround[row];
                for (int j = 0; j < width; j++) {
                    tmp[j] = 0;     // 重置消除行的数组数值
                }
                System.arraycopy(backGround, 0, backGround, 1, row++);
                backGround[0] = tmp;
                if (++line % 30 == 0)
                    // 每消除 30 行提升一级难度，会影响触发定时下落的频率
                    level++;
                subLine++; // 奖励SP点，可以用于更换方块。
            }
            if (row == 0) {     // 如果有格子的行数已经达到顶层
                state = ImageLoader.game_over;      // 更换游戏结束的背景
                gameStart = false;      // 判定游戏结束
                break;      // 跳出此循环
            }
            if (inCell == 0)
                break;      // 当遍历到空行则不再进行背景的扫描
        }

        switch (subLine) {// 根据消除行数选择奖励
            case 2:
                sP += 1;
                break;
            case 3:
                sP += 3;
                break;
            case 4:
                sP += 6;
                break;
        }
        sP = sP > 30 ? 30 : sP;        // 限制 SP 个数为三十

    }

    /**
     * 左、右、下、旋转越界、重叠判断. 遍历方块，测试方块周围的状态并依此设置方块动作开关 。 当动作开关为 false时，将不允许执行相应的動作。
     */
    private void limit() {
        for (Cell t : tetromino[i].cells) {
            if (t.row < 0) {        // 如果方块处在上边界之上，则仅允许自动下落
                allowLeft = allowRight = turnDrop = hardDrop = false;
                break;
            }
            if (turnLeft && (t.col == 0 || backGround[t.row][t.col - 1] != 0))  // 同上，左面
                // 如果方块左方是边界或方块，则关闭左移开关
                allowLeft = false;
            if (turnRight && (t.col == backGround[0].length - 1 || backGround[t.row][t.col + 1] != 0))   // 同上，右面
                // 如果方块右方是边界或方块，则关闭右移开关
                allowRight = false;
            if (t.row == height - 1 || backGround[t.row + 1][t.col] != 0)
                // 如果方块下方是边界或方块，则关闭下降开关
                allowDrop = false;
        }
    }

    /**
     * 测试顺时针旋转 90 度 交换 row 与 col 的坐标并左右翻转 在交换过程中需要减去与0,0的间距，然后用2减去col，再将间距加回来。
     * 在0.0点交换 row 与 col 会得到与目标图样左右颠倒的图样， 这时候用 2 减去 col 则会得到顺时针旋转 90
     * 度的图样。再移动回原来的位置即可实现翻转
     */
    private void rotateRight() {
        // 初始化空列计数器，获得背景数组长宽。
        int tmp, amend = 0, rowMin = height, colMin = backGround[0].length;
        for (Cell t : tetromino[i].cells) {             // 获得下落方块距 0,0 点最短距离
            rowMin = t.row < rowMin ? t.row : rowMin;   // 获取距 0,0 点最小 row 值
            colMin = t.col < colMin ? t.col : colMin;   // 获取距 0,0 点最小 col 值
        }
        for (Cell t : tetromino[i].cells) {     // 交換 row 與 col 坐標
            tmp = t.row - rowMin;
            t.row = t.col - colMin + rowMin;    // 将 col 值赋予 row
            t.col = 2 - tmp + colMin;           // 将左右翻转后的 row 值赋予 col
            amend += colMin != t.col ? 1 : 0;   // 记录左一列是否为空
        }
        if (amend == 4)                 // 如果左一列均为空
            move(0, -1);       // 方块左移
        if (turnRotate) // 防止此方法自我循环时执行此处
            for (Cell t : tetromino[i].cells) {     // 測試是否越界、重合
                if (t.row > height - 1 || t.row < 0
                        || t.col > backGround[0].length - 1 || t.col < 0
                        || backGround[t.row][t.col] != 0)
                    // 如果下落方块越界，或与背景中静态方块重合则关闭旋转开关
                    turnRotate = false;
            }
        if (!turnRotate && rotate-- > 0)    // 如果旋转开关关闭，则再旋转三次回到原位
            rotateRight();      // 触发本方法
    }

    public void paint(Graphics g) {     // 【绘制画面】
        if (gameState)  // 用于防止多次调用
            g.drawImage(state, 0, 0, null); // 覆盖背景图片
        if (!gameStart) {           // 如果游戏状态为停止
            gameState = false;      // 防止多次覆盖背景
            return;     // 跳出此方法
        }
        g.setFont(new Font("Monospaced", Font.BOLD, 20));       // 设置文字大小字体等
        g.setColor(new Color(0x777777));                 // 设置文字颜色
        g.drawString("SP: " + sP, 310, 175);        // 显示得分
        g.drawString("Line: " + line, 310, 230);    // 显示行数
        g.drawString("Level: " + (level + 1), 310, 285); // 显示行数
        g.translate(15, -12); // 调整相对位置

        for (int j = 0; j < tetromino[1 - i].cells.length; j++) {
            Cell t0 = tetromino[1 - i].cells[j];
            Cell t1 = tetromino[i].cells[j];
            // 在背景上绘制预览方块，根据对象的 imgColor 属性绘制 color 数组中对应下标的图片。
            g.drawImage(ImageLoader.color[t0.img - 1], t0.col * booboo + 340, t0.row
                    * booboo + booboo + 23, null);
            if (t1.row > -1)
                g.drawImage(ImageLoader.color[t1.img - 1], t1.col * booboo, t1.row * booboo
                        + booboo, null);
        }
        for (int row = height - 1; row >= 0; row--) {
            int inCell = 0;
            for (int col = 0; col < width; col++) {     // 绘制背景
                if (backGround[row][col] != 0) {
                    inCell++;
                    // 根据背景数组中的值调用 color 数组中相应下标的图片绘制于背景之上
                    g.drawImage(ImageLoader.color[backGround[row][col] - 1], col * booboo,
                            row * booboo + booboo, null);
                }
            }
            if (inCell == 0)
                break;      // 当遍历到空行则不再进行背景的扫描
        }
    }
}
