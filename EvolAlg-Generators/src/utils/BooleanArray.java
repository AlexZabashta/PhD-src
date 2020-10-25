package utils;

import java.util.Arrays;
import java.util.Random;

public class BooleanArray {
    public static void main(String[] args) {
        System.out.println(Arrays.toString(random(10, 2, new Random())));
    }

    public static boolean[] random(int n, int m, Random random) {
        if (m < 0 || m > n) {
            throw new IllegalArgumentException("'m' must be in [0, n]");
        }

        boolean[] array = new boolean[n];

        for (int i = n - m; i < n; i++) {
            int j = random.nextInt(i + 1);
            array[i] = array[j];
            array[j] = true;
        }

        return array;
    }

    public static boolean[] random(int n, Random random) {
        boolean[] array = new boolean[n];

        for (int i = 0; i < n; i++) {
            array[i] = random.nextBoolean();
        }

        return array;
    }
}
