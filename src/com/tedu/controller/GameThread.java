// package com.tedu.controller;

// import java.util.List;
// import java.util.Map;
// import com.tedu.element.ElementObj;
// import com.tedu.element.WuKong;
// import com.tedu.element.Weapon;
// import com.tedu.element.enemy2;
// import com.tedu.manager.ElementManager;
// import com.tedu.manager.GameElement;
// import com.tedu.manager.GameLoad;

// /**
//  * 游戏主线程：控制加载、运行、碰撞判定与逻辑更新
//  */
// public class GameThread extends Thread {
//     private ElementManager em;
//     public static boolean isGameRunning = true;
//     private boolean isPortalSpawned = false;


//     public GameThread() {
//         em = ElementManager.getManager();
//     }

//     @Override
//     public void run() {
//         while (true) {
//             gameLoad(); // 加载资源
//             gameRun();  // 运行游戏
//             gameOver(); // 场景结束
//             try {
//                 sleep(50);
//             } catch (InterruptedException e) {
//                 e.printStackTrace();
//             }
//         }
//     }

//     /**
//      * 游戏加载：初始化地图、大圣、武器和怪物
//      */
//     private void gameLoad() {
//         GameLoad.loadImg();  // 加载图片缓存
//         GameLoad.loadMap();  // 加载卷轴地图
//         GameLoad.loadPlay(); // 加载玩家(大圣、武器)和初识怪物
//         isGameRunning = true;
//     }

//     public void initGame() {
//         // 1. 呼叫管理员，强制清空上一局的所有内存残留！
//         com.tedu.manager.ElementManager.getManager().clearAll();
        
//         // 2. 把所有控制关卡流程的开关，全部掰回初始状态！
//         this.isPortalSpawned = false; 
//         this.isGameRunning = true; 
        
//         // 3. 【关键】：在这里重新把地图、大圣、怪物加载进来！
//         // 你原本游戏刚启动时，是怎么生成大圣和怪物的？
//         // 把那些代码（或者调用的方法）搬到这里来！比如：
//         // loadMap();
//         // loadPlayer();
//         // loadEnemies();
        
//         System.out.println("✨ 游戏状态已重置，全新关卡加载完毕！");
//     }

//     private void cleanUpDeadElements(List<ElementObj> plays) {
//         if (plays == null) return;
        
//         // 倒着遍历，安全删除集合里的元素
//         for (int i = plays.size() - 1; i >= 0; i--) {
//             ElementObj obj = plays.get(i);
            
//             // 1. 清理撞毁或飞出屏幕的火球
//             if (obj instanceof com.tedu.element.Fireball) {
//                 if (!((com.tedu.element.Fireball) obj).isActive) {
//                     plays.remove(i); 
//                 }
//             }
//             // 2. 清理已经彻底透明消失的伤害飘字
//             else if (obj instanceof com.tedu.element.DamageText) {
//                 if (((com.tedu.element.DamageText) obj).isDead()) {
//                     plays.remove(i);
//                 }
//             }
//             // 以后如果有吃完的血瓶/蓝瓶，也可以继续加在这里...
//         }
//     }

//     /**
//      * 游戏运行：每10ms刷新一次逻辑
//      */
//     private void gameRun() {
//         long gameTime = 0L;
        
//         while (isGameRunning) {
//             // 1. 获取全场所有的游戏元素
//             Map<GameElement, List<ElementObj>> all = em.getGameElements();
//             List<ElementObj> plays = em.getElementsByKey(GameElement.PLAY);
//             List<ElementObj> enemys = em.getElementsByKey(GameElement.ENEMY);

//             // 2. 打扫战场 (清理上一帧死掉的火球、飘字等内存垃圾)
//             cleanUpDeadElements(plays);

//             // 3. 物理引擎与动画推进 (处理重力、移动、切图)
//             moveAndUpdate(all, gameTime);

//             // 4. 核心战斗与碰撞检测 (大圣打怪、怪打大圣、火球炸怪、进传送门)
//             checkCombat(plays, enemys);

