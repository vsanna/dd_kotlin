package dev.ishikawa.dd_kotlin._7javainterop;

public class JavaCallingKotlin {
    public KotlinCalledByJava hello() {
        System.out.println(KotlinCallingJavaUtil.topLevelFunc());
        return new KotlinCalledByJava("name");
    }
}
