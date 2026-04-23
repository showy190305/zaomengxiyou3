package com.tedu.element;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

// 【核心】：继承 ElementObj，作为所有敌人的通用“基类”
public abstract class BaseEnemy extends ElementObj {
    
    // ==========================================================
    // 1. 把原来 private 的变量改成 protected，这样子类就能用了
    // ==========================================================
    public long lastHitAttackId = 0;
    
    protected List<BufferedImage> frames;
    protected Map<String, List<Integer>> actionGroups;
    
    protected String currentAction = "idle"; 
    protected int currentFrame = 0;          
    protected int currentActionFrameIndex = 0; 
    protected boolean isLeft = true;         
    
    public int hp = 100;
    public int maxHp = 100;
    protected boolean attackLock = false;    
    protected boolean isHit = false;  
    protected boolean isDead = false; 
    public int hitRecoveryTimer = 0; 
    
    protected int walkStep = 1; 
    protected int aiTimer = 0;
    protected int attackRange = 80; 
    
    protected long lastMoveTime = 0;
    // 【微调】：去掉了 final，方便以后如果有移速更快的怪物可以修改这个值
    protected long moveInterval = 150; 
    
    public int expDrop = 30; 
    protected int visionRange = 400;

    public int invincibleTimer = 0; 
    public int attackCooldown = 0;  
    private boolean dropGenerated = false;

    // ==========================================================
    // 2. 以下逻辑（受击、渲染、AI）一字未改，直接原样搬过来
    // ==========================================================
    public void takeDamage(int damage) {
        if (isDead || invincibleTimer > 0) return; 
        
        this.hp -= damage;
        attackLock = false;
        invincibleTimer = 20;
        System.out.println("怪物受到攻击！扣除血量: " + damage + "，剩余血量: " + this.hp);

        com.tedu.element.DamageText dt = new com.tedu.element.DamageText(
            this.getX() + 30, this.getY() - 30, damage, java.awt.Color.ORANGE
        );
        com.tedu.manager.ElementManager.getManager().addElement(dt, com.tedu.manager.GameElement.PLAY);

        attackLock = false; 

        if (this.hp <= 0) {
            this.hp = 0;
            isDead = true;
            currentAction = "dead";
            currentActionFrameIndex = 0; 
            
            try {
                java.util.List<ElementObj> players = com.tedu.manager.ElementManager.getManager().getElementsByKey(com.tedu.manager.GameElement.PLAY);
                for (ElementObj obj : players) {
                    if (obj instanceof WuKong) {
                        ((WuKong) obj).addExp(this.expDrop);
                        break;
                    }
                }
            } catch (Exception e) {}
            
        } else {
            isHit = true;
            currentAction = "hit";
            currentActionFrameIndex = 0;
            hitRecoveryTimer = 0; 
        }
    }

    @Override
    public ElementObj createElement(String str) {
        String[] params = str.split(",");
        if (params.length >= 4) {
            this.setX(Integer.parseInt(params[0]));
            this.setY(Integer.parseInt(params[1]));
            this.setW(Integer.parseInt(params[2]));
            this.setH(Integer.parseInt(params[3]));
        } else {
            this.setW(150);
            this.setH(150);
        }
        return this;
    }
    
    @Override
    public void showElement(Graphics g) {
        updateAnimation();
        
        if (frames != null && !frames.isEmpty() && currentFrame < frames.size()) {
            BufferedImage currentImage = frames.get(currentFrame);
            int screenX = this.getX() + MapObj.bgOffsetX;
            
            if (screenX > -200 && screenX < 1000) {
                if (!isLeft) {  
                    Graphics2D g2d = (Graphics2D) g.create();  
                    g2d.translate(screenX + this.getW(), this.getY());  
                    g2d.scale(-1, 1);  
                    g2d.drawImage(currentImage, 0, 0, this.getW(), this.getH(), null);
                    g2d.dispose();  
                } else {        
                    g.drawImage(currentImage, screenX, this.getY(), this.getW(), this.getH(), null); 
                }

                if (!isDead) {
                    g.setColor(java.awt.Color.DARK_GRAY);
                    g.fillRect(screenX + 30, this.getY() + 20, 80, 6); 
                    g.setColor(new java.awt.Color(220, 50, 50)); 
                    g.fillRect(screenX + 30, this.getY() + 20, (int)(80 * ((double)hp / maxHp)), 6); 
                    g.setColor(java.awt.Color.WHITE);
                    g.drawRect(screenX + 30, this.getY() + 20, 80, 6); 
                }
            }
        }
    }
    
    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();

