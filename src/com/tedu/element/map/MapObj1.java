package com.tedu.element.map;

import java.awt.Graphics;
import javax.swing.ImageIcon;

import com.tedu.element.ElementObj;
import com.tedu.element.WuKong;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import java.util.List;

public class MapObj1 extends MapBase {

    private ImageIcon[] bgs = new ImageIcon[6];
    private int bgWidth = 0; 
    private int bgHeight = 600; 
    
    public MapObj1() {
        for (int i = 0; i < 6; i++) {
            bgs[i] = new ImageIcon("image/bg/longgong/bg" + (i + 1) + ".jpg");
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

        if (cameraX > 400) {
            MapBase.bgOffsetX = -(cameraX - 400); 
        } else {
            MapBase.bgOffsetX = 0;
        }


        // 绘制无缝背景
        for (int i = 0; i < 6; i++) {
            int drawX = MapBase.bgOffsetX + (i * bgWidth);
            g.drawImage(bgs[i].getImage(), drawX, 0, bgWidth, bgHeight, null);
        }
    }

    @Override public void keyClick(boolean bl, int key) {}
    @Override protected void move() {}
    @Override public void updateImage(long time) {}
}