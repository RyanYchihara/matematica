package mathinvader.core;

import java.util.Random;

public class MathQuestion {

    public enum Difficulty { EASY, MEDIUM, HARD, CUSTOM }

    private final String expression;
    private final int answer;
    private static final Random rand = new Random();

    /** Construtor padrão — usa dificuldade automática (modo normal). */
    public MathQuestion(Difficulty difficulty) {
        int[] result = generate(difficulty, null);
        this.answer = result[2];
        this.expression = buildExpression(result[0], result[1], result[3]);
    }

    /** Construtor para modo personalizado. */
    public MathQuestion(CustomConfig config) {
        int[] result = generate(Difficulty.CUSTOM, config);
        this.answer = result[2];
        this.expression = buildExpression(result[0], result[1], result[3]);
    }

    // ── geração ──────────────────────────────────────────────────────────────

    private int[] generate(Difficulty difficulty, CustomConfig config) {
        if (difficulty == Difficulty.CUSTOM && config != null) {
            return generateCustom(config);
        }
        switch (difficulty) {
            case EASY:   return generateEasy();
            case MEDIUM: return generateMedium();
            case HARD:   return generateHard();
            default:     return generateEasy();
        }
    }

    private int[] generateEasy() {
        int op = rand.nextInt(2);
        int a, b;
        if (op == 0) {
            a = rand.nextInt(50) + 1;
            b = rand.nextInt(50) + 1;
            return new int[]{a, b, a + b, 0};
        } else {
            a = rand.nextInt(50) + 10;
            b = rand.nextInt(a) + 1;
            return new int[]{a, b, a - b, 1};
        }
    }

    private int[] generateMedium() {
        int op = rand.nextInt(3);
        if (op == 2) {
            int a = rand.nextInt(12) + 2;
            int b = rand.nextInt(12) + 2;
            return new int[]{a, b, a * b, 2};
        }
        return generateEasy();
    }

    private int[] generateHard() {
        int op = rand.nextInt(4);
        if (op == 3) {
            int b = rand.nextInt(11) + 2;
            int a = b * (rand.nextInt(10) + 2);
            return new int[]{a, b, a / b, 3};
        } else if (op == 2) {
            int a = rand.nextInt(15) + 2;
            int b = rand.nextInt(15) + 2;
            return new int[]{a, b, a * b, 2};
        }
        int a = rand.nextInt(200) + 50;
        int b = rand.nextInt(100) + 1;
        return op == 0 ? new int[]{a, b, a + b, 0} : new int[]{a, b, a - b, 1};
    }

    /**
     * Gera uma questão de acordo com as configurações do modo personalizado.
     * Escolhe a operação aleatoriamente dentre as habilitadas, e ajusta
     * os operandos conforme a faixa selecionada.
     */
    private int[] generateCustom(CustomConfig cfg) {
        int[] opCodes = cfg.enabledOpCodes();
        int opCode = opCodes[rand.nextInt(opCodes.length)];

        switch (opCode) {
            case 0: return generateAdd(cfg.getRange());
            case 1: return generateSub(cfg.getRange());
            case 2: return generateMul(cfg.getRange());
            case 3: return generateDiv(cfg.getRange());
            default: return generateAdd(cfg.getRange());
        }
    }

    // ── geradores por operação + faixa ────────────────────────────────────────

    private int[] generateAdd(CustomConfig.OpRange range) {
        int a, b;
        switch (range) {
            case EASY:
                a = rand.nextInt(20) + 1;
                b = rand.nextInt(20) + 1;
                break;
            case MEDIUM:
                a = rand.nextInt(100) + 10;
                b = rand.nextInt(100) + 10;
                break;
            default: // HARD
                a = rand.nextInt(500) + 100;
                b = rand.nextInt(500) + 100;
                break;
        }
        return new int[]{a, b, a + b, 0};
    }

    private int[] generateSub(CustomConfig.OpRange range) {
        int a, b;
        switch (range) {
            case EASY:
                a = rand.nextInt(20) + 5;
                b = rand.nextInt(a) + 1;
                break;
            case MEDIUM:
                a = rand.nextInt(150) + 20;
                b = rand.nextInt(a / 2) + 1;
                break;
            default: // HARD
                a = rand.nextInt(800) + 200;
                b = rand.nextInt(a / 2) + 1;
                break;
        }
        return new int[]{a, b, a - b, 1};
    }

    private int[] generateMul(CustomConfig.OpRange range) {
        int a, b;
        switch (range) {
            case EASY:
                a = rand.nextInt(5) + 2;
                b = rand.nextInt(5) + 2;
                break;
            case MEDIUM:
                a = rand.nextInt(10) + 2;
                b = rand.nextInt(10) + 2;
                break;
            default: // HARD
                a = rand.nextInt(20) + 5;
                b = rand.nextInt(15) + 5;
                break;
        }
        return new int[]{a, b, a * b, 2};
    }

    private int[] generateDiv(CustomConfig.OpRange range) {
        int b, a;
        switch (range) {
            case EASY:
                b = rand.nextInt(5) + 2;       // divisor 2-6
                a = b * (rand.nextInt(5) + 2);  // resultado 2-6
                break;
            case MEDIUM:
                b = rand.nextInt(9) + 2;        // divisor 2-10
                a = b * (rand.nextInt(10) + 2); // resultado 2-11
                break;
            default: // HARD
                b = rand.nextInt(11) + 2;       // divisor 2-12
                a = b * (rand.nextInt(20) + 3); // resultado 3-22
                break;
        }
        return new int[]{a, b, a / b, 3};
    }

    // ── expressão ────────────────────────────────────────────────────────────

    private String buildExpression(int a, int b, int opCode) {
        String op;
        switch (opCode) {
            case 1:  op = "-"; break;
            case 2:  op = "x"; break;
            case 3:  op = "/"; break;
            default: op = "+"; break;
        }
        return a + " " + op + " " + b + " =";
    }

    public String getExpression() { return expression; }
    public int getAnswer()        { return answer; }

    public boolean check(String input) {
        try {
            return Integer.parseInt(input.trim()) == answer;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
