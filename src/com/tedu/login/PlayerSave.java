package com.tedu.login;

// 导入序列化接口，使对象能够被保存到文件中
import java.io.Serializable;

/**
 * 玩家存档数据类（可扩展）
 * 用于存储玩家的游戏进度信息，包括用户名、等级等数据
 * 实现Serializable接口，支持对象的序列化和反序列化
 */
public class PlayerSave implements Serializable {
    // 序列化版本ID，确保不同版本间的兼容性
    private static final long serialVersionUID = 1L;
    
    // 玩家用户名
    private String username;
    
    // 玩家等级
    private int level;
    
    // 其他属性：装备、地图进度等

    /**
     * 构造函数，创建一个玩家存档对象
     * @param username 玩家用户名
     */
    public PlayerSave(String username) {
        this.username = username;
        this.level = 1; // 默认等级为1
        // 初始化其他属性
    }

    // getter/setter方法用于访问私有成员变量
    
    /**
     * 获取用户名
     * @return 玩家用户名
     */
    public String getUsername() { return username; }
    
    /**
     * 获取玩家等级
     * @return 玩家当前等级
     */
    public int getLevel() { return level; }
    
    /**
     * 设置玩家等级
     * @param level 要设置的新等级
     */
    public void setLevel(int level) { this.level = level; }
    
    // 其他 getter/setter
}