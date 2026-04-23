package com.tedu.login;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton loginBtn;
    private JButton registerBtn;

    private JButton newGameBtn;
    private JButton continueBtn;
    private JButton exitBtn;

    private JPanel loginPanel;
    private JPanel menuPanel;
    private JLabel confirmPasswordLabel;
    private boolean menuMode = false;
    private boolean registerMode = false;

    private String currentUser = null;
    private final DataManager dataManager;
    private final LoginSuccessListener loginSuccessListener;

    public LoginFrame(LoginSuccessListener listener) {
        super("造梦西游3 - 登录/存档管理");
        this.loginSuccessListener = listener;
        this.dataManager = new DataManager();

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setContentPane(new BgPanel());
        setLayout(null);

        initLoginPanel();
        initMenuPanel();

        add(loginPanel);
        add(menuPanel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dataManager.saveAccounts();
            }
        });
    }

    private void initLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setOpaque(false);
        loginPanel.setBounds(250, 155, 320, 210);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.EAST;

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        confirmPasswordField = new JPasswordField(15);

        styleInput(usernameField);
        styleInput(passwordField);
        styleInput(confirmPasswordField);

        loginBtn = new JButton("登录");
        registerBtn = new JButton("注册");
        stylePrimaryButton(loginBtn);
        stylePrimaryButton(registerBtn);

        JLabel usernameLabel = createFormLabel("用户名:");
        JLabel passwordLabel = createFormLabel("密码:");
        confirmPasswordLabel = createFormLabel("确认密码:");

        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        loginPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        loginPanel.add(confirmPasswordLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(confirmPasswordField, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(loginBtn);
        btnPanel.add(registerBtn);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(btnPanel, gbc);

        confirmPasswordField.setVisible(false);
        confirmPasswordLabel.setVisible(false);

        registerBtn.addActionListener(e -> {
            if (!registerMode) {
                switchToRegisterMode();
            } else {
                onRegister();
            }
        });

        loginBtn.addActionListener(e -> {
            if (registerMode) {
                switchToLoginMode();
                return;
            }
            onLogin();
        });
    }

    private void initMenuPanel() {
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBounds(610, 205, 145, 250);
        menuPanel.setVisible(false);

        newGameBtn = createMenuButton("新的游戏");
        continueBtn = createMenuButton("继续游戏");
        exitBtn = createMenuButton("退出游戏");

        newGameBtn.addActionListener(e -> onNewGame());
        continueBtn.addActionListener(e -> onContinueGame());
        exitBtn.addActionListener(e -> System.exit(0));

        menuPanel.add(Box.createVerticalStrut(26));
        menuPanel.add(newGameBtn);
        menuPanel.add(Box.createVerticalStrut(18));
        menuPanel.add(continueBtn);
        menuPanel.add(Box.createVerticalStrut(18));
        menuPanel.add(exitBtn);
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        return label;
    }

    private void styleInput(JTextField field) {
        Dimension inputSize = new Dimension(180, 34);
        field.setPreferredSize(inputSize);
        field.setMinimumSize(inputSize);
        field.setMaximumSize(inputSize);
        field.setBackground(new Color(255, 255, 255, 235));
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(110, 137, 187)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
    }

    private void switchToRegisterMode() {
        registerMode = true;
        confirmPasswordField.setVisible(true);
        confirmPasswordLabel.setVisible(true);
        registerBtn.setText("确认注册");
        loginBtn.setText("返回登录");
        loginPanel.revalidate();
        loginPanel.repaint();
    }

    private void switchToLoginMode() {
        registerMode = false;
        confirmPasswordField.setVisible(false);
        confirmPasswordLabel.setVisible(false);
        confirmPasswordField.setText("");
        registerBtn.setText("注册");
        loginBtn.setText("登录");
        loginPanel.revalidate();
        loginPanel.repaint();
    }

    private void stylePrimaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        button.setBackground(new Color(79, 129, 219));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(CENTER_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setMaximumSize(new Dimension(120, 34));
        button.setPreferredSize(new Dimension(120, 34));
        button.setMinimumSize(new Dimension(120, 34));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("微软雅黑", Font.BOLD, 21));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

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
        switchToLoginMode();
    }

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

    private void switchToMenu() {
        menuMode = true;
        remove(loginPanel);
        loginPanel = null;
        menuPanel.setVisible(true);
        repaintBackgroundWindow();
        revalidate();
        repaint();
    }

    private void repaintBackgroundWindow() {
        Window window = SwingUtilities.getWindowAncestor(menuPanel);
        if (window != null) {
            window.repaint();
        }
    }

    private void onNewGame() {
        if (currentUser == null) {
            return;
        }

        if (dataManager.saveExists(currentUser)) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "已有存档，是否覆盖？",
                    "提示",
                    JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }

        PlayerSave save = new PlayerSave(currentUser);
        dataManager.savePlayerSave(currentUser, save);
        showMsg("新存档已创建，进入游戏！");
        enterGame(save);
    }

    private void onContinueGame() {
        if (currentUser == null) {
            return;
        }

        PlayerSave save = dataManager.loadPlayerSave(currentUser);
        if (save == null) {
            showMsg("没有存档，请先开始新的游戏！");
            return;
        }

        showMsg("读取存档成功，进入游戏！");
        enterGame(save);
    }

    private void enterGame(PlayerSave save) {
        com.tedu.manager.GameLoad.currentSave = save;
        dispose();
        if (loginSuccessListener != null) {
            loginSuccessListener.onLoginSuccess(save);
        }
    }

    private void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    private class BgPanel extends JPanel {
        private final Image bg;

        BgPanel() {
            Image image;
            try {
                image = Toolkit.getDefaultToolkit().createImage("image/login/login_bg.png");
            } catch (Exception e) {
                image = null;
            }
            this.bg = image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (bg != null) {
                g2.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            } else {
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            if (!menuMode) {
                drawLoginBoard(g2);
            } else {
                drawMenuBoard(g2);
            }

            g2.dispose();
        }

        private void drawLoginBoard(Graphics2D g2) {
            g2.setColor(new Color(52, 58, 72, 215));
            g2.fillRoundRect(230, 130, 340, 260, 24, 24);

            g2.setColor(new Color(14, 14, 14, 235));
            g2.fillRoundRect(230, 130, 340, 34, 24, 24);
            g2.fillRect(230, 148, 340, 16);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("微软雅黑", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            String tip = "本游戏必须登录后才能进行游戏及保存存档";
            int textX = 230 + (340 - fm.stringWidth(tip)) / 2;
            int textY = 130 + (34 + fm.getAscent()) / 2 - 2;
            g2.drawString(tip, textX, textY);
        }

        private void drawMenuBoard(Graphics2D g2) {
            g2.setColor(new Color(0, 0, 0, 210));
            g2.fillRoundRect(595, 130, 170, 360, 16, 16);
            g2.setColor(new Color(255, 255, 255, 45));
            g2.drawRoundRect(595, 130, 170, 360, 16, 16);
        }
    }
}
