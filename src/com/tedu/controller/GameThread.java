package com.tedu.controller;

import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;

import com.tedu.element.ElementObj;
import com.tedu.element.WuKong;
import com.tedu.element.Inventory.BloodPotion;
import com.tedu.element.Inventory.Inventory;
import com.tedu.element.Inventory.Item;
import com.tedu.element.Inventory.ManaPotion;
import com.tedu.element.enemy.BaseEnemy;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;

/**
 * @说明 游戏的主线程，用于控制游戏加载，游戏关卡，游戏运行时自动化
 * 游戏判定；游戏地图切换 资源释放和重新读取。。。
 */
public class GameThread extends Thread {
    private static final Random DROP_RANDOM = new Random();
    private ElementManager em;
    public static boolean isGameRunning = true;
    private boolean isPortalSpawned = false;

    private int levelIndex = 1; // 默认关卡为1
    
    public GameThread() {
        em = ElementManager.getManager();
    }
    
    public GameThread(int levelIndex) {
        this();
        this.levelIndex = levelIndex;
    }

    /**
     * 【重构点1】：run 方法的逻辑控制
     */
    @Override
    public void run() {
        // 1. 关卡开始前，必须先进行初始化（洗脑清空）
        initGame(); 
        
        // 2. 加载当前关卡的资源（此时 ElementManager 是干净的）
        gameLoad(); 
        
        // 3. 进入游戏主循环
        gameRun();  
        
        // 4. 循环跳出意味着关卡结束（通关或死亡）
        gameOver(); 
    }

    public void initGame() {
        // A. 强制清空上一局残留
        em.clearAll(); 
        Inventory.getInstance().clear();
        
        // B. 重置本线程内的逻辑开关
        this.isPortalSpawned = false; 
        isGameRunning = true; 
        
        System.out.println("🧹 内存已清空，准备加载新关卡...");
    }

    private void gameLoad() {
        GameLoad.loadImg();  
        // 根据关卡参数加载不同的地图
        GameLoad.loadMap(levelIndex);  // 使用关卡参数加载对应地图
        GameLoad.loadPlay(levelIndex); 
        System.out.println("✅ 资源加载完毕");
    }

    private void cleanUpDeadElements(List<ElementObj> plays) {
        if (plays == null) return;
        for (int i = plays.size() - 1; i >= 0; i--) {
            ElementObj obj = plays.get(i);
            if (obj instanceof com.tedu.element.skill.Fireball) {
                if (!((com.tedu.element.skill.Fireball) obj).isActive) plays.remove(i); 
            }
            else if (obj instanceof com.tedu.element.DamageText) {
                if (((com.tedu.element.DamageText) obj).isDead()) plays.remove(i);
            }
        }
    }

    /**
     * 【融合点】：游戏主循环
     */
    private void gameRun() {
        long gameTime = 0L;
        while (isGameRunning) {
            Map<GameElement, List<ElementObj>> all = em.getGameElements();
            List<ElementObj> plays = em.getElementsByKey(GameElement.PLAY);
            List<ElementObj> enemys = em.getElementsByKey(GameElement.ENEMY);

            cleanUpDeadElements(plays);
            moveAndUpdate(all, gameTime);
            
            // 我们强大的战斗检测
            checkCombat(plays, enemys);
            
            // 【保留组员新增】：拾取检测 (让大圣能捡东西)
            pickupUpdate();

            gameTime++;
            try {
                sleep(10); 
            } catch (InterruptedException e) {
                break; // 被中断时跳出
            }
        }
    }

