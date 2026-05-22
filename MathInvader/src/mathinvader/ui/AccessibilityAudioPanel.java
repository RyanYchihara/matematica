package mathinvader.ui;

import mathinvader.audio.SoundManager;
import mathinvader.audio.TTSEngine;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Tela de Acessibilidade - Audio e Narração.
 * Permite configurar sons e narração por voz para pessoas cegas ou com
 * baixa visão, sem depender de bibliotecas externas.
 */
public class AccessibilityAudioPanel {

    // ─── Seções de configuração ──────────────────────────────────────────────
    private static final int SECTION_TTS    = 0;
    private static final int SECTION_SOUNDS = 1;
    private static final int TOTAL_SECTIONS = 2;

    // Opções dentro da seção TTS
    private static final int OPT_TTS_TOGGLE  = 0;
    private static final int OPT_TTS_RATE    = 1;
    private static final int OPT_TTS_TEST    = 2;
    private static final int OPT_TTS_HEAR_ENEMY = 3; // demo: ouça como um inimigo é anunciado
    private static final int TTS_OPTS = 4;

    // Opções dentro da seção Sons
    private static final int OPT_SND_MUSIC  = 0;
    private static final int OPT_SND_EFFECTS= 1;
    private static final int OPT_SND_ACERTO = 2;
    private static final int OPT_SND_ERRO   = 3;
    private static final int OPT_SND_EXPLOS = 4;
    private static final int OPT_SND_VIDA   = 5;
    private static final int OPT_SND_OVER   = 6;
    private static final int SND_OPTS = 7;

    private int section   = SECTION_TTS;
    private int selection = 0;
    private float animTimer = 0;
    private String feedbackMsg  = "";
    private int    feedbackTimer = 0;
    private Color  feedbackColor = new Color(80, 220, 120);

    private final TTSEngine   tts   = TTSEngine.getInstance();
    private final SoundManager sound = SoundManager.getInstance();

    public void update() {
        animTimer += 0.05f;
        if (feedbackTimer > 0) feedbackTimer--;
    }

    // ─── Navegação ───────────────────────────────────────────────────────────

    public void moveUp() {
        selection = Math.max(0, selection - 1);
        announceSelection();
    }

    public void moveDown() {
        int max = section == SECTION_TTS ? TTS_OPTS - 1 : SND_OPTS - 1;
        selection = Math.min(max, selection + 1);
        announceSelection();
    }

    public void switchSection(int dir) {
        section = (section + dir + TOTAL_SECTIONS) % TOTAL_SECTIONS;
        selection = 0;
        tts.speak(section == SECTION_TTS ? "Seção narração por voz" : "Seção efeitos sonoros");
    }

