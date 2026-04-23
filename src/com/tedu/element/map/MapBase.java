package com.tedu.element.map;

import java.awt.Graphics;
import javax.swing.ImageIcon;

import com.tedu.element.ElementObj;
import com.tedu.element.WuKong;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import java.util.List;

public abstract class MapBase extends ElementObj {

    protected int bgWidth = 0; 
    protected int bgHeight = 600; 
    
    // 【核心引擎变量】：全局摄像机偏移量
    public static int bgOffsetX = 0; 

    public MapBase() {
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

        // 子类负责具体的背景绘制
    }

    @Override public void keyClick(boolean bl, int key) {}
    @Override protected void move() {}
    @Override public void updateImage(long time) {}
    
    // 获取背景偏移量的方法
    public static int getBgOffsetX() {
        return bgOffsetX;
    }
}