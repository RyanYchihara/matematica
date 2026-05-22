package mathinvader.ui;

import mathinvader.core.MathQuestion;
import mathinvader.core.PlayerStats;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class HUD {

    private static final Color ACCENT   = new Color(80, 220, 120);
    private static final Color WARN     = new Color(255, 160, 40);
    private static final Color DANGER   = new Color(255, 60, 60);
    private static final Color DIM      = new Color(140, 140, 160);
    private static final Color PANEL_BG = new Color(8, 12, 20, 210);

    private boolean doublePoints = false;
    private int doubleTimer = 0;
    private boolean slowMo = false;
    private int slowTimer = 0;

    public void update() {
        if (doubleTimer > 0) doubleTimer--; else doublePoints = false;
        if (slowTimer  > 0) slowTimer--;  else slowMo = false;
    }

    public void activateDouble(int frames) { doublePoints = true; doubleTimer = frames; }
    public void activateSlowMo(int frames) { slowMo = true; slowTimer = frames; }
    public boolean isDoublePoints() { return doublePoints; }
    public boolean isSlowMo()       { return slowMo; }

    public void draw(Graphics2D g, PlayerStats stats, String inputBuffer,
                     int panelW, int panelH, int activeEnemies) {
        drawTopBar(g, stats, panelW);
        drawInputBox(g, inputBuffer, panelW, panelH);
        drawComboDisplay(g, stats, panelW);
        drawActiveIndicator(g, activeEnemies);
        drawPowerUpIndicators(g, panelW);
        drawScanlines(g, panelW, panelH);
    }

    private void drawTopBar(Graphics2D g, PlayerStats stats, int panelW) {
        g.setColor(PANEL_BG);
        g.fillRect(0, 0, panelW, 56);
        g.setColor(ACCENT.darker().darker());
        g.drawLine(0, 56, panelW, 56);

        int y = 36;

        drawLabel(g, "PONTOS", 18, 14, DIM);
        drawValue(g, String.format("%07d", stats.getScore()), 18, y, ACCENT);

        drawLabel(g, "NIVEL", 185, 14, DIM);
        String diff;
        MathQuestion.Difficulty d = stats.getCurrentDifficulty();
        if      (d == MathQuestion.Difficulty.EASY)   diff = "FACIL";
        else if (d == MathQuestion.Difficulty.MEDIUM)  diff = "MEDIO";
        else                                           diff = "DIFICIL";
        drawValue(g, stats.getLevel() + " - " + diff, 185, y, WARN);

        drawLabel(g, "TEMPO", 370, 14, DIM);
        long s = stats.getSurvivedSeconds();
        drawValue(g, String.format("%02d:%02d", s / 60, s % 60), 370, y, Color.WHITE);

        int liveX = panelW - 130;
        drawLabel(g, "VIDAS", liveX, 14, DIM);
        drawHearts(g, stats.getLives(), liveX, y);

        g.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 80));
        g.drawLine(0, 0, panelW, 0);
    }

    private void drawHearts(Graphics2D g, int lives, int x, int y) {
        g.setFont(new Font("Serif", Font.PLAIN, 20));
        for (int i = 0; i < PlayerStats.MAX_LIVES; i++) {
            g.setColor(i < lives ? DANGER : new Color(60, 40, 40));
            g.drawString("*", x + i * 26, y);
        }
        // Draw actual hearts using ovals
        for (int i = 0; i < PlayerStats.MAX_LIVES; i++) {
            if (i < lives) g.setColor(DANGER);
            else           g.setColor(new Color(60, 40, 40));
            int hx = x + i * 26;
            // simple heart shape with ovals + triangle
            g.fillOval(hx,     y - 16, 9, 9);
            g.fillOval(hx + 5, y - 16, 9, 9);
            int[] xp = {hx, hx + 14, hx + 7};
            int[] yp = {y - 10, y - 10, y};
            g.fillPolygon(xp, yp, 3);
        }
    }

    private void drawInputBox(Graphics2D g, String input, int panelW, int panelH) {
        int bw = 320; int bh = 52;
        int bx = panelW / 2 - bw / 2;
        int by = panelH - 85;

        g.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 30));
        g.fill(new RoundRectangle2D.Float(bx - 6, by - 6, bw + 12, bh + 12, 16, 16));

        g.setColor(PANEL_BG);
        g.fill(new RoundRectangle2D.Float(bx, by, bw, bh, 10, 10));
        g.setColor(ACCENT);
        g.setStroke(new BasicStroke(2));
        g.draw(new RoundRectangle2D.Float(bx, by, bw, bh, 10, 10));
        g.setStroke(new BasicStroke(1));

        g.setFont(new Font("Courier New", Font.BOLD, 12));
        g.setColor(DIM);
        g.drawString("RESPOSTA:", bx + 12, by + 17);

        g.setFont(new Font("Courier New", Font.BOLD, 26));
        g.setColor(Color.WHITE);
        String display = input.isEmpty() ? "  " : input;
        g.drawString(display, bx + 18, by + 42);

        long now = System.currentTimeMillis();
        if ((now / 500) % 2 == 0) {
            g.setFont(new Font("Courier New", Font.BOLD, 26));
            FontMetrics fm = g.getFontMetrics();
            int cursorX = bx + 18 + fm.stringWidth(display);
            g.setColor(ACCENT);
            g.fillRect(cursorX + 2, by + 22, 2, 20);
        }

        g.setFont(new Font("Courier New", Font.PLAIN, 10));
        g.setColor(DIM);
        g.drawString("[ENTER] confirmar", bx + bw - 115, by + bh - 6);
    }

    private void drawComboDisplay(Graphics2D g, PlayerStats stats, int panelW) {
        int combo = stats.getCombo();
        if (combo < 2) return;

        String text = "COMBO x" + combo;
        Color c;
        if      (combo >= 10) c = new Color(255, 215, 0);
        else if (combo >= 5)  c = new Color(255, 130, 30);
        else                  c = new Color(255, 200, 80);

        g.setFont(new Font("Courier New", Font.BOLD, combo >= 5 ? 18 : 14));
        FontMetrics fm = g.getFontMetrics();
        int tx = panelW - fm.stringWidth(text) - 18;
        int ty = 90;

        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 40));
        g.fillRoundRect(tx - 8, ty - fm.getAscent() - 2, fm.stringWidth(text) + 16, 24, 6, 6);
        g.setColor(c);
        g.drawString(text, tx, ty);
    }

    private void drawActiveIndicator(Graphics2D g, int count) {
        g.setFont(new Font("Courier New", Font.PLAIN, 11));
        g.setColor(DIM);
        g.drawString("INIMIGOS: " + count, 18, 75);
    }

    private void drawPowerUpIndicators(Graphics2D g, int panelW) {
        int ix = panelW / 2 - 80;
        if (doublePoints) {
            drawPowerBadge(g, "x2 PONTOS", new Color(255, 130, 30), ix, 75,
                (float) doubleTimer / 600);
            ix += 110;
        }
        if (slowMo) {
            drawPowerBadge(g, "LENTO", new Color(160, 80, 255), ix, 75,
                (float) slowTimer / 600);
        }
    }

    private void drawPowerBadge(Graphics2D g, String text, Color c, int x, int y, float progress) {
        int bw = 100;
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
        g.fillRoundRect(x, y - 14, bw, 18, 6, 6);
        g.setColor(c);
        g.fillRoundRect(x, y + 5, (int)(bw * progress), 3, 2, 2);
        g.setFont(new Font("Courier New", Font.BOLD, 10));
        g.setColor(Color.WHITE);
        g.drawString(text, x + 4, y - 1);
    }

    private void drawScanlines(Graphics2D g, int w, int h) {
        g.setColor(new Color(0, 0, 0, 12));
        for (int y = 56; y < h - 90; y += 3) g.drawLine(0, y, w, y);
    }

    private void drawLabel(Graphics2D g, String label, int x, int y, Color c) {
        g.setFont(new Font("Courier New", Font.PLAIN, 10));
        g.setColor(c);
        g.drawString(label, x, y);
    }

    private void drawValue(Graphics2D g, String value, int x, int y, Color c) {
        g.setFont(new Font("Courier New", Font.BOLD, 16));
        g.setColor(c);
        g.drawString(value, x, y);
    }
}
