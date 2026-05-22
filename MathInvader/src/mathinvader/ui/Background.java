package mathinvader.ui;

import java.awt.*;
import java.util.Random;

public class Background {

    private final int[][] stars;   // x, y, brightness, speed
    private final int width, height;
    private float gridOffset = 0;
    private static final Random rand = new Random();

    public Background(int width, int height) {
        this.width = width;
        this.height = height;
        stars = new int[120][4];
        for (int i = 0; i < stars.length; i++) {
            stars[i][0] = rand.nextInt(width);
            stars[i][1] = rand.nextInt(height);
            stars[i][2] = 40 + rand.nextInt(180);
            stars[i][3] = 1 + rand.nextInt(3);
        }
    }

    public void update() {
        gridOffset = (gridOffset + 0.4f) % 40;
        for (int[] star : stars) {
            star[1] += star[3];
            if (star[1] > height) {
                star[1] = -5;
                star[0] = rand.nextInt(width);
            }
        }
    }

    public void draw(Graphics2D g) {
        // Deep space background gradient
        GradientPaint bg = new GradientPaint(0, 0, new Color(2, 4, 14),
            0, height, new Color(6, 2, 18));
        g.setPaint(bg);
        g.fillRect(0, 0, width, height);

        drawGrid(g);
        drawStars(g);
        drawNebula(g);
        drawVignette(g);
    }

    private void drawGrid(Graphics2D g) {
        g.setColor(new Color(30, 60, 40, 25));
        g.setStroke(new BasicStroke(0.5f));
        int spacing = 40;
        for (int x = 0; x < width; x += spacing) {
            g.drawLine(x, 56, x, height - 90);
        }
        for (int y = 56 + (int)gridOffset; y < height - 90; y += spacing) {
            g.drawLine(0, y, width, y);
        }
        g.setStroke(new BasicStroke(1));
    }

    private void drawStars(Graphics2D g) {
        for (int[] star : stars) {
            int bright = star[2];
            g.setColor(new Color(bright, bright, Math.min(255, bright + 30), bright));
            int size = star[3] == 3 ? 2 : 1;
            g.fillRect(star[0], star[1], size, size);
        }
    }

    private void drawNebula(Graphics2D g) {
        // Subtle colored nebula clouds using radial gradients
        drawNebulaCloud(g, width / 4, height / 3, 200, new Color(0, 60, 120, 20));
        drawNebulaCloud(g, width * 3/4, height * 2/3, 160, new Color(60, 0, 80, 15));
    }

    private void drawNebulaCloud(Graphics2D g, int cx, int cy, int r, Color c) {
        RadialGradientPaint rp = new RadialGradientPaint(cx, cy, r,
            new float[]{0f, 1f},
            new Color[]{c, new Color(0, 0, 0, 0)});
        g.setPaint(rp);
        g.fillOval(cx - r, cy - r, r * 2, r * 2);
        g.setPaint(null);
    }

    private void drawVignette(Graphics2D g) {
        RadialGradientPaint vignette = new RadialGradientPaint(
            width / 2f, height / 2f, width * 0.7f,
            new float[]{0.5f, 1f},
            new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 150)});
        g.setPaint(vignette);
        g.fillRect(0, 0, width, height);
        g.setPaint(null);
    }

    public void drawPlayerLine(Graphics2D g) {
        // The "ground" / player defense line
        g.setColor(new Color(80, 220, 120, 60));
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            0, new float[]{8, 6}, System.currentTimeMillis() / 50f % 14));
        g.drawLine(20, 625, width - 20, 625);
        g.setStroke(new BasicStroke(1));

        // turret icon
        g.setFont(new Font("Courier New", Font.BOLD, 14));
        g.setColor(new Color(80, 220, 120, 180));
        g.drawString("▲ DEFESA ▲", width / 2 - 50, 645);
    }
}
