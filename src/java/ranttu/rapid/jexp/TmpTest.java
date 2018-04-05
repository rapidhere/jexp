package ranttu.rapid.jexp;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * for test usage only
 */
@SuppressWarnings("all")
class TmpTest {
    static class CustomClassLoader extends ClassLoader {
        @Override
        public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            System.out.println("load class: " + name);
            if (name.contains("SomeClass")) {
                try {
                    byte[] bytes = IOUtils.toByteArray(SomeClass.class
                        .getResourceAsStream("TmpTest$SomeClass.class"));

                    return defineClass(SomeClass.class.getName(), bytes, 0, bytes.length);
                } catch (IOException e) {
                    return null;
                }
            } else {
                return super.loadClass(name, resolve);
            }
        }
    }

    public static class SomeClass {
        public static ClassA a = new ClassA();

        public ClassB b;

        public void func(ClassC c) {

        }
    }

    public static class ClassA {

    }

    public static class ClassB {

    }

    public static class ClassC {

    }

    public static void main(String... args) throws Exception {
        CustomClassLoader customClassLoader = new CustomClassLoader();

        Class<SomeClass> someClassClazz = (Class<SomeClass>) customClassLoader
            .loadClass(SomeClass.class.getName());

        Object handler = someClassClazz.newInstance();
        SomeClass.class.getMethod("func", ClassC.class).invoke(handler, null);
    }
}