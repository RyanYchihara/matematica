package mathinvader.audio;

import javax.sound.sampled.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundManager {

    private static SoundManager instance;
    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "sound-thread");
        t.setDaemon(true);
        return t;
    });

    private boolean enabled = true;
    private boolean musicEnabled = true;
    private Thread musicThread;
    private volatile boolean musicRunning = false;

    private SoundManager() {}

    public static SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    public void playCorrect() {
        if (!enabled) return;
        executor.submit(() -> playTone(880, 80, 0.4f));
        executor.submit(() -> { sleep(90); playTone(1100, 80, 0.35f); });
    }

    public void playWrong() {
        if (!enabled) return;
        executor.submit(() -> playNoise(120, 0.3f));
    }

    public void playExplosion() {
        if (!enabled) return;
        executor.submit(() -> playNoise(200, 0.5f));
        executor.submit(() -> { sleep(50); playNoise(150, 0.4f); });
    }

    public void playEnemyReach() {
        if (!enabled) return;
        executor.submit(() -> playTone(200, 300, 0.5f));
    }

    public void playGameOver() {
        if (!enabled) return;
        executor.submit(() -> {
            playTone(400, 200, 0.5f); sleep(220);
            playTone(300, 200, 0.5f); sleep(220);
            playTone(200, 400, 0.5f);
        });
    }

    public void playCombo(int combo) {
        if (!enabled || combo < 3) return;
        final int freq = Math.min(1800, 600 + combo * 80);
        executor.submit(() -> playTone(freq, 100, 0.3f));
    }

    public void playBossWarning() {
        if (!enabled) return;
        executor.submit(() -> {
            for (int i = 0; i < 3; i++) {
                playTone(150, 200, 0.6f);
                sleep(250);
            }
        });
    }

    public void startMusic() {
        if (!musicEnabled || musicRunning) return;
        musicRunning = true;
        musicThread = new Thread(() -> {
            int[] melody = {262, 294, 330, 349, 392, 440, 494, 523};
            int[] pattern = {0, 2, 4, 2, 1, 3, 5, 3, 4, 6, 7, 6};
            int idx = 0;
            while (musicRunning) {
                try {
                    playTone(melody[pattern[idx % pattern.length]], 180, 0.12f);
                    Thread.sleep(220);
                    idx++;
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "music-thread");
        musicThread.setDaemon(true);
        musicThread.start();
    }

    public void stopMusic() {
        musicRunning = false;
        if (musicThread != null) musicThread.interrupt();
    }

    public void toggleSound()  { enabled = !enabled; }
    public void toggleMusic()  {
        musicEnabled = !musicEnabled;
        if (!musicEnabled) stopMusic(); else startMusic();
    }

    public boolean isSoundEnabled() { return enabled; }
    public boolean isMusicEnabled() { return musicEnabled; }

    private void playTone(int frequency, int durationMs, float volume) {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            int samples = (int)(44100 * durationMs / 1000.0);
            byte[] buf = new byte[samples * 2];
            for (int i = 0; i < samples; i++) {
                double angle = 2.0 * Math.PI * frequency * i / 44100;
                double envelope = Math.min(1.0, Math.min((double)i / 200, (double)(samples - i) / 200));
                short val = (short)(Math.sin(angle) * Short.MAX_VALUE * volume * envelope);
                buf[i * 2]     = (byte)(val & 0xFF);
                buf[i * 2 + 1] = (byte)((val >> 8) & 0xFF);
            }
            writeAudio(format, buf);
        } catch (Exception ignored) {}
    }

    private void playNoise(int durationMs, float volume) {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            int samples = (int)(44100 * durationMs / 1000.0);
            byte[] buf = new byte[samples * 2];
            java.util.Random rng = new java.util.Random();
            for (int i = 0; i < samples; i++) {
                double envelope = (double)(samples - i) / samples;
                short val = (short)(rng.nextGaussian() * Short.MAX_VALUE * volume * envelope * 0.6);
                buf[i * 2]     = (byte)(val & 0xFF);
                buf[i * 2 + 1] = (byte)((val >> 8) & 0xFF);
            }
            writeAudio(format, buf);
        } catch (Exception ignored) {}
    }

    private void writeAudio(AudioFormat format, byte[] buf) throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
        line.write(buf, 0, buf.length);
        line.drain();
        line.close();
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
