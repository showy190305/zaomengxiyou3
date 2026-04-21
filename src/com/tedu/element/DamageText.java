package com.tedu.element;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class DamageText extends ElementObj {
    private String text;
    private Color color;
    private int life = 40;       // 飘字存活时间（帧）
    private double vy = -4.0;    // 向上飘的初始速度
    private float alpha = 1.0f;  // 透明度（用于渐渐消失）

    public DamageText(int x, int y, int damage, Color color) {
        this.setX(x);
        this.setY(y);
        this.text = "-" + damage;
        this.color = color;
    }

    @Override
    public void showElement(Graphics g) {
        if (life <= 0) return;

        // 1. 物理逻辑：向上飘且慢慢减速
        this.setY((int)(this.getY() + vy));
        vy *= 0.90; // 阻力减速
        life--;

        // 2. 消失动画：最后 15 帧开始变透明
        if (life < 15) {
            alpha -= 0.06f;
        }
        if (alpha < 0) alpha = 0;

        // 3. 渲染逻辑（跟着屏幕摄像机一起移动）
        int screenX = this.getX() + com.tedu.element.MapObj.bgOffsetX;
        g.setFont(new Font("微软雅黑", Font.BOLD, 26)); // 字体加大一点，更有打击感
        
        // 画个黑色文字阴影，防止背景太亮看不清
        g.setColor(new Color(0, 0, 0, (int)(255 * alpha)));
        g.drawString(text, screenX + 2, this.getY() + 2);
        
        // 画主文字颜色
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255 * alpha)));
        g.drawString(text, screenX, this.getY());
    }

    // =======================================================
    // 【防止报错的核心】：必须重写 ElementObj 的这两个抽象方法！
    // 哪怕飘字不需要键盘控制，也必须写出来空着。
    // =======================================================
    @Override
    public ElementObj createElement(String str) {
        return this;
    }

    @Override
    public void keyClick(boolean bl, int key) {
        // 飘字不需要响应键盘
    }

    // 给裁判用的判断方法
    public boolean isDead() { 
        return life <= 0; 
    }
}