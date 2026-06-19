package mathinvader.core;

/**
 * Singleton que guarda o fator de escala de fonte global do jogo.
 * Todas as classes devem usar FontSettings.scale(tamanhoBase) ao criar fontes.
 */
public class FontSettings {

    private static FontSettings instance;

    public enum FontSize {
        PEQUENA("Pequena", 0.78f),
        NORMAL ("Normal",  1.00f),
        GRANDE ("Grande",  1.25f),
        ENORME ("Enorme",  1.55f);

        public final String label;
        public final float  scale;
        FontSize(String label, float scale) { this.label = label; this.scale = scale; }
    }

    private FontSize current = FontSize.NORMAL;

    private FontSettings() {}

    public static FontSettings getInstance() {
        if (instance == null) instance = new FontSettings();
        return instance;
    }

    public FontSize getCurrent()             { return current; }
    public void     setCurrent(FontSize fs)  { current = fs; }

    /** Retorna o tamanho em pixels escalado pelo fator atual. Mínimo de 8. */
    public int scale(int baseSize) {
        return Math.max(8, Math.round(baseSize * current.scale));
    }

    public float scalef(float baseSize) {
        return Math.max(8f, baseSize * current.scale);
    }

    /** Cicla para o próximo tamanho disponível. */
    public void cycleNext() {
        FontSize[] vals = FontSize.values();
        current = vals[(current.ordinal() + 1) % vals.length];
    }

    /** Cicla para o tamanho anterior. */
    public void cyclePrev() {
        FontSize[] vals = FontSize.values();
        current = vals[(current.ordinal() - 1 + vals.length) % vals.length];
    }
}
