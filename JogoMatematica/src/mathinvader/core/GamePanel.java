package mathinvader.core;

import mathinvader.audio.SoundManager;
import mathinvader.audio.TTSEngine;
import mathinvader.core.FontSettings;
import mathinvader.data.HighScoreManager;
import mathinvader.effects.FloatingTextSystem;
import mathinvader.effects.LevelUpAnimation;
import mathinvader.effects.ParticleSystem;
import mathinvader.effects.ScreenFlash;
import mathinvader.entities.Enemy;
import mathinvader.entities.PowerUp;
import mathinvader.core.CustomConfig;
import mathinvader.ui.AccessibilityAudioPanel;
import mathinvader.ui.CustomModePanel;
import mathinvader.ui.Background;
import mathinvader.ui.HUD;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {

    public static final int WIDTH  = 700;
    public static final int HEIGHT = 700;

    public enum GameState { MENU, PLAYING, PAUSED, GAMEOVER, AUDIO, RECORDES, VISUAL, CUSTOM }
    private GameState state = GameState.MENU;

    // Fator de escala para tela cheia (definido pelo GameWindow)
    private double scaleFactor = 1.0;

    public void setScaleFactor(double sf) { this.scaleFactor = sf; }

    private final PlayerStats     stats      = new PlayerStats();
    private final HUD             hud        = new HUD();
    private final Background      background;
    private final ParticleSystem  particles  = new ParticleSystem();
    private final LevelUpAnimation levelUpAnim = new LevelUpAnimation();
    private final FloatingTextSystem floats  = new FloatingTextSystem();
    private final ScreenFlash     screenFlash = new ScreenFlash();
    private final SoundManager    sound       = SoundManager.getInstance();
    private final TTSEngine       tts         = TTSEngine.getInstance();
    private final HighScoreManager highScores  = new HighScoreManager();
    private final AccessibilityAudioPanel audioPanel = new AccessibilityAudioPanel();
    private final CustomConfig   customConfig = new CustomConfig();
    private final CustomModePanel customPanel  = new CustomModePanel(customConfig);
    private boolean isCustomMode = false;

    private final List<Enemy>   enemies  = new ArrayList<Enemy>();
    private final List<PowerUp> powerUps = new ArrayList<PowerUp>();

    private final StringBuilder inputBuffer = new StringBuilder();

    private int spawnTimer    = 0;
    private int spawnInterval = 280;
    private final Random rand = new Random();
    private int powerUpTimer  = 0;

    private int menuSelection  = 0;
    private float menuAnimTimer = 0;
    private boolean enteringName = false;
    private final StringBuilder nameBuffer = new StringBuilder();
    private boolean newHighScore = false;

    private static final Color DANGER_COLOR = new Color(255, 60, 60);

    private final Timer gameLoop;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        // Impede o Swing de interceptar Tab para navegação de foco
        setFocusTraversalKeysEnabled(false);
        background = new Background(WIDTH, HEIGHT);
        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
        setupKeyboard();
    }

    private void setupKeyboard() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if      (state == GameState.PLAYING)  handlePlayingKey(e);
                else if (state == GameState.MENU)     handleMenuKey(e);
                else if (state == GameState.GAMEOVER) handleGameOverKey(e);
                else if (state == GameState.PAUSED) {
                    if (code == KeyEvent.VK_P || code == KeyEvent.VK_ESCAPE) resumeGame();
                } else if (state == GameState.AUDIO) {
                    if (code == KeyEvent.VK_ESCAPE) { state = GameState.MENU; tts.stop(); }
                    else if (code == KeyEvent.VK_UP)    audioPanel.moveUp();
                    else if (code == KeyEvent.VK_DOWN)  audioPanel.moveDown();
                    else if (code == KeyEvent.VK_LEFT)  audioPanel.adjustLeft();
                    else if (code == KeyEvent.VK_RIGHT) audioPanel.adjustRight();
                    else if (code == KeyEvent.VK_TAB)   audioPanel.switchSection(1);
                    else if (code == KeyEvent.VK_Q)     audioPanel.switchSection(1);
                    else if (code == KeyEvent.VK_E)     audioPanel.switchSection(-1);
                    else if (code == KeyEvent.VK_ENTER) audioPanel.activate();
                } else if (state == GameState.VISUAL) {
                    if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_ENTER) state = GameState.MENU;
                    else if (code == KeyEvent.VK_LEFT)  FontSettings.getInstance().cyclePrev();
                    else if (code == KeyEvent.VK_RIGHT) FontSettings.getInstance().cycleNext();
                    else if (code == KeyEvent.VK_UP)    FontSettings.getInstance().cyclePrev();
                    else if (code == KeyEvent.VK_DOWN)  FontSettings.getInstance().cycleNext();
                } else if (state == GameState.RECORDES) {
                    if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_ENTER) state = GameState.MENU;
                } else if (state == GameState.CUSTOM) {
                    if (code == KeyEvent.VK_ESCAPE) { state = GameState.MENU; }
                    else if (code == KeyEvent.VK_UP)    customPanel.moveUp();
                    else if (code == KeyEvent.VK_DOWN)  customPanel.moveDown();
                    else if (code == KeyEvent.VK_LEFT)  customPanel.adjustLeft();
                    else if (code == KeyEvent.VK_RIGHT) customPanel.adjustRight();
                    else if (code == KeyEvent.VK_ENTER) {
                        String action = customPanel.activate();
                        if ("play".equals(action))  { isCustomMode = true; startGame(); }
                        else if ("back".equals(action)) state = GameState.MENU;
                    }
                }
            }
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (state == GameState.PLAYING) {
                    if (c == '-' && inputBuffer.length() == 0) inputBuffer.append(c);
                    else if (Character.isDigit(c))             inputBuffer.append(c);
                } else if (state == GameState.GAMEOVER && enteringName) {
                    if (Character.isLetterOrDigit(c) && nameBuffer.length() < 12)
                        nameBuffer.append(Character.toUpperCase(c));
                }
            }
        });
    }

    private void handlePlayingKey(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_BACK_SPACE && inputBuffer.length() > 0) {
            inputBuffer.deleteCharAt(inputBuffer.length() - 1);
        } else if (code == KeyEvent.VK_ENTER) {
            submitAnswer();
        } else if (code == KeyEvent.VK_P || code == KeyEvent.VK_ESCAPE) {
            pauseGame();
        } else if (code == KeyEvent.VK_M) {
            sound.toggleMusic();
        } else if (code == KeyEvent.VK_S) {
            sound.toggleSound();
        }
    }

    private void handleMenuKey(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP)   menuSelection = Math.max(0, menuSelection - 1);
        if (code == KeyEvent.VK_DOWN) menuSelection = Math.min(5, menuSelection + 1);
        if (code == KeyEvent.VK_ENTER) {
            if      (menuSelection == 0) { isCustomMode = false; startGame(); }
            else if (menuSelection == 1) state = GameState.CUSTOM;
            else if (menuSelection == 2) state = GameState.RECORDES;
            else if (menuSelection == 3) state = GameState.AUDIO;
            else if (menuSelection == 4) state = GameState.VISUAL;
            else                         System.exit(0);
        }
    }

    private void handleGameOverKey(KeyEvent e) {
        int code = e.getKeyCode();
        if (enteringName) {
            if (code == KeyEvent.VK_BACK_SPACE && nameBuffer.length() > 0)
                nameBuffer.deleteCharAt(nameBuffer.length() - 1);
            if (code == KeyEvent.VK_ENTER) {
                String playerName = nameBuffer.length() > 0 ? nameBuffer.toString() : "JOGADOR";
                highScores.addEntry(playerName, stats.getScore(),
                    stats.getLevel(), stats.getSurvivedSeconds());
                enteringName = false;
            }
        } else {
            if (code == KeyEvent.VK_R || code == KeyEvent.VK_ENTER) restartGame();
            if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_M) returnToMenu();
        }
    }

    private void startGame() {
        stats.reset();
        enemies.clear(); powerUps.clear(); particles.clear(); floats.clear();
        inputBuffer.setLength(0);
        spawnTimer = 0; spawnInterval = 280; powerUpTimer = 0;
        newHighScore = false;
        state = GameState.PLAYING;
        sound.startMusic();
    }

    private void restartGame() { startGame(); }

    private void returnToMenu() {
        sound.stopMusic();
        state = GameState.MENU;
        enemies.clear(); powerUps.clear(); particles.clear(); floats.clear();
    }

    private void pauseGame()  { state = GameState.PAUSED;  sound.stopMusic(); }
    private void resumeGame() { state = GameState.PLAYING; sound.startMusic(); }

    private void gameOver() {
        state = GameState.GAMEOVER;
        stats.freezeTimer();   // congela o cronômetro no momento exato do game over
        sound.stopMusic();
        sound.playGameOver();
        screenFlash.flash(new Color(200, 30, 30), 0.8f);
        if (highScores.isHighScore(stats.getScore())) {
            newHighScore = true;
            enteringName = true;
            nameBuffer.setLength(0);
        }
    }

    private void submitAnswer() {
        if (inputBuffer.length() == 0) return;
        String answer = inputBuffer.toString();
        inputBuffer.setLength(0);
        Enemy target = findTargetEnemy(answer);
        if (target != null) destroyEnemy(target);
        else                onWrongAnswer();
    }

    private Enemy findTargetEnemy(String answer) {
        Enemy closest = null;
        float closestY = Float.MIN_VALUE;
        for (Enemy e : enemies) {
            if (e.isAlive() && e.getQuestion().check(answer) && e.getY() > closestY) {
                closestY = e.getY();
                closest = e;
            }
        }
        return closest;
    }

    private void destroyEnemy(Enemy e) {
        e.destroy();
        int x = e.getCenterX(); int y = e.getCenterY();
        particles.spawnExplosion(x, y, e.getColor());
        sound.playExplosion();
        sound.playCorrect();
        stats.registerKill();
        int prevLevel = stats.getLevel();
        if (!isCustomMode || !customConfig.isLevelLocked()) stats.updateLevel();
        if (stats.getLevel() > prevLevel) {
            levelUpAnim.trigger(stats.getLevel());
            tts.speak("Nivel " + stats.getLevel() + "!");
        }

        int points = 100 * stats.getLevel();
        if (hud.isDoublePoints()) points *= 2;
        int combo = stats.getCombo();
        String txt = combo >= 3 ? "+" + points + " COMBO!" : "+" + points;
        Color c = combo >= 3 ? new Color(255, 215, 0) : new Color(80, 220, 120);
        floats.spawn(x, y - 20, txt, c);

        if (combo >= 3) {
            particles.spawnCombo(x, y, combo);
            sound.playCombo(combo);
            tts.speak("Combo " + combo + "!");
        }
        screenFlash.flash(new Color(80, 220, 120), 0.15f);
        updateSpawnInterval();
    }

    private void onWrongAnswer() {
        stats.registerMiss();
        sound.playWrong();
        screenFlash.flash(new Color(220, 60, 60), 0.25f);
        for (Enemy e : enemies) e.shake();
        floats.spawn(WIDTH / 2, HEIGHT / 2, "ERROU!", new Color(255, 60, 60));
        tts.speak("Resposta errada. Tente novamente.");
    }

    private void updateSpawnInterval() {
        spawnInterval = Math.max(80, 280 - stats.getLevel() * 14);
    }

    private void spawnEnemy() {
        int alive = 0;
        for (Enemy e : enemies) if (e.isAlive()) alive++;
        int maxEnemies = Math.min(4, 2 + stats.getLevel());
        if (alive >= maxEnemies) return;

        int x = 80 + rand.nextInt(WIDTH - 160);
        Enemy enemy = isCustomMode
            ? new Enemy(x, WIDTH, stats.getLevel(), customConfig)
            : new Enemy(x, WIDTH, stats.getLevel(), stats.getCurrentDifficulty());
        if (enemy.getType() == Enemy.EnemyType.BOSS) {
            sound.playBossWarning();
            floats.spawnBig(WIDTH / 2, HEIGHT / 2 - 50, "!! BOSS !!", new Color(255, 60, 60));
        }
        enemies.add(enemy);
        // Narração para acessibilidade: fala a conta do inimigo que acabou de aparecer
        tts.speakEquation(enemy.getQuestion().getExpression());
    }

    private void maybeSpawnPowerUp() {
        if (rand.nextInt(800) < 1) {
            powerUps.add(new PowerUp(100 + rand.nextInt(WIDTH - 200), 100));
        }
    }

    private void updatePlaying() {
        background.update();
        hud.update();
        levelUpAnim.update();
        particles.update();
        floats.update();
        screenFlash.update();

        spawnTimer++;
        if (spawnTimer >= spawnInterval) { spawnTimer = 0; spawnEnemy(); }

        powerUpTimer++;
        if (powerUpTimer > 30) { powerUpTimer = 0; maybeSpawnPowerUp(); }

        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            if (!e.isAlive()) { it.remove(); continue; }
            e.update();
            if (e.hasReachedPlayer()) {
                it.remove();
                stats.loseLife();
                sound.playEnemyReach();
                screenFlash.flash(new Color(255, 80, 0), 0.6f);
                floats.spawnBig(e.getCenterX(), 580, "-1 VIDA!", DANGER_COLOR);
                tts.speak("Vida perdida! Vidas restantes: " + stats.getLives());
                if (stats.isDead()) {
                    tts.speak("Game over!");
                    gameOver();
                    return;
                }
            }
        }

        Iterator<PowerUp> pit = powerUps.iterator();
        while (pit.hasNext()) {
            PowerUp p = pit.next();
            p.update();
            if (p.isCollected() || p.isOffScreen()) { pit.remove(); continue; }
            Rectangle playerZone = new Rectangle(WIDTH / 2 - 200, 580, 400, 60);
            if (p.getBounds().intersects(playerZone)) {
                activatePowerUp(p);
                pit.remove();
            }
        }
    }

    private void activatePowerUp(PowerUp p) {
        p.collect();
        PowerUp.PowerType t = p.getType();
        switch (t) {
            case SHIELD:
                stats.addLife();
                floats.spawnBig(p.getX(), p.getY(), "+VIDA! ESCUDO", t.color);
                break;
            case SLOWMO:
                hud.activateSlowMo(600);
                floats.spawnBig(p.getX(), p.getY(), t.label, t.color);
                break;
            case CLEAR:
                for (Enemy e : enemies) {
                    if (e.isAlive()) {
                        particles.spawnExplosion(e.getCenterX(), e.getCenterY(), e.getColor());
                        e.destroy();
                        stats.registerKill();
                        stats.updateLevel();
                    }
                }
                floats.spawnBig(WIDTH / 2, HEIGHT / 2, "CAMPO LIMPO!", t.color);
                sound.playExplosion();
                break;
            case DOUBLE:
                hud.activateDouble(600);
                floats.spawnBig(p.getX(), p.getY(), "PONTOS x2!", t.color);
                break;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        menuAnimTimer += 0.02f;
        if      (state == GameState.PLAYING)  updatePlaying();
        else if (state == GameState.MENU)     background.update();
        else if (state == GameState.AUDIO)    { background.update(); audioPanel.update(); }
        else if (state == GameState.RECORDES) background.update();
        else if (state == GameState.CUSTOM)   { background.update(); customPanel.update(); }
        else if (state == GameState.VISUAL)   background.update();
        else if (state == GameState.GAMEOVER) { background.update(); particles.update(); floats.update(); }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;

        // Se estiver em tela cheia, aplica escala para preencher o painel
        if (scaleFactor != 1.0) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.scale(scaleFactor, scaleFactor);
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        background.draw(g);

        switch (state) {
            case MENU:     drawMenu(g);     break;
            case PLAYING:  drawPlaying(g);  break;
            case PAUSED:   drawPlaying(g);  drawPauseOverlay(g); break;
            case GAMEOVER: drawGameOver(g); break;
            case AUDIO:    audioPanel.draw(g, WIDTH, HEIGHT); break;
            case RECORDES: drawRecordes(g); break;
            case VISUAL:   drawVisualSettings(g); break;
            case CUSTOM:   customPanel.draw(g, WIDTH, HEIGHT); break;
        }
    }

    private void drawPlaying(Graphics2D g) {
        background.drawPlayerLine(g);
        for (PowerUp p : powerUps) p.draw(g);
        for (Enemy e : enemies) if (e.isAlive()) e.draw(g);
        particles.draw(g);
        floats.draw(g);
        screenFlash.draw(g, WIDTH, HEIGHT);

        int aliveCount = 0;
        for (Enemy e : enemies) if (e.isAlive()) aliveCount++;
        hud.draw(g, stats, inputBuffer.toString(), WIDTH, HEIGHT, aliveCount);
        levelUpAnim.draw(g, WIDTH, HEIGHT);
    }

    private void drawMenu(Graphics2D g) {
        int cx = WIDTH / 2;

        g.setColor(new Color(30, 80, 50, 20));
        for (int y = 0; y < HEIGHT; y += 30) g.drawLine(0, y, WIDTH, y);
        for (int x = 0; x < WIDTH; x += 30) g.drawLine(x, 0, x, HEIGHT);

        drawGlowText(g, "MATEMATICA", cx, 210, 64, new Color(80, 220, 120));

        g.setFont(new Font("Courier New", Font.PLAIN, 13));
        g.setColor(new Color(140, 140, 160));
        String sub = "RESOLVA CONTAS - DESTRUA INIMIGOS - SOBREVIVA";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(sub, cx - fm.stringWidth(sub)/2, 240);

        String[] options = {"> JOGAR", "# PERSONALIZADO", "* RECORDES", ") AUDIO", "@ VISUAL", "X SAIR"};
        for (int i = 0; i < options.length; i++) {
            drawMenuItem(g, options[i], cx, 300 + i * 60, i == menuSelection);
        }


    }

    private void drawMenuItem(Graphics2D g, String text, int cx, int y, boolean selected) {
        int bw = 260; int bh = 38;
        int bx = cx - bw / 2; int by = y - 26;

        if (selected) {
            float pulse = (float)(Math.sin(menuAnimTimer * 4) * 0.3 + 0.7);
            g.setColor(new Color(80, 220, 120, (int)(40 * pulse)));
            g.fillRoundRect(bx - 4, by - 2, bw + 8, bh + 4, 10, 10);
            g.setColor(new Color(80, 220, 120, (int)(200 * pulse)));
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(bx, by, bw, bh, 10, 10);
            g.setStroke(new BasicStroke(1));
            g.setFont(new Font("Courier New", Font.BOLD, 18));
            g.setColor(new Color(160, 255, 180));
        } else {
            g.setColor(new Color(8, 12, 20, 150));
            g.fillRoundRect(bx, by, bw, bh, 10, 10);
            g.setColor(new Color(40, 60, 50, 100));
            g.drawRoundRect(bx, by, bw, bh, 10, 10);
            g.setFont(new Font("Courier New", Font.PLAIN, 16));
            g.setColor(new Color(100, 130, 110));
        }
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, cx - fm.stringWidth(text) / 2, y);
    }

    private void drawPauseOverlay(Graphics2D g) {
        drawDimOverlay(g, 0.7f);
        int cx = WIDTH / 2;
        drawGlowText(g, "PAUSADO", cx, HEIGHT / 2 - 30, 42, new Color(255, 215, 0));
        g.setFont(new Font("Courier New", Font.PLAIN, 16));
        g.setColor(new Color(180, 180, 200));
        String hint = "[ P ] ou [ ESC ] para continuar";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(hint, cx - fm.stringWidth(hint)/2, HEIGHT / 2 + 20);
    }

    private void drawRecordes(Graphics2D g) {
        background.draw(g);
        drawDimOverlay(g, 0.75f);
        int cx = WIDTH / 2;
        drawGlowText(g, "RECORDES", cx, 90, 40, new Color(255, 215, 0));

        List<HighScoreManager.Entry> entries = highScores.getEntries();

        int px = cx - 220; int py = 115;
        int bw = 440;
        int bh = entries.isEmpty() ? 60 : Math.min(10, entries.size()) * 48 + 20;

        g.setColor(new Color(8, 12, 22, 220));
        g.fillRoundRect(px, py, bw, bh, 12, 12);
        g.setColor(new Color(255, 215, 0, 80));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(px, py, bw, bh, 12, 12);
        g.setStroke(new BasicStroke(1));

        if (entries.isEmpty()) {
            g.setFont(new Font("Courier New", Font.PLAIN, 14));
            g.setColor(new Color(140, 150, 170));
            g.drawString("Nenhum recorde ainda. Jogue para pontuar!", px + 20, py + 38);
        } else {
            // Header row
            g.setFont(new Font("Courier New", Font.BOLD, 12));
            g.setColor(new Color(255, 215, 0, 160));
            g.drawString("#",    px + 14, py + 22);
            g.drawString("NOME",      px + 40,  py + 22);
            g.drawString("PONTOS",    px + 180, py + 22);
            g.drawString("NIVEL",     px + 290, py + 22);
            g.drawString("TEMPO",     px + 370, py + 22);
            g.setColor(new Color(255, 215, 0, 50));
            g.drawLine(px + 10, py + 28, px + bw - 10, py + 28);

            for (int i = 0; i < Math.min(10, entries.size()); i++) {
                HighScoreManager.Entry e = entries.get(i);
                int ry = py + 48 + i * 46;
                boolean top3 = i < 3;

                // Row bg
                Color rowC = i == 0 ? new Color(255, 215, 0, 18) :
                             i == 1 ? new Color(200, 200, 200, 12) :
                             i == 2 ? new Color(205, 127, 50, 12) :
                                      new Color(40, 50, 60, 30);
                g.setColor(rowC);
                g.fillRoundRect(px + 6, ry - 28, bw - 12, 40, 6, 6);

                // Rank medal
                Color rankC = i == 0 ? new Color(255, 215, 0) :
                              i == 1 ? new Color(192, 192, 192) :
                              i == 2 ? new Color(205, 127, 50) :
                                       new Color(120, 130, 150);
                g.setFont(new Font("Courier New", Font.BOLD, top3 ? 16 : 13));
                g.setColor(rankC);
                g.drawString(String.valueOf(i + 1), px + 14, ry);

                // Name
                g.setFont(new Font("Courier New", Font.BOLD, top3 ? 15 : 13));
                g.setColor(top3 ? Color.WHITE : new Color(180, 190, 200));
                g.drawString(e.name.length() > 12 ? e.name.substring(0, 12) : e.name, px + 40, ry);

                // Score
                g.setFont(new Font("Courier New", Font.BOLD, top3 ? 15 : 13));
                g.setColor(rankC);
                g.drawString(String.format("%,d", e.score), px + 180, ry);

                // Level
                g.setFont(new Font("Courier New", Font.PLAIN, 12));
                g.setColor(new Color(160, 170, 190));
                g.drawString(String.valueOf(e.level), px + 290, ry);

                // Time
                g.drawString(e.survivedSeconds + "s", px + 370, ry);
            }
        }

        g.setFont(new Font("Courier New", Font.PLAIN, 12));
        g.setColor(new Color(100, 120, 100));
        g.drawString("[ ESC ] ou [ ENTER ] Voltar", cx - 90, HEIGHT - 30);
    }

    private void drawGameOver(Graphics2D g) {
        particles.draw(g);
        screenFlash.draw(g, WIDTH, HEIGHT);
        drawDimOverlay(g, 0.75f);

        int cx = WIDTH / 2;
        drawGlowText(g, "GAME OVER", cx, 120, 52, new Color(255, 60, 60));

        int px = cx - 190; int py = 150;
        g.setColor(new Color(8, 12, 22, 220));
        g.fillRoundRect(px, py, 380, 220, 12, 12);
        g.setColor(new Color(255, 60, 60, 100));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(px, py, 380, 220, 12, 12);
        g.setStroke(new BasicStroke(1));

        drawStat(g, "PONTUACAO",  String.format("%,d", stats.getScore()),  px + 20, py + 45);
        drawStat(g, "NIVEL",      String.valueOf(stats.getLevel()),          px + 20, py + 80);
        drawStat(g, "TEMPO",      stats.getSurvivedSeconds() + "s",          px + 20, py + 115);
        drawStat(g, "ABATIDOS",   String.valueOf(stats.getTotalKills()),      px + 20, py + 150);
        drawStat(g, "COMBO MAX.", String.valueOf(stats.getMaxCombo()),        px + 20, py + 185);

        if (newHighScore && enteringName) {
            drawGlowText(g, "** NOVO RECORDE! **", cx, 405, 20, new Color(255, 215, 0));
            g.setFont(new Font("Courier New", Font.PLAIN, 14));
            g.setColor(new Color(180, 180, 200));
            g.drawString("Digite seu nome (ENTER confirma):", cx - 130, 432);
            String nameDisplay = nameBuffer.toString() + "|";
            g.setFont(new Font("Courier New", Font.BOLD, 22));
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(nameDisplay, cx - fm.stringWidth(nameDisplay)/2, 462);

            // Mini scoreboard preview
            List<HighScoreManager.Entry> entries = highScores.getEntries();
            if (!entries.isEmpty()) {
                g.setFont(new Font("Courier New", Font.PLAIN, 11));
                g.setColor(new Color(100, 110, 130));
                g.drawString("TOP 3 atual:", cx - 50, 495);
                for (int i = 0; i < Math.min(3, entries.size()); i++) {
                    HighScoreManager.Entry e = entries.get(i);
                    g.setColor(i == 0 ? new Color(255,215,0) : new Color(140,150,170));
                    g.drawString(String.format("%d. %-8s %,d", i+1, e.name, e.score),
                        cx - 70, 512 + i * 16);
                }
            }
        } else {
            // Show top 3 scores
            List<HighScoreManager.Entry> entries = highScores.getEntries();
            if (!entries.isEmpty()) {
                g.setFont(new Font("Courier New", Font.BOLD, 13));
                g.setColor(new Color(255, 215, 0));
                g.drawString("TOP RECORDES:", cx - 70, 408);
                g.setFont(new Font("Courier New", Font.PLAIN, 12));
                for (int i = 0; i < Math.min(5, entries.size()); i++) {
                    HighScoreManager.Entry e = entries.get(i);
                    Color c = i == 0 ? new Color(255,215,0) :
                              i == 1 ? new Color(192,192,192) :
                              i == 2 ? new Color(205,127,50) :
                                       new Color(160,170,190);
                    g.setColor(c);
                    g.drawString(String.format("%d. %-10s %,7d pts  Lv%d",
                        i+1, e.name, e.score, e.level), cx - 130, 428 + i * 20);
                }
            }
            g.setFont(new Font("Courier New", Font.PLAIN, 14));
            g.setColor(new Color(140, 200, 140));
            g.drawString("[ ENTER / R ]  Jogar Novamente", cx - 130, HEIGHT - 70);
            g.setColor(new Color(140, 140, 180));
            g.drawString("[ ESC / M ]    Menu Principal",  cx - 120, HEIGHT - 48);
        }
    }

    private void drawVisualSettings(Graphics2D g) {
        drawDimOverlay(g, 0.82f);
        int cx = WIDTH / 2;
        FontSettings fs = FontSettings.getInstance();

        // Título
        drawGlowText(g, "VISUAL", cx, 100, 38, new Color(160, 100, 255));

        g.setFont(new Font("Courier New", Font.PLAIN, 12));
        g.setColor(new Color(120, 130, 150));
        String sub = "Ajuste o tamanho das fontes do jogo";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(sub, cx - fm.stringWidth(sub)/2, 128);

        // Painel central
        int pw = 480; int ph = 260;
        int px = cx - pw/2; int py = 155;
        g.setColor(new Color(8, 12, 22, 220));
        g.fillRoundRect(px, py, pw, ph, 14, 14);
        g.setColor(new Color(160, 100, 255, 80));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(px, py, pw, ph, 14, 14);
        g.setStroke(new BasicStroke(1));

        // Label principal
        g.setFont(new Font("Courier New", Font.PLAIN, 13));
        g.setColor(new Color(160, 170, 190));
        g.drawString("TAMANHO DAS FONTES", px + 20, py + 36);

        // Valor atual em destaque
        FontSettings.FontSize cur = fs.getCurrent();
        g.setFont(new Font("Courier New", Font.BOLD, 32));
        g.setColor(new Color(200, 150, 255));
        fm = g.getFontMetrics();
        g.drawString(cur.label.toUpperCase(), cx - fm.stringWidth(cur.label.toUpperCase())/2, py + 95);

        // Barra de opções
        FontSettings.FontSize[] sizes = FontSettings.FontSize.values();
        int barY = py + 125;
        int slotW = pw / sizes.length;
        for (int i = 0; i < sizes.length; i++) {
            boolean sel = sizes[i] == cur;
            int sx = px + i * slotW;
            Color c = sel ? new Color(160, 100, 255) : new Color(60, 50, 80);
            g.setColor(sel ? new Color(160, 100, 255, 40) : new Color(10, 10, 20, 100));
            g.fillRoundRect(sx + 8, barY, slotW - 16, 36, 8, 8);
            g.setColor(sel ? new Color(160, 100, 255, 180) : new Color(60, 50, 80, 100));
            g.setStroke(sel ? new BasicStroke(1.8f) : new BasicStroke(0.8f));
            g.drawRoundRect(sx + 8, barY, slotW - 16, 36, 8, 8);
            g.setStroke(new BasicStroke(1));
            g.setFont(new Font("Courier New", sel ? Font.BOLD : Font.PLAIN, sel ? 14 : 12));
            g.setColor(sel ? Color.WHITE : new Color(120, 110, 150));
            fm = g.getFontMetrics();
            g.drawString(sizes[i].label, sx + slotW/2 - fm.stringWidth(sizes[i].label)/2, barY + 24);
        }

        // Preview de texto
        int prevY = barY + 65;
        g.setColor(new Color(40, 35, 55));
        g.fillRoundRect(px + 16, prevY, pw - 32, 62, 8, 8);
        g.setColor(new Color(80, 60, 120, 120));
        g.drawRoundRect(px + 16, prevY, pw - 32, 62, 8, 8);

        g.setFont(new Font("Courier New", Font.PLAIN, 10));
        g.setColor(new Color(100, 90, 130));
        g.drawString("PREVIA:", px + 28, prevY + 16);

        // Mostra a conta no tamanho escalado
        g.setFont(new Font("Courier New", Font.BOLD, fs.scale(20)));
        g.setColor(new Color(80, 220, 120));
        String preview = "12 + 7 = ?";
        fm = g.getFontMetrics();
        g.drawString(preview, cx - fm.stringWidth(preview)/2, prevY + 46);

        // Rodapé
        g.setFont(new Font("Courier New", Font.PLAIN, 11));
        g.setColor(new Color(90, 80, 120));
        String hint = "[ ← → ] Mudar tamanho   [ ESC / ENTER ] Voltar";
        fm = g.getFontMetrics();
        g.drawString(hint, cx - fm.stringWidth(hint)/2, HEIGHT - 28);
    }

    private void drawGlowText(Graphics2D g, String text, int cx, int y, int size, Color color) {
        g.setFont(new Font("Courier New", Font.BOLD, size));
        FontMetrics fm = g.getFontMetrics();
        int tx = cx - fm.stringWidth(text) / 2;
        for (int i = 4; i >= 1; i--) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30 / i));
            g.drawString(text, tx - i, y + i);
            g.drawString(text, tx + i, y - i);
        }
        g.setColor(color);
        g.drawString(text, tx, y);
    }

    private void drawDimOverlay(Graphics2D g, float alpha) {
        g.setColor(new Color(0, 0, 0, (int)(alpha * 255)));
        g.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private void drawStat(Graphics2D g, String label, String value, int x, int y) {
        g.setFont(new Font("Courier New", Font.PLAIN, 12));
        g.setColor(new Color(120, 130, 150));
        g.drawString(label + ":", x, y);
        g.setFont(new Font("Courier New", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString(value, x + 140, y);
    }
}
