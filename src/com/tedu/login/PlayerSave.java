package com.tedu.login;

import java.io.Serializable;

/**
 * 玩家存档数据类
 * 用于存储玩家的完整游戏进度信息（包含RPG属性）
 */
public class PlayerSave implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    
    // ================= 核心 RPG 属性 =================
    private int level;
    private int hp;
    private int maxHp;
    private int mp;
    private int maxMp;
    private int exp;
    private int maxExp;
    private int attackPower;

    /**
     * 构造函数，创建新存档时的初始默认数值
     */
    public PlayerSave(String username) {
        this.username = username;
        this.level = 1;
        this.hp = 100;
        this.maxHp = 100;
        this.mp = 50;
        this.maxMp = 50;
        this.exp = 0;
        this.maxExp = 100;
        this.attackPower = 35;
    }

    // ================= Getters & Setters =================
    public String getUsername() { return username; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }

    public int getMp() { return mp; }
    public void setMp(int mp) { this.mp = mp; }

    public int getMaxMp() { return maxMp; }
    public void setMaxMp(int maxMp) { this.maxMp = maxMp; }

    public int getExp() { return exp; }
    public void setExp(int exp) { this.exp = exp; }

    public int getMaxExp() { return maxExp; }
    public void setMaxExp(int maxExp) { this.maxExp = maxExp; }

    public int getAttackPower() { return attackPower; }
    public void setAttackPower(int attackPower) { this.attackPower = attackPower; }
}