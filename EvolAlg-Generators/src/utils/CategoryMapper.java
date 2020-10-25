package utils;

import java.util.Arrays;
import java.util.Random;
import java.util.function.IntUnaryOperator;

public class CategoryMapper implements IntUnaryOperator {
    public static void main(String[] args) {
        Random random = new Random();

        int n = 0, m = 20;

        int[] values = new int[n];

        for (int i = 0; i < n; i++) {
            values[i] = random.nextInt(m);
        }
        CategoryMapper mapper = new CategoryMapper(values.clone());

        System.out.println(mapper.range());
        for (int i = 0; i < n; i++) {
            System.out.printf("%2d  %2d -> %2d%n", i, values[i], mapper.applyAsInt(values[i]));
        }

    }
    private final int[] domain, codomain;

    private final int range;

    public CategoryMapper(int[] values) {
        domain = values;
        Arrays.sort(domain);

        int len = domain.length;

        codomain = new int[len];

        int c = -1;

        for (int valueId = 0; valueId < len; valueId++) {
            if (valueId == 0 || domain[valueId - 1] != domain[valueId]) {
                ++c;
            }
            codomain[valueId] = c;
        }

        range = c + 1;
    }

    @Override
    public int applyAsInt(int value) {
        int index = Arrays.binarySearch(domain, value);
        if (index < 0) {
            return -1;
        } else {
            return codomain[index];
        }
    }

    public int range() {
        return range;
    }

}
