package mathinvader.entities;

import mathinvader.core.CustomConfig;
import mathinvader.core.MathQuestion;
import mathinvader.core.FontSettings;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

public class Enemy {

    public enum EnemyType { NORMAL, FAST, TANK, BOSS }

    private float x, y;
    private final int targetY = 620;
    private float speed;
    private final MathQuestion question;
    private final EnemyType type;
    private boolean alive = true;
    private float pulseTimer = 0;
    private final Color primaryColor;
    private float shakeX = 0;
    private int shakeTimer = 0;
    private final int width;
    private final int height = 44;

    private static final Random rand = new Random();

    /** Construtor modo normal. */
    public Enemy(int startX, int panelWidth, int level, MathQuestion.Difficulty difficulty) {
        this.x = startX;
        this.y = -60;
        this.question = new MathQuestion(difficulty);
        this.type = pickType(level);
        this.speed = baseSpeed(type, level);
        this.primaryColor = colorForType(type);
        this.width = sizeForType(type);
    }

    /** Construtor modo personalizado — gera questão conforme CustomConfig. */
    public Enemy(int startX, int panelWidth, int level, CustomConfig config) {
        this.x = startX;
        this.y = -60;
        this.question = new MathQuestion(config);
        this.type = pickType(level);
        this.speed = baseSpeed(type, level);
        this.primaryColor = colorForType(type);
        this.width = sizeForType(type);
    }

    private EnemyType pickType(int level) {
        int r = rand.nextInt(100);
        if (level >= 10 && r < 5)  return EnemyType.BOSS;
        if (level >= 5  && r < 20) return EnemyType.TANK;
        if (level >= 3  && r < 30) return EnemyType.FAST;
        return EnemyType.NORMAL;
    }

    private float baseSpeed(EnemyType t, int level) {
        float base;
        switch (t) {
            case FAST: base = 0.8f;  break;
            case TANK: base = 0.28f; break;
            case BOSS: base = 0.22f; break;
            default:   base = 0.45f; break;
        }
        return base + level * 0.05f;
    }

    private Color colorForType(EnemyType t) {
        switch (t) {
            case FAST: return new Color(60, 200, 255);
            case TANK: return new Color(180, 80, 255);
            case BOSS: return new Color(255, 50, 50);
            default:   return new Color(80, 220, 120);
        }
    }

    private int sizeForType(EnemyType t) {
        switch (t) {
            case TANK: return 130;
            case BOSS: return 160;
            case FAST: return 100;
            default:   return 115;
        }
    }

    public void update() {
        pulseTimer += 0.08f;
        y += speed;
        if (shakeTimer > 0) {
            shakeX = (rand.nextFloat() - 0.5f) * 6;
            shakeTimer--;
        } else {
            shakeX = 0;
        }
    }

    public void shake() { shakeTimer = 8; }

    public void draw(Graphics2D g) {
        if (!alive) return;
        int px = (int)(x + shakeX);
        int py = (int)y;
        float pulse = (float)(Math.sin(pulseTimer) * 0.15 + 0.85);

        drawGlow(g, px, py, pulse);

        Color bodyColor = primaryColor.darker();
        Color borderColor = brighten(primaryColor, pulse);
        g.setColor(bodyColor);
        g.fill(new RoundRectangle2D.Float(px - width/2f, py, width, height, 10, 10));
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(2));
        g.draw(new RoundRectangle2D.Float(px - width/2f, py, width, height, 10, 10));
        g.setStroke(new BasicStroke(1));

        g.setColor(new Color(0, 0, 0, 30));
        for (int sy = py + 4; sy < py + height - 2; sy += 4) {
            g.drawLine(px - width/2 + 2, sy, px + width/2 - 2, sy);
        }

        drawEnemyFace(g, px, py);
        drawQuestion(g, px, py);
    }

    private void drawGlow(Graphics2D g, int px, int py, float pulse) {
        Color gc = new Color(primaryColor.getRed(), primaryColor.getGreen(),
            primaryColor.getBlue(), (int)(40 * pulse));
        int gw = width + 20; int gh = height + 20;
        g.setColor(gc);
        g.fill(new RoundRectangle2D.Float(px - gw/2f, py - 10, gw, gh, 20, 20));
    }

    private void drawEnemyFace(Graphics2D g, int px, int py) {
        int cx = px;
        int cy = py + height / 2;
        String icon;
        Color c;
        switch (type) {
            case BOSS:
                icon = "X_X"; c = new Color(255, 80, 80);
                g.setFont(new Font("Courier New", Font.BOLD, 14));
                break;
            case TANK:
                icon = "[=]"; c = new Color(200, 100, 255);
                g.setFont(new Font("Courier New", Font.BOLD, 14));
                break;
            case FAST:
                icon = ">>"; c = new Color(80, 210, 255);
                g.setFont(new Font("Courier New", Font.BOLD, 16));
                break;
            default:
                icon = "<*>"; c = new Color(80, 220, 120);
                g.setFont(new Font("Courier New", Font.BOLD, 14));
                break;
        }
        g.setColor(c);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(icon, cx - fm.stringWidth(icon)/2, cy + 6);
    }

    private void drawQuestion(Graphics2D g, int px, int py) {
        FontSettings fs = FontSettings.getInstance();
        String expr = question.getExpression();
        g.setFont(new Font("Courier New", Font.BOLD, fs.scale(14)));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(expr);
        int qy = py - 8;

        int bw = tw + 16; int bh = fm.getHeight() + 4;
        g.setColor(new Color(10, 10, 20, 200));
        g.fillRoundRect(px - bw/2, qy - bh + 2, bw, bh, 6, 6);
        g.setColor(primaryColor.brighter());
        g.setStroke(new BasicStroke(1));
        g.drawRoundRect(px - bw/2, qy - bh + 2, bw, bh, 6, 6);

        g.setColor(Color.WHITE);
        g.drawString(expr, px - tw/2, qy - 1);
    }

    private Color brighten(Color c, float factor) {
        int r = Math.min(255, (int)(c.getRed()   * factor + 40));
        int gr= Math.min(255, (int)(c.getGreen() * factor + 40));
        int b = Math.min(255, (int)(c.getBlue()  * factor + 40));
        return new Color(r, gr, b);
    }

    public boolean hasReachedPlayer() { return y >= targetY; }
    public boolean isAlive()          { return alive; }
    public void destroy()             { alive = false; }

    public float getX()       { return x; }
    public float getY()       { return y; }
    public int getCenterX()   { return (int)x; }
    public int getCenterY()   { return (int)(y + height/2f); }
    public MathQuestion getQuestion() { return question; }
    public EnemyType getType()        { return type; }
    public Color getColor()           { return primaryColor; }
    public int getWidth()             { return width; }
    public int getHeight()            { return height; }
}
