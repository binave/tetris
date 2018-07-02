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
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 图片加载器，支持读取本地文件和 jar 包
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

    static {
        try {

            Map<String, BufferedImage> imageMap = getImageMap("image");
            // 加载图片
            background = imageMap.get("image/background.png");
            backgroundDual = imageMap.get("image/backgrounddouble.png");
            pause = imageMap.get("image/pause.png");
            game_over = imageMap.get("image/game_over.png");
            color[0] = imageMap.get("image/red.png");
            color[1] = imageMap.get("image/orange.png");
            color[2] = imageMap.get("image/yellow.png");
            color[3] = imageMap.get("image/green.png");
            color[4] = imageMap.get("image/blue2.png");
            color[5] = imageMap.get("image/blue1.png");
            color[6] = imageMap.get("image/purple.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 通过路径，获得图片
     *
     */
    private static Map<String, BufferedImage> getImageMap(String path) throws IOException {
        Enumeration<URL> dirs = ClassLoader.getSystemClassLoader().getResources(path);

        List<String> list = new ArrayList<>();
        while (dirs.hasMoreElements()) {
            // 获取下一个元素
            URL url = dirs.nextElement();
            // 得到协议的名称
            String protocol = url.getProtocol();
            // 如果是以文件的形式保存在服务器上

            if ("file".equals(protocol)) {
                // 以文件的方式扫描整个包下的文件 并添加到集合中
                findInDir(list, url.getFile());
            } else if ("jar".equals(protocol)) {
                // jar 包内容
                findInJar(list, url, path);
            }

        }

        // 当前路径
        URL url = ClassLoader.getSystemClassLoader().getResource(".");
        String pwd = url != null ? url.getPath() : "";

        Map<String, BufferedImage> imageMap = new HashMap<>();
        for (String resource : list) {
            resource = pwd.length() > 0 && resource.startsWith(pwd)
                    ? resource.substring(pwd.length())
                    : resource;
            InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(resource);
            imageMap.put(resource, ImageIO.read(is));
        }

        return imageMap;
    }


    /**
     * 获得 jar 文件
     */
    private static void findInJar(List<String> list, URL url, String packageDirName) {

        // 如果是jar包文件，定义一个JarFile
        JarFile jar;
        try {

            // 获取jar
            jar = ((JarURLConnection) url.openConnection()).getJarFile();
            Enumeration<JarEntry> entries = jar.entries();

            // 同样的进行循环迭代
            while (entries.hasMoreElements()) {
                // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                String name = entries.nextElement().getName();

                // 如果是以'/'开头的，获取后面的字符串
                if (name.charAt(0) == '/') name = name.substring(1);
                // 显示文件，如果以 '/' 结尾，是一个包
                if (name.startsWith(packageDirName) && '/' != name.charAt(name.length() - 1)) {
                    list.add(name);
                }

            }
        } catch (IOException e) {
            // 在扫描用户定义视图时从jar包获取文件出错
            e.printStackTrace();
        }
    }


    /**
     * 以文件的形式来获取包下的所有文件
     * 包含递归
     */
    private static void findInDir(List<String> list, String packagePath) {
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists()) return;
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                // 如果是目录 则继续扫描
                if (file.isDirectory()) {
                    findInDir(list, packagePath + '/' + file.getName());
                } else list.add(packagePath + '/' + file.getName());
            }
        } else list.add(packagePath + '/' + dir.getName());
    }


}