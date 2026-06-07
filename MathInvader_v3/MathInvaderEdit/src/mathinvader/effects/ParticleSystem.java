package mathinvader.effects;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ParticleSystem {

    private final List<Particle> particles = new ArrayList<>();
    private static final Random rand = new Random();

    public void spawnExplosion(int x, int y, Color color) {
        int count = 18 + rand.nextInt(10);
        for (int i = 0; i < count; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double speed = 1.5 + Math.random() * 5;
            float vx = (float)(Math.cos(angle) * speed);
            float vy = (float)(Math.sin(angle) * speed);
            Color c = vary(color);
            particles.add(new Particle(x, y, vx, vy, c, 30 + rand.nextInt(25), rand.nextInt(5) + 2));
        }
    }

    public void spawnCorrect(int x, int y) {
        for (int i = 0; i < 8; i++) {
            double angle = -Math.PI / 2 + (Math.random() - 0.5);
            double speed = 2 + Math.random() * 4;
            particles.add(new Particle(x, y,
                (float)(Math.cos(angle) * speed),
                (float)(Math.sin(angle) * speed),
                new Color(80, 255, 120), 25, 3));
        }
    }

    public void spawnWrong(int x, int y) {
        for (int i = 0; i < 5; i++) {
            double angle = Math.random() * 2 * Math.PI;
            particles.add(new Particle(x, y,
                (float)(Math.cos(angle) * 2),
                (float)(Math.sin(angle) * 2),
                new Color(255, 60, 60), 20, 4));
        }
    }

    public void spawnCombo(int x, int y, int combo) {
        Color c = combo >= 5 ? new Color(255, 215, 0) : new Color(255, 160, 0);
        for (int i = 0; i < combo * 2; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double speed = 1 + Math.random() * 4;
            particles.add(new Particle(x, y,
                (float)(Math.cos(angle) * speed),
                (float)(Math.sin(angle) * speed),
                c, 35, 4));
        }
    }

    public void update() {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.update();
            if (p.isDead()) it.remove();
        }
    }

    public void draw(Graphics2D g) {
        for (Particle p : particles) p.draw(g);
    }

    public void clear() { particles.clear(); }

    private Color vary(Color base) {
        int r = Math.max(0, Math.min(255, base.getRed()   + rand.nextInt(60) - 30));
        int gr= Math.max(0, Math.min(255, base.getGreen() + rand.nextInt(60) - 30));
        int b = Math.max(0, Math.min(255, base.getBlue()  + rand.nextInt(60) - 30));
        return new Color(r, gr, b);
    }

    // ─── Inner Particle ───────────────────────────────────────────────────────

    private static class Particle {
        float x, y, vx, vy;
        Color color;
        int life, maxLife, size;

        Particle(float x, float y, float vx, float vy, Color c, int life, int size) {
            this.x = x; this.y = y;
            this.vx = vx; this.vy = vy;
            this.color = c;
            this.life = this.maxLife = life;
            this.size = size;
        }

        void update() {
            x += vx; y += vy;
            vy += 0.15f; // gravity
            vx *= 0.95f;
            life--;
        }

        boolean isDead() { return life <= 0; }

        void draw(Graphics2D g) {
            float alpha = (float) life / maxLife;
            Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(),
                (int)(alpha * 230));
            g.setColor(c);
            int s = Math.max(1, (int)(size * alpha));
            g.fillOval((int)x - s/2, (int)y - s/2, s, s);
        }
    }
}
