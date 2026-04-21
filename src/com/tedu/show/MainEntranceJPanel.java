package com.tedu.show;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import com.tedu.login.PlayerSave;

/**
 * 游戏主入口菜单面板
 * @author L1r0gn
 */
public class MainEntranceJPanel extends JPanel {
    // 开始游戏按钮
    private static final int START_BTN_X = 60;  
    private static final int START_BTN_Y = 120; 
    private static final int START_BTN_W = 180; 
    private static final int START_BTN_H = 50;  

    // 退出游戏按钮
    private static final int EXIT_BTN_X = 60;   
    private static final int EXIT_BTN_Y = 200;  
    private static final int EXIT_BTN_W = 180;  
    private static final int EXIT_BTN_H = 50;   

    // 游戏图标占位
    private static final int ICON_X = 620;      
    private static final int ICON_Y = 30;       
    private static final int ICON_W = 150;      
    private static final int ICON_H = 150;      

    private boolean startHover = false;         
    private boolean exitHover = false;          

    private GameMainJPanel gamePanel;           
    private PlayerSave playerSave;
    private int selectedLevel = -1; 

    private static final int LEVEL_COUNT = 5;
    private Rectangle[] levelRects = new Rectangle[LEVEL_COUNT];

    public MainEntranceJPanel() {
        init();
    }

