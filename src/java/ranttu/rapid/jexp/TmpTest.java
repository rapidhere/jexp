package ranttu.rapid.jexp;

/**
 * for test usage only
 */
@SuppressWarnings("all")
class TmpTest {
    public int test(String a) {
        int t = a.hashCode();
        throw new NoSuchFieldError(a);
    }
}