//             // 5. 时间流逝与帧率控制
//             gameTime++;
//             try {
//                 sleep(10); // 约 100 帧的刷新频率，保证丝滑
//             } catch (InterruptedException e) {
//                 e.printStackTrace();
//             }
//         }
//     }

//     /**
//      * 核心战斗检测：AABB碰撞算法
//      */
//     // 【高级战斗与碰撞判定】
//     private void checkCombat(List<ElementObj> plays, List<ElementObj> enemys) {
//         if (plays == null) return;

//         com.tedu.element.Weapon weapon = null;
//         com.tedu.element.WuKong wukong = null;

//         // ==============================================================
//         // 【核心修复】：将高级 for 循环改为下标遍历，彻底杜绝并发修改异常！
//         // 1. 从 PLAY 集合里抓出大圣和武器
//         // ==============================================================
//         for (int i = 0; i < plays.size(); i++) {
//             ElementObj obj = plays.get(i);
//             if (obj instanceof com.tedu.element.Weapon) weapon = (com.tedu.element.Weapon) obj;
//             if (obj instanceof com.tedu.element.WuKong) wukong = (com.tedu.element.WuKong) obj;
//         }

//         if (wukong == null) return; 

//         // ==============================================================
//         // --- 大圣阵亡判定优先 ---
//         if (wukong.isDead) {
//             if (wukong.deathTimer > 100) { 
//                 System.out.println("💀 游戏结束，返回主菜单...");
//                 backToMainMenu(false); 
//                 return; 
//             }
//         }
        
//         // --- 生成碰撞箱 ---
//         java.awt.Rectangle weaponBox = null;
//         if (weapon != null) {
//             weaponBox = new java.awt.Rectangle(
//                 weapon.getX() + 20, weapon.getY() + 20, weapon.getW() - 40, weapon.getH() - 40
//             );
//         }
        
//         java.awt.Rectangle wukongBox = new java.awt.Rectangle(
//             wukong.getX() + 40, wukong.getY() + 20, wukong.getW() - 80, wukong.getH() - 20
//         );

//         // ================== A. 怪物相关判定 ==================
//         if (enemys != null && !enemys.isEmpty()) {
//             for (int i = 0; i < enemys.size(); i++) {
//                 ElementObj obj = enemys.get(i);
//                 if (obj instanceof com.tedu.element.enemy2) {
//                     com.tedu.element.enemy2 enemy = (com.tedu.element.enemy2) obj;
                    
//                     if (enemy.hp <= 0) continue; 

//                     java.awt.Rectangle enemyBox = new java.awt.Rectangle(
//                         enemy.getX() + 30, enemy.getY() + 30, enemy.getW() - 60, enemy.getH() - 60
//                     );

//                     // 逻辑 1: 金箍棒打怪 
//                     if (weaponBox != null && weaponBox.intersects(enemyBox) && weapon.isAttacking() && enemy.invincibleTimer == 0) {
//                         if (enemy.lastHitAttackId != wukong.currentAttackId) {
//                             enemy.takeDamage(wukong.attackPower); 
//                             enemy.lastHitAttackId = wukong.currentAttackId; 
//                         }
//                     }

//                     // 逻辑 2: 怪物打大圣
//                     if (enemyBox.intersects(wukongBox) && "attack".equals(enemy.getCurrentAction()) && wukong.invincibleTimer == 0) {
//                         wukong.takeDamage(10); 
//                     }

//                     // ==============================================
//                     // 【核心修复】：火球遍历也必须改为下标遍历
//                     // ==============================================
//                     for (int j = 0; j < plays.size(); j++) {
//                         ElementObj playObj = plays.get(j);
//                         if (playObj instanceof com.tedu.element.Fireball) {
//                             com.tedu.element.Fireball fireball = (com.tedu.element.Fireball) playObj;
                            
