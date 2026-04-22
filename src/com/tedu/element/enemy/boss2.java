package com.tedu.element.enemy;

import com.tedu.element.ElementObj;
import com.tedu.element.WuKong;
import com.tedu.element.map.MapBase;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;

public class boss2 extends BaseEnemy {

    private int idleFrameCount = 6;
    private int walkFrameCount = 4;
    private int hitRowIndex = 2;
    private int hitFrameCount = 1;
    private int deadRowIndex = 3;
    private int deadFrameCount = 6;
    private int attackRowIndex = 4;
    private int attackFrameCount = 6;
    private int skillRowIndex = 6;
    private int skillFrameCount = 4;

    private int attackCountBeforeSkill = 0;
    private boolean skillLock = false;
    private boolean skillHitFrame1Done = false;
    private boolean skillHitFrame3Done = false;

    private BufferedImage skillEffect1;
    private BufferedImage skillEffect2;

    private static final int REQUIRED_ATTACKS = 3;
    private static final int SKILL_COOLDOWN = 180;
    private static final int SKILL_DAMAGE = 18;

    public boss2() {
        this.hp = 100;
        this.maxHp = 100;
        this.expDrop = 30;
        this.walkStep = 1;
        this.attackRange = 100;
        this.visionRange = 450;

        this.setW(350);
        this.setH(350);

        loadSprites();
        loadSkillEffects();
    }

    private void loadSprites() {
        try {
            BufferedImage spriteSheet = ImageIO.read(new File("image/enemy/boss2/boss2.png"));
            frames = new ArrayList<>();
            actionGroups = new HashMap<>();

            int frameWidth = 350;
            int frameHeight = 350;

            List<Integer> idleFrames = new ArrayList<>();
            for (int col = 0; col < idleFrameCount; col++) {
                frames.add(spriteSheet.getSubimage(col * frameWidth, 0, frameWidth, frameHeight));
                idleFrames.add(frames.size() - 1);
            }
            actionGroups.put("idle", idleFrames);

            if (spriteSheet.getHeight() >= frameHeight * 2) {
                List<Integer> walkFrames = new ArrayList<>();
                for (int col = 0; col < walkFrameCount; col++) {
                    frames.add(spriteSheet.getSubimage(col * frameWidth, frameHeight, frameWidth, frameHeight));
                    walkFrames.add(frames.size() - 1);
                }
                actionGroups.put("walk", walkFrames);
            }

            if (spriteSheet.getHeight() >= frameHeight * (hitRowIndex + 1)) {
                List<Integer> hitFrames = new ArrayList<>();
                for (int col = 0; col < hitFrameCount; col++) {
                    frames.add(spriteSheet.getSubimage(col * frameWidth, hitRowIndex * frameHeight, frameWidth, frameHeight));
                    hitFrames.add(frames.size() - 1);
                }
                actionGroups.put("hit", hitFrames);
            }

            if (spriteSheet.getHeight() >= frameHeight * (deadRowIndex + 1)) {
                List<Integer> deadFrames = new ArrayList<>();
                for (int col = 0; col < deadFrameCount; col++) {
                    frames.add(spriteSheet.getSubimage(col * frameWidth, deadRowIndex * frameHeight, frameWidth, frameHeight));
                    deadFrames.add(frames.size() - 1);
                }
                actionGroups.put("dead", deadFrames);
            }

            if (spriteSheet.getHeight() >= frameHeight * (attackRowIndex + 1)) {
                List<Integer> attackFrames = new ArrayList<>();
                for (int col = 0; col < attackFrameCount; col++) {
                    frames.add(spriteSheet.getSubimage(col * frameWidth, attackRowIndex * frameHeight, frameWidth, frameHeight));
                    attackFrames.add(frames.size() - 1);
                }
                actionGroups.put("attack", attackFrames);
            }

            if (spriteSheet.getHeight() >= frameHeight * (skillRowIndex + 1)) {
                List<Integer> skillFrames = new ArrayList<>();
                for (int col = 0; col < skillFrameCount; col++) {
                    frames.add(spriteSheet.getSubimage(col * frameWidth, skillRowIndex * frameHeight, frameWidth, frameHeight));
                    skillFrames.add(frames.size() - 1);
                }
                actionGroups.put("skill", skillFrames);
            }

            System.out.println("boss2 sprite loaded.");
        } catch (IOException e) {
            System.out.println("load boss2 sprite failed: " + e.getMessage());
        }
    }

    private void loadSkillEffects() {
        try {
            skillEffect1 = ImageIO.read(new File("image/enemy/boss2/skill1.png"));
        } catch (IOException e) {
            skillEffect1 = null;
            System.out.println("load boss2 skill1 effect failed: " + e.getMessage());
        }

        try {
            skillEffect2 = ImageIO.read(new File("image/enemy/boss2/skill2.png"));
        } catch (IOException e) {
            skillEffect2 = null;
            System.out.println("load boss2 skill2 effect failed: " + e.getMessage());
        }
    }

