package dev.ishikawa.dd_kotlin._7javainterop;

public class JavaSandbox {
    private int bar = 0;
    class Inner{
        private int foo = 1;

        public int getFoo() {
            System.out.println(this.foo);
            System.out.println(JavaSandbox.this);
            return foo;
        }
    }
    static class Nested {
        private int geho = 2;
    }

    static void test() {
        JavaSandbox javaSandbox = new JavaSandbox();
        Nested nested = new Nested();
        Inner inner = javaSandbox.new Inner();
        inner.getFoo();
    }
}