//                             if (fireball.isActive && fireball.getHitBox().intersects(enemyBox)) {
//                                 enemy.takeDamage(fireball.damage);
//                                 fireball.isActive = false; 
//                                 System.out.println("🔥 火球命中怪物！造成 " + fireball.damage + " 点伤害！");
//                             }
//                         }
//                     }
//                 }
//             }
//         }

//         // ================== C. 动态传送门降临 ==================
//         if (enemys != null && enemys.isEmpty() && !isPortalSpawned && !wukong.isDead) {
//             System.out.println("✨ 怪物已全军覆没！传送门开启！");
//             ElementObj portal = new com.tedu.element.Portal();
//             int portalX = wukong.getX() + 300;
//             if (portalX > 8800) portalX = 8800; 
            
//             portal.createElement(portalX + ",350,100,150");
//             com.tedu.manager.ElementManager.getManager().addElement(portal, com.tedu.manager.GameElement.PLAY);
//             isPortalSpawned = true; 
//         }

//         // ================== B. 传送门通关判定 ==================
//         // 【核心修复】：同样改为下标遍历
//         for (int i = 0; i < plays.size(); i++) {
//             ElementObj obj = plays.get(i);
//             if (obj instanceof com.tedu.element.Portal) {
//                 com.tedu.element.Portal portal = (com.tedu.element.Portal) obj;
//                 java.awt.Rectangle portalBox = new java.awt.Rectangle(
//                     portal.getX(), portal.getY(), portal.getW(), portal.getH()
//                 );
                
//                 if (wukongBox.intersects(portalBox)) {
//                     System.out.println("🎉 大圣抵达终点！准备结算数据...");
//                     if (com.tedu.manager.GameLoad.currentSave != null) {
//                         wukong.updateToSave(com.tedu.manager.GameLoad.currentSave);
//                         com.tedu.login.DataManager dm = new com.tedu.login.DataManager();
//                         dm.savePlayerSave(com.tedu.manager.GameLoad.currentSave.getUsername(), com.tedu.manager.GameLoad.currentSave);
//                     }
//                     backToMainMenu(true); 
//                     return; 
//                 }
//             }
//         }
//     }

//     /**
//      * 游戏元素自动化与死亡清理
//      */
//     public void moveAndUpdate(Map<GameElement, List<ElementObj>> all, long gameTime) {
//         for (GameElement ge : GameElement.values()) {
//             List<ElementObj> list = all.get(ge);
//             for (int i = list.size() - 1; i >= 0; i--) {
//                 ElementObj obj = list.get(i);
//                 if (!obj.isLive()) {
//                     obj.die();
//                     list.remove(i);
//                     continue;
//                 }
//                 obj.model(gameTime);
//             }
//         }
//     }

//     // 通用切画面方法：退回主菜单
//     // =====================================================================
//     // 【完美版】：退回游戏主菜单（跳过登录）
//     // =====================================================================
//     private void backToMainMenu(boolean isWin) {
//         isGameRunning = false; 
        
//         javax.swing.SwingUtilities.invokeLater(() -> {
//             // 1. 关闭当前的战斗画面
//             for (java.awt.Window window : java.awt.Window.getWindows()) {
//                 if (window instanceof javax.swing.JFrame) {
//                     window.dispose(); 
//                 }
//             }
            
//             // 2. 先弹出提示（一定要在画面出来前弹，不然会卡层级）
//             if (isWin) {
//                 javax.swing.JOptionPane.showMessageDialog(null, "通关成功！已保存进度！");
//             } else {
//                 javax.swing.JOptionPane.showMessageDialog(null, "胜败乃兵家常事，大侠请重新来过！");
//             }

//             // 3. 【核心修复】：直接召唤你找到的 MainEntranceJPanel 选关界面！
//             com.tedu.show.GameJFrame gj = new com.tedu.show.GameJFrame();
//             com.tedu.show.MainEntranceJPanel menuPanel = new com.tedu.show.MainEntranceJPanel();
            
