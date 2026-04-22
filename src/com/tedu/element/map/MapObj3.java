package com.tedu.element.map;

import java.awt.Graphics;
import javax.swing.ImageIcon;

import com.tedu.element.ElementObj;
import com.tedu.element.WuKong;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import java.util.List;

public class MapObj3 extends MapBase {

    private ImageIcon bg1;  // 固定底层背景
    private ImageIcon bg2;   // 大场景背景图
    private ImageIcon[] bg3s = new ImageIcon[6];  // 滚动地板背景
    private int bgWidth = 0; 
    private int bgHeight = 600; 
    private int bg2Width = 0; // bg2宽度
    private int bg3Width = 0; // bg3宽度
    
    // 【核心引擎变量】：全局摄像机偏移量
    public static int bgOffsetX = 0; 

    public MapObj3() {
        // 加载固定底层背景
        bg1 = new ImageIcon("image/bg/nantianmen/bg1.png");
        
        // 加载大场景背景图
        bg2 = new ImageIcon("image/bg/nantianmen/bg2.png");
        bg2Width = bg2.getIconWidth();
        if (bg2Width <= 0) bg2Width = 1500;  // 增加默认宽度
        
        // 加载滚动地板背景
        for (int i = 0; i < 6; i++) {
            bg3s[i] = new ImageIcon("image/bg/nantianmen/bg3.jpg");
        }
        bg3Width = bg3s[0].getIconWidth();
        if (bg3Width <= 0) bg3Width = 1500;
        
        System.out.println("bg2:"+bg2Width);

        // 设置整体背景尺寸
        bgWidth = Math.min(bg2Width, bg3Width);
        if (bgWidth <= 0) bgWidth = 1500; 
    }

    @Override
    public void showElement(Graphics g) {
        int cameraX = 0;
        try {
            List<ElementObj> players = ElementManager.getManager().getElementsByKey(GameElement.PLAY);
            for (ElementObj obj : players) {
                // 【修复Bug】：必须认准是WuKong，不能抓到Weapon！
                if (obj instanceof WuKong) {
                    cameraX = obj.getX();
                    break;
                }
            }
        } catch (Exception e) {}

        // 当猴子走到屏幕中点(400)后，计算背景需要往左拉多少
        if (cameraX > 400) {
            bgOffsetX = -(cameraX - 400); 
        } else {
            bgOffsetX = 0;
        }

        // 绘制固定底层背景
        g.drawImage(bg1.getImage(), 0, 0, bg1.getIconWidth(), bgHeight, null);

        // 绘制大场景背景图（bg2），随人物移动反向滚动
        int bg2DrawX = bgOffsetX % bg2Width;
        // 确保背景图至少覆盖整个屏幕
        while (bg2DrawX > 0) {
            bg2DrawX -= bg2Width;
        }
        // 绘制背景图，可能需要重复绘制以覆盖整个屏幕
        for (int i = 0; i < 3; i++) {  // 绘制最多3次以确保覆盖屏幕
            g.drawImage(bg2.getImage(), bg2DrawX + i * bg2Width, 0, bg2Width, bgHeight, null);
        }
        
        // 绘制滚动地板背景（bg3），从底部开始，长度超过bg2
        for (int i = 0; i < 6; i++) {
            int drawX = bgOffsetX + (i * bg3Width);
            g.drawImage(bg3s[i].getImage(), drawX, bgHeight - bg3s[i].getIconHeight(), bg3Width, bg3s[i].getIconHeight(), null);
        }
    }

    @Override public void keyClick(boolean bl, int key) {}
    @Override protected void move() {}
    @Override public void updateImage(long time) {}
}