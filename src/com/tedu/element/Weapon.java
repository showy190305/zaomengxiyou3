package com.tedu.element;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class Weapon extends ElementObj {
    // 攻击动作锁定
    private boolean attackLock = false;
    // 攻击动作记忆性计数
    // attackQueued未用，移除
    
    // 武器角色的图像帧列表
    private List<BufferedImage> frames;
    private BufferedImage spriteSheet; // 整张精灵图
    
    // 动作分组
    private Map<String, List<Integer>> actionGroups;
    
    // 当前帧索引
    private int currentFrame = 0;
    private String currentAction = "idle"; // 默认为待机动作
    
    // 键盘控制相关
    private boolean isLeft = false; // 是否面向左侧
    private int attackCount = 0; // 攻击计数
    
    // 二连击奔跑相关
    private boolean aPressed = false; // A键是否被按下
    private boolean dPressed = false; // D键是否被按下
    private int aClickCount = 0; // A键点击次数
    private int dClickCount = 0; // D键点击次数
    private long lastAReleaseTime = 0; // 上次松开A的时间
    private long lastDReleaseTime = 0; // 上次松开D的时间
    private static final long DOUBLE_CLICK_TIMEOUT = 200; // 0.5秒超时
    
    // 动画控制
    private int animationCounter = 0;
    private final int FRAME_DELAY = 10; // 每隔多少次刷新切换一次帧
    
    // 移动相关
    private int walkStep = 3; // 行走时每帧移动的距离
    private int runStep = 9;  // 奔跑时每帧移动的距离

    // 添加移动频率控制
    private long lastMoveTime = 0;
    private static final long MOVE_INTERVAL = 100; // 0.1秒移动间隔
    
    // 记忆当前动作的帧索引
    private int currentActionFrameIndex = 0; // 当前动作中播放到第几帧

    public Weapon() {
        // 初始化图像帧
        loadSprites();
        // 初始化动作分组
        initializeActionGroups();
    // ...existing code...
    }
    
    // 加载武器精灵图并裁剪出各个帧
    private void loadSprites() {
        try {
            // 使用ImageIO加载整张精灵图
            spriteSheet = ImageIO.read(new File("image/weapon/weapon0.png"));
            
            // 初始化帧列表
            frames = new ArrayList<>();
            
            // 根据测试结果：第一帧坐标(100,110)，相邻帧坐标差值200
            int firstX = 20;  // 第一帧的X坐标
            int firstY = 30;  // 第一帧的Y坐标
            int frameSpacing = 200; // 帧之间的间距
            int frameWidth = 150;    // 每帧的宽度
            int frameHeight = 150;   // 每帧的高度
            
            // 计算可以提取多少帧
            // 假设精灵图按行列排列，我们从第一帧开始逐个提取
            int maxCols = (spriteSheet.getWidth() - firstX) / frameSpacing;
            int maxRows = (spriteSheet.getHeight() - firstY) / frameSpacing;
            
            // 提取帧
            for (int row = 0; row < maxRows; row++) {
                for (int col = 0; col < maxCols; col++) {
                    int x = firstX + col * frameSpacing;
                    int y = firstY + row * frameSpacing;
                    
                    // 确保坐标在图像范围内
                    if (x + frameWidth <= spriteSheet.getWidth() && 
                        y + frameHeight <= spriteSheet.getHeight()) {
                        
                        BufferedImage frame = spriteSheet.getSubimage(x, y, frameWidth, frameHeight);
                        frames.add(frame);
                    }
                }
            }
            
            System.out.println("武器精灵图加载完成，共加载 " + frames.size() + " 帧");
            System.out.println("使用坐标: 第一帧(" + firstX + "," + firstY + ")，间距:" + frameSpacing + 
                             "，帧尺寸:" + frameWidth + "x" + frameHeight);
            
        } catch (IOException e) {
            System.out.println("加载武器精灵图失败: " + e.getMessage());
            e.printStackTrace();
            
            // 如果文件不存在，创建一个临时的BufferedImage
            frames = new ArrayList<>();
            BufferedImage tempImage = new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB);
            frames.add(tempImage);
        }
    }
    
    @Override
    public void keyClick(boolean bl, int key) {
        long currentTime = System.currentTimeMillis();

        if (attackLock) {
            return;
        }
        // --- J键逻辑 ---
        if (key == 'J' || key == 'j') {
            if (bl) {
                if (!attackLock) {
                    attackCount++;
                    if (attackCount > 4) attackCount = attackCount % 4;
                    attackLock = true;
                    playAttackByCount();
                }
            } else {
                checkIdleState();
            }
        }
        // --- A键逻辑 ---
        else if (key == 'A' || key == 'a') {
            if (bl) { // 按下
                aPressed = true;
                isLeft = true;
                // 只在未奔跑时判断超时
                if (!"run".equals(currentAction) && aClickCount == 1 && (currentTime - lastAReleaseTime > DOUBLE_CLICK_TIMEOUT)) {
                    aClickCount = 0;
                }
                if (aClickCount == 1) {
                    setAction("run");
                } else {
                    setAction("walk");
                }
            } else { // 松开
                aPressed = false;
                aClickCount++;
                lastAReleaseTime = currentTime;
                if (aClickCount > 1) aClickCount = 1;
                // 松开时如果是奔跑，清零计数
                if ("run".equals(currentAction)) {
                    aClickCount = 0;
                }
                checkIdleState();
            }
        }
        // --- D键逻辑 ---
        else if (key == 'D' || key == 'd') {
            if (bl) { // 按下
                dPressed = true;
                isLeft = false;
                if (!"run".equals(currentAction) && dClickCount == 1 && (currentTime - lastDReleaseTime > DOUBLE_CLICK_TIMEOUT)) {
                    dClickCount = 0;
                }
                if (dClickCount == 1) {
                    setAction("run");
                } else {
                    setAction("walk");
                }
            } else { // 松开
                dPressed = false;
                dClickCount++;
                lastDReleaseTime = currentTime;
                if (dClickCount > 1) dClickCount = 1;
                if ("run".equals(currentAction)) {
                    dClickCount = 0;
                }
                checkIdleState();
            }
        }
    }

    // 根据attackCount播放对应攻击动作
    private void playAttackByCount() {
        if (attackCount == 1) {
            setAction("attack1");
        } else if (attackCount == 2) {
            setAction("attack2");
        } else if (attackCount == 3) {
            setAction("attack3");
        } else if (attackCount == 4) {
            setAction("attack4");
        } else {
            setAction("idle");
        }
    }
    // 检查是否需要回到待机状态
    private void checkIdleState() {
        // 如果没有任何移动按键被按下，则回到待机状态
        if (!aPressed && !dPressed) {
            if (!"idle".equals(currentAction) && !"attack1".equals(currentAction) && 
                !"attack2".equals(currentAction) && !"attack3".equals(currentAction) && 
                !"attack4".equals(currentAction)) {
                setAction("idle");
            }
        }
    }
    
    // 初始化动作分组
    private void initializeActionGroups() {
        actionGroups = new HashMap<>();
        
        // 待机动作: 帧0-4
        List<Integer> idleFrames = new ArrayList<>();
        for (int i = 0; i <= 4; i++) {
            idleFrames.add(i);
        }
        actionGroups.put("idle", idleFrames);
        
        // 行走动作: 帧10-13
        List<Integer> walkFrames = new ArrayList<>();
        for (int i = 10; i <= 13; i++) {
            walkFrames.add(i);
        }
        actionGroups.put("walk", walkFrames);
        
        // 奔跑动作: 帧15-18
        List<Integer> runFrames = new ArrayList<>();
        for (int i = 15; i <= 18; i++) {
            runFrames.add(i);
        }
        actionGroups.put("run", runFrames);
        
        // 向上动作: 帧20
        List<Integer> upFrames = new ArrayList<>();
        upFrames.add(20);
        actionGroups.put("up", upFrames);
        
        // 向下动作: 帧21
        List<Integer> downFrames = new ArrayList<>();
        downFrames.add(21);
        actionGroups.put("down", downFrames);
        
        // 翻滚动作: 帧25-29
        List<Integer> rollFrames = new ArrayList<>();
        for (int i = 25; i <= 29; i++) {
            rollFrames.add(i);
        }
        actionGroups.put("roll", rollFrames);
        
        // 打击动作1: 帧30-34
        List<Integer> attack1Frames = new ArrayList<>();
        for (int i = 30; i <= 34; i++) {
            attack1Frames.add(i);
        }
        actionGroups.put("attack1", attack1Frames);
        
        // 打击动作2: 帧35-39
        List<Integer> attack2Frames = new ArrayList<>();
        for (int i = 35; i <= 39; i++) {
            attack2Frames.add(i);
        }
        actionGroups.put("attack2", attack2Frames);
        
        // 打击动作3: 帧40-44
        List<Integer> attack3Frames = new ArrayList<>();
        for (int i = 40; i <= 44; i++) {
            attack3Frames.add(i);
        }
        actionGroups.put("attack3", attack3Frames);
        
        // 打击动作4: 帧45-47 
        List<Integer> attack3bFrames = new ArrayList<>();
        for (int i = 45; i <= 47; i++) {
            attack3bFrames.add(i);
        }
        actionGroups.put("attack4", attack3bFrames);
        
        // 受击动作: 帧62
        List<Integer> hitFrames = new ArrayList<>();
        hitFrames.add(62);
        actionGroups.put("hit", hitFrames);
    }
    
    // 设置当前动作
    public void setAction(String actionName) {
        if (actionGroups.containsKey(actionName)) {
            if (!actionName.equals(this.currentAction)) {
                // 只有动作切换时才重置帧索引
                this.currentAction = actionName;
                this.currentActionFrameIndex = 0;
                List<Integer> actionFrames = actionGroups.get(actionName);
                if (actionFrames != null && !actionFrames.isEmpty()) {
                    this.currentFrame = actionFrames.get(0);
                }
            }
            // 如果动作没变，不重置currentActionFrameIndex，实现帧记忆
        } else {
            System.out.println("未找到动作: " + actionName);
        }
    }
    
    // 获取指定动作的所有帧索引
    public List<Integer> getActionFrames(String actionName) {
        return actionGroups.get(actionName);
    }
    
    // 获取当前动作的所有帧索引
    public List<Integer> getCurrentActionFrames() {
        return actionGroups.get(currentAction);
    }
    
    @Override
    public ElementObj createElement(String str) {
        // 解析字符串参数，例如 "x,y,width,height"
        String[] params = str.split(",");
        if (params.length >= 2) {
            this.setX(Integer.parseInt(params[0]));
            this.setY(Integer.parseInt(params[1]));
            
            // 如果提供了宽高参数
            if (params.length >= 4) {
                this.setW(Integer.parseInt(params[2]));
                this.setH(Integer.parseInt(params[3]));
            } else {
                // 设置默认宽高
                this.setW(150);
                this.setH(150);
            }
        }
        return this;
    }
    
    @Override
    public void showElement(Graphics g) {
        // 更新动画帧
        updateAnimation();
        
        // 绘制当前帧
        if (!frames.isEmpty() && currentFrame < frames.size()) {
            BufferedImage currentImage = frames.get(currentFrame);
            
            // 如果需要镜像翻转（面向右侧），则进行水平翻转
            if (!isLeft) {  // 面向右侧
                Graphics2D g2d = (Graphics2D) g.create();  // 创建副本以不影响其他绘制操作
                g2d.translate(this.getX() + this.getW(), this.getY());  // 移动到目标位置
                g2d.scale(-1, 1);  // 水平翻转
                g2d.drawImage(currentImage, 0, 0, this.getW(), this.getH(), null);
                g2d.dispose();  // 释放资源
            } else {  // 面向左侧，正常绘制
                g.drawImage(currentImage, this.getX(), this.getY(), this.getW(), this.getH(), null);
            }
        }
    }
    
    // 更新动画帧
    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        boolean canMove = (currentTime - lastMoveTime >= MOVE_INTERVAL);

        // 奔跑状态下不自动清零点击次数，只有在未奔跑时才清零
        if (!"run".equals(currentAction)) {
            if (aClickCount == 1 && (currentTime - lastAReleaseTime > DOUBLE_CLICK_TIMEOUT)) {
                aClickCount = 0;
            }
            if (dClickCount == 1 && (currentTime - lastDReleaseTime > DOUBLE_CLICK_TIMEOUT)) {
                dClickCount = 0;
            }
        }

        if (canMove && (("walk".equals(currentAction) || "run".equals(currentAction)) && (aPressed || dPressed))) {
            lastMoveTime = currentTime;
            List<Integer> actionFrames = actionGroups.get(currentAction);
            if (actionFrames != null && !actionFrames.isEmpty()) {
                currentActionFrameIndex = (currentActionFrameIndex + 1) % actionFrames.size();
                this.currentFrame = actionFrames.get(currentActionFrameIndex);
            }
            if ("walk".equals(currentAction)) {
                if (isLeft) {
                    this.setX(this.getX() - walkStep);
                } else {
                    this.setX(this.getX() + walkStep);
                }
            } else if ("run".equals(currentAction)) {
                if (isLeft) {
                    this.setX(this.getX() - runStep);
                } else {
                    this.setX(this.getX() + runStep);
                }
            }
        }
        // idle自动循环播放
        else if (!aPressed && !dPressed && "idle".equals(currentAction)) {
            if (canMove) {
                lastMoveTime = currentTime;
                List<Integer> actionFrames = actionGroups.get("idle");
                if (actionFrames != null && !actionFrames.isEmpty()) {
                    currentActionFrameIndex = (currentActionFrameIndex + 1) % actionFrames.size();
                    this.currentFrame = actionFrames.get(currentActionFrameIndex);
                }
            }
        }
        // 攻击动作完整播放锁定，记忆性
        else if (attackLock && (currentAction.startsWith("attack"))) {
            if (canMove) {
                lastMoveTime = currentTime;
                List<Integer> actionFrames = actionGroups.get(currentAction);
                if (actionFrames != null && !actionFrames.isEmpty()) {
                    currentActionFrameIndex++;
                    if (currentActionFrameIndex >= actionFrames.size()) {
                        // 攻击动作播放完毕，解锁并进入idle
                        attackLock = false;
                        setAction("idle");
                    } else {
                        this.currentFrame = actionFrames.get(currentActionFrameIndex);
                    }
                }
            }
        }
    }
    
    // 获取当前帧
    public BufferedImage getCurrentFrame() {
        if (!frames.isEmpty() && currentFrame < frames.size()) {
            return frames.get(currentFrame);
        }
        return null;
    }
    
    // 获取总帧数
    public int getTotalFrames() {
        return frames.size();
    }
    
    // 设置当前帧
    public void setCurrentFrame(int frameIndex) {
        if (frameIndex >= 0 && frameIndex < frames.size()) {
            this.currentFrame = frameIndex;
        }
    }
    
    // 获取所有动作名称
    public List<String> getAllActionNames() {
        return new ArrayList<>(actionGroups.keySet());
    }
}