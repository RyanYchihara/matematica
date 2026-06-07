package mathinvader.entities;

import java.awt.*;
import java.util.Random;

public class PowerUp {

    public enum PowerType {
        SHIELD("ESCUDO",  new Color(80, 180, 255)),
        SLOWMO("LENTO",   new Color(200, 100, 255)),
        CLEAR ("LIMPAR",  new Color(255, 215, 0)),
        DOUBLE("DOBRO",   new Color(255, 130, 30));

        public final String label;
        public final Color color;

        PowerType(String label, Color color) {
            this.label = label;
            this.color = color;
        }
    }

    private float x, y;
    private final PowerType type;
    private float timer;
    private boolean collected = false;
    private static final Random rand = new Random();
    private float angle = 0;

    public PowerUp(int x, int y) {
        this.x = x; this.y = y;
        this.type = PowerType.values()[rand.nextInt(PowerType.values().length)];
        this.timer = 0;
    }

    public void update() {
        timer += 0.05f;
        angle += 0.04f;
        y += 0.5f;
    }

    public void draw(Graphics2D g) {
        if (collected) return;
        int px = (int)x;
        int py = (int)y;
        float pulse = (float)(Math.sin(timer * 3) * 0.2 + 0.8);

        Color gc = new Color(type.color.getRed(), type.color.getGreen(),
            type.color.getBlue(), (int)(60 * pulse));
        g.setColor(gc);
        g.fillOval(px - 28, py - 28, 56, 56);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(px, py);
        g2.rotate(angle);
        g2.setColor(type.color);
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            0, new float[]{6, 4}, 0));
        g2.drawOval(-18, -18, 36, 36);
        g2.dispose();

        g.setColor(new Color(10, 10, 20, 220));
        g.fillOval(px - 15, py - 15, 30, 30);
        g.setColor(type.color);
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(px - 15, py - 15, 30, 30);
        g.setStroke(new BasicStroke(1));

        g.setFont(new Font("Courier New", Font.BOLD, 9));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Color.WHITE);
        g.drawString(type.label, px - fm.stringWidth(type.label)/2, py + 22);
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x - 15, (int)y - 15, 30, 30);
    }

    public boolean isOffScreen()  { return y > 680; }
    public boolean isCollected()  { return collected; }
    public void collect()         { collected = true; }
    public PowerType getType()    { return type; }
    public int getX()             { return (int)x; }
    public int getY()             { return (int)y; }
}
