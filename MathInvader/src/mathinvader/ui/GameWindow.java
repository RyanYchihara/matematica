package mathinvader.ui;

import mathinvader.core.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class GameWindow extends JFrame {

    private boolean isFullscreen = false;
    private final GraphicsDevice device;
    private final GamePanel panel;

    public GameWindow() {
        device = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                    .getDefaultScreenDevice();

        setTitle("Matematica");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        panel = new GamePanel();
        add(panel);
        pack();
        setLocationRelativeTo(null);
        setIconImage(createIcon());

        // F11 or Alt+Enter toggles fullscreen
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F11 ||
                    (e.getKeyCode() == KeyEvent.VK_ENTER &&
                     e.getModifiers() == KeyEvent.ALT_MASK)) {
                    toggleFullscreen();
                }
            }
        });

        panel.requestFocusInWindow();
    }

    private void toggleFullscreen() {
        if (!isFullscreen) {
            enterFullscreen();
        } else {
            exitFullscreen();
        }
    }

    private void enterFullscreen() {
        if (!device.isFullScreenSupported()) {
            // Fallback: maximise window with undecorated frame
            dispose();
            setUndecorated(true);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setVisible(true);
            panel.requestFocusInWindow();
            isFullscreen = true;
            return;
        }
        dispose();
        setUndecorated(true);
        device.setFullScreenWindow(this);
        isFullscreen = true;
        panel.requestFocusInWindow();
    }

    private void exitFullscreen() {
        device.setFullScreenWindow(null);
        dispose();
        setUndecorated(false);
        setExtendedState(JFrame.NORMAL);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        panel.requestFocusInWindow();
        isFullscreen = false;
    }

    private Image createIcon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(4, 8, 18));
        g.fillRect(0, 0, 64, 64);
        g.setColor(new Color(80, 220, 120));
        g.setFont(new Font("Courier New", Font.BOLD, 36));
        g.drawString("E", 14, 48);
        g.dispose();
        return img;
    }
}
