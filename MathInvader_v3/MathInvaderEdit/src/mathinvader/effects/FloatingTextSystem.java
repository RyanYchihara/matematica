package mathinvader.effects;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FloatingTextSystem {

    private final List<FloatingText> texts = new ArrayList<>();

    public void spawn(int x, int y, String text, Color color) {
        texts.add(new FloatingText(x, y, text, color, 1.2f, 50));
    }

    public void spawnBig(int x, int y, String text, Color color) {
        texts.add(new FloatingText(x, y, text, color, 1.8f, 60));
    }

    public void update() {
        Iterator<FloatingText> it = texts.iterator();
        while (it.hasNext()) {
            FloatingText t = it.next();
            t.update();
            if (t.isDead()) it.remove();
        }
    }

    public void draw(Graphics2D g) {
        for (FloatingText t : texts) t.draw(g);
    }

    public void clear() { texts.clear(); }

    private static class FloatingText {
        float x, y;
        String text;
        Color color;
        float scale;
        int life, maxLife;

        FloatingText(float x, float y, String text, Color color, float scale, int life) {
            this.x = x; this.y = y;
            this.text = text; this.color = color;
            this.scale = scale;
            this.life = this.maxLife = life;
        }

        void update() { y -= 0.8f; life--; }
        boolean isDead() { return life <= 0; }

        void draw(Graphics2D g) {
            float alpha = (float) life / maxLife;
            int fontSize = (int)(14 * scale);
            g.setFont(new Font("Courier New", Font.BOLD, fontSize));
            FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(text);

            Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(),
                (int)(alpha * 255));
            // shadow
            g.setColor(new Color(0, 0, 0, (int)(alpha * 150)));
            g.drawString(text, (int)x - tw/2 + 2, (int)y + 2);
            g.setColor(c);
            g.drawString(text, (int)x - tw/2, (int)y);
        }
    }
}