    /**
     * 核心战斗检测：AABB碰撞算法 (保留 jwj 版本)
     */
    private void checkCombat(List<ElementObj> plays, List<ElementObj> enemys) {
        if (plays == null) return;

        com.tedu.element.Weapon weapon = null;
        com.tedu.element.WuKong wukong = null;

        for (int i = 0; i < plays.size(); i++) {
            ElementObj obj = plays.get(i);
            if (obj instanceof com.tedu.element.Weapon) weapon = (com.tedu.element.Weapon) obj;
            if (obj instanceof com.tedu.element.WuKong) wukong = (com.tedu.element.WuKong) obj;
        }

        if (wukong == null) return; 

        // --- 大圣阵亡判定优先 ---
        if (wukong.isDead) {
            if (wukong.deathTimer > 100) { 
                System.out.println("💀 游戏结束，返回主菜单...");
                backToMainMenu(false); 
                return; 
            }
        }
        
        // --- 生成碰撞箱 ---
        java.awt.Rectangle weaponBox = null;
        if (weapon != null) {
            weaponBox = new java.awt.Rectangle(
                weapon.getX() + 20, weapon.getY() + 20, weapon.getW() - 40, weapon.getH() - 40
            );
        }
        
        java.awt.Rectangle wukongBox = new java.awt.Rectangle(
            wukong.getX() + 40, wukong.getY() + 20, wukong.getW() - 80, wukong.getH() - 20
        );

        // ================== A. 怪物相关判定 ==================
        if (enemys != null && !enemys.isEmpty()) {
            for (int i = 0; i < enemys.size(); i++) {
                ElementObj obj = enemys.get(i);
                if (obj instanceof com.tedu.element.enemy.BaseEnemy) {
                    com.tedu.element.enemy.BaseEnemy enemy = (com.tedu.element.enemy.BaseEnemy) obj;
                    
                    if (enemy.hp <= 0) {
                        handleEnemyDrop(enemy);
                        continue;
                    }

                    java.awt.Rectangle enemyBox = new java.awt.Rectangle(
                        enemy.getX() + 30, enemy.getY() + 30, enemy.getW() - 60, enemy.getH() - 60
                    );

                    // 逻辑 1: 金箍棒打怪 
                    if (weaponBox != null && weaponBox.intersects(enemyBox) && weapon.isAttacking() && enemy.invincibleTimer == 0) {
                        if (enemy.lastHitAttackId != wukong.currentAttackId) {
                            enemy.takeDamage(wukong.attackPower); 
                            enemy.lastHitAttackId = wukong.currentAttackId; 
                        }
                    }

                    // 逻辑 2: 怪物打大圣
                    if (enemyBox.intersects(wukongBox) && "attack".equals(enemy.getCurrentAction()) && wukong.invincibleTimer == 0) {
                        wukong.takeDamage(10); 
                    }

                    // 火球判定
                    for (int j = 0; j < plays.size(); j++) {
                        ElementObj playObj = plays.get(j);
                        if (playObj instanceof com.tedu.element.skill.Fireball) {
                            com.tedu.element.skill.Fireball fireball = (com.tedu.element.skill.Fireball) playObj;
                            if (fireball.isActive && fireball.getHitBox().intersects(enemyBox)) {
                                enemy.takeDamage(fireball.damage);
                                fireball.isActive = false; 
                                System.out.println("🔥 火球命中怪物！造成 " + fireball.damage + " 点伤害！");
                            }
                        }
                    }

                    handleEnemyDrop(enemy);
                }
            }
        }

        // ================== C. 动态传送门降临 ==================
        if (enemys != null && enemys.isEmpty() && !isPortalSpawned && !wukong.isDead) {
            System.out.println("✨ 怪物已全军覆没！传送门开启！");
            ElementObj portal = new com.tedu.element.Portal();
            int portalX = wukong.getX() + 300;
            if (portalX > 4600) portalX = 4600; 
            
            portal.createElement(portalX + ",350,100,150");
            com.tedu.manager.ElementManager.getManager().addElement(portal, com.tedu.manager.GameElement.PLAY);
            isPortalSpawned = true; 
        }

        // ================== B. 传送门通关判定 ==================
        for (int i = 0; i < plays.size(); i++) {
            ElementObj obj = plays.get(i);
            if (obj instanceof com.tedu.element.Portal) {
                com.tedu.element.Portal portal = (com.tedu.element.Portal) obj;
                java.awt.Rectangle portalBox = new java.awt.Rectangle(
                    portal.getX(), portal.getY(), portal.getW(), portal.getH()
                );
                
                if (wukongBox.intersects(portalBox)) {
                    System.out.println("🎉 大圣抵达终点！准备结算数据...");
                    
                    // 【防报错临时注释】：组员的 PlayerSave 如果没写完，这里会报错。暂时注释，不影响战斗。
                    
                    if (com.tedu.manager.GameLoad.currentSave != null) {
                        wukong.updateToSave(com.tedu.manager.GameLoad.currentSave);
                        com.tedu.login.DataManager dm = new com.tedu.login.DataManager();
                        dm.savePlayerSave(com.tedu.manager.GameLoad.currentSave.getUsername(), com.tedu.manager.GameLoad.currentSave);
                    }
                    
                    
                    backToMainMenu(true); 
                    return; 
                }
            }
        }
    }

