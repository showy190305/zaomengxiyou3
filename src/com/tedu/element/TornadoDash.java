package com.tedu.element;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class TornadoDash extends ElementObj {
    private List<BufferedImage> images = new ArrayList<>();
    private int frameIndex = 0;
    private int timer = 0;
    
    // 动画播放速度（数字越小，龙卷风转得越快）
    private int interval = 3;  
    
    // 龙卷风的核心属性：方向和速度
    private boolean isLeft;    // 记录龙卷风是朝左还是朝右
    private int moveSpeed = 10; // 龙卷风突进的速度（决定了大圣能冲多快）

    private int damage;

    // 构造方法：比普通的特效多接收了一个 isLeft 参数
    public TornadoDash(int x, int y, int w, int h, boolean isLeft,int damage) {
        this.setX(x);
        this.setY(y);
        this.setW(w);
        this.setH(h);
        this.isLeft = isLeft;
        this.damage = damage; // 接收伤害
        loadImages();
    }

    private void loadImages() {
        try {
            // 加载 12 张素材，假设你的图片命名是 dash_1.png 到 dash_12.png
            for (int i = 1; i <= 12; i++) {
                images.add(ImageIO.read(new File("image/skill/dash_" + i + ".png")));
            }
        } catch (Exception e) {
            System.out.println("⚠️ 龙卷风素材加载失败，请检查 image/skill/dash_x.png 路径！");
        }
    }

    private void updateFrame() {
        // 1. 动画帧更新
        timer++;
        if (timer >= interval) {
            frameIndex++;
            timer = 0;
            // 播完 12 张图就销毁自己
            if (frameIndex >= images.size()) {
                this.setLive(false); 
            }
        }

        // 2. 位置向前移动逻辑
        if (this.isLive()) {
            if (isLeft) {
                // 如果朝左，X坐标不断减小
                this.setX(this.getX() - moveSpeed);
            } else {
                // 如果朝右，X坐标不断增大
                this.setX(this.getX() + moveSpeed);
            }
        }

        // =======================================================
        // 【新增】：突进过程中的伤害判定
        // =======================================================
        if (this.isLive()) {
            int screenX = this.getX() + com.tedu.element.MapObj.bgOffsetX;
            java.awt.Rectangle skillRect = new java.awt.Rectangle(screenX, this.getY(), this.getW(), this.getH());

            java.util.List<com.tedu.element.ElementObj> enemies = 
                com.tedu.manager.ElementManager.getManager().getElementsByKey(com.tedu.manager.GameElement.ENEMY);

            for (com.tedu.element.ElementObj obj : enemies) {
                if (obj instanceof com.tedu.element.BaseEnemy) {
                    com.tedu.element.BaseEnemy enemy = (com.tedu.element.BaseEnemy) obj;
                    int enemyScreenX = enemy.getX() + com.tedu.element.MapObj.bgOffsetX;
                    java.awt.Rectangle enemyRect = new java.awt.Rectangle(enemyScreenX, enemy.getY(), enemy.getW(), enemy.getH());

                    // 如果撞到了怪物
                    if (skillRect.intersects(enemyRect)) {
                        // 触发真实伤害！(同样享受怪物的 20 帧无敌保护，防止秒杀)
                        enemy.takeDamage(this.damage); 
                    }
                }
            }
        }
    }

    @Override
    public void showElement(Graphics g) {
        // 先更新逻辑
        if (this.isLive()) {
            updateFrame();
        }

        // 再执行渲染
        if (this.isLive() && frameIndex < images.size()) {
            // 注意加上地图偏移量
            int screenX = this.getX() + MapObj.bgOffsetX;
            BufferedImage currentImage = images.get(frameIndex);

            // 如果大圣朝左释放技能，我们需要把素材水平翻转一下
            if (isLeft) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                g2d.translate(screenX + this.getW(), this.getY());
                g2d.scale(-1, 1);
                g2d.drawImage(currentImage, 0, 0, this.getW(), this.getH(), null);
                g2d.dispose();
            } else {
                // 朝右释放则正常绘制
                g.drawImage(currentImage, screenX, this.getY(), this.getW(), this.getH(), null);
            }
        }
    }
}