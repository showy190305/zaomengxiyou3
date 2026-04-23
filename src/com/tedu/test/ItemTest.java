package com.tedu.test;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.tedu.element.WuKong;
import com.tedu.element.BloodPotion;
import com.tedu.element.ManaPotion;

/**
 * 道具测试类
 * 测试血瓶和蓝瓶的拾取和使用功能
 */
public class ItemTest extends JPanel implements KeyListener {
    private WuKong wukong;
    private BloodPotion bloodPotion;
    private ManaPotion manaPotion;
    
    private Thread gameThread;
    private boolean running = true;

    public ItemTest() {
        // 初始化悟空对象
        wukong = new WuKong();
        wukong.createElement("100,300,150,150");

        // 初始化血瓶道具
        bloodPotion = new BloodPotion(300, 350);
        
        // 初始化蓝瓶道具
        manaPotion = new ManaPotion(500, 350);

        // 添加键盘监听器
        addKeyListener(this);
        setFocusable(true);
        setBackground(Color.BLACK);

        // 启动游戏循环线程
        gameThread = new Thread(new GameLoopRunnable(this));
        gameThread.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 绘制标题和操作提示
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Item Test - BloodPotion & ManaPotion", 10, 30);
        g.drawString("使用 A/D 键移动悟空，走到道具上进行拾取", 10, 50);
        g.drawString("A: 左移, D: 右移", 10, 70);
        
        // 显示悟空的HP和MP
        g.drawString("悟空 HP: " + wukong.getHp() + " / " + wukong.getMaxHp(), 10, 100);
        g.drawString("悟空 MP: " + wukong.getMp() + " / " + wukong.getMaxMp(), 10, 120);
        
        // 绘制悟空
        if (wukong.getTotalFrames() > 0) {
            wukong.showElement(g);
        } else {
            g.setColor(Color.YELLOW);
            g.fillOval(wukong.getX(), wukong.getY(), wukong.getW(), wukong.getH());
            g.setColor(Color.BLACK);
            g.drawString("WuKong", wukong.getX() + 10, wukong.getY() + 30);
        }
        
        // 绘制血瓶 (如果还存在)
        if (bloodPotion.isLive()) {
            bloodPotion.showElement(g);
            g.setColor(Color.RED);
            g.drawString("血瓶 (+30 HP)", bloodPotion.getX() - 10, bloodPotion.getY() - 10);
        } else {
            g.setColor(Color.GRAY);
            g.drawString("血瓶已拾取", bloodPotion.getX(), bloodPotion.getY() + 50);
        }
        
        // 绘制蓝瓶 (如果还存在)
        if (manaPotion.isLive()) {
            manaPotion.showElement(g);
            g.setColor(Color.BLUE);
            g.drawString("蓝瓶 (+20 MP)", manaPotion.getX() - 10, manaPotion.getY() - 10);
        } else {
            g.setColor(Color.GRAY);
            g.drawString("蓝瓶已拾取", manaPotion.getX(), manaPotion.getY() + 50);
        }
        
        // 显示道具位置
        g.setColor(Color.WHITE);
        g.drawString("血瓶位置: (" + bloodPotion.getX() + ", " + bloodPotion.getY() + ")", 10, 150);
        g.drawString("蓝瓶位置: (" + manaPotion.getX() + ", " + manaPotion.getY() + ")", 10, 170);
        g.drawString("悟空位置: (" + wukong.getX() + ", " + wukong.getY() + ")", 10, 190);
        
        // 检测碰撞并拾取道具
        checkItemPickup();
    }
    
    /**
     * 检测玩家与道具的碰撞并拾取
     */
    private void checkItemPickup() {
        // 检测血瓶
        if (bloodPotion.isLive() && wukong.pk(bloodPotion)) {
            bloodPotion.applyEffect(wukong);
            System.out.println("拾取血瓶! 恢复 " + bloodPotion.getValue() + " 点HP, 当前HP: " + wukong.getHp());
        }
        
        // 检测蓝瓶
        if (manaPotion.isLive() && wukong.pk(manaPotion)) {
            manaPotion.applyEffect(wukong);
            System.out.println("拾取蓝瓶! 恢复 " + manaPotion.getValue() + " 点MP, 当前MP: " + wukong.getMp());
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        wukong.keyClick(true, e.getKeyCode());
        repaint();
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        wukong.keyClick(false, e.getKeyCode());
        repaint();
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // 不需要实现
    }
    
    // 游戏主循环线程
    private static class GameLoopRunnable implements Runnable {
        private ItemTest panel;
        public GameLoopRunnable(ItemTest panel) {
            this.panel = panel;
        }
        @Override
        public void run() {
            final int FPS = 60;
            final int frameTime = 1000 / FPS;
            while (panel.running) {
                long start = System.currentTimeMillis();
                panel.repaint();
                long cost = System.currentTimeMillis() - start;
                long sleep = Math.max(2, frameTime - cost);
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Item Test - BloodPotion & ManaPotion");
        ItemTest testPanel = new ItemTest();
        
        frame.add(testPanel);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