    public void moveAndUpdate(Map<GameElement, List<ElementObj>> all, long gameTime) {
        for (GameElement ge : GameElement.values()) {
            List<ElementObj> list = all.get(ge);
            for (int i = list.size() - 1; i >= 0; i--) {
                ElementObj obj = list.get(i);
                if (!obj.isLive()) {
                    obj.die();
                    list.remove(i);
                    continue;
                }
                obj.model(gameTime);
            }
        }
    }

    private void backToMainMenu(boolean isWin) {
        isGameRunning = false; 
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            for (java.awt.Window window : java.awt.Window.getWindows()) {
                if (window instanceof javax.swing.JFrame) {
                    window.dispose(); 
                }
            }
            
            String msg = isWin ? "恭喜通关！大圣归来！" : "胜败乃兵家常事，请重新来过！";
            javax.swing.JOptionPane.showMessageDialog(null, msg);

            com.tedu.show.GameJFrame gj = new com.tedu.show.GameJFrame();
            com.tedu.show.MainEntranceJPanel menuPanel = new com.tedu.show.MainEntranceJPanel();
            
            // 【防报错临时注释】：等组员把存档系统传上来后，再解开这里的注释
            
            menuPanel.setPlayerSave(com.tedu.manager.GameLoad.currentSave); 
            
            
            com.tedu.show.GameMainJPanel gameMainPanel = new com.tedu.show.GameMainJPanel();
            menuPanel.setGameMainPanel(gameMainPanel);
            
            gj.setjPanel(menuPanel);
            gj.add(menuPanel); 
            gj.setVisible(true); 
        });
    }
    
    private void gameOver() {
        System.out.println("🏁 场景线程结束");
    }

    /**
     * 【保留组员新增】：拾取检测
     */
    private void pickupUpdate() {
        List<ElementObj> players = em.getElementsByKey(GameElement.PLAY);
        if (players == null || players.isEmpty()) return;
        
        com.tedu.element.Inventory.PickupDetector detector = new com.tedu.element.Inventory.PickupDetector();
        com.tedu.element.ElementObj playerObj = players.get(0);
        if (playerObj instanceof com.tedu.element.WuKong) {
            detector.detectAndPickup((com.tedu.element.WuKong) playerObj);
        }
    }

    private void handleEnemyDrop(BaseEnemy enemy) {
        if (enemy == null || enemy.hp > 0 || enemy.isDropGenerated()) {
            return;
        }

        Item drop = createRandomPotion(enemy);
        em.addElement(drop, GameElement.ITEM);
        enemy.markDropGenerated();
        System.out.println("怪物掉落: " + drop.getClass().getSimpleName());
    }

    private Item createRandomPotion(BaseEnemy enemy) {
        int dropX = enemy.getX() + enemy.getW() / 2 - 15;
        int dropY = enemy.getY() + enemy.getH() - 40;
        return DROP_RANDOM.nextBoolean() ? new BloodPotion(dropX, dropY) : new ManaPotion(dropX, dropY);
    }
}