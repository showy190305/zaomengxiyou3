package com.tedu.game;

import com.tedu.controller.GameListener;
import com.tedu.controller.GameThread;
import com.tedu.show.GameJFrame;
import com.tedu.show.GameMainJPanel;
import com.tedu.show.MainEntranceJPanel;
import com.tedu.login.LoginFrame;
import com.tedu.login.PlayerSave;
import com.tedu.login.LoginSuccessListener;

import javax.swing.*;

public class GameStart {
	/**
	 * 程序的唯一入口
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
            // 弹出登录窗口，登录成功后回调
            LoginFrame loginFrame = new LoginFrame((PlayerSave save) -> {
                // 登录成功后，显示游戏主菜单面板
                GameJFrame gj = new GameJFrame();
                MainEntranceJPanel menuPanel = new MainEntranceJPanel();
                menuPanel.setPlayerSave(save); // 传递存档
                
                // 设置游戏主面板供后续切换使用
                GameMainJPanel gameMainPanel = new GameMainJPanel();
                menuPanel.setGameMainPanel(gameMainPanel);
                
                // 只设置面板，不设置监听器和线程，直到用户选择开始游戏
                gj.setjPanel(menuPanel);
                gj.add(menuPanel); // 直接添加菜单面板
                gj.setVisible(true); // 只显示界面，不启动游戏线程
            });
            loginFrame.setVisible(true); // 构造方法已设置可见，无需重复
        });
	}
}

/**
 * 1.分析游戏，设计游戏的 配置文件格式，文件读取格式（load格式）
 * 2.设计游戏角色，分析游戏需求(抽象基于基类的继承)
 * 3.开发pojo类(Vo)....
 * 4.需要的方法就在父类中重写(如果父类不支持，可以采用修改父类)
 * 5.检查配置，完成对象的 load和add到Manage.
 * 6.碰撞等等细节代码。
 * 
 * web网页游戏
 */