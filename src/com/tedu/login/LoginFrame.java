package com.tedu.login;

// 导入Swing组件库，用于构建图形用户界面
import javax.swing.*;
// 导入AWT包，包含基本的图形和窗口组件
import java.awt.*;
// 导入AWT事件包，处理用户交互事件
import java.awt.event.*;
// 导入HashMap类
import java.util.HashMap;
// 导入登录成功回调接口
import com.tedu.login.LoginSuccessListener;

/**
 * 登录界面类
 * 继承自JFrame，提供用户登录、注册和存档管理的图形界面
 * 包含用户名密码输入、登录验证、新游戏和继续游戏等功能
 */
public class LoginFrame extends JFrame {
    // 登录相关组件
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton loginBtn, registerBtn;

    // 主菜单相关组件
    private JButton newGameBtn, continueBtn, exitBtn;
    private JPanel menuPanel;

    // 当前登录用户
    private String currentUser = null;

    // 数据管理
    private DataManager dataManager;

    private LoginSuccessListener loginSuccessListener;

    public LoginFrame(LoginSuccessListener listener) {
        super("造梦西游3 - 登录/存档管理");
        this.loginSuccessListener = listener;

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        dataManager = new DataManager();

        // 背景面板
        setContentPane(new BgPanel());

        // 登录面板
        JPanel loginPanel = new JPanel();
        loginPanel.setOpaque(false);
        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        confirmPasswordField = new JPasswordField(15);
        
        // 设置输入框背景为白色，文字为黑色以符合新设计
        usernameField.setBackground(Color.WHITE);
        usernameField.setForeground(Color.BLACK);
        passwordField.setBackground(Color.WHITE);
        passwordField.setForeground(Color.BLACK);
        confirmPasswordField.setBackground(Color.WHITE);
        confirmPasswordField.setForeground(Color.BLACK);
        
        loginBtn = new JButton("登录");
        registerBtn = new JButton("注册");
        
        // 设置按钮样式
        loginBtn.setBackground(new Color(100, 150, 255));
        loginBtn.setForeground(Color.WHITE);
        registerBtn.setBackground(new Color(100, 150, 255));
        registerBtn.setForeground(Color.WHITE);
        
        // 用户名
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        JLabel usernameLabel = new JLabel("用户名:");
        usernameLabel.setForeground(Color.WHITE);  // 设置标签文字为白色
        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        loginPanel.add(usernameLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(usernameField, gbc);

        // 密码
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setForeground(Color.WHITE);  // 设置标签文字为白色
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        loginPanel.add(passwordLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(passwordField, gbc);

        // 确认密码（注册时显示）
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        JLabel confirmPasswordLabel = new JLabel("确认密码:");
        confirmPasswordLabel.setForeground(Color.WHITE);  // 设置标签文字为白色
        confirmPasswordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        loginPanel.add(confirmPasswordLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(confirmPasswordField, gbc);

        // 按钮 - 使用单独的GridBagConstraints来确保居中
        GridBagConstraints btnGbc = new GridBagConstraints();
        btnGbc.gridx = 0; 
        btnGbc.gridy = 3; 
        btnGbc.gridwidth = 2;
        btnGbc.anchor = GridBagConstraints.CENTER;
        btnGbc.insets = new Insets(10, 10, 10, 10); // 保持相同的边距
        
        // 创建按钮面板并设置为居中
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(loginBtn);
        btnPanel.add(registerBtn);
        loginPanel.add(btnPanel, btnGbc);

        // 主菜单面板
        menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new FlowLayout());
        newGameBtn = new JButton("新的游戏");
        continueBtn = new JButton("继续游戏");
        exitBtn = new JButton("退出游戏");
        menuPanel.add(newGameBtn);
        menuPanel.add(continueBtn);
        menuPanel.add(exitBtn);
        menuPanel.setVisible(false);

        // 布局
        setLayout(null);
        loginPanel.setBounds(250, 150, 300, 220);
        menuPanel.setBounds(250, 400, 300, 60);
        add(loginPanel);
        add(menuPanel);

        // 事件绑定
        confirmPasswordField.setVisible(false); // 默认隐藏
        // 将模式切换逻辑整合到ActionListener中以提高Java版本兼容性
        registerBtn.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                confirmPasswordField.setVisible(true);  // 显示确认密码框
                revalidate();  // 重新验证布局
                repaint();     // 重绘界面
            });
            onRegister();  // 执行注册逻辑
        });
        loginBtn.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                confirmPasswordField.setVisible(false);  // 隐藏确认密码框
                revalidate();  // 重新验证布局
                repaint();     // 重绘界面
            });
            onLogin();  // 执行登录逻辑
        });
        newGameBtn.addActionListener(e -> onNewGame());
        continueBtn.addActionListener(e -> onContinueGame());
        exitBtn.addActionListener(e -> System.exit(0));

        // 窗口关闭时保存数据
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dataManager.saveAccounts();
            }
        });


    }

    // 注册逻辑
    private void onRegister() {
        String username = usernameField.getText().trim();
        String pwd1 = new String(passwordField.getPassword());
        String pwd2 = new String(confirmPasswordField.getPassword());
        if (username.isEmpty() || pwd1.isEmpty() || pwd2.isEmpty()) {
            showMsg("请填写完整信息！");
            return;
        }
        if (!pwd1.equals(pwd2)) {
            showMsg("两次密码不一致！");
            return;
        }
        if (dataManager.accountExists(username)) {
            showMsg("用户名已存在！");
            return;
        }
        dataManager.addAccount(username, pwd1);
        showMsg("注册成功，请登录！");
        confirmPasswordField.setText("");
    }

    // 登录逻辑
    private void onLogin() {
        String username = usernameField.getText().trim();
        String pwd = new String(passwordField.getPassword());
        if (username.isEmpty() || pwd.isEmpty()) {
            showMsg("请输入用户名和密码！");
            return;
        }
        if (!dataManager.checkLogin(username, pwd)) {
            showMsg("用户名或密码错误！");
            return;
        }
        currentUser = username;
        showMsg("登录成功！");
        switchToMenu();
    }

    // 切换到主菜单
    private void switchToMenu() {
        usernameField.setVisible(false);
        passwordField.setVisible(false);
        confirmPasswordField.setVisible(false);
        loginBtn.setVisible(false);
        registerBtn.setVisible(false);
        menuPanel.setVisible(true);
    }

    // 新的游戏
    private void onNewGame() {
        if (currentUser == null) return;
        if (dataManager.saveExists(currentUser)) {
            int res = JOptionPane.showConfirmDialog(this,
                    "已有存档，是否覆盖？", "提示", JOptionPane.YES_NO_OPTION);
            if (res != JOptionPane.YES_OPTION) return;
        }
        PlayerSave save = new PlayerSave(currentUser);
        dataManager.savePlayerSave(currentUser, save);
        showMsg("新存档已创建，进入游戏！");
        enterGame(save);
    }

    // 继续游戏
    private void onContinueGame() {
        if (currentUser == null) return;
        PlayerSave save = dataManager.loadPlayerSave(currentUser);
        if (save == null) {
            showMsg("没有存档，请先开始新游戏！");
            return;
        }
        showMsg("加载存档成功，进入游戏！");
        enterGame(save);
    }

    // 进入游戏主场景（与现有框架衔接）
    private void enterGame(PlayerSave save) {
        // 登录窗口关闭
        this.dispose();
        // 回调通知GameStart
        if (loginSuccessListener != null) {
            loginSuccessListener.onLoginSuccess(save);
        }
    }

    // 消息弹窗
    private void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    // 背景面板（可自定义背景图）
    static class BgPanel extends JPanel {
        private Image bg;
        public BgPanel() {
            // 可替换为你的背景图片路径
            try {
                bg = Toolkit.getDefaultToolkit().createImage("image/login/login_bg.png");
            } catch (Exception e) {
                bg = null;
            }
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) {
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
            
            // 绘制深灰色圆角矩形区域来突出显示登录表单
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // 启用抗锯齿
            
            // 绘制深灰色圆角矩形背景
            g2d.setColor(new Color(64, 64, 64)); // 深灰色
            g2d.fillRoundRect(230, 130, 340, 260, 20, 20); // x, y, width, height, arcWidth, arcHeight
            
            // 绘制顶部黑色标题栏
            g2d.setColor(Color.BLACK);
            g2d.fillRoundRect(230, 130, 340, 30, 20, 20); // 黑色标题栏，高度30
            
            // 在黑色标题栏中绘制白色文字
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("微软雅黑", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            String text = "本游戏必须登录才能进行游戏及保存存档";
            int textWidth = fm.stringWidth(text);
            int textX = 230 + (340 - textWidth) / 2; // 水平居中
            int textY = 130 + (30 + fm.getAscent()) / 2 - 2; // 垂直居中
            g2d.drawString(text, textX, textY);
            
            g2d.dispose();
        }
    }
}