package ranttu.rapid.jexp;

/**
 * for test usage only
 */
class TmpTest {
    public static Object isBlank(String s) {
        return s == null || s.length() == 0;
    }

    public String f(int a, double b) {
        System.out.print(1.0 % b);
        return "1" + b + a;
    }
}
