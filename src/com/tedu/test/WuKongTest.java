// package com.tedu.test;

// import java.awt.Color;
// import java.awt.Font;
// import java.awt.Graphics;
// import java.awt.event.KeyEvent;
// import java.awt.event.KeyListener;
// import java.lang.reflect.Field;

// import javax.swing.JFrame;
// import javax.swing.JPanel;

// import com.tedu.element.WuKong;
// import com.tedu.element.Weapon;

// public class WuKongTest extends JPanel implements KeyListener {
//     private WuKong wukong;
//     private Weapon weapon;
    
//     private Thread gameThread;
//     private boolean running = true;

//     public WuKongTest() {
//         // 初始化悟空对象
//         wukong = new WuKong();
//         wukong.createElement("100,200,150,150"); // x, y, width, height

//         // 初始化武器对象，初始坐标与悟空一致
//         weapon = new Weapon();
//         weapon.createElement("100,200,150,150");

//         // 添加键盘监听器
//         addKeyListener(this);
//         setFocusable(true);
//         setBackground(Color.BLACK); // 设置背景为黑色

//         // 启动刷新线程，优化卡顿
//         gameThread = new Thread(new GameLoopRunnable(this));
//         gameThread.start();
//     }
    
//     @Override
//     protected void paintComponent(Graphics g) {
//         // 绘制背景信息
//         g.setColor(Color.WHITE);
//         g.setFont(new Font("Arial", Font.PLAIN, 16));
//         g.drawString("WuKong & Weapon Test - 使用 A/D 键移动，J 键攻击", 10, 30);
//         g.drawString("A: 左移, D: 右移, J: 攻击", 10, 50);
//         g.drawString("双击A/D可跑步", 10, 70);

//         // 显示悟空当前动作
//         try {
//             Field field = WuKong.class.getDeclaredField("currentAction");
//             field.setAccessible(true);
//             String currentAction = (String) field.get(wukong);
//             g.drawString("悟空当
//         super.paintComponent(g);
//         前动作: " + currentAction, 10, 90);
//         } catch (Exception e) {
//             g.drawString("悟空当前动作: unknown", 10, 90);
//         }
        
//         // 显示武器当前动作
//         try {
//             Field field = Weapon.class.getDeclaredField("currentAction");
//             field.setAccessible(true);
//             String currentAction = (String) field.get(weapon);
//             g.drawString("武器当前动作: " + currentAction, 10, 110);
//         } catch (Exception e) {
//             g.drawString("武器当前动作: unknown", 10, 110);
//         }
        
//         // 绘制悟空
//         if (wukong.getTotalFrames() > 0) {
//             wukong.showElement(g);
//         } else {
//             // 如果没有加载到悟空图片，绘制一个占位符
//             g.setColor(Color.YELLOW);
//             g.fillOval(wukong.getX(), wukong.getY(), wukong.getW(), wukong.getH());
//             g.setColor(Color.BLACK);
//             g.drawString("WuKong", wukong.getX() + 10, wukong.getY() + 30);
//         }
//         // 武器坐标始终与悟空一致
//         weapon.setX(wukong.getX());
//         weapon.setY(wukong.getY());
//         if (weapon.getTotalFrames() > 0) {
//             weapon.showElement(g);
//         } else {
//             // 如果没有加载到武器图片，绘制一个占位符
//             g.setColor(Color.BLUE);
//             g.fillOval(weapon.getX(), weapon.getY(), weapon.getW(), weapon.getH());
//             g.setColor(Color.WHITE);
//             g.drawString("Weapon", weapon.getX() + 10, weapon.getY() + 30);
//         }
//         // 绘制悟空位置信息
//         g.drawString("悟空位置: (" + wukong.getX() + ", " + wukong.getY() + ")", 10, 130);
//         // 绘制武器位置信息
//         g.drawString("武器位置: (" + weapon.getX() + ", " + weapon.getY() + ")", 10, 150);
//     }
    
//     @Override
//     public void keyPressed(KeyEvent e) {
//         // 将按键事件传递给悟空和武器
//         wukong.keyClick(true, e.getKeyCode());
//         weapon.keyClick(true, e.getKeyCode());
//         repaint(); // 重绘界面
//     }
    
//     @Override
//     public void keyReleased(KeyEvent e) {
//         // 将按键事件传递给悟空和武器
//         wukong.keyClick(false, e.getKeyCode());
//         weapon.keyClick(false, e.getKeyCode());
//         repaint(); // 重绘界面
//     }
    
//     @Override
//     public void keyTyped(KeyEvent e) {
//         // 不需要实现
//     }
    
//     // 游戏主循环线程
//     private static class GameLoopRunnable implements Runnable {
//         private WuKongTest panel;
//         public GameLoopRunnable(WuKongTest panel) {
//             this.panel = panel;
//         }
//         @Override
//         public void run() {
//             final int FPS = 60;
//             final int frameTime = 1000 / FPS;
//             while (panel.running) {
//                 long start = System.currentTimeMillis();
//                 panel.repaint();
//                 long cost = System.currentTimeMillis() - start;
//                 long sleep = Math.max(2, frameTime - cost);
//                 try {
//                     Thread.sleep(sleep);
//                 } catch (InterruptedException e) {
//                     e.printStackTrace();
//                 }
//             }
//         }
//     }
    
//     public static void main(String[] args) {
//         JFrame frame = new JFrame("WuKong & Weapon Test");
//         WuKongTest testPanel = new WuKongTest();
        
//         frame.add(testPanel);
//         frame.setSize(800, 600);
//         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//         frame.setVisible(true);
//     }
// }