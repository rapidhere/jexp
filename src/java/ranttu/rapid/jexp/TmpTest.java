package ranttu.rapid.jexp;

/**
 * for test usage only
 */
@SuppressWarnings("all")
class TmpTest {
    public static Object isBlank(String s) {
        return s == null || s.length() == 0;
    }

    public String f(int a, double b) {
        System.out.print(Double.valueOf(1.0));
        return "1" + b + a;
    }
}
