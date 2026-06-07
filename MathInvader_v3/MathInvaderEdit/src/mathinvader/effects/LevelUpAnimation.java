package mathinvader.effects;

import java.awt.*;

public class LevelUpAnimation {

    private boolean active = false;
    private int level = 0;
    private int timer = 0;

    private static final int DURATION = 120; // 2 seconds at 60fps
    private static final int FLASH_PERIOD = 10; // frames per flash cycle

    public void trigger(int newLevel) {
        active = true;
        level  = newLevel;
        timer  = DURATION;
    }

    public void update() {
        if (active && timer > 0) timer--;
        if (timer <= 0) active = false;
    }

    public boolean isActive() { return active; }

    public void draw(Graphics2D g, int W, int H) {
        if (!active) return;

        // Flash visibility: on during odd flash periods
        int flashPhase = timer / FLASH_PERIOD;
        boolean visible = (flashPhase % 2 == 0);
        if (!visible) return;

        // Fade out in the last 30 frames
        float alpha = timer < 30 ? timer / 30f : 1f;

        int cx = W / 2;
        int cy = H / 2;

        // Background pulse ring
        float scale = 1f + (1f - (float) timer / DURATION) * 0.4f;
        int ringR = (int)(120 * scale);
        Color ringC = new Color(255, 215, 0, (int)(40 * alpha));
        g.setColor(ringC);
        g.fillOval(cx - ringR, cy - ringR, ringR * 2, ringR * 2);

        // "NIVEL X" large flashing text
        String line1 = "NIVEL";
        String line2 = String.valueOf(level);

        // Glow layers
        for (int i = 6; i >= 1; i--) {
            int a = (int)(40 / i * alpha);
            g.setColor(new Color(255, 215, 0, a));
            drawCentered(g, line1, cx, cy - 24, 28 + i);
            drawCentered(g, line2, cx, cy + 48, 72 + i * 2);
        }

        // Main text
        g.setColor(new Color(255, 255, 180, (int)(255 * alpha)));
        drawCentered(g, line1, cx, cy - 24, 28);

        g.setColor(new Color(255, 215, 0, (int)(255 * alpha)));
        drawCentered(g, line2, cx, cy + 48, 72);

        // "UP!" label
        g.setFont(new Font("Courier New", Font.BOLD, 20));
        g.setColor(new Color(80, 220, 120, (int)(230 * alpha)));
        FontMetrics fm = g.getFontMetrics();
        g.drawString("UP!", cx - fm.stringWidth("UP!")/2, cy - 52);
    }

    private void drawCentered(Graphics2D g, String text, int cx, int y, int size) {
        g.setFont(new Font("Courier New", Font.BOLD, size));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, cx - fm.stringWidth(text) / 2, y);
    }
}
