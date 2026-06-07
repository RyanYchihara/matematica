package mathinvader.core;

public class PlayerStats {

    private int score;
    private int lives;
    private int level;
    private int combo;
    private int maxCombo;
    private long startTime;
    private int totalKills;
    private int totalMisses;

    public static final int MAX_LIVES = 3;

    public PlayerStats() { reset(); }

    public void reset() {
        score = 0;
        lives = MAX_LIVES;
        level = 1;
        combo = 0;
        maxCombo = 0;
        startTime = System.currentTimeMillis();
        totalKills = 0;
        totalMisses = 0;
    }

    public void registerKill() {
        combo++;
        if (combo > maxCombo) maxCombo = combo;
        int basePoints = 100 * level;
        int comboBonus = (int)(basePoints * (combo - 1) * 0.5);
        score += basePoints + comboBonus;
        totalKills++;
    }

    public void registerMiss() { combo = 0; totalMisses++; }

    public void loseLife() { lives--; combo = 0; }

    public boolean isDead() { return lives <= 0; }

    public void updateLevel() {
        int newLevel = Math.min(10, 1 + score / 800);
        if (newLevel > level) level = newLevel;
    }

    public void addLife() {
        if (lives < MAX_LIVES) lives++;
    }

    public long getSurvivedSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    public MathQuestion.Difficulty getCurrentDifficulty() {
        if (level <= 3) return MathQuestion.Difficulty.EASY;
        if (level <= 7) return MathQuestion.Difficulty.MEDIUM;
        return MathQuestion.Difficulty.HARD;
    }

    public int getScore()      { return score; }
    public int getLives()      { return lives; }
    public int getLevel()      { return level; }
    public int getCombo()      { return combo; }
    public int getMaxCombo()   { return maxCombo; }
    public int getTotalKills() { return totalKills; }
    public int getTotalMisses(){ return totalMisses; }
}
