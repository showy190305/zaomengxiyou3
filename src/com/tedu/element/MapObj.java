package com.tedu.element;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import java.util.List;

public class MapObj extends ElementObj {

    private ImageIcon[] bgs = new ImageIcon[6];
    private int bgWidth = 0; 
    private int bgHeight = 600; 
    
    // 【核心引擎变量】：全局摄像机偏移量
    public static int bgOffsetX = 0; 

    public MapObj() {
        for (int i = 0; i < 6; i++) {
            bgs[i] = new ImageIcon("image/bg" + (i + 1) + ".jpg");
        }
        bgWidth = bgs[0].getIconWidth(); 
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


        // 绘制无缝背景
        for (int i = 0; i < 6; i++) {
            int drawX = bgOffsetX + (i * bgWidth);
            g.drawImage(bgs[i].getImage(), drawX, 0, bgWidth, bgHeight, null);
        }
    }

    @Override public void keyClick(boolean bl, int key) {}
    @Override protected void move() {}
    @Override public void updateImage(long time) {}
}