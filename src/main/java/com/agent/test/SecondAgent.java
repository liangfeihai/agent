package com.agent.test;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class SecondAgent implements ClassFileTransformer {
    public final String injectedClassName = "com.agent.test.FirstTest";
    public final String methodName = "hello";

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        className = className.replace("/", ".");
        if (className.equals(injectedClassName)) {
            CtClass ctClass = null;
            try {
                ClassPool classPool=ClassPool.getDefault();
                ctClass = classPool.getCtClass(injectedClassName);// 使用全称,用于取得字节码类<使用javassist>
                CtMethod ctMethod = ctClass.getDeclaredMethod(methodName);// 得到这方法实例
                //插入本地变量
                ctMethod.addLocalVariable("begin",CtClass.longType);
                ctMethod.addLocalVariable("end",CtClass.longType);

                ctMethod.insertBefore("begin=System.currentTimeMillis();System.out.println(\"begin=\"+begin);");
                //前面插入：最后插入的放最上面
                ctMethod.insertBefore("System.out.println( \"埋点开始-2\" );");
                ctMethod.insertBefore("System.out.println( \"埋点开始-1\" );");

                ctMethod.insertAfter("end=System.currentTimeMillis();System.out.println(\"end=\"+end);");
                ctMethod.insertAfter("System.out.println(\"性能:\"+(end-begin)+\"毫秒\");");

                //后面插入：最后插入的放最下面
                ctMethod.insertAfter("System.out.println( \"埋点结束-1\" );");
                ctMethod.insertAfter("System.out.println( \"埋点结束-2\" );");
                return ctClass.toBytecode();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }
}
