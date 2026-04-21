package com.tedu.login;

// 导入IO相关类，用于文件读写操作
import java.io.*;
// 导入HashMap类，用于存储键值对数据
import java.util.HashMap;

/**
 * 数据管理类
 * 负责管理用户账户信息和玩家存档数据的持久化存储
 * 包括账户的增删改查、存档的保存和加载等功能
 */
public class DataManager {
    // 定义数据存储目录路径
    private static final String DATA_DIR = "./GameData";
    
    // 定义账户数据文件路径
    private static final String ACCOUNTS_FILE = DATA_DIR + "/accounts.dat";
    
    // 存储账户信息的哈希表，键为用户名，值为密码
    private HashMap<String, String> accounts;

    /**
     * 构造函数，初始化数据管理器
     * 创建数据目录（如果不存在）并加载已有账户信息
     */
    public DataManager() {
        // 创建数据目录
        File dir = new File(DATA_DIR);
        if (!dir.exists()) dir.mkdirs();
        // 加载已有的账户信息
        loadAccounts();
    }

    /**
     * 加载账户信息
     * 从账户数据文件中读取账户信息到内存中
     * 如果文件不存在或读取失败，则初始化为空的HashMap
     */
    @SuppressWarnings("unchecked")
    private void loadAccounts() {
        File file = new File(ACCOUNTS_FILE);
        // 如果文件不存在，初始化为空的HashMap
        if (!file.exists()) {
            accounts = new HashMap<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            // 从文件中读取账户信息
            accounts = (HashMap<String, String>) ois.readObject();
        } catch (Exception e) {
            // 如果读取失败，初始化为空的HashMap
            accounts = new HashMap<>();
        }
    }

    /**
     * 保存账户信息
     * 将内存中的账户信息写入到账户数据文件中
     */
    public void saveAccounts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ACCOUNTS_FILE))) {
            // 将账户信息写入文件
            oos.writeObject(accounts);
        } catch (IOException e) {
            // 输出异常信息
            e.printStackTrace();
        }
    }

    /**
     * 添加新账户
     * 将新用户名和密码添加到账户列表中，并保存到文件
     * @param username 要添加的用户名
     * @param password 对应的密码
     */
    public void addAccount(String username, String password) {
        // 将用户名和密码添加到哈希表中
        accounts.put(username, password);
        // 保存到文件
        saveAccounts();
    }

    /**
     * 检查账户是否存在
     * @param username 要检查的用户名
     * @return 如果账户存在返回true，否则返回false
     */
    public boolean accountExists(String username) {
        // 检查用户名是否存在于哈希表中
        return accounts.containsKey(username);
    }

    /**
     * 登录校验
     * 验证用户名和密码是否匹配
     * @param username 用户名
     * @param password 密码
     * @return 如果用户名和密码匹配返回true，否则返回false
     */
    public boolean checkLogin(String username, String password) {
        // 获取该用户名对应的密码并比较
        return password.equals(accounts.get(username));
    }

    /**
     * 获取存档文件路径
     * 根据用户名生成对应的存档文件路径
     * @param username 用户名
     * @return 存档文件的完整路径
     */
    private String getSaveFile(String username) {
        // 生成存档文件路径
        return DATA_DIR + "/save_" + username + ".dat";
    }

    /**
     * 检查存档是否存在
     * @param username 用户名
     * @return 如果存档文件存在返回true，否则返回false
     */
    public boolean saveExists(String username) {
        // 检查存档文件是否存在
        return new File(getSaveFile(username)).exists();
    }

    /**
     * 保存玩家存档
     * 将玩家的存档数据保存到对应用户的存档文件中
     * @param username 用户名
     * @param save 玩家存档对象
     */
    public void savePlayerSave(String username, PlayerSave save) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getSaveFile(username)))) {
            // 将存档对象写入文件
            oos.writeObject(save);
        } catch (IOException e) {
            // 输出异常信息
            e.printStackTrace();
        }
    }

    /**
     * 读取玩家存档
     * 从对应用户的存档文件中加载存档数据
     * @param username 用户名
     * @return 玩家存档对象，如果不存在或读取失败则返回null
     */
    public PlayerSave loadPlayerSave(String username) {
        File file = new File(getSaveFile(username));
        // 如果存档文件不存在，返回null
        if (!file.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            // 从文件中读取存档对象
            return (PlayerSave) ois.readObject();
        } catch (Exception e) {
            // 如果读取失败，返回null
            return null;
        }
    }
}