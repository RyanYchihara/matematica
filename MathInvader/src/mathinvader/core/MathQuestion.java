package mathinvader.core;

import java.util.Random;

public class MathQuestion {

    public enum Difficulty { EASY, MEDIUM, HARD }

    private final String expression;
    private final int answer;
    private static final Random rand = new Random();

    public MathQuestion(Difficulty difficulty) {
        int[] result = generate(difficulty);
        this.answer = result[2];
        this.expression = buildExpression(result[0], result[1], result[3]);
    }

    private int[] generate(Difficulty difficulty) {
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
