package mathinvader.ui;

import mathinvader.core.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Janela principal do jogo.
 *
 * Tela cheia corrigida: usa um JPanel wrapper que renderiza o GamePanel
 * escalado com aspect-ratio preservado, sem glitches de redimensionamento.
 */
public class GameWindow extends JFrame {

    private boolean isFullscreen = false;
    private final GraphicsDevice device;
    private final GamePanel panel;

    // Dimensões da janela em modo janela
    private static final int WIN_W = GamePanel.WIDTH;
    private static final int WIN_H = GamePanel.HEIGHT;

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

        // F11 ou Alt+Enter alterna tela cheia
        KeyAdapter fsToggle = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                boolean f11 = e.getKeyCode() == KeyEvent.VK_F11;
                boolean altEnter = e.getKeyCode() == KeyEvent.VK_ENTER
                        && (e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0;
                if (f11 || altEnter) toggleFullscreen();
            }
        };
        panel.addKeyListener(fsToggle);
        panel.requestFocusInWindow();
    }

    // ─── Tela Cheia ──────────────────────────────────────────────────────────

    private void toggleFullscreen() {
        if (isFullscreen) exitFullscreen();
        else              enterFullscreen();
    }

    private void enterFullscreen() {
        // Impede double-entry
        if (isFullscreen) return;
        isFullscreen = true;

        if (device.isFullScreenSupported()) {
            // Modo exclusivo: mais robusto e sem bordas de SO
            dispose();
            setUndecorated(true);
            setResizable(false);
            device.setFullScreenWindow(this);
            // Força o layout a ocupar a tela toda
            Dimension screen = getSize();
            resizePanelScaled(screen.width, screen.height);
        } else {
            // Fallback: maximizado sem decoração
            dispose();
            setUndecorated(true);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setVisible(true);
            SwingUtilities.invokeLater(() -> {
                Dimension screen = getSize();
                resizePanelScaled(screen.width, screen.height);
                panel.requestFocusInWindow();
            });
        }
        panel.requestFocusInWindow();
    }

    private void exitFullscreen() {
        if (!isFullscreen) return;
        isFullscreen = false;

        device.setFullScreenWindow(null);
        dispose();
        setUndecorated(false);
        setExtendedState(JFrame.NORMAL);
        setResizable(false);
        restorePanelOriginalSize();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        panel.requestFocusInWindow();
    }

    /**
     * Redimensiona o GamePanel para preencher a área disponível,
     * mantendo a proporção original (aspect-ratio letterbox).
     */
    private void resizePanelScaled(int availW, int availH) {
        double scaleX = (double) availW / WIN_W;
        double scaleY = (double) availH / WIN_H;
        double scale  = Math.min(scaleX, scaleY);

        int newW = (int)(WIN_W * scale);
        int newH = (int)(WIN_H * scale);

        // Centraliza com margens pretas
        getContentPane().setBackground(Color.BLACK);
        getContentPane().setLayout(null);
        panel.setBounds((availW - newW) / 2, (availH - newH) / 2, newW, newH);
        panel.setScaleFactor(scale);
        panel.revalidate();
    }

    private void restorePanelOriginalSize() {
        getContentPane().setLayout(new BorderLayout());
        panel.setBounds(0, 0, WIN_W, WIN_H);
        panel.setScaleFactor(1.0);
        getContentPane().add(panel);
        panel.setPreferredSize(new Dimension(WIN_W, WIN_H));
    }

    // ─── Ícone ───────────────────────────────────────────────────────────────

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
