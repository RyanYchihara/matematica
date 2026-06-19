package mathinvader.data;

import java.io.*;
import java.util.*;

public class HighScoreManager {

    private static final String FILE_PATH = "highscores.dat";
    private static final int MAX_ENTRIES = 10;

    public static class Entry implements Comparable<Entry>, Serializable {
        private static final long serialVersionUID = 1L;
        public final String name;
        public final int score;
        public final int level;
        public final long survivedSeconds;

        public Entry(String name, int score, int level, long survivedSeconds) {
            this.name = name;
            this.score = score;
            this.level = level;
            this.survivedSeconds = survivedSeconds;
        }

        @Override
        public int compareTo(Entry o) {
            return Integer.compare(o.score, this.score);
        }
    }

    private final List<Entry> entries = new ArrayList<Entry>();

    public HighScoreManager() {
        load();
    }

    public void addEntry(String name, int score, int level, long survivedSeconds) {
        entries.add(new Entry(name, score, level, survivedSeconds));
        Collections.sort(entries);
        if (entries.size() > MAX_ENTRIES) {
            entries.subList(MAX_ENTRIES, entries.size()).clear();
        }
        save();
    }

    public boolean isHighScore(int score) {
        if (entries.size() < MAX_ENTRIES) return true;
        return score >= entries.get(entries.size() - 1).score;
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    @SuppressWarnings("unchecked")
    private void load() {
        File f = new File(FILE_PATH);
        if (!f.exists()) return;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            List<Entry> loaded = (List<Entry>) ois.readObject();
            ois.close();
            entries.addAll(loaded);
            Collections.sort(entries);
        } catch (Exception ignored) {}
    }

    private void save() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH));
            oos.writeObject(new ArrayList<Entry>(entries));
            oos.close();
        } catch (IOException ignored) {}
    }
}
