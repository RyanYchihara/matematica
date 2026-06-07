package mathinvader.ui;

import mathinvader.audio.SoundManager;
import mathinvader.audio.TTSEngine;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Tela de Acessibilidade - Audio e Narração.
 * Layout recalculado para caber todas as opções sem cortar na tela de 700px.
 */
public class AccessibilityAudioPanel {

    // ─── Seções ──────────────────────────────────────────────────────────────
    private static final int SECTION_TTS    = 0;
    private static final int SECTION_SOUNDS = 1;
    private static final int TOTAL_SECTIONS = 2;

    // Opções TTS
    private static final int OPT_TTS_TOGGLE   = 0;
    private static final int OPT_TTS_RATE      = 1;
    private static final int OPT_TTS_TEST      = 2;
    private static final int OPT_TTS_HEAR_ENEMY= 3;
    private static final int TTS_OPTS = 4;

    // Opções Sons
    private static final int OPT_SND_MUSIC   = 0;
    private static final int OPT_SND_EFFECTS = 1;
    private static final int OPT_SND_ACERTO  = 2;
    private static final int OPT_SND_ERRO    = 3;
    private static final int OPT_SND_EXPLOS  = 4;
    private static final int OPT_SND_VIDA    = 5;
    private static final int OPT_SND_OVER    = 6;
    private static final int SND_OPTS = 7;

    // ─── Layout fixo calculado para caber em 700px ───────────────────────────
    // Título:          y=50  (altura ~25)
    // Subtítulo:       y=72
    // Header TTS:      y=88
    // Opções TTS:      y=108, rowH=34  → 4 itens → até y=244
    // Header Sons:     y=250
    // Opções Sons:     y=270, rowH=30  → 7 itens → até y=480
    // Feedback:        y=510
    // Rodapé hints:    y=680..695

    private static final int HDR_TTS_Y   = 88;
    private static final int TTS_START_Y = 108;
    private static final int TTS_ROW_H   = 34;

    private static final int HDR_SND_Y   = 250;
    private static final int SND_START_Y = 270;
    private static final int SND_ROW_H   = 30;

    // ─── Estado ──────────────────────────────────────────────────────────────
    private int section   = SECTION_TTS;
    private int selection = 0;
    private float animTimer = 0;
    private String feedbackMsg   = "";
    private int    feedbackTimer = 0;
    private Color  feedbackColor = new Color(80, 220, 120);

    private final TTSEngine    tts   = TTSEngine.getInstance();
    private final SoundManager sound = SoundManager.getInstance();

    // ─── Update ──────────────────────────────────────────────────────────────

    public void update() {
        animTimer += 0.05f;
        if (feedbackTimer > 0) feedbackTimer--;
    }

    // ─── Navegação ───────────────────────────────────────────────────────────

    public void moveUp() {
        if (selection > 0) {
            selection--;
        } else if (section == SECTION_SOUNDS) {
            // Ao chegar no topo dos Sons, sobe para o TTS
            section = SECTION_TTS;
            selection = TTS_OPTS - 1;
            tts.speak("Seção narração por voz");
        }
        announceSelection();
    }

    public void moveDown() {
        int max = section == SECTION_TTS ? TTS_OPTS - 1 : SND_OPTS - 1;
        if (selection < max) {
            selection++;
        } else if (section == SECTION_TTS) {
            // Ao chegar no fim do TTS, desce para os Sons
            section = SECTION_SOUNDS;
            selection = 0;
            tts.speak("Seção efeitos sonoros");
        }
        announceSelection();
    }

    public void switchSection(int dir) {
        section = (section + dir + TOTAL_SECTIONS) % TOTAL_SECTIONS;
        selection = 0;
        tts.speak(section == SECTION_TTS ? "Seção narração por voz" : "Seção efeitos sonoros");
    }

    public void activate() {
        if (section == SECTION_TTS) activateTTS();
        else                        activateSound();
    }

    public void adjustLeft()  { adjust(-1); }
    public void adjustRight() { adjust(+1); }

    // ─── Ações ───────────────────────────────────────────────────────────────

