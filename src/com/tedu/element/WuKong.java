package com.tedu.element;

import java.awt.Color;
import java.awt.Font;
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

public class WuKong extends ElementObj {
// ===================================
    // 【新增】：当前攻击的唯一ID (保留 jwj)
    // ===================================
    public long currentAttackId = 0;

    // 攻击动作锁定
    private boolean attackLock = false;
    
    // 悟空角色的图像帧列表
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
    
    // ===================================================
    // 移动相关 (保留 jwj)
    // ===================================================
    private int walkStep = 2; // 行走时每帧移动的距离
    private int runStep = 5;  // 奔跑时每帧移动的距离

    // 添加移动频率控制
    private long lastMoveTime = 0;
    private static final long MOVE_INTERVAL = 100; // 0.1秒移动间隔
    
    // 记忆当前动作的帧索引
    private int currentActionFrameIndex = 0; // 当前动作中播放到第几帧

    // ===================================================
    // 背包 (保留组员 HEAD)
    // ===================================================
    private Inventory inventory;

    // ===================================================
    // --- 跳跃物理相关 (保留 jwj) ---
    // ===================================================
    private boolean isJumping = false; // 是否在空中
    private double vy = 0;             // Y轴垂直速度
    private final double GRAVITY = 1; // 重力加速度
    private final double JUMP_FORCE = -20; // 起跳初始爆发力
    private int groundY = 400;         // 地面基准高度 (出生点Y坐标)
    
    
    // ================= RPG 基础属性面板 (保留 jwj) =================
    public int level = 1;              // 当前等级

    public int hp = 100;               // 当前血量
    public int maxHp = 100;            // 最大血量

    public int mp = 50;                // 当前蓝量
    public int maxMp = 50;             // 最大蓝量

    public int exp = 0;                // 当前经验值
    public int maxExp = 100;           // 升到下一级所需的经验值

    public int attackPower = 35;       // 基础攻击力
    // ===================================================
    
    public boolean isHit = false;        // 是否处于挨打僵直中
    public int hitRecoveryTimer = 0;    // 挨打僵直计时器 (防止被瞬间秒杀)
    public boolean isDead = false;

    // ===================================================
    public int invincibleTimer = 0;      // 无敌帧计时器
    private int hitCount = 0;            // 挨打次数计数（用于触发倒地保护）
    public int deathTimer = 0;           // 死亡后倒计时，用于延迟退出
    // ===================================================

    // ===================================================
    // 【新增】：技能栏相关属性 (保留 jwj)
    // ===================================================
    // 技能栏必备属性 (一定要写在类最上面！)
    private java.awt.image.BufferedImage skillIcon;     // 技能图标
    public int skillCooldown = 0;                       // 火球术当前的冷却时间
    public final int SKILL_MAX_CD = 100;                // 火球术的总冷却时间

    // --- 技能开关与逻辑计时器 ---
    private boolean isHpToMpActive = false; // 技能2：血转蓝开关 (U)
    private boolean isMpToHpActive = false; // 技能3：蓝转血开关 (O)
    private int toggleTimer = 0;           // 资源转换频率计
    private int skillAniTimer = 0;         // 特效动画频率计

    // --- 特效序列帧集合 (初始化直接 new，防止报错) ---
    private java.util.List<java.awt.image.BufferedImage> skill2Frames = new java.util.ArrayList<>();
    private java.util.List<java.awt.image.BufferedImage> skill3Frames = new java.util.ArrayList<>();
    private int skill2FrameIndex = 0;
    private int skill3FrameIndex = 0;

    // 大招状态计时器 (控制大圣隐身和定身的时间)
    private int specialSkillTimer = 0;
    // --- 大招冷却与 UI 变量 ---
    private int specialSkillCooldown = 0;      // 当前冷却剩余时间
    private final int MAX_COOLDOWN = 300;      // 总冷却时间 (约 5 秒)
    private java.awt.image.BufferedImage spIconImg; // 右上角的技能图标

    // --- 技能 5：龙卷风突进 (假设按 P 键) ---
    private int dashSkillTimer = 0;            // 突进状态计时器 (控制大圣隐身和移动)
    private int dashSkillCooldown = 0;         // 突进技能冷却
    private final int DASH_MAX_COOLDOWN = 150; // 冷却时间 (比大招短一点，约2.5秒)
    private java.awt.image.BufferedImage dashIconImg; // 突进技能图标

