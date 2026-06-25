package mathinvader.effects;

import java.awt.*;

public class ScreenFlash {

    private Color flashColor = Color.WHITE;
    private float alpha = 0;
    private float decay = 0.08f;

    public void flash(Color color, float intensity) {
        this.flashColor = color;
        this.alpha = Math.min(1f, intensity);
        this.decay = 0.07f;
    }

    public void update() {
        if (alpha > 0) alpha = Math.max(0, alpha - decay);
    }

    public void draw(Graphics2D g, int width, int height) {
        if (alpha <= 0) return;
        Color c = new Color(flashColor.getRed(), flashColor.getGreen(),
            flashColor.getBlue(), (int)(alpha * 180));
        g.setColor(c);
        g.fillRect(0, 0, width, height);
    }
}
