/*
 * Copyright (c) 2026 nidnil@icloud.com.
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

package org.binave.game.tetris.common;

import org.binave.game.tetris.entity.Cell;
import org.binave.game.tetris.entity.Tetromino;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 俄罗斯方块视图渲染器
 * 统一管理字体、颜色和绘制方法，避免 paint() 中重复创建对象
 */
public class TetrisView {

    // 预分配字体 (Epsilon GC 兼容)
    public static final Font FONT_SMALL = new Font("Monospaced", Font.BOLD, 15);
    public static final Font FONT_MEDIUM = new Font("Monospaced", Font.BOLD, 20);
    public static final Font FONT_LARGE = new Font("Monospaced", Font.BOLD, 35);

    // 预分配颜色 (Epsilon GC 兼容)
    public static final Color COLOR_BLUE = new Color(0x333777);
    public static final Color COLOR_YELLOW = new Color(0x777333);
    public static final Color COLOR_GRAY = new Color(0x777777);

    // 常量
    public static final int BOO_BOO = 26;  // 方块尺寸
    public static final int TRANSLATE_X = 15;
    public static final int TRANSLATE_Y = -12;

    // 布局常量 - 单人模式
    public static final int SINGLE_INFO_X = 310;
    public static final int SINGLE_PREVIEW_X = 300;

    // 布局常量 - 双人模式
    public static final int DUAL_1P_INFO_X = 300;
    public static final int DUAL_2P_INFO_X = 420;
    public static final int DUAL_1P_FIELD_X = 0;
    public static final int DUAL_2P_FIELD_X = 512;
    public static final int DUAL_PREVIEW_X = 340;
    public static final int DUAL_SCORE_X = 380;

    // 私有构造，纯静态工具类
    private TetrisView() {}

    /**
     * 应用坐标偏移
     */
    public static void applyTranslate(Graphics g) {
        g.translate(TRANSLATE_X, TRANSLATE_Y);
    }

    /**
     * 绘制背景图片
     */
    public static void drawBackground(Graphics g, java.awt.image.BufferedImage bg) {
        g.drawImage(bg, 0, 0, null);
    }

    /**
     * 绘制单个方块
     *
     * @param g          图形上下文
     * @param colorIndex 方块颜色索引 (1-based)
     * @param x          x 坐标
     * @param y          y 坐标
     */
    public static void drawBlock(Graphics g, int colorIndex, int x, int y) {
        if (colorIndex > 0 && colorIndex <= ImageGenerator.color.length) {
            g.drawImage(ImageGenerator.color[colorIndex - 1], x, y, null);
        }
    }

    /**
     * 绘制方块组
     *
     * @param g        图形上下文
     * @param t        方块组
     * @param offsetX  x 偏移
     * @param offsetY  y 偏移
     * @param checkRow 是否检查行号 >= 0
     */
    public static void drawTetromino(Graphics g, Tetromino t, int offsetX, int offsetY, boolean checkRow) {
        int colorIndex = t.getImgColor();
        for (Cell c : t.getCells()) {
            if (!checkRow || c.getRow() > -1) {
                drawBlock(g, colorIndex,
                        c.getColumn() * BOO_BOO + offsetX,
                        c.getRow() * BOO_BOO + BOO_BOO + offsetY);
            }
        }
    }

    /**
     * 绘制预览方块 (固定位置)
     *
     * @param g       图形上下文
     * @param t       方块组
     * @param offsetX x 偏移
     * @param offsetY y 偏移
     */
    public static void drawPreviewTetromino(Graphics g, Tetromino t, int offsetX, int offsetY) {
        int colorIndex = t.getImgColor();
        for (Cell c : t.getCells()) {
            drawBlock(g, colorIndex,
                    c.getColumn() * BOO_BOO + offsetX,
                    c.getRow() * BOO_BOO + BOO_BOO + offsetY);
        }
    }

    /**
     * 绘制背景格子 (byte 数组版本 - 单人)
     *
     * @param g      图形上下文
     * @param bg     背景数组 [row][col]
     * @param width  宽度
     * @param height 高度
     * @param offsetX x 偏移
     */
    public static void drawBackGroundSingle(Graphics g, byte[][] bg, int width, int height, int offsetX) {
        for (int row = height - 1; row >= 0; row--) {
            int inCell = 0;
            for (int col = 0; col < width; col++) {
                if (bg[row][col] != 0) {
                    inCell++;
                    drawBlock(g, bg[row][col], col * BOO_BOO + offsetX, row * BOO_BOO + BOO_BOO);
                }
            }
            if (inCell == 0) break;
        }
    }

    /**
     * 绘制背景格子 (byte 数组版本 - 双人，指定玩家)
     *
     * @param g       图形上下文
     * @param bg      背景数组 [player][row][col]
     * @param player  玩家索引
     * @param width   宽度
     * @param height  高度
     * @param offsetX x 偏移
     * @return 是否有格子
     */
    public static int drawBackGroundPlayer(Graphics g, byte[][][] bg, int player, int width, int height, int offsetX) {
        int totalCells = 0;
        for (int row = height - 1; row >= 0; row--) {
            int inCell = 0;
            for (int col = 0; col < width; col++) {
                if (bg[player][row][col] != 0) {
                    inCell++;
                    totalCells++;
                    drawBlock(g, bg[player][row][col], col * BOO_BOO + offsetX, row * BOO_BOO + BOO_BOO);
                }
            }
            if (row < height - 1 && inCell == 0) break;
        }
        return totalCells;
    }

