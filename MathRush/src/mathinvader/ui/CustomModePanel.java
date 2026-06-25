package mathinvader.ui;

import mathinvader.core.CustomConfig;
import mathinvader.core.CustomConfig.OpRange;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Tela de configuração do Modo Personalizado.
 *
 * Seções:
 *   [0] Operações   — toggles para +  -  x  ÷
 *   [1] Dificuldade — seleção FÁCIL / MÉDIO / DIFÍCIL
 *   [2] Nível       — fixo ou progressivo
 *   [3] Jogar / Voltar
 *
 * Navegação: ↑↓ mudam row; ← → ajustam valor; ENTER ativa; ESC volta.
 */
public class CustomModePanel {

    // ── linhas do menu ────────────────────────────────────────────────────────
    private static final int ROW_ADD   = 0;
    private static final int ROW_SUB   = 1;
    private static final int ROW_MUL   = 2;
    private static final int ROW_DIV   = 3;
    private static final int ROW_RANGE = 4;
    private static final int ROW_LEVEL = 5;
    private static final int ROW_PLAY  = 6;
    private static final int ROW_BACK  = 7;
    private static final int TOTAL_ROWS = 8;

    private int selection = 0;
    private float animTimer = 0f;

    private final CustomConfig config;

    public CustomModePanel(CustomConfig config) {
        this.config = config;
    }

    // ── navegação ─────────────────────────────────────────────────────────────

    public void moveUp()    { selection = Math.max(0, selection - 1); }
    public void moveDown()  { selection = Math.min(TOTAL_ROWS - 1, selection + 1); }

    public void adjustLeft() {
        if (selection == ROW_RANGE) config.cycleRangePrev();
    }
    public void adjustRight() {
        if (selection == ROW_RANGE) config.cycleRangeNext();
    }

    /**
     * Ativa a opção selecionada.
     * @return "play" se o jogador escolheu Jogar, "back" se Voltar, null caso contrário.
     */
    public String activate() {
        switch (selection) {
            case ROW_ADD:   config.toggleAdd();         return null;
            case ROW_SUB:   config.toggleSub();         return null;
            case ROW_MUL:   config.toggleMul();         return null;
            case ROW_DIV:   config.toggleDiv();         return null;
            case ROW_RANGE: config.cycleRangeNext();    return null;
            case ROW_LEVEL: config.toggleLevelLocked(); return null;
            case ROW_PLAY:  return "play";
            case ROW_BACK:  return "back";
        }
        return null;
    }

    public void update() { animTimer += 0.05f; }

    // ── desenho ───────────────────────────────────────────────────────────────

    public void draw(Graphics2D g, int W, int H) {
        // fundo escuro
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, W, H);

        int cx = W / 2;

        // título
        drawGlow(g, "MODO PERSONALIZADO", cx, 54, 24, new Color(255, 160, 40));

        g.setFont(new Font("Courier New", Font.PLAIN, 10));
        g.setColor(new Color(90, 100, 120));
        String hint = "[ ↑↓ ] Navegar   [ ENTER / ← → ] Alterar   [ ESC ] Voltar";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(hint, cx - fm.stringWidth(hint) / 2, 72);

        // ── seção operações ──────────────────────────────────────────────────
        int sy = 90;
        drawSectionHeader(g, "OPERACOES MATEMATICAS", cx, sy, new Color(80, 200, 255));

        int opY = sy + 22;
        int opRowH = 42;
        drawToggleRow(g, cx, opY,              ROW_ADD, "Adicao",       "+",   config.isAddEnabled(), new Color(80, 220, 120));
        drawToggleRow(g, cx, opY + opRowH,     ROW_SUB, "Subtracao",   "-",   config.isSubEnabled(), new Color(255, 180, 60));
        drawToggleRow(g, cx, opY + opRowH * 2, ROW_MUL, "Multiplicacao","x",  config.isMulEnabled(), new Color(160, 100, 255));
        drawToggleRow(g, cx, opY + opRowH * 3, ROW_DIV, "Divisao",     "/",   config.isDivEnabled(), new Color(255, 80,  80));

        // ── seção dificuldade ────────────────────────────────────────────────
        int ds = opY + opRowH * 4 + 14;
        drawSectionHeader(g, "DIFICULDADE DOS NUMEROS", cx, ds, new Color(255, 160, 40));

        drawRangeRow(g, cx, ds + 22, ROW_RANGE);

        // ── seção nível ──────────────────────────────────────────────────────
        int ls = ds + 22 + 46 + 10;
        drawSectionHeader(g, "PROGRESSAO DE NIVEL", cx, ls, new Color(120, 200, 255));

        drawToggleRow(g, cx, ls + 22, ROW_LEVEL,
            config.isLevelLocked() ? "Nivel Fixo (sem progressao)" : "Nivel Progressivo (sobe com pontos)",
            config.isLevelLocked() ? "FIXO" : "PROG",
            true, config.isLevelLocked() ? new Color(255, 200, 60) : new Color(80, 220, 120));

