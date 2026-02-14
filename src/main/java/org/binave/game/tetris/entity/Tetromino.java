/*
 * Copyright (c) 2015 nidnil@icloud.com
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

package org.binave.game.tetris.entity;

import lombok.Getter;

import java.util.Random;

/**
 * 将四个格子 Cell 设成一个对象进行处理
 */

public class Tetromino {

    /* 建立格子数组 */
    @Getter
    private Cell[] cells;

    /* 方块颜色图片 */
    @Getter
    private byte imgColor;

    /* 准备随机数 */
    private final Random ran = new Random();

    public Tetromino() {
        // 将四个格子组成一个方块
        cells = new Cell[4];
        for (int i = 0; i < cells.length; i++) {
            cells[i] = new Cell();
        }
    }

    /**
     * 随机 7 种形状和若干颜色
     * <p>
     * 除了 I 型以外，其他六种形状都会占用三行两列。
     * 先排除 row = 1，column = 1 的点。
     * <p>
     * 排除 row = 0，column = 0 点与其余任意一点，剩余的点可以组成 Z L O T 四种方块之一。
     * 如果排除 row = 0，column = 2 点与其余任意一点，则可生成相反朝向的四种方块（包括两种“田”）。
     */
    public void init() {
        int column, row, times = 0;

        int lock = ran.nextInt(2) == 0 ? 2 : 0; // 决定方块朝向

        imgColor = (byte) (ran.nextInt(7) + 1); // 存储随机图片下标

        do { // 确定 row = 0，col = 0 以外的另一个需要排除的点。
            row = ran.nextInt(2);
            column = ran.nextInt(3);
            times++;
        } while ((row == 1 && column == 1) || (row == 0 && column == lock));

        if (times > 2) {
            // 当重复次数超过三次生成 I 型
            for (int i = 0; i < 4; i++) {
                cells[i].setRow(0);
                cells[i].setColumn(i);
                cells[i].setImg(imgColor);
            }
        } else {
            // 刨除排除的点，产生正反 Z L O T 之一
            int i = 0;
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 3; k++) {
                    if (!(j == row && k == column || j == 0 && k == lock)) {
                        cells[i].setRow(j);
                        cells[i].setColumn(k);
                        cells[i].setImg(imgColor);
                        i++;
                    }
                }
            }
        }
    }
}