    public void activate() {
        if (section == SECTION_TTS)    activateTTS();
        else                           activateSound();
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
                // Fala o status DEPOIS de ativar
                if (tts.isEnabled()) tts.speak("Narração por voz ativada com sucesso!");
                break;

            case OPT_TTS_RATE:
                // Ciclo: lento -> normal -> rápido
                float r = tts.getRate();
                if (r < 0.8f)      { tts.setRate(1.0f); feedback("Velocidade: Normal", new Color(255,215,0)); }
                else if (r < 1.4f) { tts.setRate(1.6f); feedback("Velocidade: Rapido", new Color(255,150,40)); }
                else               { tts.setRate(0.6f); feedback("Velocidade: Lento",  new Color(100,180,255)); }
                tts.speak("Velocidade ajustada.");
                break;

            case OPT_TTS_TEST:
                feedback("Testando narração...", new Color(120,200,255));
                tts.speak("Olá! A narração por voz está funcionando. Você ouvirá as contas matemáticas assim: doze mais sete, quanto é?");
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
            case OPT_SND_MUSIC:  s.toggleMusic();  feedback(s.isMusicEnabled() ? "Musica: Ligada" : "Musica: Desligada", new Color(120,200,255)); tts.speak(s.isMusicEnabled() ? "Música ligada" : "Música desligada"); break;
            case OPT_SND_EFFECTS:s.toggleSound();  feedback(s.isSoundEnabled() ? "Efeitos: Ligados" : "Efeitos: Desligados", new Color(120,200,255)); tts.speak(s.isSoundEnabled() ? "Efeitos sonoros ligados" : "Efeitos sonoros desligados"); break;
            case OPT_SND_ACERTO: s.playCorrect();  feedback(">> Tocando: Acerto",  new Color(80,220,120)); tts.speak("Som de acerto"); break;
            case OPT_SND_ERRO:   s.playWrong();    feedback(">> Tocando: Erro",    new Color(255,80,80));  tts.speak("Som de erro"); break;
            case OPT_SND_EXPLOS: s.playExplosion();feedback(">> Tocando: Explosao",new Color(255,150,40)); tts.speak("Som de explosão"); break;
            case OPT_SND_VIDA:   s.playEnemyReach();feedback(">> Tocando: Vida perdida",new Color(80,160,255)); tts.speak("Som de vida perdida"); break;
            case OPT_SND_OVER:   s.playGameOver(); feedback(">> Tocando: Game Over",new Color(200,80,200)); tts.speak("Som de fim de jogo"); break;
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
        // Narrar o item selecionado para quem não enxerga
        if (!tts.isEnabled()) return;
        if (section == SECTION_TTS) {
            String[] names = {"Narração por voz", "Velocidade da fala", "Testar narração", "Ouvir exemplo de inimigo"};
            if (selection < names.length) tts.speak(names[selection]);
        } else {
            String[] names = {"Música de fundo", "Efeitos sonoros", "Som de acerto", "Som de erro", "Som de explosão", "Som de vida perdida", "Som de game over"};
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
        // overlay
        g.setColor(new Color(0, 0, 0, 215));
        g.fillRect(0, 0, W, H);

        int cx = W / 2;
        Color titleColor = new Color(120, 200, 255);

        drawGlow(g, "ACESSIBILIDADE", cx, 68, 30, titleColor);

        g.setFont(new Font("Courier New", Font.PLAIN, 11));
        g.setColor(new Color(120, 130, 150));
        String sub = "Configuracoes de audio e narração para acessibilidade";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(sub, cx - fm.stringWidth(sub)/2, 90);

        // ── Seção TTS ────────────────────────────────────────────────────────
        boolean ttsActive = section == SECTION_TTS;
        drawSectionHeader(g, "NARRACAO POR VOZ", cx - 180, 112, 360, ttsActive, new Color(120,200,255));

        if (!tts.isSupported()) {
            g.setFont(new Font("Courier New", Font.ITALIC, 11));
            g.setColor(new Color(180, 100, 100));
            g.drawString("Sistema operacional não suportado para narração.",
                cx - 170, 145);
            g.drawString("(Compatível com Windows, macOS e Linux com espeak)",
                cx - 200, 160);
        } else {
            drawTTSOptions(g, cx, ttsActive);
        }

        // ── Seção Sons ───────────────────────────────────────────────────────
        boolean sndActive = section == SECTION_SOUNDS;
        drawSectionHeader(g, "EFEITOS SONOROS", cx - 180, 340, 360, sndActive, new Color(255,160,40));
        drawSoundOptions(g, cx, sndActive);

        // ── Feedback ─────────────────────────────────────────────────────────
        if (feedbackTimer > 0) {
            float alpha = Math.min(1f, feedbackTimer / 30f);
            g.setFont(new Font("Courier New", Font.BOLD, 13));
            Color fc = new Color(feedbackColor.getRed(), feedbackColor.getGreen(),
                feedbackColor.getBlue(), (int)(alpha * 255));
            g.setColor(fc);
            fm = g.getFontMetrics();
            g.drawString(feedbackMsg, cx - fm.stringWidth(feedbackMsg)/2, H - 90);
        }

        // ── Rodapé ───────────────────────────────────────────────────────────
        g.setFont(new Font("Courier New", Font.PLAIN, 10));
        g.setColor(new Color(70, 80, 100));
        String[] hints = {
            "[ Setas ↑↓ ] Navegar opcoes",
            "[ ENTER ] Ativar / Testar",
            "[ ← → ] Ajustar valor",
            "[ Tab ] Trocar secao",
            "[ ESC ] Voltar ao menu"
        };
        int hx = cx - 280;
        for (int i = 0; i < hints.length; i++) {
            g.drawString(hints[i], hx + i * 115, H - 18);
        }
    }

    private void drawSectionHeader(Graphics2D g, String title, int x, int y,
                                   int w, boolean active, Color c) {
        // Background da seção
        Color bg = active ? new Color(c.getRed(), c.getGreen(), c.getBlue(), 18)
                          : new Color(10, 14, 22, 120);
        g.setColor(bg);
        g.fillRoundRect(x, y, w, 18, 6, 6);

        Color border = active ? new Color(c.getRed(), c.getGreen(), c.getBlue(), 140)
                              : new Color(40, 50, 65, 120);
        g.setColor(border);
        g.setStroke(active ? new BasicStroke(1.5f) : new BasicStroke(0.8f));
        g.drawRoundRect(x, y, w, 18, 6, 6);
        g.setStroke(new BasicStroke(1));

        g.setFont(new Font("Courier New", Font.BOLD, 12));
        g.setColor(active ? c : new Color(100, 110, 130));
        FontMetrics fm = g.getFontMetrics();
        int tx = x + w/2 - fm.stringWidth(title)/2;
        g.drawString(title, tx, y + 13);
    }

    private void drawTTSOptions(Graphics2D g, int cx, boolean sectionActive) {
        TTSEngine tts = TTSEngine.getInstance();
        int startY = 142;
        int rowH   = 46;

        // Linha 0: Toggle narração
        drawOption(g, cx, startY, OPT_TTS_TOGGLE, sectionActive,
            "Narracao por Voz",
            tts.isEnabled() ? "ATIVADA" : "DESATIVADA",
            tts.isEnabled() ? new Color(80,220,120) : new Color(180,80,80),
            "Fala as contas em voz alta para pessoas com deficiencia visual");

        // Linha 1: Velocidade
        String rateLabel;
        Color  rateColor;
        float r = tts.getRate();
        if      (r < 0.8f)  { rateLabel = "LENTO";  rateColor = new Color(100,180,255); }
        else if (r < 1.4f)  { rateLabel = "NORMAL"; rateColor = new Color(255,215,0); }
        else                 { rateLabel = "RAPIDO"; rateColor = new Color(255,130,40); }
        drawOption(g, cx, startY + rowH, OPT_TTS_RATE, sectionActive,
            "Velocidade da Fala",
            rateLabel + "  (" + String.format("%.1f", r) + "x)",
            rateColor,
            "Use [ ← → ] ou ENTER para mudar a velocidade");

        // Linha 2: Testar
        drawOption(g, cx, startY + rowH*2, OPT_TTS_TEST, sectionActive,
            "Testar Narracao",
            "[ ENTER ] para ouvir",
            new Color(120,200,255),
            "Toca uma frase de exemplo para verificar se esta funcionando");

        // Linha 3: Demo inimigo
        drawOption(g, cx, startY + rowH*3, OPT_TTS_HEAR_ENEMY, sectionActive,
            "Exemplo: Como um inimigo e anunciado",
            "[ ENTER ] ouvir '12 + 7, quanto e?'",
            new Color(80,220,120),
            "Assim e como o jogo ira falar as contas para voce");
    }

    private void drawSoundOptions(Graphics2D g, int cx, boolean sectionActive) {
        SoundManager s = SoundManager.getInstance();
        int startY = 368;
        int rowH   = 40;

        Object[][] opts = {
            {"Musica de Fundo",   s.isMusicEnabled()  ? "LIGADA"  : "DESLIGADA",
             s.isMusicEnabled()  ? new Color(80,220,120) : new Color(180,80,80), OPT_SND_MUSIC},
            {"Efeitos Sonoros",   s.isSoundEnabled()  ? "LIGADOS" : "DESLIGADOS",
             s.isSoundEnabled()  ? new Color(80,220,120) : new Color(180,80,80), OPT_SND_EFFECTS},
            {"Som de Acerto",     "ENTER para ouvir", new Color(80,220,120),  OPT_SND_ACERTO},
            {"Som de Erro",       "ENTER para ouvir", new Color(255,80,80),   OPT_SND_ERRO},
            {"Som de Explosao",   "ENTER para ouvir", new Color(255,150,40),  OPT_SND_EXPLOS},
            {"Som de Vida Perdida","ENTER para ouvir",new Color(80,160,255),  OPT_SND_VIDA},
            {"Som de Game Over",  "ENTER para ouvir", new Color(200,80,200),  OPT_SND_OVER},
        };

        for (int i = 0; i < opts.length; i++) {
            int optIdx = (Integer) opts[i][3];
            drawOptionCompact(g, cx, startY + i * rowH, optIdx, sectionActive,
                (String) opts[i][0], (String) opts[i][1], (Color) opts[i][2]);
        }
    }

    private void drawOption(Graphics2D g, int cx, int y, int optIdx,
                            boolean sectionActive, String label, String value,
                            Color valueColor, String description) {
        boolean sel = sectionActive && selection == optIdx;
        float pulse = (float)(Math.sin(animTimer * 4) * 0.3 + 0.7);

        int bw = 500; int bh = 40;
        int bx = cx - bw/2;

        Color accent = valueColor;
        if (sel) {
            g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(25*pulse)));
            g.fill(new RoundRectangle2D.Float(bx - 3, y - 2, bw + 6, bh + 4, 10, 10));
            g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(160*pulse)));
            g.setStroke(new BasicStroke(1.8f));
            g.draw(new RoundRectangle2D.Float(bx, y, bw, bh, 8, 8));
            g.setStroke(new BasicStroke(1));
        } else {
            g.setColor(new Color(10, 14, 24, 150));
            g.fill(new RoundRectangle2D.Float(bx, y, bw, bh, 8, 8));
            g.setColor(new Color(35, 45, 58, 120));
            g.draw(new RoundRectangle2D.Float(bx, y, bw, bh, 8, 8));
        }

        // Label
        g.setFont(new Font("Courier New", Font.BOLD, sel ? 13 : 12));
        g.setColor(sel ? Color.WHITE : new Color(160, 170, 185));
        g.drawString(label, bx + 14, y + 16);

        // Value (alinhado à direita)
        g.setFont(new Font("Courier New", Font.BOLD, 12));
        g.setColor(sel ? valueColor : valueColor.darker());
        FontMetrics fm = g.getFontMetrics();
        g.drawString(value, bx + bw - fm.stringWidth(value) - 14, y + 16);

        // Description
        if (sel) {
            g.setFont(new Font("Courier New", Font.PLAIN, 10));
            g.setColor(new Color(160, 170, 190));
            g.drawString(description, bx + 14, y + 32);
        }
    }

    private void drawOptionCompact(Graphics2D g, int cx, int y, int optIdx,
                                   boolean sectionActive, String label,
                                   String value, Color valueColor) {
        boolean sel = sectionActive && selection == optIdx;
        float pulse = (float)(Math.sin(animTimer * 4) * 0.3 + 0.7);

        int bw = 500; int bh = 32;
        int bx = cx - bw/2;

        if (sel) {
            g.setColor(new Color(valueColor.getRed(), valueColor.getGreen(),
                valueColor.getBlue(), (int)(22*pulse)));
            g.fill(new RoundRectangle2D.Float(bx - 2, y - 1, bw + 4, bh + 2, 8, 8));
            g.setColor(new Color(valueColor.getRed(), valueColor.getGreen(),
                valueColor.getBlue(), (int)(150*pulse)));
            g.setStroke(new BasicStroke(1.5f));
            g.draw(new RoundRectangle2D.Float(bx, y, bw, bh, 6, 6));
            g.setStroke(new BasicStroke(1));
        } else {
            g.setColor(new Color(10, 14, 24, 120));
            g.fill(new RoundRectangle2D.Float(bx, y, bw, bh, 6, 6));
        }

        g.setFont(new Font("Courier New", sel ? Font.BOLD : Font.PLAIN, 12));
        g.setColor(sel ? Color.WHITE : new Color(140, 150, 168));
        g.drawString(label, bx + 14, y + 21);

        g.setFont(new Font("Courier New", Font.BOLD, 11));
        g.setColor(sel ? valueColor : valueColor.darker());
        FontMetrics fm = g.getFontMetrics();
        g.drawString(value, bx + bw - fm.stringWidth(value) - 14, y + 21);
    }

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