    private void activateTTS() {
        switch (selection) {
            case OPT_TTS_TOGGLE:
                tts.toggle();
                String msg = tts.isEnabled() ? "Narração ativada" : "Narração desativada";
                feedback(msg, tts.isEnabled() ? new Color(80,220,120) : new Color(200,80,80));
                if (tts.isEnabled()) tts.speak("Narração por voz ativada com sucesso!");
                break;

            case OPT_TTS_RATE:
                float r = tts.getRate();
                if      (r < 0.8f) { tts.setRate(1.0f); feedback("Velocidade: Normal", new Color(255,215,0)); }
                else if (r < 1.4f) { tts.setRate(1.6f); feedback("Velocidade: Rapido", new Color(255,150,40)); }
                else               { tts.setRate(0.6f); feedback("Velocidade: Lento",  new Color(100,180,255)); }
                tts.speak("Velocidade ajustada.");
                break;

            case OPT_TTS_TEST:
                feedback("Testando narração...", new Color(120,200,255));
                tts.speak("Olá! A narração por voz está funcionando. Você ouvirá as contas matemáticas assim: doze mais sete.");
                break;

            case OPT_TTS_HEAR_ENEMY:
                feedback("Ouvindo exemplo de inimigo...", new Color(80,220,120));
                tts.speakEquation("12 + 7 =");
                break;
        }
    }

    private void activateSound() {
        SoundManager s = SoundManager.getInstance();
        switch (selection) {
            case OPT_SND_MUSIC:
                s.toggleMusic();
                feedback(s.isMusicEnabled() ? "Musica: Ligada" : "Musica: Desligada", new Color(120,200,255));
                tts.speak(s.isMusicEnabled() ? "Música ligada" : "Música desligada");
                break;
            case OPT_SND_EFFECTS:
                s.toggleSound();
                feedback(s.isSoundEnabled() ? "Efeitos: Ligados" : "Efeitos: Desligados", new Color(120,200,255));
                tts.speak(s.isSoundEnabled() ? "Efeitos sonoros ligados" : "Efeitos sonoros desligados");
                break;
            case OPT_SND_ACERTO:  s.playCorrect();     feedback(">> Tocando: Acerto",       new Color(80,220,120));  tts.speak("Som de acerto"); break;
            case OPT_SND_ERRO:    s.playWrong();       feedback(">> Tocando: Erro",         new Color(255,80,80));   tts.speak("Som de erro"); break;
            case OPT_SND_EXPLOS:  s.playExplosion();   feedback(">> Tocando: Explosao",     new Color(255,150,40));  tts.speak("Som de explosão"); break;
            case OPT_SND_VIDA:    s.playEnemyReach();  feedback(">> Tocando: Vida perdida", new Color(80,160,255));  tts.speak("Som de vida perdida"); break;
            case OPT_SND_OVER:    s.playGameOver();    feedback(">> Tocando: Game Over",    new Color(200,80,200));  tts.speak("Som de fim de jogo"); break;
        }
    }

    private void adjust(int dir) {
        if (section == SECTION_TTS && selection == OPT_TTS_RATE) {
            tts.setRate(tts.getRate() + dir * 0.2f);
            feedback("Velocidade: " + String.format("%.1fx", tts.getRate()), new Color(255,215,0));
            tts.speak("Velocidade ajustada.");
        }
    }

    private void announceSelection() {
        if (!tts.isEnabled()) return;
        if (section == SECTION_TTS) {
            String[] names = {"Narração por voz","Velocidade da fala","Testar narração","Ouvir exemplo de inimigo"};
            if (selection < names.length) tts.speak(names[selection]);
        } else {
            String[] names = {"Música de fundo","Efeitos sonoros","Som de acerto","Som de erro","Som de explosão","Som de vida perdida","Som de game over"};
            if (selection < names.length) tts.speak(names[selection]);
        }
    }

    private void feedback(String msg, Color c) {
        feedbackMsg   = msg;
        feedbackColor = c;
        feedbackTimer = 150;
    }

    // ─── Desenho ─────────────────────────────────────────────────────────────