    @Override
    public void showElement(Graphics g) {
        super.showElement(g);

        if (!skillLock || !"skill".equals(currentAction)) return;

        BufferedImage effect = (currentActionFrameIndex == 0 || currentActionFrameIndex == 2) ? skillEffect1 : skillEffect2;
        if (effect == null) return;

        int screenX = this.getX() + MapBase.bgOffsetX;
        int drawW = this.getW() + 120;
        int drawH = this.getH() + 100;
        int drawX = screenX - (drawW - this.getW()) / 2;
        int drawY = this.getY() - (drawH - this.getH()) / 2;

        if (!isLeft) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.translate(drawX + drawW, drawY);
            g2d.scale(-1, 1);
            g2d.drawImage(effect, 0, 0, drawW, drawH, null);
            g2d.dispose();
        } else {
            g.drawImage(effect, drawX, drawY, drawW, drawH, null);
        }
    }

    @Override
    protected void updateAnimation() {
        long currentTime = System.currentTimeMillis();

        if (invincibleTimer > 0) invincibleTimer--;
        if (attackCooldown > 0) attackCooldown--;

        WuKong target = findTarget();

        if (isDead) {
            // no-op
        } else if (isHit) {
            hitRecoveryTimer++;
            if (hitRecoveryTimer > 40) {
                isHit = false;
                currentAction = "idle";
                currentActionFrameIndex = 0;
                hitRecoveryTimer = 0;
                skillLock = false;
            }
        } else {
            if (skillLock) {
                currentAction = "skill";
            } else {
                handleNormalAI(target);
            }
        }

        if (currentTime - lastMoveTime >= moveInterval) {
            lastMoveTime = currentTime;
            updateFrameState(target);
        }
    }

    private WuKong findTarget() {
        try {
            List<ElementObj> players = com.tedu.manager.ElementManager.getManager()
                .getElementsByKey(com.tedu.manager.GameElement.PLAY);
            for (ElementObj obj : players) {
                if (obj instanceof WuKong) return (WuKong) obj;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void handleNormalAI(WuKong target) {
        boolean isChasing = false;
        boolean isWaitingCooldown = false;

        if (!attackLock && target != null) {
            int distX = target.getX() - this.getX();
            int distY = Math.abs(target.getY() - this.getY());
            int absDistX = Math.abs(distX);

            if (distY < 130) {
                if (absDistX <= attackRange) {
                    isLeft = (distX < 0);
                    if (attackCooldown == 0) {
                        if (attackCountBeforeSkill >= REQUIRED_ATTACKS && actionGroups.containsKey("skill")) {
                            startSkill(target);
                        } else {
                            attackLock = true;
                            currentAction = "attack";
                            currentActionFrameIndex = 0;
                            attackCooldown = 150;
                        }
                    } else {
                        isWaitingCooldown = true;
                        currentAction = "idle";
                    }
                } else if (absDistX <= visionRange) {
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
            if (this.getX() > 4700 - this.getW()) this.setX(4700 - this.getW());
        }
    }

    private void startSkill(WuKong target) {
        skillLock = true;
        attackLock = false;
        currentAction = "skill";
        currentActionFrameIndex = 0;
        skillHitFrame1Done = false;
        skillHitFrame3Done = false;
        attackCooldown = SKILL_COOLDOWN;

        List<Integer> skillFrames = actionGroups.get("skill");
        if (skillFrames != null && !skillFrames.isEmpty()) {
            this.currentFrame = skillFrames.get(0);
        }

        applySkillDamageOnFrame(target, 0);
    }

    private void applySkillDamageOnFrame(WuKong target, int frameIndex) {
        if (target == null || target.isDead) return;

        if (frameIndex == 0 && skillHitFrame1Done) return;
        if (frameIndex == 2 && skillHitFrame3Done) return;
        if (frameIndex != 0 && frameIndex != 2) return;

        Rectangle skillRect = new Rectangle(
            this.getX() - 80,
            this.getY() + 20,
            this.getW() + 160,
            this.getH() - 40
        );
        Rectangle wukongRect = new Rectangle(
            target.getX() + 40,
            target.getY() + 20,
            target.getW() - 80,
            target.getH() - 20
        );

        if (skillRect.intersects(wukongRect)) {
            target.takeDamage(SKILL_DAMAGE);
        }

        if (frameIndex == 0) skillHitFrame1Done = true;
        if (frameIndex == 2) skillHitFrame3Done = true;
    }

    private void updateFrameState(WuKong target) {
        List<Integer> actionFrames = actionGroups.get(currentAction);
        if (actionFrames == null || actionFrames.isEmpty()) return;

        if (isDead) {
            if (currentActionFrameIndex < actionFrames.size() - 1) {
                currentActionFrameIndex++;
                this.currentFrame = actionFrames.get(currentActionFrameIndex);
            } else {
                this.setLive(false);
            }
            return;
        }

        if (isHit) {
            this.currentFrame = actionFrames.get(0);
            return;
        }

        if (skillLock && "skill".equals(currentAction)) {
            currentActionFrameIndex++;
            if (currentActionFrameIndex >= actionFrames.size()) {
                skillLock = false;
                currentAction = "idle";
                currentActionFrameIndex = 0;
                attackCountBeforeSkill = 0;
                skillHitFrame1Done = false;
                skillHitFrame3Done = false;
            } else {
                this.currentFrame = actionFrames.get(currentActionFrameIndex);
                applySkillDamageOnFrame(target, currentActionFrameIndex);
            }
            return;
        }

        if (attackLock && "attack".equals(currentAction)) {
            currentActionFrameIndex++;
            if (currentActionFrameIndex >= actionFrames.size()) {
                attackLock = false;
                currentAction = "idle";
                currentActionFrameIndex = 0;
                attackCountBeforeSkill++;
            } else {
                this.currentFrame = actionFrames.get(currentActionFrameIndex);
            }
            return;
        }

        currentActionFrameIndex = (currentActionFrameIndex + 1) % actionFrames.size();
        this.currentFrame = actionFrames.get(currentActionFrameIndex);
    }

    public static boss2 createForSpriteExtraction() {
        boss2 instance = new boss2();
        instance.loadSprites();
        return instance;
    }
}
