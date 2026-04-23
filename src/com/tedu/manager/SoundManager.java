package com.tedu.manager;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

/**
 * 极简全能音频管理器
 * 负责背景音乐的循环播放和短音效的并发播放
 */
public class SoundManager {
    
    // 专门用来保存 BGM 的 Clip，方便随时暂停或切换
    private static Clip bgmClip;

    /**
     * 🎵 播放背景音乐 (无限循环)
     * @param filePath 音频文件路径，例如 "audio/bgm.wav"
     */
    public static void playBGM(String filePath) {
        try {
            // 如果当前有 BGM 正在播放，先停掉并释放资源
            if (bgmClip != null && bgmClip.isRunning()) {
                bgmClip.stop();
                bgmClip.close();
            }
            
            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                System.err.println("⚠️ 找不到BGM文件: " + filePath);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);
            
            // 设置为无限循环播放
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            System.out.println("▶️ 正在播放背景音乐: " + filePath);
            
        } catch (Exception e) {
            System.err.println("❌ BGM 播放出错！请检查文件格式是否为标准 PCM WAV。");
            e.printStackTrace();
        }
    }

    /**
     * ⏹️ 停止背景音乐
     */
    public static void stopBGM() {
        if (bgmClip != null) {
            bgmClip.stop();
            bgmClip.close();
            System.out.println("⏸️ 背景音乐已停止。");
        }
    }

    /**
     * ⚔️ 播放短音效 (挥刀、受击、爆炸等)
     * 使用独立线程播放，支持多音效并发（刀光剑影不会互相顶替），且播完自动清理内存
     * @param filePath 音频文件路径，例如 "audio/hit.wav"
     */
    public static void playSFX(String filePath) {
        // 开个新线程去播声音，绝对不卡游戏主画面！
        new Thread(() -> {
            try {
                File audioFile = new File(filePath);
                if (!audioFile.exists()) return;

                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                Clip sfxClip = AudioSystem.getClip();
                sfxClip.open(audioStream);
                sfxClip.start();

                // 核心细节：监听器。声音播完后，立刻销毁回收内存，防止内存泄漏！
                sfxClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        sfxClip.close();
                    }
                });
            } catch (Exception e) {
                // 音效报错可以静默处理，不影响游戏运行
            }
        }).start();
    }
}