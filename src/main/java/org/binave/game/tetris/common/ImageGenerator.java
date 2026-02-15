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

package org.binave.game.tetris.common;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 图片生成器，用于生成和缓存游戏图片。
 *
 * @author by bin jin on 2018/07/02 20:32.
 * @version 2.0 (Dynamic AWT version)
 */
public class ImageGenerator {

    public static final BufferedImage background;
    public static final BufferedImage backgroundDual;
    public static final BufferedImage pause;
    public static final BufferedImage game_over;

    /**
     * 方块颜色图片数组
     */
    public static final BufferedImage[] color = new BufferedImage[7];

    static {
        // 1. 初始化背景 (440x560 是原始背景的大致尺寸)
        background = TetrisView.generateBackground(440, 560);
        backgroundDual = TetrisView.generateBackground(800, 560);

        // 2. 初始化提示界面
        pause = TetrisView.generateOverlay("PAUSE", 440, 560, Color.ORANGE);
        game_over = TetrisView.generateOverlay("GAME OVER", 440, 560, Color.RED);

        // 3. 初始化方块图片 (26px)
        // 使用 7 种经典俄罗斯方块颜色及其高光/阴影
        color[0] = createBlock(new Color(0xFF0000)); // Red (Z)
        color[1] = createBlock(new Color(0xFF7F00)); // Orange (L)
        color[2] = createBlock(new Color(0xFFFF00)); // Yellow (O)
        color[3] = createBlock(new Color(0x00FF00)); // Green (S)
        color[4] = createBlock(new Color(0x00FFFF)); // Cyan (I)
        color[5] = createBlock(new Color(0x0000FF)); // Blue (J)
        color[6] = createBlock(new Color(0x8B00FF)); // Purple (T)
    }

    /**
     * 辅助方法：由单色生成带倒角的方块
     */
    private static BufferedImage createBlock(Color base) {
        Color highlight = brighten(base, 0.5);
        Color shadow = darken(base, 0.4);
        return TetrisView.generateBlock(base, highlight, shadow, 26);
    }

    private static Color brighten(Color color, double fraction) {
        int r = (int) Math.min(255, color.getRed() + (255 - color.getRed()) * fraction);
        int g = (int) Math.min(255, color.getGreen() + (255 - color.getGreen()) * fraction);
        int b = (int) Math.min(255, color.getBlue() + (255 - color.getBlue()) * fraction);
        return new Color(r, g, b);
    }

    private static Color darken(Color color, double fraction) {
        int r = (int) Math.max(0, color.getRed() * (1 - fraction));
        int g = (int) Math.max(0, color.getGreen() * (1 - fraction));
        int b = (int) Math.max(0, color.getBlue() * (1 - fraction));
        return new Color(r, g, b);
    }
}
