package com.tedu.controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tedu.element.ElementObj;
import com.tedu.element.InventoryPanel;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;

/**
 * @说明 监听类，用于监听用户的操作 KeyListener
 * @author renjj
 *
 */
public class GameListener implements KeyListener {
    private ElementManager em = ElementManager.getManager();
    private InventoryPanel inventoryPanel;
    
    /*能否通过一个集合来记录所有按下的键，如果重复触发，就直接结束
     * 同时，第1次按下，记录到集合中，第2次判定集合中否有。
     * 松开就直接删除集合中的记录。
     * set集合
     * */
    private Set<Integer> set = new HashSet<Integer>();
    
    public void setInventoryPanel(InventoryPanel inventoryPanel) {
        this.inventoryPanel = inventoryPanel;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    /**
     * 按下: 左37 上38 右39 下40    按tab没有反应
     * 实现主角的移动
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        // B键切换背包显示
        if (key == 'B' || key == 'b') {
            if (inventoryPanel != null) {
                inventoryPanel.toggleVisibility();
            }
            return;
        }
        
        if(set.contains(key)) {
            return;
        }
        set.add(key);
        List<ElementObj> play = em.getElementsByKey(GameElement.PLAY);
        
        // 【核心修复】：使用下标遍历，防止大圣放技能时向集合添加元素导致并发修改异常！
        for(int i = 0; i < play.size(); i++) {
            play.get(i).keyClick(true, e.getKeyCode());
        }
    }

    /**松开*/
    @Override
    public void keyReleased(KeyEvent e) {
        if(!set.contains(e.getKeyCode())) { 
            return;
        } 
        
        set.remove(e.getKeyCode()); 
        List<ElementObj> play = em.getElementsByKey(GameElement.PLAY);
        
        // 【核心修复】：使用下标遍历
        for(int i = 0; i < play.size(); i++) {
            play.get(i).keyClick(false, e.getKeyCode());
        }
    }
}