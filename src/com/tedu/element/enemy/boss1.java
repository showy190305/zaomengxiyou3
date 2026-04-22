package com.tedu.element.enemy;

import com.tedu.element.ElementObj;
import com.tedu.element.WuKong;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;

public class boss1 extends BaseEnemy {

    private int idleFrameCount = 6;
    private int walkFrameCount = 4;
    private int hitRowIndex = 2;
    private int hitFrameCount = 1;
    private int deadRowIndex = 3;
    private int deadFrameCount = 6;
    private int attackRowIndex = 4;
    private int attackFrameCount = 4;
    private int skillRowIndex = 5;
    private int skillFrameCount = 4;

    private int attackCountBeforeSkill = 0;
    private boolean skillLock = false;
    private int skillPullTimer = 0;

    private static final int REQUIRED_ATTACKS = 3;
    private static final int SKILL_PULL_DURATION = 28;
    private static final int SKILL_COOLDOWN = 180;

    public boss1() {
        this.hp = 100;
        this.maxHp = 100;
        this.expDrop = 30;
        this.walkStep = 1;
        this.attackRange = 80;
        this.visionRange = 400;

        this.setW(190);
        this.setH(190);

        loadSprites();
    }

    private void loadSprites() {
        try {
            BufferedImage spriteSheet = ImageIO.read(new File("image/enemy/boss1/boss1.png"));
            frames = new ArrayList<>();
            actionGroups = new HashMap<>();

            int frameWidth = 190;
            int frameHeight = 190;

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

            System.out.println("boss1 sprite loaded.");
        } catch (IOException e) {
            System.out.println("load boss1 sprite failed: " + e.getMessage());
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
                hitRecoveryTimer = 0;
                skillLock = false;
            }
        } else {
            if (skillLock) {
                currentAction = "skill";
                pullWukongTowardsBoss(target);
            } else {
                handleNormalAI(target);
            }
        }

        if (currentTime - lastMoveTime >= moveInterval) {
            lastMoveTime = currentTime;
            updateFrameState();
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

            if (distY < 100) {
                if (absDistX <= attackRange) {
                    isLeft = (distX < 0);
                    if (attackCooldown == 0) {
                        if (attackCountBeforeSkill >= REQUIRED_ATTACKS && actionGroups.containsKey("skill")) {
                            startSkill();
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

    private void startSkill() {
        skillLock = true;
        attackLock = false;
        currentAction = "skill";
        currentActionFrameIndex = 0;
        skillPullTimer = 0;
        attackCooldown = SKILL_COOLDOWN;
    }

    private void pullWukongTowardsBoss(WuKong target) {
        if (target == null) return;

        int frontOffset = 90;
        int targetX = isLeft
            ? this.getX() - target.getW() + frontOffset
            : this.getX() + this.getW() - frontOffset;
        int targetY = this.getY() + this.getH() - target.getH();

        int dx = targetX - target.getX();
        int dy = targetY - target.getY();

        int stepX = calcStep(dx, 10, 2);
        int stepY = calcStep(dy, 6, 1);

        target.setX(target.getX() + stepX);
        target.setY(target.getY() + stepY);

        if (target.getX() < 0) target.setX(0);
        if (target.getX() > 4700 - target.getW()) target.setX(4700 - target.getW());
        if (target.getY() < 0) target.setY(0);

        skillPullTimer++;
        if (skillPullTimer >= SKILL_PULL_DURATION) {
            target.setX(targetX);
            target.setY(targetY);
        }
    }

    private int calcStep(int distance, int maxStep, int minStep) {
        if (distance == 0) return 0;
        int raw = (int) Math.round(distance * 0.24);
        if (raw == 0) raw = distance > 0 ? minStep : -minStep;
        if (raw > maxStep) raw = maxStep;
        if (raw < -maxStep) raw = -maxStep;
        return raw;
    }

    private void updateFrameState() {
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
                skillPullTimer = 0;
            } else {
                this.currentFrame = actionFrames.get(currentActionFrameIndex);
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

    public static boss1 createForSpriteExtraction() {
        boss1 instance = new boss1();
        instance.loadSprites();
        return instance;
    }
}