    /**
     * 绘制背景格子 (int 数组版本 - 位压缩，客户端用)
     *
     * @param g       图形上下文
     * @param bg      背景数组 [player][row]，每个 int 存储 10 个格子
     * @param player  玩家索引
     * @param width   宽度
     * @param height  高度
     * @param offsetX x 偏移
     */
    public static void drawBackGroundCompressed(Graphics g, int[][] bg, int player, int width, int height, int offsetX) {
        for (int row = height - 1; row >= 0; row--) {
            for (int col = 1; col <= width; col++) {
                int colorIndex = bg[player][row] << 32 - col * 3 >>> 32 - 3;
                if (colorIndex != 0) {
                    drawBlock(g, colorIndex, (col - 1) * BOO_BOO + offsetX, row * BOO_BOO + BOO_BOO);
                }
            }
            if (row < height - 1 && bg[player][row] == 0) break;
        }
    }

    /**
     * 绘制文字信息
     */
    public static void drawText(Graphics g, String text, int x, int y, Font font, Color color) {
        g.setFont(font);
        g.setColor(color);
        g.drawString(text, x, y);
    }

    // ========== 便捷方法 ==========

    /**
     * 绘制单人模式信息面板
     */
    public static void drawSingleInfo(Graphics g, int sp, int line, int level) {
        g.setFont(FONT_MEDIUM);
        g.setColor(COLOR_GRAY);
        g.drawString("SP: " + sp, SINGLE_INFO_X, 175);
        g.drawString("Line: " + line, SINGLE_INFO_X, 230);
        g.drawString("Level: " + (level + 1), SINGLE_INFO_X, 285);
    }

    /**
     * 绘制双人模式信息面板
     */
    public static void drawDualInfo(Graphics g, int[] sp, int[] line, int[] win) {
        // 胜点
        g.setFont(FONT_MEDIUM);
        g.setColor(COLOR_YELLOW);
        g.drawString(win[0] + ":" + win[1], DUAL_SCORE_X, 200);

        // 1P 信息
        g.setFont(FONT_SMALL);
        g.setColor(COLOR_GRAY);
        g.drawString("1P SP: " + sp[0], DUAL_1P_INFO_X, 155);
        g.drawString(" Line: " + line[0], DUAL_1P_INFO_X, 175);

        // 2P 信息
        g.setColor(COLOR_BLUE);
        g.drawString("2P SP: " + sp[1], DUAL_2P_INFO_X, 225);
        g.drawString(" Line: " + line[1], DUAL_2P_INFO_X, 245);
    }

    /**
     * 生成带 3D 倒角效果的方块图片
     * 优化：合并绘图步骤，由多次 fillRect 改为两个 Polygon，减少 GPU/绘图指令。
     */
    public static BufferedImage generateBlock(Color base, Color highlight, Color shadow, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            // 1. 填充底色
            g.setColor(base);
            g.fillRect(0, 0, size, size);

            int b = size / 6; // 倒角宽度

            // 2. 绘制高光 (左侧和顶部) - 使用一个 6 点多边形
            g.setColor(highlight);
            g.fillPolygon(new int[]{0, size, size - b, b, b, 0},
                    new int[]{0, 0, b, b, size - b, size}, 6);

            // 3. 绘制阴影 (右侧和底部) - 使用一个 6 点多边形
            g.setColor(shadow);
            g.fillPolygon(new int[]{size, size, 0, b, size - b, size - b},
                    new int[]{0, size, size, size - b, size - b, b}, 6);

        } finally {
            g.dispose();
        }
        return image;
    }

    /**
     * 生成主背景图片
     * 修正：基于 paint() 中的 translate(15, -12) 精确对齐 1P/2P 和预览框。
     */
    public static BufferedImage generateBackground(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            // 深灰色背景
            g.setColor(new Color(0x2b2b2b));
            g.fillRect(0, 0, width, height);

            g.setColor(new Color(0x444444));
            g.setStroke(new BasicStroke(2));

            // 1P 主游戏区域 (10x20 booboo=26)
            g.drawRect(15, 14, 260, 520);

            if (width > 500) { // 双人模式 (880 宽)
                g.drawRect(527, 14, 260, 520); // 2P 主游戏区域
                g.drawRect(295, 135, 215, 130); // 中央装饰框 (得分/SP 区域)

            } else {
                g.drawRect(295, 135, 130, 200); // 单人模式信息框
            }

        } finally {
            g.dispose();
        }
        return image;
    }

    /**
     * 生成提示界面图片 (Pause / Game Over)
     *
     * @param text 显示文字
     * @param width 宽度
     * @param height 高度
     * @return 绘好的图片
     */
    public static BufferedImage generateOverlay(String text, int width, int height, Color textColor) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            // 半透明蒙版
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, width, height);

            // 绘制文字
            g.setColor(textColor);
            g.setFont(new Font("Monospaced", Font.BOLD, 40));
            FontMetrics fm = g.getFontMetrics();
            int x = (width - fm.stringWidth(text)) / 2;
            int y = (height - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(text, x, y);

        } finally {
            g.dispose();
        }
        return image;
    }
}
