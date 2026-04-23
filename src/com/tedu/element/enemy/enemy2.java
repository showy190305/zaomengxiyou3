package com.tedu.element.enemy;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;

// 【核心】：继承刚才写的 BaseEnemy
public class enemy2 extends BaseEnemy {
    
    // 这只怪物专属的图片切割配置
    private int idleFrameCount = 6;   
    private int walkFrameCount = 4;   
    private int hitRowIndex = 3;      
    private int hitFrameCount = 1;    
    private int deadRowIndex = 4;     
    private int deadFrameCount = 5;   
    private int attackRowIndex = 6;   
    private int attackFrameCount = 4; 

    public enemy2() {
        // 1. 初始化属于 enemy2 的独有数值
        this.hp = 100;
        this.maxHp = 100;
        this.expDrop = 30;
        this.walkStep = 1; 
        this.attackRange = 80;
        this.visionRange = 400;
        
        // 2. 加载图片
        loadSprites();
    }
    
    // 这个方法一字未改，专门负责切 enemy2 自己的雪碧图
    private void loadSprites() {
        try {
            BufferedImage spriteSheet = ImageIO.read(new File("image/enemy/enemy2/enemy2.png"));
            // 注意：frames 和 actionGroups 在父类里定义过了，这里直接用
            frames = new ArrayList<>();
            actionGroups = new HashMap<>();
            
            int frameWidth = 200;
            int frameHeight = 200;
            
            // 1. 切割待机
            List<Integer> idleFrames = new ArrayList<>();
            for (int col = 0; col < idleFrameCount; col++) {
                frames.add(spriteSheet.getSubimage(col * frameWidth, 0, frameWidth, frameHeight));
                idleFrames.add(frames.size() - 1);
            }
            actionGroups.put("idle", idleFrames);
            
            // 2. 切割移动
            if (spriteSheet.getHeight() >= frameHeight * 2) {
                List<Integer> walkFrames = new ArrayList<>();
                for (int col = 0; col < walkFrameCount; col++) {
                    frames.add(spriteSheet.getSubimage(col * frameWidth, frameHeight, frameWidth, frameHeight));
                    walkFrames.add(frames.size() - 1); 
                }
                actionGroups.put("walk", walkFrames);
            }

            // 3. 切割受击
            if (spriteSheet.getHeight() >= frameHeight * (hitRowIndex + 1)) {
                List<Integer> hitFrames = new ArrayList<>();
                for (int col = 0; col < hitFrameCount; col++) {
                    frames.add(spriteSheet.getSubimage(col * frameWidth, hitRowIndex * frameHeight, frameWidth, frameHeight));
                    hitFrames.add(frames.size() - 1); 
                }
                actionGroups.put("hit", hitFrames);
            }

            // 4. 切割死亡
            if (spriteSheet.getHeight() >= frameHeight * (deadRowIndex + 1)) {
                List<Integer> deadFrames = new ArrayList<>();
                for (int col = 0; col < deadFrameCount; col++) {
                    frames.add(spriteSheet.getSubimage(col * frameWidth, deadRowIndex * frameHeight, frameWidth, frameHeight));
                    deadFrames.add(frames.size() - 1); 
                }
                actionGroups.put("dead", deadFrames);
            }

            // 5. 切割攻击
            if (spriteSheet.getHeight() >= frameHeight * (attackRowIndex + 1)) {
                List<Integer> attackFrames = new ArrayList<>();
                for (int col = 0; col < attackFrameCount; col++) {
                    frames.add(spriteSheet.getSubimage(col * frameWidth, attackRowIndex * frameHeight, frameWidth, frameHeight));
                    attackFrames.add(frames.size() - 1); 
                }
                actionGroups.put("attack", attackFrames);
            }
            
            System.out.println("enemy2 图片精确切割完毕！");
            
        } catch (IOException e) {
            System.out.println("加载 enemy2 图片失败: " + e.getMessage());
        }
    }
}