        if (invincibleTimer > 0) invincibleTimer--;
        if (attackCooldown > 0) attackCooldown--;

        if (isDead) {
        } 
        else if (isHit) {
            hitRecoveryTimer++;
            if (hitRecoveryTimer > 40) { 
                isHit = false;
                currentAction = "idle";
                hitRecoveryTimer = 0; 
            }
        }
        else {
            WuKong target = null;
            try {
                java.util.List<ElementObj> players = com.tedu.manager.ElementManager.getManager().getElementsByKey(com.tedu.manager.GameElement.PLAY);
                for (ElementObj obj : players) {
                    if (obj instanceof WuKong) {
                        target = (WuKong) obj;
                        break;
                    }
                }
            } catch (Exception e) {}

            boolean isChasing = false; 
            boolean isWaitingCooldown = false; 

            if (!attackLock && target != null) {
                int distX = target.getX() - this.getX(); 
                int distY = Math.abs(target.getY() - this.getY());
                int absDistX = Math.abs(distX);

                if (distY < 100) {
                    if (absDistX <= attackRange) {
                        if (attackCooldown == 0) {
                            attackLock = true;
                            currentAction = "attack";
                            currentActionFrameIndex = 0; 
                            isLeft = (distX < 0); 
                            attackCooldown = 150;  
                        } else {
                            isWaitingCooldown = true;
                            currentAction = "idle";
                            isLeft = (distX < 0);
                        }
                    } 
                    else if (absDistX <= visionRange) {
                        isChasing = true;
                        currentAction = "walk";
                        isLeft = (distX < 0); 
                        
                        if (isLeft) this.setX(this.getX() - (walkStep + 1));
                        else this.setX(this.getX() + (walkStep + 1));
                    }
                }
            }

            if (!attackLock && !isChasing && !isWaitingCooldown) {
                aiTimer++;
                if (aiTimer > 150) { 
                    aiTimer = 0;
                    int rand = new java.util.Random().nextInt(3);
                    if (rand == 0) {
                        currentAction = "idle";
                    } else if (rand == 1) {
                        currentAction = "walk";
                        isLeft = true;
                    } else {
                        currentAction = "walk";
                        isLeft = false;
                    }
                    currentActionFrameIndex = 0;
                }

                if ("walk".equals(currentAction)) {
                    if (isLeft) this.setX(this.getX() - walkStep);
                    else this.setX(this.getX() + walkStep);
                }

                if (this.getX() < 0) this.setX(0);
                if (this.getX() > 9000 - this.getW()) this.setX(9000 - this.getW());
            }
        }

        if (currentTime - lastMoveTime >= moveInterval) {
            lastMoveTime = currentTime;
            
            List<Integer> actionFrames = actionGroups.get(currentAction);
            if (actionFrames != null && !actionFrames.isEmpty()) {
                
                if (isDead) {
                    if (currentActionFrameIndex < actionFrames.size() - 1) {
                        currentActionFrameIndex++;
                        this.currentFrame = actionFrames.get(currentActionFrameIndex);
                    } else {
                        this.setLive(false); 
                    }
                }
                else if (isHit) {
                    this.currentFrame = actionFrames.get(0);
                }
                else if (attackLock && "attack".equals(currentAction)) {
                    currentActionFrameIndex++;
                    if (currentActionFrameIndex >= actionFrames.size()) {
                        attackLock = false; 
                        currentAction = "idle"; 
                        currentActionFrameIndex = 0;
                    } else {
                        this.currentFrame = actionFrames.get(currentActionFrameIndex);
                    }
                } 
                else {
                    currentActionFrameIndex = (currentActionFrameIndex + 1) % actionFrames.size();
                    this.currentFrame = actionFrames.get(currentActionFrameIndex);
                }
            }
        }
    }

    public String getCurrentAction() {
        return this.currentAction;
    }

    public boolean isDropGenerated() {
        return dropGenerated;
    }

    public void markDropGenerated() {
        this.dropGenerated = true;
    }
}
