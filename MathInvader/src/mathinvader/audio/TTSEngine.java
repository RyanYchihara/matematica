package mathinvader.audio;

import java.io.*;
import java.util.concurrent.*;

/**
 * Motor de Text-to-Speech para acessibilidade de pessoas cegas.
 * Detecta o sistema operacional e usa o sintetizador nativo:
 *   Windows  -> PowerShell + System.Speech.Synthesis.SpeechSynthesizer (SAPI)
 *   macOS    -> comando 'say'
 *   Linux    -> espeak ou festival
 *
 * Todas as chamadas são assíncronas para não travar o jogo.
 * A fala é interrompida quando uma nova chega (fila de 1 item).
 */
public class TTSEngine {

    private static TTSEngine instance;

    public enum OS { WINDOWS, MAC, LINUX, UNKNOWN }

    private final OS os;
    private boolean enabled = false; // começa desligado; usuário ativa nas opções
    private float rate = 1.0f;       // velocidade da fala (0.5 = lento, 2.0 = rápido)

    // Executor de thread única — garante que só uma fala acontece por vez
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "tts-thread");
        t.setDaemon(true);
        return t;
    });

    private Process currentProcess = null;

    private TTSEngine() {
        String name = System.getProperty("os.name", "").toLowerCase();
        if (name.contains("win"))   os = OS.WINDOWS;
        else if (name.contains("mac")) os = OS.MAC;
        else if (name.contains("nix") || name.contains("nux")) os = OS.LINUX;
        else os = OS.UNKNOWN;
    }

    public static TTSEngine getInstance() {
        if (instance == null) instance = new TTSEngine();
        return instance;
    }

    // ─── API Pública ─────────────────────────────────────────────────────────

    /** Fala o texto imediatamente, interrompendo qualquer fala em andamento. */
    public void speak(String text) {
        if (!enabled || text == null || text.isEmpty()) return;
        stopCurrent();
        executor.submit(() -> doSpeak(text));
    }

    /** Fala a equação matemática de forma clara para pessoas cegas. */
    public void speakEquation(String expression) {
        if (!enabled) return;
        // Converte a expressão para linguagem natural
        String spoken = toSpeech(expression);
        speak(spoken);
    }

    /** Fala o texto apenas se o TTS estiver ativo — sem interromper. */
    public void speakIfEnabled(String text) {
        if (!enabled) return;
        speak(text);
    }

    /** Para qualquer fala em andamento. */
    public void stop() { stopCurrent(); }

    /** Testa o TTS com uma frase de exemplo. */
    public void test() {
        speak("Teste de audio. Dois mais tres igual a cinco.");
    }

    // ─── Getters / Setters ───────────────────────────────────────────────────

    public boolean isEnabled()        { return enabled; }
    public void setEnabled(boolean v) { enabled = v; if (!v) stopCurrent(); }
    public void toggle()              { setEnabled(!enabled); }
    public OS getOS()                 { return os; }
    public float getRate()            { return rate; }
    public void setRate(float r)      { rate = Math.max(0.4f, Math.min(2.5f, r)); }

    /** Verifica se o sistema tem suporte a TTS. */
    public boolean isSupported() {
        return os == OS.WINDOWS || os == OS.MAC || hasLinuxTTS();
    }

    // ─── Lógica interna ──────────────────────────────────────────────────────

    private void doSpeak(String text) {
        try {
            String[] cmd = buildCommand(text);
            if (cmd == null) return;

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            synchronized (this) { currentProcess = p; }
            p.waitFor(15, TimeUnit.SECONDS);
            synchronized (this) { if (currentProcess == p) currentProcess = null; }
        } catch (Exception ignored) {}
    }

    private synchronized void stopCurrent() {
        if (currentProcess != null) {
            currentProcess.destroy();
            currentProcess = null;
        }
    }

    private String[] buildCommand(String text) {
        // Escapa aspas para segurança
        String safe = text.replace("\"", "").replace("'", "").replace("`", "");

        switch (os) {
            case WINDOWS:
                // PowerShell usa SpeechSynthesizer do Windows (SAPI 5 - built-in desde XP)
                int wRate = Math.round((rate - 1.0f) * 5); // SAPI: -10 a +10
                String psScript = String.format(
                    "Add-Type -AssemblyName System.speech; " +
                    "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "$s.Rate = %d; " +
                    "$s.Speak('%s');",
                    wRate, safe);
                return new String[]{"powershell", "-NoProfile", "-WindowStyle", "Hidden",
                                    "-Command", psScript};

            case MAC:
                // 'say' é nativo no macOS, suporte a -r (rate em palavras/min)
                int macRate = Math.round(180 * rate);
                return new String[]{"say", "-r", String.valueOf(macRate), safe};

            case LINUX:
                if (hasEspeak()) {
                    // espeak: -s palavras/min, -v pt (português)
                    int speed = Math.round(160 * rate);
                    return new String[]{"espeak", "-v", "pt", "-s", String.valueOf(speed), safe};
                } else if (hasFestival()) {
                    // festival via pipe de texto
                    return new String[]{"bash", "-c",
                        "echo \"" + safe + "\" | festival --tts"};
                }
                return null;

            default:
                return null;
        }
    }

    private String toSpeech(String expression) {
        // "12 + 7 =" -> "doze mais sete, quanto é?"
        // "15 - 3 =" -> "quinze menos três, quanto é?"
        // "4 x 6 ="  -> "quatro vezes seis, quanto é?"
        // "20 / 4 =" -> "vinte dividido por quatro, quanto é?"

        String expr = expression.trim();
        // Remove o "=" do final
        if (expr.endsWith("=")) expr = expr.substring(0, expr.length() - 1).trim();

        // Separa os tokens
        String[] parts = expr.split("\\s+");
        if (parts.length < 3) return expression;

        String a   = parts[0];
        String op  = parts[1];
        String b   = parts[2];

        String operator;
        switch (op) {
            case "+": operator = "mais";          break;
            case "-": operator = "menos";         break;
            case "x":
            case "*":
            case "X": operator = "vezes";         break;
            case "/":
            case "÷": operator = "dividido por";  break;
            default:   operator = op;             break;
        }

        return a + " " + operator + " " + b + ". Quanto é?";
    }

    private boolean hasEspeak() {
        return commandExists("espeak");
    }

    private boolean hasFestival() {
        return commandExists("festival");
    }

    private boolean hasLinuxTTS() {
        return hasEspeak() || hasFestival();
    }

    private boolean commandExists(String cmd) {
        try {
            Process p = new ProcessBuilder("which", cmd).start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
