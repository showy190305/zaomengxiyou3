package com.tedu.element;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.tedu.element.map.MapBase;

public class Portal extends ElementObj {

    @Override
    public ElementObj createElement(String str) {
        String[] params = str.split(",");
        this.setX(Integer.parseInt(params[0]));
        this.setY(Integer.parseInt(params[1]));
        this.setW(Integer.parseInt(params[2]));
        this.setH(Integer.parseInt(params[3]));
        return this;
    }

    @Override
    public void showElement(Graphics g) {
        // 摄像机偏移，让传送门老老实实呆在地图尽头
        int screenX = this.getX() + MapBase.getBgOffsetX();
        
        // 当传送门进入屏幕视野时才绘制
        if (screenX > -200 && screenX < 1000) {
            Graphics2D g2d = (Graphics2D) g;
            
            // 画一个半透明的蓝色传送门
            g2d.setColor(new Color(50, 150, 255, 150)); 
            g2d.fillOval(screenX, this.getY(), this.getW(), this.getH());
            
            // 画一个发光的青色边框
            g2d.setColor(Color.CYAN);
            g2d.drawOval(screenX, this.getY(), this.getW(), this.getH());
            
            // 加上文字提示
            g2d.setColor(Color.WHITE);
            g2d.setFont(new java.awt.Font("微软雅黑", java.awt.Font.BOLD, 16));
            g2d.drawString("【过关传送门】", screenX + 10, this.getY() - 10);
        }
    }
}