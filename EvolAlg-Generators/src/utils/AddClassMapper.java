package utils;

import java.util.Random;
import java.util.function.IntUnaryOperator;

public class AddClassMapper implements IntUnaryOperator {

    public static void main(String[] args) {
        Random random = new Random();
        int n = 9, m = 7;
        AddClassMapper mapper = new AddClassMapper(n, m, random);

        for (int i = 0; i < n; i++) {
            System.out.printf("%2d :", i);
            for (int j = 0; j < 10; j++) {
                System.out.printf(" %2d", mapper.applyAsInt(i));
            }
            System.out.println();
        }

    }
    final int oldNumClasses, newNumClasses;
    final int[] p, q;

    final Random random;

    public AddClassMapper(int oldNumClasses, int newNumClasses, Random random) {
        this.random = random;
        this.oldNumClasses = oldNumClasses;
        this.newNumClasses = newNumClasses;
        this.p = RandomUtils.randomPermutation(oldNumClasses, random);
        this.q = RandomUtils.randomPermutation(newNumClasses, random);
    }

    @Override
    public int applyAsInt(int value) {
        int x = p[value];
        int t = (newNumClasses - x - 1) / oldNumClasses;
        int y = x + random.nextInt(t + 1) * oldNumClasses;
        return q[y];
    }

}