    public void draw(Graphics2D g, int W, int H) {
        // Overlay escuro
        g.setColor(new Color(0, 0, 0, 210));
        g.fillRect(0, 0, W, H);

        int cx = W / 2;

        // Título
        drawGlow(g, "AUDIO / ACESSIBILIDADE", cx, 52, 24, new Color(120, 200, 255));

        g.setFont(new Font("Courier New", Font.PLAIN, 10));
        g.setColor(new Color(90, 100, 120));
        String sub = "[ ↑↓ ] Navegar / Trocar seção   [ ENTER ] Ativar   [ ← → ] Ajustar   [ Q ] Próxima seção   [ ESC ] Voltar";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(sub, cx - fm.stringWidth(sub)/2, 70);

        // ── Seção TTS ────────────────────────────────────────────────────────
        boolean ttsActive = section == SECTION_TTS;
        drawSectionHeader(g, "NARRACAO POR VOZ", cx, HDR_TTS_Y, ttsActive, new Color(120,200,255));

        if (!tts.isSupported()) {
            g.setFont(new Font("Courier New", Font.ITALIC, 11));
            g.setColor(new Color(180, 100, 100));
            g.drawString("Sistema nao suportado para narracao. (Windows, macOS ou Linux com espeak)", cx - 240, TTS_START_Y + 20);
        } else {
            drawTTSOptions(g, cx, ttsActive);
        }

        // ── Seção Sons ───────────────────────────────────────────────────────
        boolean sndActive = section == SECTION_SOUNDS;
        drawSectionHeader(g, "EFEITOS SONOROS", cx, HDR_SND_Y, sndActive, new Color(255,160,40));
        drawSoundOptions(g, cx, sndActive);

        // ── Feedback ─────────────────────────────────────────────────────────
        if (feedbackTimer > 0) {
            float alpha = Math.min(1f, feedbackTimer / 30f);
            g.setFont(new Font("Courier New", Font.BOLD, 13));
            Color fc = new Color(feedbackColor.getRed(), feedbackColor.getGreen(),
                feedbackColor.getBlue(), (int)(alpha * 255));
            g.setColor(fc);
            fm = g.getFontMetrics();
            int feedY = SND_START_Y + SND_OPTS * SND_ROW_H + 24;
            g.drawString(feedbackMsg, cx - fm.stringWidth(feedbackMsg)/2, feedY);
        }
    }

    // ─── Cabeçalho de seção ──────────────────────────────────────────────────

