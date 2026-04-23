package com.tedu.element.skill;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import com.tedu.element.ElementObj;
import com.tedu.element.map.MapBase;

public class SpecialSpin extends ElementObj {
    private List<BufferedImage> images = new ArrayList<>();
    private int frameIndex = 0;
    private int timer = 0;
    private int interval = 4; // 每张图停留 4 帧（控制旋转快慢）
    private int damage;

    public SpecialSpin(int x, int y, int w, int h, int damage) {
        this.setX(x);
        this.setY(y);
        this.setW(w);
        this.setH(h);
        this.damage = damage;
        loadImages();
    }

    private void loadImages() {
        try {
            // 加载 10 张大招素材，注意检查路径和文件名
            for (int i = 1; i <= 10; i++) {
                images.add(ImageIO.read(new File("image/skill/sp_" + i + ".png")));
            }
        } catch (Exception e) {
            System.out.println("大招素材加载失败，请检查路径 image/skill/sp_x.png！");
        }
    }

    // 【绕过 final model 的绝招】：自己写一个更新方法
    private void updateFrame() {
        timer++;
        if (timer >= interval) {
            frameIndex++;
            timer = 0;
            // 如果 10 张图播完了
            if (frameIndex >= images.size()) {
                this.setLive(false); // 标记为死亡，ElementManager 会自动把它当垃圾扫掉
            }
        }
        // =======================================================
        // 【新增】：持续范围伤害判定 (利用 BaseEnemy 的多态)
        // =======================================================
        if (this.isLive()) {
            // 1. 获取大招当前的判定区域 (一定要加 bgOffsetX 算屏幕真实坐标)
            int screenX = this.getX() + com.tedu.element.map.MapBase.getBgOffsetX();
            java.awt.Rectangle skillRect = new java.awt.Rectangle(screenX, this.getY(), this.getW(), this.getH());

            // 2. 从管理器获取所有敌人
            java.util.List<com.tedu.element.ElementObj> enemies = 
                com.tedu.manager.ElementManager.getManager().getElementsByKey(com.tedu.manager.GameElement.ENEMY);

            // 3. 遍历敌人，如果有交集，就扣血
            for (com.tedu.element.ElementObj obj : enemies) {
                // 【多态的威力】：只要是 BaseEnemy 的子类（不管是 enemy2 还是未来的 Boss），统统生效！
                if (obj instanceof com.tedu.element.enemy.BaseEnemy) {
                    com.tedu.element.enemy.BaseEnemy enemy = (com.tedu.element.enemy.BaseEnemy) obj;
                    
                    int enemyScreenX = enemy.getX() + com.tedu.element.map.MapBase.getBgOffsetX();
                    java.awt.Rectangle enemyRect = new java.awt.Rectangle(enemyScreenX, enemy.getY(), enemy.getW(), enemy.getH());

                    if (skillRect.intersects(enemyRect)) {
                        // 每次判定造成 2 点伤害
                        // 因为 enemy 自带 20 帧无敌保护，所以怪物只会在大招里持续受到多段伤害，不会瞬间被秒
                        enemy.takeDamage(this.damage);
                    }
                }
            }
        }
    }

    @Override
    public void showElement(Graphics g) {
        // 1. 画图前，先更新一下帧数和生死状态
        if (this.isLive()) {
            updateFrame();
        }

        // 2. 如果更新完还活着，就把当前的图画出来
        if (this.isLive() && frameIndex < images.size()) {
            int screenX = this.getX() + MapBase.getBgOffsetX();
            g.drawImage(images.get(frameIndex), screenX, this.getY(), this.getW(), this.getH(), null);
        }
    }
}