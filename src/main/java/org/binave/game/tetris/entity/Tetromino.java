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

import java.util.Random;

/**
 * 将四个格子 Cell 设成一个对象进行处理
 */

public class Tetromino {

    /* 建立格子数组 */
    public Cell[] cells;

    /* 方块颜色图片 */
    public byte imgColor;

    /* 准备随机数 */
    private Random ran = new Random();

    public Tetromino() {
        // 将四个格子组成一个方块
        cells = new Cell[4];
        for (int i = 0; i < cells.length; i++) {
            cells[i] = new Cell();
        }
    }

    /**
     * 随机生成 7 种方块之一
     * 除了I型以外，其他六种方块都会占用 三行 两列
     * 如果排除 row = 1，col = 1 点，
     * row = 0，col = 0 点 与其余任意一点构成“阴文”，
     * 再用三行两列排除这两个阴文得到的“阳文”就是三种方块之一
     * 而 使用 row = 0，col = 2 则可依样生成另外三种方块。
     */
    public void tetromino() {
        // 初始化“阴文”col 值达到左右颠倒的效果
        int col, row, times = 0, index = 0, lock = ran.nextInt(2) == 0 ? 2 : 0;
        imgColor = (byte) (ran.nextInt(7) + 1); // 存储随机图片下标

        // 给其中的 cell 颜色赋值
        for (Cell cell : cells) {
            cell.img = imgColor;
        }

        do { // 确定另一个“阴文”
            row = ran.nextInt(2);
            col = ran.nextInt(3);
            times++;
        } while (row == 1 && col == 1 || row == 0 && col == lock);
        if (times > 2) {
            // 当重复次数超过三次生成 I 型
            for (int i = 0; i < 4; i++) {
                cells[i].row = 0;
                cells[i].col = i;
            }
        } else {
            // 利用“阴文”生成“阳文”，产生 Z L O T 正反之一
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 3; j++) {
                    if (!(i == row && j == col || i == 0 && j == lock)) {
                        cells[index].row = i;
                        cells[index].col = j;
                        index++;
                    }
                }
            }
        }
    }
}
