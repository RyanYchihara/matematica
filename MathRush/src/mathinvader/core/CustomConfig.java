package mathinvader.core;

/**
 * Configurações do modo personalizado.
 * Guarda quais operações estão habilitadas e o nível de dificuldade fixo.
 */
public class CustomConfig {

    public enum OpRange {
        EASY  ("Fácil",   "Números pequenos"),
        MEDIUM("Médio",   "Números médios"),
        HARD  ("Difícil", "Números grandes");

        public final String label;
        public final String desc;
        OpRange(String label, String desc) { this.label = label; this.desc = desc; }
    }

    // Operações habilitadas
    private boolean addEnabled  = true;
    private boolean subEnabled  = true;
    private boolean mulEnabled  = false;
    private boolean divEnabled  = false;

    // Faixa de números
    private OpRange range = OpRange.EASY;

    // Nível fixo (não sobe automaticamente)
    private boolean levelLocked = true;

    public boolean isAddEnabled()  { return addEnabled; }
    public boolean isSubEnabled()  { return subEnabled; }
    public boolean isMulEnabled()  { return mulEnabled; }
    public boolean isDivEnabled()  { return divEnabled; }
    public OpRange getRange()      { return range; }
    public boolean isLevelLocked() { return levelLocked; }

    public void toggleAdd() { addEnabled = !addEnabled; ensureAtLeastOne(); }
    public void toggleSub() { subEnabled = !subEnabled; ensureAtLeastOne(); }
    public void toggleMul() { mulEnabled = !mulEnabled; ensureAtLeastOne(); }
    public void toggleDiv() { divEnabled = !divEnabled; ensureAtLeastOne(); }

    public void cycleRangeNext() {
        OpRange[] vals = OpRange.values();
        range = vals[(range.ordinal() + 1) % vals.length];
    }
    public void cycleRangePrev() {
        OpRange[] vals = OpRange.values();
        range = vals[(range.ordinal() - 1 + vals.length) % vals.length];
    }

    public void toggleLevelLocked() { levelLocked = !levelLocked; }

    /** Garante que pelo menos uma operação esteja ativa. */
    private void ensureAtLeastOne() {
        if (!addEnabled && !subEnabled && !mulEnabled && !divEnabled) {
            addEnabled = true;
        }
    }

    /** Retorna os op-codes (0=+,1=-,2=x,3=/) habilitados. */
    public int[] enabledOpCodes() {
        int count = 0;
        if (addEnabled) count++;
        if (subEnabled) count++;
        if (mulEnabled) count++;
        if (divEnabled) count++;
        int[] codes = new int[count];
        int i = 0;
        if (addEnabled) codes[i++] = 0;
        if (subEnabled) codes[i++] = 1;
        if (mulEnabled) codes[i++] = 2;
        if (divEnabled) codes[i++] = 3;
        return codes;
    }
}
