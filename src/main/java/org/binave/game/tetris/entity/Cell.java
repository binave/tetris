package org.binave.game.tetris.entity;


import lombok.Getter;
import lombok.Setter;

/**
 * 格子，方块的基本组成
 */
@Setter
@Getter
public class Cell {
    private int row;     //格子行
    private int column;     //格子列
    private byte img;    //图片下标

    public void incrByRow(int value) {
        this.row += value;
    }

    public void incrByColumn(int value) {
        this.column += value;
    }


}