    public void setGameMainPanel(GameMainJPanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void setPlayerSave(PlayerSave save) {
        this.playerSave = save;
    }

    private void init() {
        setLayout(null);
        initializeLevelRects();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleHover(e.getX(), e.getY());
            }
        });
    }
    
    private void initializeLevelRects() {
        int startX = 200; 
        int startY = 300; 
        int spacing = 80; 
        for (int i = 0; i < LEVEL_COUNT; i++) {
            levelRects[i] = new Rectangle(startX + i * spacing, startY, 50, 50);
        }
    }

    // ==============================================================
    // 【核心重构】：把重复的启动代码抽取成一个统一的方法
    // ==============================================================
    private void startGame(int levelIndex) {
        if (gamePanel == null) return;
        GameJFrame frame = (GameJFrame) getTopLevelAncestor();
        
        GameMainJPanel newGamePanel = new GameMainJPanel();
        com.tedu.controller.GameListener listener = new com.tedu.controller.GameListener();
        frame.setKeyListener(listener);
        frame.addKeyListener(listener);
        
        com.tedu.controller.GameThread thread = new com.tedu.controller.GameThread();
        
        // ==============================================================
        // 【未来关卡扩充预留口】：
        // 以后你可以在 GameLoad 里面加一个 public static int currentLevel = 1;
        // 然后在这里写上：com.tedu.manager.GameLoad.currentLevel = levelIndex;
        // 这样底层的加载器就知道该刷什么怪、加载什么地图了！
        // ==============================================================
        
        frame.setThead(thread);
        frame.setjPanel(newGamePanel);
        
        // 彻底清空旧画面，换上新面板
        frame.getContentPane().removeAll();
        frame.add(newGamePanel);
        frame.revalidate();
        frame.repaint();
        
        // 启动框架
        frame.start();
        
        // 【双保险防泡泡堂】：强制启动我们的大圣专属线程！
        if (!thread.isAlive()) {
            thread.start();
        }
        
        System.out.println("🚀 游戏启动！即将进入关卡: " + levelIndex);
    }

    // ==============================================================
    // 【代码瞬间清爽】：点击事件现在的逻辑极其清晰
    // ==============================================================
    private void handleClick(int x, int y) {
        // 1. 点击【开始游戏】（默认进第 1 关）
        if (x >= START_BTN_X && x <= START_BTN_X + START_BTN_W
                && y >= START_BTN_Y && y <= START_BTN_Y + START_BTN_H) {
            startGame(1); 
            return;
        }
        
        // 2. 点击【退出游戏】
        if (x >= EXIT_BTN_X && x <= EXIT_BTN_X + EXIT_BTN_W
                && y >= EXIT_BTN_Y && y <= EXIT_BTN_Y + EXIT_BTN_H) {
            System.exit(0);
        }
        
        // 3. 点击【关卡 1~5】
        for (int i = 0; i < LEVEL_COUNT; i++) {
            if (levelRects[i].contains(x, y)) {
                selectedLevel = i;
                // 传入具体的关卡号 (i + 1)
                startGame(i + 1); 
                return;
            }
        }
    }

    private void handleHover(int x, int y) {
        boolean newStartHover = (x >= START_BTN_X && x <= START_BTN_X + START_BTN_W
                && y >= START_BTN_Y && y <= START_BTN_Y + START_BTN_H);
        boolean newExitHover = (x >= EXIT_BTN_X && x <= EXIT_BTN_X + EXIT_BTN_W
                && y >= EXIT_BTN_Y && y <= EXIT_BTN_Y + EXIT_BTN_H);
        if (newStartHover != startHover || newExitHover != exitHover) {
            startHover = newStartHover;  
            exitHover = newExitHover;    
            repaint();                   
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2);  
        drawTitle(g2);       
        drawButton(g2, START_BTN_X, START_BTN_Y, START_BTN_W, START_BTN_H, "开始游戏", startHover);
        drawButton(g2, EXIT_BTN_X, EXIT_BTN_Y, EXIT_BTN_W, EXIT_BTN_H, "退出游戏", exitHover);
        drawIconBox(g2);     
        
        for (int i = 0; i < LEVEL_COUNT; i++) {
            Rectangle rect = levelRects[i];
            g2.setColor(selectedLevel == i ? Color.ORANGE : Color.LIGHT_GRAY);
            g2.fillOval(rect.x, rect.y, rect.width, rect.height);
            g2.setColor(Color.BLACK);
            g2.drawOval(rect.x, rect.y, rect.width, rect.height);
            g2.drawString("关卡" + (i + 1), rect.x + 10, rect.y + 35);
        }
    }

    private void drawBackground(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();
        g2.setColor(new Color(20, 20, 40));  
        g2.fillRect(0, 0, w, h);
        g2.setColor(new Color(80, 80, 120));  
        g2.setFont(new Font("SansSerif", Font.ITALIC, 16));  
        String text = "[ 背景图片占位区域 ]";  
        int tw = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (w - tw) / 2, h / 2);
    }

    private void drawTitle(Graphics2D g2) {
        g2.setColor(new Color(255, 220, 100));  
        g2.setFont(new Font("SansSerif", Font.BOLD, 36));  
        String title = "造梦西游";  
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (GameJFrame.GameX - tw) / 2, 70);
    }

    private void drawButton(Graphics2D g2, int x, int y, int w, int h, String text, boolean hover) {
        g2.setColor(hover ? new Color(80, 160, 255) : new Color(50, 100, 180));  
        g2.fillRoundRect(x, y, w, h, 10, 10);
        g2.setColor(new Color(100, 180, 255));  
        g2.drawRoundRect(x, y, w, h, 10, 10);
        g2.setColor(Color.WHITE);  
        g2.setFont(new Font("SansSerif", Font.BOLD, 20));  
        int tw = g2.getFontMetrics().stringWidth(text);
        int tx = x + (w - tw) / 2;  
        int ty = y + (h + g2.getFontMetrics().getHeight()) / 2 - 5;  
        g2.drawString(text, tx, ty);
    }

    private void drawIconBox(Graphics2D g2) {
        g2.setColor(new Color(40, 40, 70));  
        g2.fillRoundRect(ICON_X, ICON_Y, ICON_W, ICON_H, 8, 8);
        g2.setColor(new Color(100, 100, 150));  
        g2.drawRoundRect(ICON_X, ICON_Y, ICON_W, ICON_H, 8, 8);
        g2.setColor(new Color(120, 120, 160));  
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));  
        String text = "[ 游戏图标 ]";  
        int tw = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, ICON_X + (ICON_W - tw) / 2, ICON_Y + ICON_H / 2);
    }
}