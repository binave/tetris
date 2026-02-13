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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 图片加载器，支持 JAR 和 Native Image
 *
 * @author by bin jin on 2018/07/02 20:32.
 */
public class ImageLoader {

    public static BufferedImage background;
    public static BufferedImage backgroundDual;
    public static BufferedImage pause;
    public static BufferedImage game_over;

    /**
     * 方块随机颜色图片
     */
    public static BufferedImage[] color = new BufferedImage[7];

    /**
     * 已知图片资源列表（编译时确定，兼容 JAR 和 Native Image）
     */
    private static final String[] IMAGE_FILES = {
            "background.png",
            "backgrounddouble.png",
            "pause.png",
            "game_over.png",
            "red.png",
            "orange.png",
            "yellow.png",
            "green.png",
            "blue2.png",
            "blue1.png",
            "purple.png"
    };

    /**
     * Native Image 环境标识
     */
    private static final boolean IS_NATIVE_IMAGE =
            System.getProperty("org.graalvm.nativeimage.kind") != null;

    static {
        // 在 Native Image 环境中显式初始化 ImageIO SPI
        initImageIOSPI();

        try {
            Map<String, BufferedImage> imageMap = loadImageMap("image");

            // 加载图片并进行 AOT 兼容性验证
            background = getImageOrThrow(imageMap, "image/background.png");
            backgroundDual = getImageOrThrow(imageMap, "image/backgrounddouble.png");
            pause = getImageOrThrow(imageMap, "image/pause.png");
            game_over = getImageOrThrow(imageMap, "image/game_over.png");
            color[0] = getImageOrThrow(imageMap, "image/red.png");
            color[1] = getImageOrThrow(imageMap, "image/orange.png");
            color[2] = getImageOrThrow(imageMap, "image/yellow.png");
            color[3] = getImageOrThrow(imageMap, "image/green.png");
            color[4] = getImageOrThrow(imageMap, "image/blue2.png");
            color[5] = getImageOrThrow(imageMap, "image/blue1.png");
            color[6] = getImageOrThrow(imageMap, "image/purple.png");

        } catch (Throwable e) {
            System.err.printf("ImageLoader FAILED: %s, %s%n", e.getClass().getName(), e.getMessage());
            for (StackTraceElement ste : e.getStackTrace()) {
                System.err.printf("  at %s", ste.toString());
            }
            throw new RuntimeException("Failed to load images", e);
        }
    }

    /**
     * 显式初始化 ImageIO SPI，兼容 Native Image 环境
     * 在普通 JVM 中，SPI 通过 ServiceLoader 自动发现；
     * 在 Native Image 中，需要触发类加载来注册解码器。
     */
    private static void initImageIOSPI() {
        try {
            // 通过 Class.forName 触发 PNG 解码器的静态初始化
            // 这会在 JDK 内部自动注册到 IIORegistry
            Class.forName("com.sun.imageio.plugins.png.PNGImageReaderSpi");
        } catch (ClassNotFoundException e) {
            // 非标准 JDK 可能没有此类
            if (IS_NATIVE_IMAGE) {
                System.err.printf("[Native Image] PNGImageReaderSpi not found: %s%n", e.getMessage());
            }
        } catch (Exception e) {
            if (IS_NATIVE_IMAGE) {
                System.err.printf("[Native Image] ImageIO SPI init warning: %s%n", e.getMessage());
            }
        }
    }

    /**
     * 获取图片或抛出详细异常（AOT 诊断）
     */
    private static BufferedImage getImageOrThrow(Map<String, BufferedImage> imageMap, String key)
            throws IOException {
        BufferedImage image = imageMap.get(key);
        if (image == null) {
            throw new IOException(String.format(
                    "Image not loaded: %s%n" +
                            "In Native Image, check: %n" +
                            "  1. PNG ImageReader SPI registered (reflect-config.json)%n" +
                            "  2. Resource embedded (resource-config.json)",
                    key
            ));
        }
        return image;
    }


    /**
     * 通过路径加载图片（兼容 JAR classpath 和 Native Image）
     */
    private static Map<String, BufferedImage> loadImageMap(String path) throws IOException {

        Map<String, BufferedImage> imageMap = new HashMap<>();

        for (String imageFile : IMAGE_FILES) {
            String resourcePath = path + "/" + imageFile;

            // 优先从 classpath 加载（JAR 或 Native Image 嵌入资源）
            InputStream is = ClassLoader.getSystemResourceAsStream(resourcePath);
            if (is != null) {
                BufferedImage image = ImageIO.read(is);
                is.close();
                // AOT 兼容：ImageIO.read() 在无解码器时返回 null
                if (image == null) {
                    throw new IOException(String.format(
                            "ImageIO.read() returned null for: %s%n" +
                                    "In Native Image, ensure PNGImageReaderSpi is registered.",
                            resourcePath
                    ));
                }
                imageMap.put(resourcePath, image);
            } else {
                // 回退到文件系统（开发环境）
                File file = new File(path, imageFile);
                if (file.exists()) {
                    BufferedImage image = ImageIO.read(file);
                    if (image == null) {
                        throw new IOException("ImageIO.read() returned null for file: " + file);
                    }
                    imageMap.put(resourcePath, image);
                } else {
                    throw new IOException("Resource not found: " + resourcePath);
                }
            }
        }

        return imageMap;
    }
}
