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

import com.tedu.element.map.MapBase;

public class Weapon extends ElementObj {
    // 武器角色的圖像幀列表
    private List<BufferedImage> frames;
    private BufferedImage spriteSheet; // 整張精靈圖
    
    // 法杖狀態的圖像幀列表
    private List<BufferedImage> staffFrames;
    private BufferedImage staffSpriteSheet; // 法杖精靈圖
    private boolean hasStaffEquipped = false; // 是否裝備了法杖（武器贴图已包含在角色精灵图中）
    
    // 動作分組
    private Map<String, List<Integer>> actionGroups;
    
    // 當前繪製狀態
    private int currentFrame = 0;
    private String currentAction = "idle"; 
    private boolean isLeft = false; 

    public Weapon() {
        // 初始化圖像幀 (保留你完美的切圖邏輯)
        loadSprites();
        // 初始化動作分組
        initializeActionGroups();
    }
    
    // ==========================================================
    // 1. 核心精靈圖加載 (完全保留你的原版代碼，一字未改)
    // ==========================================================
    private void loadSprites() {
        try {
            spriteSheet = ImageIO.read(new File("image/weapon/weapon0.png"));
            frames = new ArrayList<>();
            
            // 加载法杖精灵图
            staffSpriteSheet = ImageIO.read(new File("image/weapon/staff_red.png"));
            staffFrames = new ArrayList<>();
            
            int firstX = 20;  
            int firstY = 30;  
            int frameSpacing = 200; 
            int frameWidth = 150;    
            int frameHeight = 150;   
            
            int maxCols = (spriteSheet.getWidth() - firstX) / frameSpacing;
            int maxRows = (spriteSheet.getHeight() - firstY) / frameSpacing;
            
            for (int row = 0; row < maxRows; row++) {
                for (int col = 0; col < maxCols; col++) {
                    int x = firstX + col * frameSpacing;
                    int y = firstY + row * frameSpacing;
                    
                    if (x + frameWidth <= spriteSheet.getWidth() && 
                        y + frameHeight <= spriteSheet.getHeight()) {
                        
                        BufferedImage frame = spriteSheet.getSubimage(x, y, frameWidth, frameHeight);
                        frames.add(frame);
                    }
                }
            }
            System.out.println("武器精靈圖加載完成，共加載 " + frames.size() + " 幀");
            
            // === 裁剪法杖状态的帧（使用相同参数） ===
            int staffMaxCols = (staffSpriteSheet.getWidth() - firstX) / frameSpacing;
            int staffMaxRows = (staffSpriteSheet.getHeight() - firstY) / frameSpacing;
            
            for (int row = 0; row < staffMaxRows; row++) {
                for (int col = 0; col < staffMaxCols; col++) {
                    int x = firstX + col * frameSpacing;
                    int y = firstY + row * frameSpacing;
                    
                    if (x + frameWidth <= staffSpriteSheet.getWidth() && 
                        y + frameHeight <= staffSpriteSheet.getHeight()) {
                        
                        BufferedImage frame = staffSpriteSheet.getSubimage(x, y, frameWidth, frameHeight);
                        staffFrames.add(frame);
                    }
                }
            }
            System.out.println("法杖精靈圖加載完成，共加載 " + staffFrames.size() + " 幀");
        } catch (IOException e) {
            System.out.println("加載武器精靈圖失敗: " + e.getMessage());
            frames = new ArrayList<>();
            frames.add(new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB));
        }
    }
    
    // ==========================================================
    // 2. 動作幀字典 (完全保留你的原版代碼)
    // ==========================================================
    private void initializeActionGroups() {
        actionGroups = new HashMap<>();
        
        List<Integer> idleFrames = new ArrayList<>();
        for (int i = 0; i <= 4; i++) idleFrames.add(i);
        actionGroups.put("idle", idleFrames);
        
        List<Integer> walkFrames = new ArrayList<>();
        for (int i = 10; i <= 13; i++) walkFrames.add(i);
        actionGroups.put("walk", walkFrames);
        
        List<Integer> runFrames = new ArrayList<>();
        for (int i = 15; i <= 18; i++) runFrames.add(i);
        actionGroups.put("run", runFrames);
        
        List<Integer> upFrames = new ArrayList<>();
        upFrames.add(20);
        actionGroups.put("up", upFrames);
        
        List<Integer> downFrames = new ArrayList<>();
        downFrames.add(21);
        actionGroups.put("down", downFrames);
        
        List<Integer> rollFrames = new ArrayList<>();
        for (int i = 25; i <= 29; i++) rollFrames.add(i);
        actionGroups.put("roll", rollFrames);
        
        List<Integer> attack1Frames = new ArrayList<>();
        for (int i = 30; i <= 34; i++) attack1Frames.add(i);
        actionGroups.put("attack1", attack1Frames);
        
        List<Integer> attack2Frames = new ArrayList<>();
        for (int i = 35; i <= 39; i++) attack2Frames.add(i);
        actionGroups.put("attack2", attack2Frames);
        
        List<Integer> attack3Frames = new ArrayList<>();
        for (int i = 40; i <= 44; i++) attack3Frames.add(i);
        actionGroups.put("attack3", attack3Frames);
        
        List<Integer> attack3bFrames = new ArrayList<>();
        for (int i = 45; i <= 47; i++) attack3bFrames.add(i);
        actionGroups.put("attack4", attack3bFrames);
        
        List<Integer> hitFrames = new ArrayList<>();
        hitFrames.add(62);
        actionGroups.put("hit", hitFrames);
    }

    // ==========================================================
    // 3. 【核心變革】：主從同步渲染 (武器變成大聖的影子)
    // ==========================================================
    @Override
    public void showElement(Graphics g) {
        // A. 尋找主人 (大聖)
        try {
            java.util.List<ElementObj> players = com.tedu.manager.ElementManager.getManager().getElementsByKey(com.tedu.manager.GameElement.PLAY);
            for (ElementObj obj : players) {
                if (obj instanceof WuKong) {
                    WuKong wk = (WuKong) obj;
                    
                    // B. 強制拷貝大聖的所有狀態！絕不自己計算！
                    this.setX(wk.getX());
                    this.setY(wk.getY());
                    this.isLeft = wk.getIsLeft(); // 確保 WuKong 有這個 Getter
                    this.currentAction = wk.getCurrentAction();
                    
                    // C. 獲取大聖當前動作播到了第幾幀
                    int wkFrameIndex = wk.getCurrentActionFrameIndex();
                    
                    // D. 在武器的動作字典裡，找出對應的那張圖
                    List<Integer> actionFrames = actionGroups.get(this.currentAction);
                    if (actionFrames != null && !actionFrames.isEmpty()) {
                        // 防越界保護（萬一大聖的 hit 有3幀，武器的 hit 只有1幀，用取模確保安全）
                        int safeIndex = wkFrameIndex % actionFrames.size();
                        this.currentFrame = actionFrames.get(safeIndex);
                    }
                    break; // 找到大聖就退出循環
                }
            }
        } catch (Exception e) {}

        // E. 最終渲染 (帶攝影機偏移和鏡像翻轉)
        List<BufferedImage> activeFrames = hasStaffEquipped ? staffFrames : frames;
        if (activeFrames != null && !activeFrames.isEmpty() && currentFrame < activeFrames.size()) {
            BufferedImage currentImage = activeFrames.get(currentFrame);
            int screenX = this.getX() + MapBase.getBgOffsetX();
            
            if (!isLeft) {  // 面向右側 (水平翻轉)
                Graphics2D g2d = (Graphics2D) g.create();  
                g2d.translate(screenX + this.getW(), this.getY());  
                g2d.scale(-1, 1);  
                g2d.drawImage(currentImage, 0, 0, this.getW(), this.getH(), null);
                g2d.dispose();  
            } else {        // 面向左側 (正常繪製)
                g.drawImage(currentImage, screenX, this.getY(), this.getW(), this.getH(), null); 
            }
        }
    }

    // ==========================================================
    // 4. 其他基礎方法
    // ==========================================================
    @Override
    public void keyClick(boolean bl, int key) {
        // 【已廢棄】：武器不再擁有大腦，不需要監聽鍵盤
    }

    @Override
    public ElementObj createElement(String str) {
        String[] params = str.split(",");
        if (params.length >= 2) {
            this.setX(Integer.parseInt(params[0]));
            this.setY(Integer.parseInt(params[1]));
            if (params.length >= 4) {
                this.setW(Integer.parseInt(params[2]));
                this.setH(Integer.parseInt(params[3]));
            } else {
                this.setW(150);
                this.setH(150);
            }
        }
        return this;
    }

    // 告訴外界（裁判 GameThread），武器現在是不是正在砍人
    public boolean isAttacking() {
        return this.currentAction != null && this.currentAction.startsWith("attack");
    }
    
    public void setStaffEquipped(boolean equipped) {
        this.hasStaffEquipped = equipped;
    }
    
    public boolean hasStaffEquipped() {
        return hasStaffEquipped;
    }
}