package com.tedu.show;

// 导入AWT布局管理器
import java.awt.BorderLayout;
// 导入颜色类
import java.awt.Color;
// 导入字体类
import java.awt.Font;
// 导入图形绘制基础类
import java.awt.Graphics;
// 导入高级2D图形绘制类
import java.awt.Graphics2D;
// 导入渲染提示类，用于优化图形渲染
import java.awt.RenderingHints;
// 导入矩形类，用于绘制和碰撞检测
import java.awt.Rectangle;
// 导入鼠标适配器类
import java.awt.event.MouseAdapter;
// 导入鼠标事件类
import java.awt.event.MouseEvent;

// 导入面板基础类
import javax.swing.JPanel;

import com.tedu.login.PlayerSave;

public class MainEntranceJPanel extends JPanel {
    // 开始游戏按钮的位置和尺寸常量
    private static final int START_BTN_X = 60;  // 开始游戏按钮X坐标
    private static final int START_BTN_Y = 120; // 开始游戏按钮Y坐标
    private static final int START_BTN_W = 180; // 开始游戏按钮宽度
    private static final int START_BTN_H = 50;  // 开始游戏按钮高度

    // 退出游戏按钮的位置和尺寸常量
    private static final int EXIT_BTN_X = 60;   // 退出游戏按钮X坐标
    private static final int EXIT_BTN_Y = 200;  // 退出游戏按钮Y坐标
    private static final int EXIT_BTN_W = 180;  // 退出游戏按钮宽度
    private static final int EXIT_BTN_H = 50;   // 退出游戏按钮高度

    // 游戏图标占位框的位置和尺寸常量
    private static final int ICON_X = 620;      // 图标占位框X坐标
    private static final int ICON_Y = 30;       // 图标占位框Y坐标
    private static final int ICON_W = 150;      // 图标占位框宽度
    private static final int ICON_H = 150;      // 图标占位框高度

    // 鼠标悬停状态标志
    private boolean startHover = false;         // 开始游戏按钮是否被悬停
    private boolean exitHover = false;          // 退出游戏按钮是否被悬停

    // 游戏主面板引用，用于切换到游戏界面
    private GameMainJPanel gamePanel;           // 游戏主面板对象引用

    // 玩家存档
    private PlayerSave playerSave;
    private int selectedLevel = -1; // 当前选择的关卡

    // 假设有5个关卡
    private static final int LEVEL_COUNT = 5;
    private Rectangle[] levelRects = new Rectangle[LEVEL_COUNT];

    /**
     * 构造函数，初始化游戏主入口菜单面板
     * 调用init方法进行具体的初始化操作
     */
    public MainEntranceJPanel() {
        init();
    }

