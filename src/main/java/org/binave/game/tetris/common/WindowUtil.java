package org.binave.game.tetris.common;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 窗口工具类
 */
public class WindowUtil {

    private WindowUtil() {}

    /**
     * 为无边框窗口启用拖动功能
     */
    public static void enableDrag(JFrame frame) {
        final Point[] dragStart = new Point[1];

        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStart[0] = e.getPoint();
            }
        });

        frame.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point now = e.getLocationOnScreen();
                frame.setLocation(now.x - dragStart[0].x, now.y - dragStart[0].y);
            }
        });
    }

    /**
     * 在窗口打开时请求焦点（解决 AOT 首次启动键盘无响应问题）
     */
    public static void requestFocusOnOpen(JFrame frame, JPanel panel) {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                panel.requestFocusInWindow();
            }
        });
    }
}