    private void drawSectionHeader(Graphics2D g, String title, int cx, int y,
                                   boolean active, Color c) {
        int bw = 520; int bh = 18;
        int bx = cx - bw/2;

        Color bg = active ? new Color(c.getRed(), c.getGreen(), c.getBlue(), 22)
                          : new Color(10, 14, 22, 100);
        g.setColor(bg);
        g.fillRoundRect(bx, y, bw, bh, 6, 6);

        Color border = active ? new Color(c.getRed(), c.getGreen(), c.getBlue(), 150)
                              : new Color(40, 50, 65, 100);
        g.setColor(border);
        g.setStroke(active ? new BasicStroke(1.5f) : new BasicStroke(0.8f));
        g.drawRoundRect(bx, y, bw, bh, 6, 6);
        g.setStroke(new BasicStroke(1));

        g.setFont(new Font("Courier New", Font.BOLD, 11));
        g.setColor(active ? c : new Color(90, 100, 120));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, cx - fm.stringWidth(title)/2, y + 13);
    }

    // ─── Opções TTS ──────────────────────────────────────────────────────────

    private void drawTTSOptions(Graphics2D g, int cx, boolean sectionActive) {
        // Toggle
        drawRow(g, cx, TTS_START_Y,
            OPT_TTS_TOGGLE, sectionActive,
            "Narracao por Voz",
            tts.isEnabled() ? "ATIVADA" : "DESATIVADA",
            tts.isEnabled() ? new Color(80,220,120) : new Color(180,80,80),
            TTS_ROW_H);

        // Velocidade
        float r = tts.getRate();
        String rateLabel = r < 0.8f ? "LENTO" : r < 1.4f ? "NORMAL" : "RAPIDO";
        Color  rateColor = r < 0.8f ? new Color(100,180,255) : r < 1.4f ? new Color(255,215,0) : new Color(255,130,40);
        drawRow(g, cx, TTS_START_Y + TTS_ROW_H,
            OPT_TTS_RATE, sectionActive,
            "Velocidade da Fala",
            rateLabel + "  (" + String.format("%.1f", r) + "x)   [ ← → ]",
            rateColor,
            TTS_ROW_H);

        // Testar
        drawRow(g, cx, TTS_START_Y + TTS_ROW_H * 2,
            OPT_TTS_TEST, sectionActive,
            "Testar Narracao",
            "[ ENTER ] ouvir frase",
            new Color(120,200,255),
            TTS_ROW_H);

        // Demo inimigo
        drawRow(g, cx, TTS_START_Y + TTS_ROW_H * 3,
            OPT_TTS_HEAR_ENEMY, sectionActive,
            "Exemplo: Como inimigo e anunciado",
            "[ ENTER ] ouvir '12 + 7'",
            new Color(80,220,120),
            TTS_ROW_H);
    }

    // ─── Opções Sons ─────────────────────────────────────────────────────────

    private void drawSoundOptions(Graphics2D g, int cx, boolean sectionActive) {
        SoundManager s = SoundManager.getInstance();

        Object[][] opts = {
            {"Musica de Fundo",
             s.isMusicEnabled()  ? "LIGADA"   : "DESLIGADA",
             s.isMusicEnabled()  ? new Color(80,220,120) : new Color(180,80,80),
             OPT_SND_MUSIC},
            {"Efeitos Sonoros",
             s.isSoundEnabled()  ? "LIGADOS"  : "DESLIGADOS",
             s.isSoundEnabled()  ? new Color(80,220,120) : new Color(180,80,80),
             OPT_SND_EFFECTS},
            {"Som de Acerto",    "ENTER para ouvir", new Color(80,220,120),  OPT_SND_ACERTO},
            {"Som de Erro",      "ENTER para ouvir", new Color(255,80,80),   OPT_SND_ERRO},
            {"Som de Explosao",  "ENTER para ouvir", new Color(255,150,40),  OPT_SND_EXPLOS},
            {"Som de Vida Perdida","ENTER para ouvir",new Color(80,160,255), OPT_SND_VIDA},
            {"Som de Game Over", "ENTER para ouvir", new Color(200,80,200),  OPT_SND_OVER},
        };

        for (int i = 0; i < opts.length; i++) {
            drawRow(g, cx, SND_START_Y + i * SND_ROW_H,
                (Integer) opts[i][3], sectionActive,
                (String) opts[i][0], (String) opts[i][1], (Color) opts[i][2],
                SND_ROW_H);
        }
    }

    // ─── Linha genérica ──────────────────────────────────────────────────────

    private void drawRow(Graphics2D g, int cx, int y, int optIdx,
                         boolean sectionActive, String label, String value,
                         Color valueColor, int rowH) {
        boolean sel = sectionActive && selection == optIdx;
        float pulse = (float)(Math.sin(animTimer * 4) * 0.3 + 0.7);

        int bw = 520; int bh = rowH - 4;
        int bx = cx - bw / 2;

        if (sel) {
            g.setColor(new Color(valueColor.getRed(), valueColor.getGreen(),
                valueColor.getBlue(), (int)(28 * pulse)));
            g.fill(new RoundRectangle2D.Float(bx - 2, y - 1, bw + 4, bh + 2, 10, 10));
            g.setColor(new Color(valueColor.getRed(), valueColor.getGreen(),
                valueColor.getBlue(), (int)(180 * pulse)));
            g.setStroke(new BasicStroke(1.8f));
            g.draw(new RoundRectangle2D.Float(bx, y, bw, bh, 8, 8));
            g.setStroke(new BasicStroke(1));
        } else {
            g.setColor(new Color(10, 14, 24, 130));
            g.fill(new RoundRectangle2D.Float(bx, y, bw, bh, 8, 8));
            g.setColor(new Color(30, 38, 50, 80));
            g.draw(new RoundRectangle2D.Float(bx, y, bw, bh, 8, 8));
        }

        // Label
        int fontSize = sel ? 12 : 11;
        g.setFont(new Font("Courier New", sel ? Font.BOLD : Font.PLAIN, fontSize));
        g.setColor(sel ? Color.WHITE : new Color(150, 160, 178));
        int textY = y + bh / 2 + 5;
        g.drawString(label, bx + 12, textY);

        // Value alinhado à direita
        g.setFont(new Font("Courier New", Font.BOLD, 11));
        g.setColor(sel ? valueColor : valueColor.darker());
        FontMetrics fm = g.getFontMetrics();
        g.drawString(value, bx + bw - fm.stringWidth(value) - 12, textY);
    }

    // ─── Utilitários ─────────────────────────────────────────────────────────

    private void drawGlow(Graphics2D g, String text, int cx, int y, int size, Color color) {
        g.setFont(new Font("Courier New", Font.BOLD, size));
        FontMetrics fm = g.getFontMetrics();
        int tx = cx - fm.stringWidth(text) / 2;
        for (int i = 3; i >= 1; i--) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 25 / i));
            g.drawString(text, tx - i, y + i);
            g.drawString(text, tx + i, y - i);
        }
        g.setColor(color);
        g.drawString(text, tx, y);
    }
}
