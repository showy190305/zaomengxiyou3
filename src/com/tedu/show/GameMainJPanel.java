package com.tedu.show;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.tedu.element.ElementObj;
import com.tedu.element.Inventory;
import com.tedu.element.InventoryPanel;
import com.tedu.element.PickupDetector;
import com.tedu.element.WuKong;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;

/**
 * @说明 游戏的主要面板
 * @author renjj
 * @功能说明 主要进行元素的显示，同时进行界面的刷新(多线程)
 * * @题外话 java开发实现思考的应该是：做继承或者是接口实现
 * * @多线程刷新 1.本类实现线程接口
 * 2.本类中定义一个内部类来实现
 */
public class GameMainJPanel extends JPanel implements Runnable{
//  联动管理器
    private ElementManager em;
    
    // 背包相关
    private InventoryPanel inventoryPanel;
    private PickupDetector pickupDetector;
    
    public GameMainJPanel() {
        init();
    }

    public void init() {
        em = ElementManager.getManager();//得到元素管理器对象
        
        // 初始化背包系统
        pickupDetector = new PickupDetector();
        
        // 添加鼠标监听，转发点击到背包面板
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (inventoryPanel != null && inventoryPanel.isVisible()) {
                    inventoryPanel.handleClick(e.getX(), e.getY());
                    repaint();
                }
            }
        });
    }
    
    /**
     * 获取背包面板，供 GameListener 绑定
     */
    public InventoryPanel getInventoryPanel() {
        if (inventoryPanel == null) {
            // 延迟初始化，需要玩家对象就绪后才创建
            WuKong player = getPlayer();
            if (player != null) {
                inventoryPanel = new InventoryPanel(player);
                // 设置面板大小与游戏面板一致
                inventoryPanel.setSize(getWidth(), getHeight());
            }
        }
        return inventoryPanel;
    }
    
    /**
     * 获取当前玩家对象
     */
    private WuKong getPlayer() {
        List<ElementObj> players = em.getElementsByKey(GameElement.PLAY);
        if (players != null && !players.isEmpty()) {
            ElementObj obj = players.get(0);
            if (obj instanceof WuKong) {
                return (WuKong) obj;
            }
        }
        return null;
    }
    
    /**
     * 执行拾取检测（由 GameThread 调用）
     */
    public void updatePickup() {
        WuKong player = getPlayer();
        if (player != null && pickupDetector != null) {
            pickupDetector.detectAndPickup(player);
        }
    }

    /**
     * paint方法是进行绘画元素。
     * 绘画时是有固定的顺序，先绘画的图片会在底层，后绘画的图片会覆盖先绘画的
     * 约定：本方法只执行一次,想实时刷新需要使用 多线程
     */
    @Override  //用于绘画的    Graphics 画笔 专门用于绘画的
    public void paint(Graphics g) {
        super.paint(g);  //调用父类的paint方法
        Map<GameElement, List<ElementObj>> all = em.getGameElements();
        for(GameElement ge:GameElement.values()) {
            List<ElementObj> list = all.get(ge);
            for(int i=0;i<list.size();i++) {
                ElementObj obj=list.get(i);//读取为基类
                obj.showElement(g);//调用每个类的自己的show方法完成自己的显示
            }
        }
        
        // 绘制背包面板（覆盖层）
        if (inventoryPanel != null && inventoryPanel.isVisible()) {
            inventoryPanel.paintComponent(g);
        }
    }
    
    @Override
    public void run() {  //接口实现
        while(true) {
            this.repaint();
//          一般情况下，多线程都会使用一个休眠,控制速度
            try {
                Thread.sleep(10); //休眠10毫秒 1秒刷新100次
            } catch (InterruptedException e) {
                e.printStackTrace();
            }       
        }
    }
}