//             // 直接喂给它内存里的全局存档
//             menuPanel.setPlayerSave(com.tedu.manager.GameLoad.currentSave); 
            
//             com.tedu.show.GameMainJPanel gameMainPanel = new com.tedu.show.GameMainJPanel();
//             menuPanel.setGameMainPanel(gameMainPanel);
            
//             gj.setjPanel(menuPanel);
//             gj.add(menuPanel); 
//             gj.setVisible(true); // 熟悉的界面回来了，而且不用重新登录！
//         });
//     }

//     private void gameOver() {
//         // 场景清理逻辑
//     }
// }

package com.tedu.controller;

import java.util.List;
import java.util.Map;
import com.tedu.element.ElementObj;
import com.tedu.element.WuKong;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;

public class GameThread extends Thread {
    private ElementManager em;
    public static boolean isGameRunning = true;
    private boolean isPortalSpawned = false;

    public GameThread() {
        em = ElementManager.getManager();
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
        
        // 注意：由于 backToMainMenu 会 dispose 窗口并开启新流程，
        // 这里的线程运行完就会自然死亡，不需要 while(true) 强行循环。
    }

    /**
     * 【重构点2】：这就是你找的“第三步”！
     * 在这里把清空和加载结合起来。
     */
    public void initGame() {
        // A. 强制清空上一局残留（解决内存不释放、传送门重叠、怪物刷两倍的问题）
        em.clearAll(); 
        
        // B. 重置本线程内的逻辑开关
        this.isPortalSpawned = false; 
        isGameRunning = true; 
        
        System.out.println("🧹 内存已清空，准备加载新关卡...");
    }

    private void gameLoad() {
        // 调用你现有的加载工具类
        GameLoad.loadImg();  
        GameLoad.loadMap();  
        GameLoad.loadPlay(); 
        System.out.println("✅ 资源加载完毕");
    }

    // --- 以下是打扫战场逻辑，保持不变 ---
    private void cleanUpDeadElements(List<ElementObj> plays) {
        if (plays == null) return;
        for (int i = plays.size() - 1; i >= 0; i--) {
            ElementObj obj = plays.get(i);
            if (obj instanceof com.tedu.element.Fireball) {
                if (!((com.tedu.element.Fireball) obj).isActive) plays.remove(i); 
            }
            else if (obj instanceof com.tedu.element.DamageText) {
                if (((com.tedu.element.DamageText) obj).isDead()) plays.remove(i);
            }
        }
    }

    /**
     * 【重构点3】：gameRun 逻辑微调
     * 确保它受 isGameRunning 严格控制
     */
    private void gameRun() {
        long gameTime = 0L;
        while (isGameRunning) {
            Map<GameElement, List<ElementObj>> all = em.getGameElements();
            List<ElementObj> plays = em.getElementsByKey(GameElement.PLAY);
            List<ElementObj> enemys = em.getElementsByKey(GameElement.ENEMY);

            cleanUpDeadElements(plays);
            moveAndUpdate(all, gameTime);
            checkCombat(plays, enemys);

            gameTime++;
            try {
                sleep(10); 
            } catch (InterruptedException e) {
                break; // 被中断时跳出
            }
        }
    }

