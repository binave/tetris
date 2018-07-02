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

/**
 * 对给定数组进行<b>位级</b>操作。
 * 用于减小 UDP 传输信息中的沉余。
 * 类似队列，遵循先进先出原则。
 *
 * @version 1.0
 */
public class UDPArrayAlter {
    private final int BUFFER_SIZE = Integer.SIZE;// 缓冲池最大位数
    private final int CUT_SIZE = Byte.SIZE;// 默认切除位数

    // 用于保证二进制位移统一性
    private final static int PATCH_VALUE = Byte.MAX_VALUE - Byte.MIN_VALUE + 1;
    private int offerBuffer;    // 存入位移缓冲池
    private int offerBit;       // 存入位计数，用于标记信息占用位
    private int offerIndex;     // 数组存入下标计数，用于标记处理位置
    private int pollBuffer;     // 取出位移缓冲池
    private int pollBit;        // 取出位计数，用于标记信息占用位
    private int pollIndex;      // 数组取出下标计数，用于标记处理位置
    private int peek;           // 取值缓存，用于显示上一次取值
    private byte[] udp;         // 数组链接

    public UDPArrayAlter() {
    }

    UDPArrayAlter(byte[] link) {
        link(link);
    }

    /**
     *
     * 准备向给定的数组中存入或取出信息
     *
     * @param link
     *            给定数组，<b>注意不要在此直接 new 数组</b>
     */
    public void link(byte[] link) {
        udp = link; // 链接数组
        resetOffer();
        resetPoll();
    }

    /**
     *
     * 初始化<b>存入</b>计数器，可重头再存
     */
    void resetOffer() {
        offerBuffer = offerBit = offerIndex = 0;
    }

    /**
     *
     * 初始化<b>取出</b>计数器，可重头再取
     */
    public void resetPoll() {
        pollBuffer = pollBit = pollIndex = 0;
    }

    /**
     *
     * 向数组中加入数值，需要给定占用的位数
     * <b>注意：</b>如果给定的数值超过给定的位数，将会抛出异常
     *
     * @param num
     *            要加入的数值
     * @param offset
     *            要加入数占用的二进制位数
     * @return 剩余二进制位数，
     *         如果返回值<b>不大于 0</b> ，则无法继续存入
     */
    public int offer(int num, int offset) throws RuntimeException {
        try {
            if (offerIndex == udp.length)   // 已到数组末尾，无法再加入数值
                return 0;
        } catch (Exception e) {
            return 0;// 空数组，无长度
        }
        if (num < 0 || num >= pow(offset) || offset + offerBit > BUFFER_SIZE)
            throw new RuntimeException(
                    offset + offerBit > BUFFER_SIZE ? "缓冲池溢出" : "输入位溢出");
        offerBuffer += num << offerBit; // 将新数值放到高位
        offerBit += offset;     // 步进计数器
        return inBytes();
    }

    /**
     * @return 返回 2 的 x 次幂
     */
    private int pow(int x) {
        int y = 1;
        for (; x > 0; x--) {
            y *= 2;
        }
        return y;
    }

    /**
     *
     * 向数组中加入 1 或 0，默认占 1 位。
     *
     * @param whether
     *            加入的值
     * @return 剩余二进制位数，
     *         如果返回值<b>不大于 0</b> ，则无法继续存入
     */
    public int offer(boolean whether) {
        return offer(whether ? 1 : 0, 1);
    }

    /**
     *
     * 如果有信息的部分大于等于 8 位，切去低 8 位放入给定数组。
     *
     * @return 剩余二进制位数
     */
    private int inBytes() {
        try {
            for (; offerBit >= CUT_SIZE; offerBit -= CUT_SIZE) {
                udp[offerIndex++] = (byte) offerBuffer;// 将数值低 8 位存入数组
                offerBuffer >>>= CUT_SIZE;// 去掉低八位
            }
            return (udp.length - offerIndex) * CUT_SIZE - offerBit;// 返回剩余位数
        } catch (RuntimeException e) {
            offerIndex--;// 最后尾
            return 0;
        }
    }

    /**
     *
     * 将最后不足 8 位的信息（如果有）切入数组，并解除与数组的链接。
     * <b>注意：</b>如果不执行此方法，将有可能丢失最后存入的数值。
     *
     * @return 返回实际占用的数组长度。
     */
    public int close() {
        try {
            // 如果位移缓冲池中并不为空， 且数组还有空余，则将剩余数值写入数组
            if (offerIndex < udp.length && offerBit > 0)
                udp[offerIndex++] = (byte) offerBuffer;
        } catch (NullPointerException e) {
            return 0;
        }

        // 释放与数组的连接
        udp = null;
        return offerIndex;
    }

    /**
     *
     * 从组中以给定位数取值。
     * 取出的顺序与放入顺序相同。
     *
     * @param offset
     *            欲取出数值的位数
     * @return 取出的数值，如果已取尽，则返回 -1
     */
    public int poll(int offset) throws RuntimeException {
        if (offset > BUFFER_SIZE)// 超出内部维护的 int 值上限
            throw new RuntimeException("缓冲池溢出");
        try {
            while (pollBit < offset) {
                // 如果预计取出数据长度不足， 从数组中取出数值并放到高位。
                pollBuffer += (udp[pollIndex++] + PATCH_VALUE) % PATCH_VALUE << pollBit;
                pollBit += CUT_SIZE;// 步进计数器
            }
        } catch (RuntimeException e) {
            return -1;// 空数组或数组下标越界，无法取值
        }
        // 获取取值
        int output = peek = pollBuffer << BUFFER_SIZE - offset >>> BUFFER_SIZE
                - offset;
        pollBit -= offset;      // 步进计数器
        pollBuffer >>>= offset; // 去掉已用低位
        return output;
    }

    /**
     * @return 返回上一次的取值。
     */
    public int peek() {
        return peek;
    }

    /**
     * @return 取出 boolean 信息，如果已取尽，也会返回 false
     */
    public boolean poll() {
        return poll(1) == 1;
    }
}
