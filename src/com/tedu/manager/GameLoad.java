package com.tedu.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.ImageIcon;

import com.tedu.element.ElementObj;
import com.tedu.element.WuKong;

/**
 * 游戏资源加载器工具类
 * 提供静态方法用于加载地图、图片、玩家等游戏资源
 */
public class GameLoad {
    // 【防报错临时注释】：等待组员完成 PlayerSave 类后解开
    
    public static com.tedu.login.PlayerSave currentSave = null;
    
    
    private static ElementManager em = ElementManager.getManager();
    public static Map<String,ImageIcon> imgMap = new HashMap<>();
    public static Map<String,List<ImageIcon>> imgMaps;
    private static Properties pro = new Properties();    

    // ================== 你的新地图逻辑 ==================
    /**
     * 加载全景卷轴地图 (替代原来的文本网格地图)
     */
    public static void loadMap() {
        // 实例化你写好的滚动背景类
        ElementObj map = new com.tedu.element.MapObj();
        // 放进 MAPS 集合中
        em.addElement(map, GameElement.MAPS);
        System.out.println("横轴全景地图加载完毕！");
    }
    // ====================================================

    // 老的网格地图加载法 (保留给你队友留底，不用管它)
    public static void MapLoad(int mapId) {
        String mapName = "com/tedu/text/" + mapId + ".map";
        ClassLoader classLoader = GameLoad.class.getClassLoader();
        InputStream maps = classLoader.getResourceAsStream(mapName);
        if (maps == null) {
            System.out.println("配置文件读取异常,请重新安装");
            return;
        }
        try {
            pro.clear();
            pro.load(maps);
            Enumeration<?> names = pro.propertyNames();
            while (names.hasMoreElements()) {
                String key = names.nextElement().toString();
                String[] arrs = pro.getProperty(key).split(";");
                for (int i = 0; i < arrs.length; i++) {
                    ElementObj obj = getObj("map");  
                    ElementObj element = obj.createElement(key + "," + arrs[i]);
                    em.addElement(element, GameElement.MAPS);
                }
            }   
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadImg() {
        String texturl = "com/tedu/text/GameData.pro";
        ClassLoader classLoader = GameLoad.class.getClassLoader();
        InputStream texts = classLoader.getResourceAsStream(texturl);
        
        // 防空指针保护
        if (texts == null) {
            System.out.println("未找到图片配置文件: " + texturl);
            return;
        }
        
        pro.clear();
        try {
            pro.load(texts);
            Set<Object> set = pro.keySet();
            for (Object o : set) {
                String url = pro.getProperty(o.toString());
                imgMap.put(o.toString(), new ImageIcon(url));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载玩家、武器与怪物
     */
    public static void loadPlay() {
        // 先加载对象映射，防止反射失败
        loadObj();

        // 1. 加载大圣
        ElementObj wukong = new com.tedu.element.WuKong();
        wukong.createElement("100,400,150,150"); 
        
        // 【防报错临时注释】：等待组员完成 PlayerSave 类后解开
        
        if (currentSave != null) {
            ((com.tedu.element.WuKong) wukong).loadFromSave(currentSave);
        }
        
        
        em.addElement(wukong, GameElement.PLAY);

        // 确保状态被重置为满血满蓝
        java.util.List<com.tedu.element.ElementObj> list = 
            com.tedu.manager.ElementManager.getManager().getElementsByKey(com.tedu.manager.GameElement.PLAY);
            
        for (com.tedu.element.ElementObj obj : list) {
            if (obj instanceof com.tedu.element.WuKong) {
                ((com.tedu.element.WuKong) obj).resetStatus();
            }
        }

        // 2. 加载武器
        ElementObj weapon = new com.tedu.element.Weapon();
        weapon.createElement("100,400,150,150");
        em.addElement(weapon, GameElement.PLAY);

        // ==========================================================
        // 3. 【优化】：关卡怪物配置表 (X坐标, 怪物数量)
        // 你可以在这里自由设计关卡节奏！
        // ==========================================================
        int[][] spawnConfig = {
            {800,  2},  // 第一波：X=800处，2只怪（新手热身）
            {2000, 3},  // 第二波：X=2000处，3只怪
            {3800, 2},  // 第三波：X=3800处，2只怪
            {5500, 4},  // 第四波：X=5500处，4只怪（小高潮）
            {7500, 5}   // 第五波：X=7500处，5只怪（关底大决战）
        };

        for (int i = 0; i < spawnConfig.length; i++) {
            int spawnX = spawnConfig[i][0];
            int count = spawnConfig[i][1];
            
            for (int j = 0; j < count; j++) {
                ElementObj enemy = new com.tedu.element.enemy2();
                // 给同批次的怪物加上偏移量 (j * 80)，防止它们完美重叠在一起
                int finalX = spawnX + (j * 80);
                enemy.createElement(finalX + ",400,150,150");
                em.addElement(enemy, GameElement.ENEMY);
            }
        }
        
        System.out.println("关卡怪物已按配置表部署完毕！");
    }
    
    public static ElementObj getObj(String str) {
        try {
            Class<?> class1 = objMap.get(str);
            if (class1 == null) {
                System.out.println("无法通过反射找到类: " + str);
                return null;
            }
            Object newInstance = class1.newInstance();
            if (newInstance instanceof ElementObj) {
                return (ElementObj) newInstance;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static Map<String,Class<?>> objMap = new HashMap<>();
    
    public static void loadObj() {
        String texturl = "com/tedu/text/obj.pro";
        ClassLoader classLoader = GameLoad.class.getClassLoader();
        InputStream texts = classLoader.getResourceAsStream(texturl);
        if (texts == null) {
            System.out.println("未找到对象映射配置文件: " + texturl);
            return;
        }
        pro.clear();
        try {
            pro.load(texts);
            Set<Object> set = pro.keySet();
            for (Object o : set) {
                String classUrl = pro.getProperty(o.toString());
                Class<?> forName = Class.forName(classUrl);
                objMap.put(o.toString(), forName);
            }
        } catch (Exception e) {
            System.out.println("加载 obj.pro 反射映射失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {}
}