package com.tedu.element.skill;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import com.tedu.element.ElementObj;
import com.tedu.element.map.MapBase;

public class Fireball extends ElementObj {
    private BufferedImage img;
    private boolean isLeft;       // 飞行方向
    private int speed = 15;       // 飞行速度（比走路快）
    public int damage;       // 火球的伤害值
    public boolean isActive = true; // 是否存活（打中怪或飞出屏幕就设为 false）

    public Fireball(int x, int y, boolean isLeft, int damage) {
        this.setX(x);
        this.setY(y);
        this.setW(80); // 暂定火球宽度
        this.setH(80); // 暂定火球高度
        this.isLeft = isLeft;
        this.damage = damage;
        try {
            // 【注意】：这里换成你找好的技能图片路径！
            img = ImageIO.read(new File("image/skill/fireball.png"));
        } catch (Exception e) {
            System.out.println("火球图片加载失败！");
        }
    }

    @Override
    public void showElement(Graphics g) {
        if (!isActive) return;

        // 1. 物理飞行逻辑
        if (isLeft) {
            this.setX(this.getX() - speed);
        } else {
            this.setX(this.getX() + speed);
        }

        // 2. 销毁逻辑：如果飞出地图边界，自动标记为死亡，防止内存泄漏
        if (this.getX() < 0 || this.getX() > 9000) {
            this.isActive = false;
        }

        // 3. 渲染逻辑（包含摄像机偏移和镜像翻转）
        if (img != null) {
            int screenX = this.getX() + MapBase.getBgOffsetX();
            
            if (isLeft) { // 如果是往左飞，把图片水平翻转一下
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.translate(screenX + this.getW(), this.getY());
                g2d.scale(-1, 1);
                g2d.drawImage(img, 0, 0, this.getW(), this.getH(), null);
                g2d.dispose();
            } else { // 往右飞正常画
                g.drawImage(img, screenX, this.getY(), this.getW(), this.getH(), null);
            }
        }
    }
    
    // 给裁判用的 Getter，获取火球的碰撞箱
    public java.awt.Rectangle getHitBox() {
        return new java.awt.Rectangle(this.getX() + 10, this.getY() + 10, this.getW() - 20, this.getH() - 20);
    }
}