    /**
     * 核心战斗检测：AABB碰撞算法
     */
    // 【高级战斗与碰撞判定】
    private void checkCombat(List<ElementObj> plays, List<ElementObj> enemys) {
        if (plays == null) return;

        com.tedu.element.Weapon weapon = null;
        com.tedu.element.WuKong wukong = null;

        // ==============================================================
        // 【核心修复】：将高级 for 循环改为下标遍历，彻底杜绝并发修改异常！
        // 1. 从 PLAY 集合里抓出大圣和武器
        // ==============================================================
        for (int i = 0; i < plays.size(); i++) {
            ElementObj obj = plays.get(i);
            if (obj instanceof com.tedu.element.Weapon) weapon = (com.tedu.element.Weapon) obj;
            if (obj instanceof com.tedu.element.WuKong) wukong = (com.tedu.element.WuKong) obj;
        }

        if (wukong == null) return; 

        // ==============================================================
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
                if (obj instanceof com.tedu.element.enemy2) {
                    com.tedu.element.enemy2 enemy = (com.tedu.element.enemy2) obj;
                    
                    if (enemy.hp <= 0) continue; 

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

                    // ==============================================
                    // 【核心修复】：火球遍历也必须改为下标遍历
                    // ==============================================
                    for (int j = 0; j < plays.size(); j++) {
                        ElementObj playObj = plays.get(j);
                        if (playObj instanceof com.tedu.element.Fireball) {
                            com.tedu.element.Fireball fireball = (com.tedu.element.Fireball) playObj;
                            
                            if (fireball.isActive && fireball.getHitBox().intersects(enemyBox)) {
                                enemy.takeDamage(fireball.damage);
                                fireball.isActive = false; 
                                System.out.println("🔥 火球命中怪物！造成 " + fireball.damage + " 点伤害！");
                            }
                        }
                    }
                }
            }
        }

        // ================== C. 动态传送门降临 ==================
        if (enemys != null && enemys.isEmpty() && !isPortalSpawned && !wukong.isDead) {
            System.out.println("✨ 怪物已全军覆没！传送门开启！");
            ElementObj portal = new com.tedu.element.Portal();
            int portalX = wukong.getX() + 300;
            if (portalX > 8800) portalX = 8800; 
            
            portal.createElement(portalX + ",350,100,150");
            com.tedu.manager.ElementManager.getManager().addElement(portal, com.tedu.manager.GameElement.PLAY);
            isPortalSpawned = true; 
        }

        // ================== B. 传送门通关判定 ==================
        // 【核心修复】：同样改为下标遍历
        for (int i = 0; i < plays.size(); i++) {
            ElementObj obj = plays.get(i);
            if (obj instanceof com.tedu.element.Portal) {
                com.tedu.element.Portal portal = (com.tedu.element.Portal) obj;
                java.awt.Rectangle portalBox = new java.awt.Rectangle(
                    portal.getX(), portal.getY(), portal.getW(), portal.getH()
                );
                
                if (wukongBox.intersects(portalBox)) {
                    System.out.println("🎉 大圣抵达终点！准备结算数据...");
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

    // --- moveAndUpdate 保持不变 ---
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
        // 1. 先停掉当前的循环，让 run() 方法能够走到头并死掉
        isGameRunning = false; 
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            // 2. 销毁当前所有旧窗口，防止内存里重叠好几个游戏
            for (java.awt.Window window : java.awt.Window.getWindows()) {
                if (window instanceof javax.swing.JFrame) {
                    window.dispose(); 
                }
            }
            
            // 3. 弹出提示
            String msg = isWin ? "恭喜通关！大圣归来！" : "胜败乃兵家常事，请重新来过！";
            javax.swing.JOptionPane.showMessageDialog(null, msg);

            // 4. 【完全镜像入口逻辑】：创建一个和刚开机时一模一样的菜单环境
            com.tedu.show.GameJFrame gj = new com.tedu.show.GameJFrame();
            com.tedu.show.MainEntranceJPanel menuPanel = new com.tedu.show.MainEntranceJPanel();
            
            // 必须把存档传回去，不然等级和数据就丢了
            menuPanel.setPlayerSave(com.tedu.manager.GameLoad.currentSave); 
            
            // 【重要】：必须把 GameMainJPanel 塞给菜单，否则菜单里的“开始游戏”按钮会失效
            com.tedu.show.GameMainJPanel gameMainPanel = new com.tedu.show.GameMainJPanel();
            menuPanel.setGameMainPanel(gameMainPanel);
            
            gj.setjPanel(menuPanel);
            gj.add(menuPanel); 
            gj.setVisible(true); // 重新打开菜单，大功告成！
        });
    }
    
    private void gameOver() {
        System.out.println("🏁 场景线程结束");
    }
}