    /**
     * 设置游戏主面板
     * 用于在点击开始游戏按钮时切换到游戏主界面
     * @param gamePanel 游戏主面板对象
     */
    public void setGameMainPanel(GameMainJPanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    /**
     * 设置玩家存档
     * @param save 玩家存档对象
     */
    public void setPlayerSave(PlayerSave save) {
        this.playerSave = save;
    }

    /**
     * 初始化面板组件和事件监听器
     * 设置布局为绝对定位，添加鼠标点击和移动事件监听器
     */
    private void init() {
        // 设置布局为绝对定位，允许手动设置组件位置
        setLayout(null);
        
        // 初始化关卡按钮位置
        initializeLevelRects();

        // 添加鼠标点击事件监听器，处理按钮点击事件
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 获取点击位置并处理点击事件
                handleClick(e.getX(), e.getY());
            }
        });

        // 添加鼠标移动事件监听器，处理鼠标悬停效果
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // 获取鼠标位置并处理悬停效果
                handleHover(e.getX(), e.getY());
            }
        });
    }
    
    /**
     * 初始化关卡按钮位置
     * 设置各个关卡按钮的位置和大小
     */
    private void initializeLevelRects() {
        int startX = 200; // 关卡按钮起始X坐标
        int startY = 300; // 关卡按钮起始Y坐标
        int spacing = 80; // 关卡按钮间距
        
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
        listener.setGamePanel(newGamePanel);
        frame.setKeyListener(listener);
        
        // 绑定背包面板到监听器
        listener.setInventoryPanel(newGamePanel.getInventoryPanel());
        
        com.tedu.controller.GameThread thread = new com.tedu.controller.GameThread();
        
        frame.setThead(thread);
        frame.setjPanel(newGamePanel);
        
        // 彻底清空旧画面，换上新面板
        frame.getContentPane().removeAll();
        frame.add(newGamePanel);
        frame.revalidate();
        frame.repaint();
        
        // 启动框架
        frame.start();
        
        // 强制启动游戏线程
        if (!thread.isAlive()) {
            thread.start();
        }
        
        System.out.println("🚀 游戏启动！即将进入关卡: " + levelIndex);
    }

    /**
     * 处理鼠标点击事件
     * 根据点击位置判断点击的是哪个按钮并执行相应操作
     * @param x 点击位置的X坐标
     * @param y 点击位置的Y坐标
     */
    private void handleClick(int x, int y) {
        // 1. 检查是否点击了开始游戏按钮 (默认第一关)
        if (x >= START_BTN_X && x <= START_BTN_X + START_BTN_W
                && y >= START_BTN_Y && y <= START_BTN_Y + START_BTN_H) {
            startGame(1);
            return;
        }
        
        // 2. 检查是否点击了退出游戏按钮
        if (x >= EXIT_BTN_X && x <= EXIT_BTN_X + EXIT_BTN_W
                && y >= EXIT_BTN_Y && y <= EXIT_BTN_Y + EXIT_BTN_H) {
            // 直接退出程序
            System.exit(0);
        }
        
        // 3. 检查是否点击了关卡按钮
        for (int i = 0; i < LEVEL_COUNT; i++) {
            if (levelRects[i].contains(x, y)) {
                selectedLevel = i;
                startGame(i + 1);
                return;
            }
        }
    }

    /**
     * 处理鼠标悬停事件
     * 根据鼠标位置判断是否悬停在按钮上方，并更新悬停状态
     * @param x 鼠标位置的X坐标
     * @param y 鼠标位置的Y坐标
     */
    private void handleHover(int x, int y) {
        // 检查鼠标是否悬停在开始游戏按钮上方
        boolean newStartHover = (x >= START_BTN_X && x <= START_BTN_X + START_BTN_W
                && y >= START_BTN_Y && y <= START_BTN_Y + START_BTN_H);
        // 检查鼠标是否悬停在退出游戏按钮上方
        boolean newExitHover = (x >= EXIT_BTN_X && x <= EXIT_BTN_X + EXIT_BTN_W
                && y >= EXIT_BTN_Y && y <= EXIT_BTN_Y + EXIT_BTN_H);
        // 如果悬停状态发生了变化，则更新状态并重绘界面
        if (newStartHover != startHover || newExitHover != exitHover) {
            startHover = newStartHover;  // 更新开始游戏按钮悬停状态
            exitHover = newExitHover;    // 更新退出游戏按钮悬停状态
            repaint();                   // 重绘界面以显示悬停效果
        }
    }

    /**
     * 重写绘制组件方法
     * 绘制整个面板的内容，包括背景、标题、按钮和图标框
     * @param g 图形上下文对象
     */
    @Override
    protected void paintComponent(Graphics g) {
        // 调用父类的绘制方法
        super.paintComponent(g);
        // 将普通Graphics对象转换为Graphics2D对象，支持高级绘制功能
        Graphics2D g2 = (Graphics2D) g;
        // 设置抗锯齿渲染提示，使图形边缘更平滑
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 依次绘制各个组件
        drawBackground(g2);  // 绘制背景
        drawTitle(g2);       // 绘制标题
        // 绘制开始游戏按钮，根据悬停状态显示不同效果
        drawButton(g2, START_BTN_X, START_BTN_Y, START_BTN_W, START_BTN_H, "开始游戏", startHover);
        // 绘制退出游戏按钮，根据悬停状态显示不同效果
        drawButton(g2, EXIT_BTN_X, EXIT_BTN_Y, EXIT_BTN_W, EXIT_BTN_H, "退出游戏", exitHover);
        drawIconBox(g2);     // 绘制图标占位框
        // 绘制关卡按钮
        for (int i = 0; i < LEVEL_COUNT; i++) {
            Rectangle rect = levelRects[i];
            g2.setColor(selectedLevel == i ? Color.ORANGE : Color.LIGHT_GRAY);
            g2.fillOval(rect.x, rect.y, rect.width, rect.height);
            g2.setColor(Color.BLACK);
            g2.drawOval(rect.x, rect.y, rect.width, rect.height);
            g2.drawString("关卡" + (i + 1), rect.x + 10, rect.y + 35);
        }
    }

    /**
     * 绘制背景
     * 绘制面板的背景色和占位文本
     * @param g2 2D图形上下文对象
     */
    private void drawBackground(Graphics2D g2) {
        // 获取面板的宽度和高度
        int w = getWidth();
        int h = getHeight();
        // 设置背景颜色并填充整个面板
        g2.setColor(new Color(20, 20, 40));  // 深蓝色背景
        g2.fillRect(0, 0, w, h);
        // 绘制背景占位文本
        g2.setColor(new Color(80, 80, 120));  // 灰蓝色文本
        g2.setFont(new Font("SansSerif", Font.ITALIC, 16));  // 设置字体
        String text = "[ 背景图片占位区域 ]";  // 占位文本
        // 计算文本的宽度以便居中显示
        int tw = g2.getFontMetrics().stringWidth(text);
        // 在面板中央绘制占位文本
        g2.drawString(text, (w - tw) / 2, h / 2);
    }

    /**
     * 绘制标题
     * 绘制游戏标题文字
     * @param g2 2D图形上下文对象
     */
    private void drawTitle(Graphics2D g2) {
        // 设置标题颜色
        g2.setColor(new Color(255, 220, 100));  // 金黄色
        // 设置标题字体
        g2.setFont(new Font("SansSerif", Font.BOLD, 36));  // 粗体，36号字
        String title = "造梦西游";  // 标题文本
        // 计算标题文本的宽度以便水平居中
        int tw = g2.getFontMetrics().stringWidth(title);
        // 在指定位置绘制标题，水平居中
        g2.drawString(title, (GameJFrame.GameX - tw) / 2, 70);
    }

    /**
     * 绘制按钮
     * 根据参数绘制指定样式的按钮
     * @param g2 2D图形上下文对象
     * @param x 按钮左上角X坐标
     * @param y 按钮左上角Y坐标
     * @param w 按钮宽度
     * @param h 按钮高度
     * @param text 按钮上的文本
     * @param hover 按钮是否处于悬停状态
     */
    private void drawButton(Graphics2D g2, int x, int y, int w, int h, String text, boolean hover) {
        // 根据悬停状态设置按钮填充颜色
        g2.setColor(hover ? new Color(80, 160, 255) : new Color(50, 100, 180));  // 悬停时亮蓝，正常时暗蓝
        // 绘制圆角矩形按钮背景
        g2.fillRoundRect(x, y, w, h, 10, 10);
        // 绘制按钮边框
        g2.setColor(new Color(100, 180, 255));  // 边框颜色
        g2.drawRoundRect(x, y, w, h, 10, 10);
        // 设置文本颜色和字体
        g2.setColor(Color.WHITE);  // 白色文本
        g2.setFont(new Font("SansSerif", Font.BOLD, 20));  // 粗体，20号字
        // 计算文本宽度以便居中显示
        int tw = g2.getFontMetrics().stringWidth(text);
        // 计算文本绘制位置，使其在按钮内居中
        int tx = x + (w - tw) / 2;  // 水平居中
        int ty = y + (h + g2.getFontMetrics().getHeight()) / 2 - 5;  // 垂直居中
        // 绘制按钮文本
        g2.drawString(text, tx, ty);
    }

    /**
     * 绘制图标占位框
     * 绘制游戏图标占位框
     * @param g2 2D图形上下文对象
     */
    private void drawIconBox(Graphics2D g2) {
        // 设置图标框背景颜色
        g2.setColor(new Color(40, 40, 70));  // 深紫色背景
        // 绘制圆角矩形图标框
        g2.fillRoundRect(ICON_X, ICON_Y, ICON_W, ICON_H, 8, 8);
        // 设置边框颜色
        g2.setColor(new Color(100, 100, 150));  // 浅紫色边框
        // 绘制边框
        g2.drawRoundRect(ICON_X, ICON_Y, ICON_W, ICON_H, 8, 8);
        // 设置图标框内文本颜色和字体
        g2.setColor(new Color(120, 120, 160));  // 灰紫色文本
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));  // 平常字体，14号字
        String text = "[ 游戏图标 ]";  // 占位文本
        // 计算文本宽度以便居中显示
        int tw = g2.getFontMetrics().stringWidth(text);
        // 在图标框内居中绘制占位文本
        g2.drawString(text, ICON_X + (ICON_W - tw) / 2, ICON_Y + ICON_H / 2);
    }
}