    public WuKong() {
        // 初始化图像帧
        loadSprites();
        // 初始化动作分组
        initializeActionGroups();
        // 初始化背包
        this.inventory = Inventory.getInstance();
    // ...existing code...
    }
    // 加载悟空精灵图并裁剪出各个帧
    private void loadSprites() {
        try {
            // 使用ImageIO加载整张精灵图
            spriteSheet = ImageIO.read(new File("image/wukong/wukong0.png"));
            skillIcon = ImageIO.read(new File("image/skill/fireball.png"));
            
            // 初始化帧列表
            frames = new ArrayList<>();
            skill2Frames = new ArrayList<>(); // 必须加上！
            skill3Frames = new ArrayList<>(); // 必须加上！
            
            // 确保 actionGroups 被初始化
            if (actionGroups == null) {
                actionGroups = new HashMap<>();
            }
            
            // === 这里是你原本的完美参数，绝对不动！ ===
            int firstX = 20;  // 第一帧的X坐标
            int firstY = 30;  // 第一帧的Y坐标
            int frameSpacing = 200; // 帧之间的间距
            int frameWidth = 150;    // 每帧的宽度
            int frameHeight = 150;   // 每帧的高度
            
            // 计算可以提取多少帧
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
            
            // ==========================================================
            // 挑出受击动作 (第14行，一共3张图)
            // ==========================================================
            int hitRowIndex = 12; // 程序里行数从0开始，第14行就是索引13
            int hitFrameCount = 3; // 受击有3张图
            List<Integer> hitFrames = new ArrayList<>();
            
            // 根据你的 maxCols 计算出这3张图在 frames 里的确切位置
            for (int col = 0; col < hitFrameCount; col++) {
                int frameIndex = hitRowIndex * maxCols + col;
                // 防越界保护
                if (frameIndex < frames.size()) {
                    hitFrames.add(frameIndex);
                }
            }
            // 把这3张图的索引存入 "hit" 动作组
            actionGroups.put("hit", hitFrames);
            
            System.out.println("悟空精灵图加载完成，共加载 " + frames.size() + " 帧");
            System.out.println("使用坐标: 第一帧(" + firstX + "," + firstY + ")，间距:" + frameSpacing + 
                             "，帧尺寸:" + frameWidth + "x" + frameHeight);
            System.out.println("成功加载受击动作，包含 " + hitFrames.size() + " 帧！");
            
            // ==========================================================
            // 加载技能 2 和技能 3 的序列帧素材
            // ==========================================================
            System.out.println("开始加载技能序列帧...");
            
            // 加载技能 2 (血转蓝，共 9 张)
            for (int i = 1; i <= 9; i++) {
                java.io.File file = new java.io.File("image/skill/hp2mp_" + i + ".png");
                if (file.exists()) {
                    skill2Frames.add(javax.imageio.ImageIO.read(file));
                } else {
                    System.out.println("警告：找不到素材 " + file.getPath());
                }
            }
            
            // 加载技能 3 (蓝转血，共 8 张)
            for (int i = 1; i <= 8; i++) {
                java.io.File file = new java.io.File("image/skill/mp2hp_" + i + ".png");
                if (file.exists()) {
                    skill3Frames.add(javax.imageio.ImageIO.read(file));
                } else {
                    System.out.println("警告：找不到素材 " + file.getPath());
                }
            }
            
            System.out.println("技能素材加载完毕：技能2(" + skill2Frames.size() + "帧), 技能3(" + skill3Frames.size() + "帧)");

            // 加载右上角技能图标
            java.io.File iconFile = new java.io.File("image/skill/sp_1.png");
            if(iconFile.exists()) spIconImg = javax.imageio.ImageIO.read(iconFile);
            
            java.io.File dashFile = new java.io.File("image/skill/dash_1.png");
            if(dashFile.exists()) dashIconImg = javax.imageio.ImageIO.read(dashFile);

        } catch (IOException e) {
            System.out.println("加载素材失败: " + e.getMessage());
            e.printStackTrace();
            
            // 如果文件不存在，创建一个临时的BufferedImage
            if (frames == null) frames = new ArrayList<>();
            BufferedImage tempImage = new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB);
            frames.add(tempImage);
        }
    }
    
    @Override
    public void keyClick(boolean bl, int key) {
        // 如果死了或者正在挨打僵直中，绝对不响应任何键盘指令！
        if (isDead || isHit) return; 

        long currentTime = System.currentTimeMillis();

        // --- U 和 O 键 (血蓝转换技能开关) ---
        if (key == java.awt.event.KeyEvent.VK_U && bl) { 
            isHpToMpActive = !isHpToMpActive;
            if (isHpToMpActive) isMpToHpActive = false; // 互斥
            System.out.println("技能2状态：" + (isHpToMpActive ? "开启" : "关闭"));
        }
        if (key == java.awt.event.KeyEvent.VK_O && bl) { 
            isMpToHpActive = !isMpToHpActive;
            if (isMpToHpActive) isHpToMpActive = false; // 互斥
            System.out.println("技能3状态：" + (isMpToHpActive ? "开启" : "关闭"));
        }
        
        // --- J 键 (攻击) ---
        if (key == java.awt.event.KeyEvent.VK_J) {
            if (bl) {
                if (!attackLock) {
                    attackLock = true;
                    // 【绑定攻击ID】：每次按下J键，生成一个全新的攻击身份证！
                    currentAttackId = System.currentTimeMillis(); 
                    
                    attackCount++;
                    if (attackCount > 4) attackCount = 1;
                    playAttackByCount(); // 播放对应的攻击动画
                }
            } else {
                if (!isJumping && !attackLock) checkIdleState();
            }
        }
        
        // --- A 键 (向左) ---
        else if (key == java.awt.event.KeyEvent.VK_A) {
            if (bl) {
                aPressed = true;
                isLeft = true;
                // 只有没挥棒、没跳跃时，按A键才会触发走路/奔跑！
                if (!attackLock && !isJumping) {
                    setAction(aClickCount == 1 ? "run" : "walk");
                }
            } else {
                aPressed = false;
                aClickCount++;
                if (aClickCount > 1) aClickCount = 1;
                if (!attackLock && !isJumping) checkIdleState();
            }
        }
        
        // --- D 键 (向右) ---
        else if (key == java.awt.event.KeyEvent.VK_D) {
            if (bl) {
                dPressed = true;
                isLeft = false;
                // 只有没挥棒、没跳跃时，按D键才会触发走路/奔跑！
                if (!attackLock && !isJumping) {
                    setAction(dClickCount == 1 ? "run" : "walk");
                }
            } else {
                dPressed = false;
                dClickCount++;
                if (dClickCount > 1) dClickCount = 1;
                if (!attackLock && !isJumping) checkIdleState();
            }
        }
        
        // --- K 键 (跳跃) ---
        else if (key == java.awt.event.KeyEvent.VK_K) {
            // 只能在地面且没攻击时起跳！
            if (bl && !isJumping && !attackLock) {
                isJumping = true;
                vy = JUMP_FORCE; // 起跳初始爆发力;
                setAction("up");
            }
        }
        
        // --- I 键 (发射火球技能) ---
        else if (key == java.awt.event.KeyEvent.VK_I) {
            if (bl) { 
                if (!isDead && !isHit && !isJumping && this.mp >= 15 && skillCooldown == 0) {
                    this.mp -= 15; 
                    skillCooldown = SKILL_MAX_CD; 
                    
                    attackLock = true;
                    currentAttackId = System.currentTimeMillis(); 
                    setAction("attack2"); 
                    
                    int fireballX = isLeft ? this.getX() - 50 : this.getX() + this.getW();
                    int fireballY = this.getY() + 30; 
                    
                    // 动态伤害公式
                    int skillDamage = (int)(this.attackPower * 1.5) + (this.level * 5);
                    
                    com.tedu.element.Fireball fb = new com.tedu.element.Fireball(fireballX, fireballY, this.isLeft, skillDamage);
                    com.tedu.manager.ElementManager.getManager().addElement(fb, com.tedu.manager.GameElement.PLAY);
                    
                    System.out.println("🔥 释放火球！动态伤害已计算为: " + skillDamage);
                } 
            } else {
                if (!isJumping && !attackLock) checkIdleState();
            }
        }
        
        // --- L 键 (化形大招) ---
        else if (key == java.awt.event.KeyEvent.VK_L) {
            int mpCost = 40; 
            if (bl && specialSkillTimer <= 0 && specialSkillCooldown <= 0 && !isDead && this.mp >= mpCost) { 
                
                this.mp -= mpCost; 
                this.specialSkillTimer = 40; 
                this.specialSkillCooldown = MAX_COOLDOWN; 
                
                int spW = 200; int spH = 200;
                int spX = (this.getX() + this.getW() / 2) - (spW / 2);
                int spY = (this.getY() + this.getH()) - spH;
                
                com.tedu.element.SpecialSpin sp = new com.tedu.element.SpecialSpin(spX, spY, spW, spH, attackPower);
                com.tedu.manager.ElementManager.getManager().addElement(sp, com.tedu.manager.GameElement.PLAY);
                
                System.out.println("🌪️ 大圣化形！大招启动！消耗MP：" + mpCost + "，进入冷却...");
            }
        }
        
        // --- P 键 (龙卷风突进) ---
        else if (key == java.awt.event.KeyEvent.VK_P) {
            int mpCost = 60; 
            
            if (bl && dashSkillTimer <= 0 && specialSkillTimer <= 0 && dashSkillCooldown <= 0 && !isDead && this.mp >= mpCost) {
                
                this.mp -= mpCost; 
                this.dashSkillTimer = 36; 
                this.dashSkillCooldown = DASH_MAX_COOLDOWN; 
                
                int dashW = 200; int dashH = 200;
                int dashX = (this.getX() + this.getW() / 2) - (dashW / 2);
                int dashY = (this.getY() + this.getH()) - dashH;
                
                int skillDamage = (int)(this.attackPower * 1.2); 
                if (skillDamage < 1) skillDamage = 1;
                
                com.tedu.element.TornadoDash dash = new com.tedu.element.TornadoDash(dashX, dashY, dashW, dashH, this.isLeft, skillDamage);
                com.tedu.manager.ElementManager.getManager().addElement(dash, com.tedu.manager.GameElement.PLAY);
                
                System.out.println("💨 龙卷风突进！伤害判定为：" + skillDamage);
            } else if (bl && this.mp < mpCost && dashSkillTimer <= 0) {
                System.out.println("法力不足！无法突进");
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
        // 只有没按方向键，且没被攻击锁定时，才允许切回 idle
        if (!aPressed && !dPressed && !attackLock) {
            if (!"idle".equals(currentAction)) {
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
        
        // ===================================
        // 【保留 jwj】：翻滚动作: 帧25-29
        // ===================================
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
        // 1. 逻辑更新
        updateAnimation();

        // =======================================================
        // 【新增】：大招与突进隐身拦截
        // =======================================================
        if (specialSkillTimer > 0 || dashSkillTimer > 0) {
            drawHUD(g); // 血条还是要画的
            return;     // 不画大圣本体
        }

        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
        int screenX = this.getX() + com.tedu.element.MapObj.bgOffsetX;

        // =======================================================
        // 第一层：【无敌帧闪烁拦截】 (保持在最底层)
        // =======================================================
        if (invincibleTimer > 0 && (invincibleTimer / 5) % 2 == 0) {
            drawHUD(g); 
            return; 
        }

        // =======================================================
        // 第二层：【绘制大圣本体】 (先画人物)
        // =======================================================
        if (!frames.isEmpty() && currentFrame < frames.size()) {
            java.awt.image.BufferedImage currentImage = frames.get(currentFrame);
            
            if (!isLeft) { // 右翻转
                java.awt.Graphics2D g2dFlip = (java.awt.Graphics2D) g.create();  
                g2dFlip.translate(screenX + this.getW(), this.getY());  
                g2dFlip.scale(-1, 1);  
                g2dFlip.drawImage(currentImage, 0, 0, this.getW(), this.getH(), null);
                g2dFlip.dispose();  
            } else { // 左侧正常
                g.drawImage(currentImage, screenX, this.getY(), this.getW(), this.getH(), null); 
            }
        }

        // =======================================================
        // 第三层：【顶层序列帧特效】 (解耦版：每个技能独立调优)
        // =======================================================
        java.awt.Composite oldComp = g2d.getComposite();
        g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.8f));

        // --- 技能 2：血转蓝 (蓝色特效) ---
        if (isHpToMpActive && !skill2Frames.isEmpty()) {
            int s2W = 120;   // 特效宽度
            int s2H = 60;    // 特效高度
            int s2OffX = -5;  // X 轴微调
            int s2OffY = 10; // Y 轴微调

            int drawX2 = (screenX + this.getW() / 2) - (s2W / 2) + s2OffX;
            int drawY2 = (this.getY() + this.getH()) - s2H + s2OffY;

            g2d.drawImage(skill2Frames.get(skill2FrameIndex % skill2Frames.size()), drawX2, drawY2, s2W, s2H, null);
        }

        // --- 技能 3：蓝转血 (红色特效) ---
        if (isMpToHpActive && !skill3Frames.isEmpty()) {
            int s3W = 150;   
            int s3H = 80;    
            int s3OffX = 0;  
            int s3OffY = -5; 

            int drawX3 = (screenX + this.getW() / 2) - (s3W / 2) + s3OffX;
            int drawY3 = (this.getY() + this.getH()) - s3H + s3OffY;

            g2d.drawImage(skill3Frames.get(skill3FrameIndex % skill3Frames.size()), drawX3, drawY3, s3W, s3H, null);
        }

        g2d.setComposite(oldComp); // 还原透明度
        
        // =======================================================
        // 第四层：【最顶层 UI】
        // =======================================================
        drawHUD(g);
    }
    
    // 更新物理坐标与动画帧 (包含受击硬直版)
    private void updateAnimation() {
        // 【新增】：突进冷却时间流逝
        if (dashSkillCooldown > 0) dashSkillCooldown--;

        // =======================================================
        // 【新增】：龙卷风突进状态拦截与强制位移
        // =======================================================
        if (dashSkillTimer > 0) {
            dashSkillTimer--;
            
            // 【身随影动】：大圣本体跟着龙卷风一起往前冲！
            int dashSpeed = 10; 
            
            if (this.isLeft) {
                this.setX(this.getX() - dashSpeed);
            } else {
                this.setX(this.getX() + dashSpeed);
            }
            
            // 防止大圣冲出地图边界
            if (this.getX() < 0) this.setX(0);

            return; // 核心：拦截平时的走路、跳跃和重力，保持霸体冲刺！
        }

        // 【新增】：技能冷却时间自然流逝 (每帧减1)
        if (specialSkillCooldown > 0) {
            specialSkillCooldown--;
        }
        
        // =======================================================
        // 【新增】：大招定身拦截
        // =======================================================
        if (specialSkillTimer > 0) {
            specialSkillTimer--;
            return; // 核心：直接 return，不执行下面的走路、跳跃、重力逻辑
        }

        toggleTimer++;
        if (toggleTimer >= 30) { // 约每 0.5 秒执行一次转换
            if (isHpToMpActive && this.hp > 10) { 
                this.hp -= 10;   // 扣血
                this.mp += 5;    // 回蓝
                if (this.mp > maxMp) this.mp = maxMp;
            }
            
            if (isMpToHpActive && this.mp > 10) {
                this.mp -= 15;   // 扣蓝
                this.hp += 5;   // 回血
                if (this.hp > maxHp) this.hp = maxHp;
            }
            toggleTimer = 0; 
        }

        if (isHpToMpActive || isMpToHpActive) {
            skillAniTimer++;
            if (skillAniTimer >= 3) {
                if (isHpToMpActive && !skill2Frames.isEmpty()) {
                    skill2FrameIndex = (skill2FrameIndex + 1) % skill2Frames.size();
                }
                if (isMpToHpActive && !skill3Frames.isEmpty()) {
                    skill3FrameIndex = (skill3FrameIndex + 1) % skill3Frames.size();
                }
                skillAniTimer = 0;
            }
        }

        // ================== 0. 全局计时器衰减 ==================
        if (invincibleTimer > 0) {
            invincibleTimer--;
            if (invincibleTimer == 0) hitCount = 0; 
        }
        if (skillCooldown > 0) {
            skillCooldown--;
        }
        
        // ================== 1. 物理引擎与状态机 ==================
        if (isDead) {
            deathTimer++;
            return; 
        } 
        else if (isHit) {
            // 受击僵直状态
            hitRecoveryTimer++;
            
            if (hitRecoveryTimer > 30) { 
                isHit = false;
                hitRecoveryTimer = 0;
                attackLock = false; 
                
                // 空中受击落地保护
                if (this.getY() < groundY) {
                    isJumping = true; 
                } else {
                    if (aPressed || dPressed) {
                        setAction((aClickCount == 1 || dClickCount == 1) ? "run" : "walk");
                    } else {
                        setAction("idle");
                    }
                }
            }
        }
        else {
            // 正常状态
            boolean canMoveX = true;
            if (attackLock && !isJumping) {
                canMoveX = false; // 地面攻击锁步
            }

            // X轴移动逻辑
            if (canMoveX && (aPressed || dPressed)) {
                int step = (aClickCount == 1 || dClickCount == 1) ? runStep : walkStep;
                if (isLeft) {
                    this.setX(this.getX() - step);
                } else {
                    this.setX(this.getX() + step);
                }
                
                // 世界边界空气墙
                if (this.getX() < 0) this.setX(0);
                if (this.getX() > 9000 - this.getW()) this.setX(9000 - this.getW());
            }

            // Y轴物理引擎
            if (isJumping) {
                this.setY((int)(this.getY() + vy));
                vy += GRAVITY;

                if (!attackLock) {
                    setAction(vy < 0 ? "up" : "down");
                }

                // 落地判定
                if (this.getY() >= groundY) {
                    this.setY(groundY);
                    isJumping = false;
                    vy = 0;
                    
                    if (!attackLock) {
                        setAction((aPressed || dPressed) ? ((aClickCount == 1 || dClickCount == 1) ? "run" : "walk") : "idle");
                    }
                }
            }
        }

        // ================== 2. 动画帧渲染切换 ==================
        if (animationCounter % FRAME_DELAY == 0) {
            
            // A. 处理不循环的动作 (受击 和 攻击)
            if (isHit || attackLock) {
                if (currentAction.startsWith("hit") || currentAction.startsWith("attack")) {
                    List<Integer> actionFrames = actionGroups.get(currentAction);
                    if (actionFrames != null && !actionFrames.isEmpty()) {
                        currentActionFrameIndex++; 
                        
                        if (currentActionFrameIndex >= actionFrames.size()) {
                            if (isHit) {
                                currentActionFrameIndex = actionFrames.size() - 1; 
                            } else {
                                attackLock = false; 
                                
                                // 粉碎滑步攻击残留
                                if (isJumping) {
                                    setAction(vy < 0 ? "up" : "down");
                                } else {
                                    if (aPressed || dPressed) {
                                        setAction((aClickCount == 1 || dClickCount == 1) ? "run" : "walk");
                                    } else {
                                        setAction("idle");
                                    }
                                }
                            }
                        } else {
                            this.currentFrame = actionFrames.get(currentActionFrameIndex);
                        }
                    }
                }
            } 
            // B. 处理普通的无限循环动作 (走路、奔跑、待机)
            else if (!isJumping) {
                List<Integer> actionFrames = actionGroups.get(currentAction);
                if (actionFrames != null && !actionFrames.isEmpty()) {
                    currentActionFrameIndex = (currentActionFrameIndex + 1) % actionFrames.size();
                    this.currentFrame = actionFrames.get(currentActionFrameIndex);
                }
            } 
            // C. 处理跳跃动作 (固定在第一帧)
            else {
                List<Integer> actionFrames = actionGroups.get(currentAction);
                if (actionFrames != null && !actionFrames.isEmpty()) {
                    this.currentFrame = actionFrames.get(0); 
                }
            }
        }
        animationCounter++;
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
// ==========================================================
    // 【保留组员代码】：道具与背包系统需要的接口
    // ==========================================================
    /**
     * 恢复HP (组员道具系统调用)
     */
    public void recoverHp(int value) {
        this.hp = Math.min(this.hp + value, maxHp);
    }
    
    /**
     * 恢复MP (组员道具系统调用)
     */
    public void recoverMp(int value) {
        this.mp = Math.min(this.mp + value, maxMp);
    }
    
    // HP/MP 相关 getter/setter (组员界面可能用到)
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = Math.min(hp, maxHp); }
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public int getMp() { return mp; }
    public void setMp(int mp) { this.mp = Math.min(mp, maxMp); }
    public int getMaxMp() { return maxMp; }
    public void setMaxMp(int maxMp) { this.maxMp = maxMp; }
    public int getAttackPower() { return attackPower; }

    public void addAttackPower(int delta) {
        this.attackPower = Math.max(0, this.attackPower + delta);
    }

    public void addMaxHp(int delta) {
        this.maxHp = Math.max(1, this.maxHp + delta);
        if (this.hp > this.maxHp) {
            this.hp = this.maxHp;
        } else if (delta > 0) {
            this.hp = Math.min(this.maxHp, this.hp + delta);
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

    // ==========================================================
    // 【保留 JWJ 代码】：核心引擎与战斗系统接口
    // ==========================================================
    // 让武器获取当前的动作状态
    public String getCurrentAction() {
        return currentAction;
    }

    public int getCurrentActionFrameIndex() {
        return this.currentActionFrameIndex;
    }
    
    // 讓武器獲取悟空目前的面向方向
    public boolean getIsLeft() {
        return this.isLeft;
    }

    // 获取经验与升级逻辑 (怪物死亡时调用)
    public void addExp(int amount) {
        this.exp += amount;
        System.out.println("大圣获得经验: " + amount + "，当前经验: " + this.exp + "/" + this.maxExp);
        
        // 使用 while 循环判定升级 (防止吃个大经验包一次性连升好几级)
        while (this.exp >= this.maxExp) {
            levelUp();
        }
    }

    // 内部升级计算
    private void levelUp() {
        this.level++;                // 等级 +1
        this.exp -= this.maxExp;     // 扣除当前等级消耗的经验
        this.maxExp = (int)(this.maxExp * 1.5); // 下一级的经验阈值变大

        // 升级带来的属性成长！
        this.maxHp += 20;            
        this.hp = this.maxHp;        // 升级回满血
        this.maxMp += 10;            
        this.mp = this.maxMp;        // 升级回满蓝
        this.attackPower += 5;       

        System.out.println("✨ 金光一闪！大圣升到了 Lv." + this.level + "！");
        System.out.println("当前最大生命: " + this.maxHp + "，攻击力: " + this.attackPower);
    }
    
    // 给队友的道具预留的加血/加蓝接口 (与 recoverHp 兼容)
    public void healHp(int amount) {
        this.hp += amount;
        if (this.hp > this.maxHp) this.hp = this.maxHp;
        System.out.println("恢复生命: " + amount + "，当前生命: " + this.hp + "/" + this.maxHp);
    }
    
    public void restoreMp(int amount) {
        this.mp += amount;
        if (this.mp > this.maxMp) this.mp = this.maxMp;
        System.out.println("恢复法力: " + amount + "，当前法力: " + this.mp + "/" + this.maxMp);
    }

    private void drawHUD(Graphics g) {
        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;

        // 1. 左上角基础属性面板 (HP, MP, EXP)
        g2d.setColor(new java.awt.Color(30, 30, 30, 200));
        g2d.fillRoundRect(20, 20, 280, 100, 15, 15);
        g2d.setColor(new java.awt.Color(200, 180, 50));
        g2d.drawRoundRect(20, 20, 280, 100, 15, 15);

        g2d.setColor(java.awt.Color.ORANGE);
        g2d.setFont(new java.awt.Font("微软雅黑", java.awt.Font.BOLD, 24));
        g2d.drawString("Lv." + this.level, 35, 65);

        int barX = 110, barY = 35, barW = 170, barH = 14;

        // 【红条】: 生命值 HP
        g2d.setColor(java.awt.Color.DARK_GRAY);
        g2d.fillRect(barX, barY, barW, barH); 
        g2d.setColor(new java.awt.Color(220, 40, 40)); 
        g2d.fillRect(barX, barY, (int)(barW * ((double)hp / maxHp)), barH); 
        g2d.setColor(java.awt.Color.WHITE);
        g2d.drawRect(barX, barY, barW, barH); 
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
        g2d.drawString("HP", barX - 25, barY + 12);

        // 【蓝条】: 法力值 MP
        barY += 24; 
        g2d.setColor(java.awt.Color.DARK_GRAY);
        g2d.fillRect(barX, barY, barW, barH); 
        g2d.setColor(new java.awt.Color(40, 120, 220)); 
        g2d.fillRect(barX, barY, (int)(barW * ((double)mp / maxMp)), barH); 
        g2d.setColor(java.awt.Color.WHITE);
        g2d.drawRect(barX, barY, barW, barH); 
        g2d.drawString("MP", barX - 25, barY + 12);

        // 【绿条】: 经验值 EXP
        barY += 24;
        g2d.setColor(java.awt.Color.DARK_GRAY);
        g2d.fillRect(barX, barY, barW, barH); 
        g2d.setColor(new java.awt.Color(100, 200, 50)); 
        g2d.fillRect(barX, barY, (int)(barW * ((double)exp / maxExp)), barH); 
        g2d.setColor(java.awt.Color.WHITE);
        g2d.drawRect(barX, barY, barW, barH);
        g2d.drawString("EXP", barX - 28, barY + 12);

        // 2. 左上角技能指示灯 (U 和 O)
        g2d.setFont(new java.awt.Font("微软雅黑", java.awt.Font.BOLD, 14));

        g2d.setColor(isHpToMpActive ? new java.awt.Color(0, 150, 255) : java.awt.Color.DARK_GRAY);
        g2d.fillRoundRect(20, 80, 40, 25, 5, 5); 
        g2d.setColor(java.awt.Color.WHITE);
        g2d.drawString("U", 33, 98);

        g2d.setColor(isMpToHpActive ? new java.awt.Color(255, 50, 50) : java.awt.Color.DARK_GRAY);
        g2d.fillRoundRect(70, 80, 40, 25, 5, 5);
        g2d.setColor(java.awt.Color.WHITE);
        g2d.drawString("O", 83, 98);
        
        g2d.setColor(java.awt.Color.LIGHT_GRAY);
        g2d.drawRoundRect(20, 80, 40, 25, 5, 5);
        g2d.drawRoundRect(70, 80, 40, 25, 5, 5);

        // 3. 右上角技能栏 (I 键火球术 & L 键大招)
        int skillBoxY = 20;
        int boxSize = 60; 

        // 技能 A：火球术 (I键)
        int skill1X = 780; 
        g2d.setColor(new java.awt.Color(0, 0, 0, 150));
        g2d.fillRoundRect(skill1X, skillBoxY, boxSize, boxSize, 10, 10);
        g2d.setColor(java.awt.Color.WHITE);
        g2d.drawRoundRect(skill1X, skillBoxY, boxSize, boxSize, 10, 10);

        if (skillIcon != null) {
            g2d.drawImage(skillIcon, skill1X + 5, skillBoxY + 5, boxSize - 10, boxSize - 10, null);
        }

        g2d.setColor(java.awt.Color.WHITE);
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        g2d.drawString("I", skill1X + 8, skillBoxY + 20);

        g2d.setColor(java.awt.Color.CYAN);
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        g2d.drawString("15MP", skill1X + 22, skillBoxY + 55);

        if (this.mp < 15) {
            g2d.setColor(new java.awt.Color(255, 0, 0, 120));
            g2d.fillRoundRect(skill1X, skillBoxY, boxSize, boxSize, 10, 10);
        } else if (skillCooldown > 0) {
            g2d.setColor(new java.awt.Color(50, 50, 50, 180));
            int maskHeight = (int) (boxSize * ((double) skillCooldown / SKILL_MAX_CD));
            g2d.fillRoundRect(skill1X, skillBoxY + (boxSize - maskHeight), boxSize, maskHeight, 10, 10);
            
            g2d.setColor(java.awt.Color.WHITE);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
            String cdText = String.format("%.1f", skillCooldown / 100.0);
            g2d.drawString(cdText, skill1X + 18, skillBoxY + 38);
        }

        // 技能 B：化形大招 (L键)
        int skill2X = 710; 
        g2d.setColor(new java.awt.Color(0, 0, 0, 150));
        g2d.fillRoundRect(skill2X, skillBoxY, boxSize, boxSize, 10, 10);
        
        if (spIconImg != null) {
            g2d.drawImage(spIconImg, skill2X + 5, skillBoxY + 5, boxSize - 10, boxSize - 10, null);
        } else {
            g2d.setColor(java.awt.Color.ORANGE);
            g2d.fillRect(skill2X + 5, skillBoxY + 5, boxSize - 10, boxSize - 10);
            g2d.setColor(java.awt.Color.BLACK);
            g2d.setFont(new java.awt.Font("微软雅黑", java.awt.Font.BOLD, 12));
            g2d.drawString("大招", skill2X + 18, skillBoxY + 35);
        }

        g2d.setColor(java.awt.Color.WHITE);
        g2d.drawRoundRect(skill2X, skillBoxY, boxSize, boxSize, 10, 10);
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        g2d.drawString("L", skill2X + 8, skillBoxY + 20);

        g2d.setColor(java.awt.Color.CYAN);
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        g2d.drawString("40MP", skill2X + 22, skillBoxY + 55);

        if (this.mp < 40) { 
            g2d.setColor(new java.awt.Color(255, 0, 0, 120));
            g2d.fillRoundRect(skill2X, skillBoxY, boxSize, boxSize, 10, 10);
        } else if (specialSkillCooldown > 0) {
            g2d.setColor(new java.awt.Color(0, 0, 0, 180)); 
            int maskHeight = (int) (boxSize * ((double) specialSkillCooldown / MAX_COOLDOWN));
            g2d.fillRoundRect(skill2X, skillBoxY + (boxSize - maskHeight), boxSize, maskHeight, 10, 10);

            g2d.setColor(java.awt.Color.WHITE);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 22));
            int seconds = (specialSkillCooldown / 60) + 1; 
            g2d.drawString(seconds + "s", skill2X + 18, skillBoxY + 38);
            
            g2d.setColor(java.awt.Color.GRAY);
            g2d.drawRoundRect(skill2X, skillBoxY, boxSize, boxSize, 10, 10);
        } else {
            g2d.setColor(java.awt.Color.YELLOW);
            g2d.drawRoundRect(skill2X, skillBoxY, boxSize, boxSize, 10, 10);
            g2d.drawRoundRect(skill2X - 1, skillBoxY - 1, boxSize + 2, boxSize + 2, 10, 10);
        }

        // 技能 C：龙卷风突进 (P键)
        int skill3X = 640; 
        g2d.setColor(new java.awt.Color(0, 0, 0, 150));
        g2d.fillRoundRect(skill3X, skillBoxY, boxSize, boxSize, 10, 10);
        
        if (dashIconImg != null) {
            g2d.drawImage(dashIconImg, skill3X + 5, skillBoxY + 5, boxSize - 10, boxSize - 10, null);
        } else {
            g2d.setColor(java.awt.Color.CYAN);
            g2d.fillRect(skill3X + 5, skillBoxY + 5, boxSize - 10, boxSize - 10);
            g2d.setColor(java.awt.Color.BLACK);
            g2d.setFont(new java.awt.Font("微软雅黑", java.awt.Font.BOLD, 12));
            g2d.drawString("突进", skill3X + 18, skillBoxY + 35);
        }

        g2d.setColor(java.awt.Color.WHITE);
        g2d.drawRoundRect(skill3X, skillBoxY, boxSize, boxSize, 10, 10);
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        g2d.drawString("P", skill3X + 8, skillBoxY + 20);

        g2d.setColor(java.awt.Color.CYAN);
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        g2d.drawString("50MP", skill3X + 22, skillBoxY + 55);

        if (this.mp < 25) { // 突进的蓝耗判定是 25MP，上面 UI 显示写错了，这里逻辑是 < 25
            g2d.setColor(new java.awt.Color(255, 0, 0, 120));
            g2d.fillRoundRect(skill3X, skillBoxY, boxSize, boxSize, 10, 10);
        } else if (dashSkillCooldown > 0) {
            g2d.setColor(new java.awt.Color(0, 0, 0, 180)); 
            int maskHeight = (int) (boxSize * ((double) dashSkillCooldown / DASH_MAX_COOLDOWN));
            g2d.fillRoundRect(skill3X, skillBoxY + (boxSize - maskHeight), boxSize, maskHeight, 10, 10);

            g2d.setColor(java.awt.Color.WHITE);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 22));
            int seconds = (dashSkillCooldown / 60) + 1; 
            g2d.drawString(seconds + "s", skill3X + 18, skillBoxY + 38);
            
            g2d.setColor(java.awt.Color.GRAY);
            g2d.drawRoundRect(skill3X, skillBoxY, boxSize, boxSize, 10, 10);
        } else {
            g2d.setColor(java.awt.Color.YELLOW);
            g2d.drawRoundRect(skill3X, skillBoxY, boxSize, boxSize, 10, 10);
            g2d.drawRoundRect(skill3X - 1, skillBoxY - 1, boxSize + 2, boxSize + 2, 10, 10); 
        }
    }

    // 开局读档，把存档里的属性注入给大圣
    public void loadFromSave(com.tedu.login.PlayerSave save) {
        if (save == null) return;
        this.level = save.getLevel();
        this.hp = save.getHp();
        this.maxHp = save.getMaxHp();
        this.mp = save.getMp();
        this.maxMp = save.getMaxMp();
        this.exp = save.getExp();
        this.maxExp = save.getMaxExp();
        this.attackPower = save.getAttackPower();
        System.out.println("大圣已读取前世记忆！当前等级: " + this.level + "，攻击力: " + this.attackPower);
    }

    // 准备存档，把大圣当前的属性回写给存档对象
    public void updateToSave(com.tedu.login.PlayerSave save) {
        if (save == null) return;
        save.setLevel(this.level);
        save.setHp(this.hp);
        save.setMaxHp(this.maxHp);
        save.setMp(this.mp);
        save.setMaxMp(this.maxMp);
        save.setExp(this.exp);
        save.setMaxExp(this.maxExp);
        save.setAttackPower(this.attackPower);
    }

    // 核心新增：大圣挨打扣血接口
    public void takeDamage(int damage) {
        if (isDead || invincibleTimer > 0) return; 

        this.hp -= damage;
        com.tedu.element.DamageText dt = new com.tedu.element.DamageText(
            this.getX() + 40, this.getY() - 30, damage, new java.awt.Color(160, 32, 240)
        );
        com.tedu.manager.ElementManager.getManager().addElement(dt, com.tedu.manager.GameElement.PLAY);
        hitCount++; 
        System.out.println("大圣受到攻击！当前血量: " + this.hp);

        if (this.hp < 0) this.hp = 0;

        // 强制断开所有键盘指令和动作，防止滑步死锁
        this.aPressed = false;               
        this.dPressed = false;               
        this.aClickCount = 0;                
        this.dClickCount = 0;                
        this.attackLock = false;             
        this.isJumping = false;              
        this.currentActionFrameIndex = 0;    
        
        // 触发受击状态
        isHit = true;
        hitRecoveryTimer = 0; 
        setAction("hit");     
        
        if (this.hp <= 0) {
            this.isDead = true;
            System.out.println("💀 大圣倒下了！");
        } else {
            // 无敌时间超级加倍
            if (hitCount >= 3) {
                invincibleTimer = 150; 
                hitCount = 0;
            } else {
                invincibleTimer = 60;  
            }
        }
    }

    // 重置大圣的状态为完美状态
    public void resetStatus() {
        this.hp = this.maxHp;
        this.mp = this.maxMp;
        this.isDead = false;
        this.isHit = false;
        this.attackLock = false;
        this.isJumping = false;
        this.invincibleTimer = 0;
        this.hitRecoveryTimer = 0;
        this.deathTimer = 0;
        this.vy = 0; 
        this.currentAction = "idle";
        this.currentActionFrameIndex = 0;
        System.out.println("💖 大圣状态已重置：满血满蓝！");
    }

}