        // ── botões ───────────────────────────────────────────────────────────
        int btnY = ls + 22 + 52;
        drawButton(g, cx - 90, btnY, 160, ">> JOGAR <<", ROW_PLAY,  new Color(80, 220, 120));
        drawButton(g, cx + 90, btnY, 140, "VOLTAR",       ROW_BACK,  new Color(160, 170, 190));

        // ── preview das operações ativas ─────────────────────────────────────
        drawPreview(g, cx, btnY + 54);
    }

    // ── seção header ──────────────────────────────────────────────────────────

    private void drawSectionHeader(Graphics2D g, String title, int cx, int y, Color c) {
        int bw = 520; int bh = 17;
        int bx = cx - bw / 2;
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 18));
        g.fillRoundRect(bx, y, bw, bh, 6, 6);
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 100));
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(bx, y, bw, bh, 6, 6);
        g.setStroke(new BasicStroke(1));
        g.setFont(new Font("Courier New", Font.BOLD, 10));
        g.setColor(c);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, cx - fm.stringWidth(title) / 2, y + 12);
    }

    // ── linha toggle (on/off) ─────────────────────────────────────────────────

    private void drawToggleRow(Graphics2D g, int cx, int y, int rowIdx,
                                String label, String symbol,
                                boolean active, Color c) {
        boolean sel = selection == rowIdx;
        float pulse = sel ? (float)(Math.sin(animTimer * 4) * 0.3 + 0.7) : 1f;

        int bw = 520; int bh = 36;
        int bx = cx - bw / 2;

        // fundo
        if (sel) {
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(28 * pulse)));
            g.fill(new RoundRectangle2D.Float(bx - 2, y - 1, bw + 4, bh + 2, 10, 10));
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(160 * pulse)));
            g.setStroke(new BasicStroke(1.8f));
            g.draw(new RoundRectangle2D.Float(bx, y, bw, bh, 8, 8));
            g.setStroke(new BasicStroke(1));
        } else {
            g.setColor(new Color(10, 14, 24, 120));
            g.fill(new RoundRectangle2D.Float(bx, y, bw, bh, 8, 8));
            g.setColor(new Color(30, 38, 50, 70));
            g.draw(new RoundRectangle2D.Float(bx, y, bw, bh, 8, 8));
        }

        int mid = y + bh / 2 + 5;

        // símbolo (quadradão à esquerda)
        int sqSize = 26;
        int sqX = bx + 10;
        int sqY = y + (bh - sqSize) / 2;
        g.setColor(active ? new Color(c.getRed(), c.getGreen(), c.getBlue(), 220)
                          : new Color(30, 38, 50, 200));
        g.fillRoundRect(sqX, sqY, sqSize, sqSize, 6, 6);
        g.setColor(active ? c.brighter() : new Color(60, 70, 80));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(sqX, sqY, sqSize, sqSize, 6, 6);
        g.setStroke(new BasicStroke(1));

        g.setFont(new Font("Courier New", Font.BOLD, 14));
        g.setColor(active ? Color.WHITE : new Color(80, 90, 100));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(symbol, sqX + sqSize / 2 - fm.stringWidth(symbol) / 2, sqY + sqSize / 2 + 5);

        // label
        g.setFont(new Font("Courier New", sel ? Font.BOLD : Font.PLAIN, sel ? 13 : 12));
        g.setColor(sel ? Color.WHITE : (active ? new Color(180, 190, 200) : new Color(100, 110, 120)));
        g.drawString(label, bx + 48, mid);

        // status
        String status = active ? "ATIVO" : "INATIVO";
        Color sc = active ? c : new Color(80, 90, 100);
        g.setFont(new Font("Courier New", Font.BOLD, 11));
        g.setColor(sel ? sc.brighter() : sc);
        fm = g.getFontMetrics();
        g.drawString(status, bx + bw - fm.stringWidth(status) - 12, mid);
    }

    // ── linha de seleção de range ─────────────────────────────────────────────

    private void drawRangeRow(Graphics2D g, int cx, int y, int rowIdx) {
        boolean sel = selection == rowIdx;
        float pulse = sel ? (float)(Math.sin(animTimer * 4) * 0.3 + 0.7) : 1f;
        Color c = new Color(255, 200, 60);

        int bw = 520; int bh = 40;
        int bx = cx - bw / 2;

        if (sel) {
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(28 * pulse)));
            g.fill(new RoundRectangle2D.Float(bx - 2, y - 1, bw + 4, bh + 2, 10, 10));
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(160 * pulse)));
            g.setStroke(new BasicStroke(1.8f));
            g.draw(new RoundRectangle2D.Float(bx, y, bw, bh, 8, 8));
            g.setStroke(new BasicStroke(1));
        } else {
            g.setColor(new Color(10, 14, 24, 120));
            g.fill(new RoundRectangle2D.Float(bx, y, bw, bh, 8, 8));
            g.setColor(new Color(30, 38, 50, 70));
            g.draw(new RoundRectangle2D.Float(bx, y, bw, bh, 8, 8));
        }

        int mid = y + bh / 2 + 5;

        // label
        g.setFont(new Font("Courier New", sel ? Font.BOLD : Font.PLAIN, 12));
        g.setColor(sel ? Color.WHITE : new Color(160, 170, 190));
        g.drawString("Faixa de numeros:", bx + 14, mid);

        // setas + valor
        OpRange[] vals = OpRange.values();
        int ord = config.getRange().ordinal();

        String leftArrow  = ord > 0                 ? "< " : "  ";
        String rightArrow = ord < vals.length - 1   ? " >" : "  ";
        String val = leftArrow + config.getRange().label.toUpperCase() + rightArrow;

        g.setFont(new Font("Courier New", Font.BOLD, 13));
        g.setColor(sel ? c : c.darker());
        FontMetrics fm = g.getFontMetrics();
        g.drawString(val, bx + bw - fm.stringWidth(val) - 12, mid);

        // descrição
        g.setFont(new Font("Courier New", Font.PLAIN, 10));
        g.setColor(new Color(120, 130, 150));
        g.drawString(config.getRange().desc, bx + 14, mid + 13);
    }

    // ── botão ─────────────────────────────────────────────────────────────────

    private void drawButton(Graphics2D g, int cx, int y, int bw, String text,
                            int rowIdx, Color c) {
        boolean sel = selection == rowIdx;
        float pulse = sel ? (float)(Math.sin(animTimer * 4) * 0.3 + 0.7) : 1f;
        int bh = 40; int bx = cx - bw / 2;

        if (sel) {
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(50 * pulse)));
            g.fillRoundRect(bx - 3, y - 3, bw + 6, bh + 6, 12, 12);
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(220 * pulse)));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(bx, y, bw, bh, 10, 10);
            g.setStroke(new BasicStroke(1));
            g.setFont(new Font("Courier New", Font.BOLD, 16));
            g.setColor(Color.WHITE);
        } else {
            g.setColor(new Color(10, 14, 24, 150));
            g.fillRoundRect(bx, y, bw, bh, 10, 10);
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 80));
            g.drawRoundRect(bx, y, bw, bh, 10, 10);
            g.setFont(new Font("Courier New", Font.PLAIN, 14));
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 180));
        }
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, cx - fm.stringWidth(text) / 2, y + bh / 2 + 5);
    }

    // ── preview ───────────────────────────────────────────────────────────────

    private void drawPreview(Graphics2D g, int cx, int y) {
        // mostra exemplos das operações ativas
        String[] opNames = {"Adicao", "Subtracao", "Multi.", "Divisao"};
        String[] opSyms  = {"+", "-", "x", "/"};
        boolean[] active = {
            config.isAddEnabled(), config.isSubEnabled(),
            config.isMulEnabled(), config.isDivEnabled()
        };
        Color[] opCols = {
            new Color(80, 220, 120), new Color(255, 180, 60),
            new Color(160, 100, 255), new Color(255, 80, 80)
        };

        g.setFont(new Font("Courier New", Font.PLAIN, 10));
        g.setColor(new Color(80, 90, 110));
        String lbl = "MODO ATIVO:";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(lbl, cx - fm.stringWidth(lbl) / 2, y);

        int totalActive = 0;
        for (boolean a : active) if (a) totalActive++;

        int slotW = 100;
        int startX = cx - (totalActive * slotW) / 2;
        int idx = 0;
        for (int i = 0; i < 4; i++) {
            if (!active[i]) continue;
            int sx = startX + idx * slotW;
            g.setColor(new Color(opCols[i].getRed(), opCols[i].getGreen(), opCols[i].getBlue(), 30));
            g.fillRoundRect(sx, y + 8, slotW - 6, 28, 8, 8);
            g.setColor(new Color(opCols[i].getRed(), opCols[i].getGreen(), opCols[i].getBlue(), 150));
            g.drawRoundRect(sx, y + 8, slotW - 6, 28, 8, 8);
            g.setFont(new Font("Courier New", Font.BOLD, 18));
            g.setColor(opCols[i]);
            fm = g.getFontMetrics();
            g.drawString(opSyms[i], sx + (slotW - 6) / 2 - fm.stringWidth(opSyms[i]) / 2, y + 28);
            idx++;
        }

        // Faixa
        g.setFont(new Font("Courier New", Font.PLAIN, 10));
        g.setColor(new Color(100, 110, 130));
        String info = "Faixa: " + config.getRange().label + "  |  Nivel: "
                + (config.isLevelLocked() ? "Fixo" : "Progressivo");
        fm = g.getFontMetrics();
        g.drawString(info, cx - fm.stringWidth(info) / 2, y + 50);
    }

    // ── utilitário ────────────────────────────────────────────────────────────

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
