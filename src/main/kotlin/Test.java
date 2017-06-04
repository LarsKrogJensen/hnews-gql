import java.util.function.Function;

public class Test {

    public static void main(String[] args) {
        Function<Integer, Integer> times2 = e -> e * 2;
        Function<Integer, Integer> squared = e -> e * e;


        Integer res1 = times2.compose(squared).apply(4);
        times2.apply(squared.apply(4));
        // Returns 32

        Integer res2 = times2.andThen(squared).apply(4);
        squared.apply(times2.apply(4));
        // Returns 64

        System.out.println("res1: " + res1 + ", res2: " + res2);
    }
}
